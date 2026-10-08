package de.pyxissapiens.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.pyxissapiens.core.ports.MagCalibrationProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CalibrationModule {
    @Binds
    @Singleton
    abstract fun bindMagCalibrationProvider(impl: CalibrationRepository): MagCalibrationProvider
}
