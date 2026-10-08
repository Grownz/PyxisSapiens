package de.pyxissapiens.core.sensors

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.pyxissapiens.core.ports.SensorPort
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorsModule {
    @Binds
    @Singleton
    abstract fun bindSensorPort(impl: AndroidSensorPort): SensorPort
}
