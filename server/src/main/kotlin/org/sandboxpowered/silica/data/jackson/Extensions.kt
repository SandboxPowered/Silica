package org.sandboxpowered.silica.data.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.blackbird.BlackbirdModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.kotlinModule

internal inline fun jsonMapperForReading(vararg modules: Module, crossinline init: JsonMapper.Builder.() -> Unit = {}) =
    com.fasterxml.jackson.module.kotlin.jsonMapper {
        addModules(
            kotlinModule {
                enable(KotlinFeature.NullToEmptyCollection)
                enable(KotlinFeature.NullToEmptyMap)
            },
            BlackbirdModule(),
            *modules
        )
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        init()
    }
