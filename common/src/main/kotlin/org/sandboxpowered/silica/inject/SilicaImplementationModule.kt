package org.sandboxpowered.silica.inject

import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import org.sandboxpowered.api.inject.FactoryProvider
import org.sandboxpowered.api.inject.Implementation
import org.sandboxpowered.api.inject.ImplementationModule
import org.sandboxpowered.api.inject.Sandbox
import kotlin.reflect.KClass

class SilicaImplementationModule : ImplementationModule() {
    override fun configure() {
        super.configure()

        bind<Implementation>().to(SilicaImplementation::class)
        bind<FactoryProvider>().to(SilicaFactoryProvider::class)

        requestStaticInjection(Sandbox::class)
    }

    private fun <T : Any> requestStaticInjection(vararg kClass: KClass<T>) {
        requestStaticInjection(*kClass.map { it.java }.toTypedArray())
    }

    private inline fun <reified T> bind(): AnnotatedBindingBuilder<T> {
        return bind(T::class.java)
    }

    private inline fun <A : Any, reified B : A> AnnotatedBindingBuilder<A>.to(kClass: KClass<B>): ScopedBindingBuilder? {
        return to(kClass.java)
    }
}