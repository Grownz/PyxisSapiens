package de.pyxissapiens.core.sensors

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.pyxissapiens.core.ports.GeomagneticPort
import de.pyxissapiens.core.ports.OrientationPort
import de.pyxissapiens.core.ports.SensorPort
import de.pyxissapiens.core.wmm.WmmGeomagneticProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorsModule {

    @Binds
    @Singleton
    abstract fun bindSensorPort(impl: AndroidSensorPort): SensorPort

    @Binds
    @Singleton
    abstract fun bindOrientationPort(impl: AndroidOrientationPort): OrientationPort

    companion object {
        @Provides
        @Singleton
        fun provideGeomagneticPort(): GeomagneticPort = WmmGeomagneticProvider()
    }
}
