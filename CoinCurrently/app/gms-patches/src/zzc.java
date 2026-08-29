package com.google.android.gms.common.util;

import android.os.StrictMode;
import java.lang.reflect.Method;

/** Reflection stand-in for play-services-basement helper that calls permitUnsafeIntentLaunch(). */
final class zzc {
    private zzc() {}

    static StrictMode.VmPolicy.Builder zza(StrictMode.VmPolicy.Builder builder) {
        try {
            Method method = StrictMode.VmPolicy.Builder.class.getMethod("permitUnsafeIntentLaunch");
            Object result = method.invoke(builder);
            return (StrictMode.VmPolicy.Builder) result;
        } catch (Throwable ignored) {
            return builder;
        }
    }
}
