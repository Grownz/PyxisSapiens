package de.pyxissapiens.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.pyxissapiens.core.database.PyxisDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PyxisDatabase =
        Room.databaseBuilder(context, PyxisDatabase::class.java, "pyxis.db")
            // TODO(sqlcipher): add .openHelperFactory(SupportOpenHelperFactory(passphrase))
            // where the passphrase is derived from a key held in the Android Keystore.
            .addMigrations(PyxisDatabase.MIGRATION_1_2)
            .build()
}
