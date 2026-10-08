package de.pyxissapiens.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.data.DataEraser
import de.pyxissapiens.core.database.crypto.PassphraseStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataEraser: DataEraser,
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _hasPassphrase = MutableStateFlow(PassphraseStore.hasUserPassphrase(context))
    val hasPassphrase: StateFlow<Boolean> = _hasPassphrase

    private val _isEncrypted = MutableStateFlow(true)
    val isEncrypted: StateFlow<Boolean> = _isEncrypted

    fun eraseAll() {
        viewModelScope.launch {
            dataEraser.eraseAll()
            _message.value = "Alle Daten gelöscht"
        }
    }

    fun setPassphrase(value: String) {
        PassphraseStore.setUserPassphrase(context, value.ifBlank { null })
        _hasPassphrase.value = PassphraseStore.hasUserPassphrase(context)
        _message.value = if (value.isBlank()) {
            "Passphrase entfernt (gilt beim nächsten Start)"
        } else {
            "Passphrase gesetzt (gilt beim nächsten Start)"
        }
    }

    fun clearMessage() { _message.value = null }
}
