package org.sandboxpowered.silica.util

import com.google.common.util.concurrent.MoreExecutors
import org.joml.Math
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Util {
    fun createService(string: String): ExecutorService {
        val i = Math.clamp(1, 32, Runtime.getRuntime().availableProcessors())
        return if (i == 1) {
            MoreExecutors.newDirectExecutorService()
        } else {
            Executors.newFixedThreadPool(i)
        }
    }
}