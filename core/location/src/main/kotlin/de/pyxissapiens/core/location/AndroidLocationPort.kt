package de.pyxissapiens.core.location

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.ports.FixSample
import de.pyxissapiens.core.ports.LocationPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Location port backed by the platform location provider.
 * Scaffold: no fix source wired yet; implement with LocationManager/FusedLocation plus
 * runtime-permission handling. Fix validation (jumps, duplicate positions) lives in the domain.
 */
@Singleton
class AndroidLocationPort @Inject constructor(
    @Suppress("unused") @ApplicationContext private val context: Context,
) : LocationPort {
    override fun fixes(): Flow<FixSample> = emptyFlow()

    override suspend fun lastKnown(): FixSample? = null
}
