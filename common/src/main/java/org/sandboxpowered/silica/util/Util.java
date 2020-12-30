package org.sandboxpowered.silica.util;

import com.google.common.util.concurrent.MoreExecutors;
import org.joml.Math;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Util {
    private static ExecutorService createService(String string) {
        int i = Math.clamp(1, 32, Runtime.getRuntime().availableProcessors());
        if (i == 1) {
            return MoreExecutors.newDirectExecutorService();
        } else {
            return Executors.newFixedThreadPool(i);
        }
    }
}