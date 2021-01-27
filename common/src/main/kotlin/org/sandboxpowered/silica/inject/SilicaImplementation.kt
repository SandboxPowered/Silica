package org.sandboxpowered.silica.inject

import com.google.inject.Inject
import com.google.inject.Singleton
import org.sandboxpowered.api.inject.FactoryProvider
import org.sandboxpowered.api.inject.Implementation

@Singleton
class SilicaImplementation @Inject private constructor(private var factoryProvider: FactoryProvider) : Implementation {

    override fun getFactoryProvider(): FactoryProvider = factoryProvider
}