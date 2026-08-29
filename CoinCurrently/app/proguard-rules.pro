# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for crash reporting.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# =============================================================================
# Gson / kotlinx.serialization models (R8 full mode)
# =============================================================================
# R8 full mode strips generic signatures (List<T>, Map<K,V>, CachedData<T>) from
# classes that are not kept. Gson then deserializes collections as LinkedTreeMap
# → ClassCastException, or leaves nested fields null (e.g. GlobalMarketModel.data).
#
# Keep every API / disk-cache / navigation model type in the app.

# Retrofit API DTOs
-keep class networking.ResponseModels.** { *; }
-keep class networking.Resource { *; }
-keep class networking.Status { *; }

# Shared cache wrappers + app models
-keep class com.cryptium.coincurrently.ui.main.Models.** { *; }

# All feature DataClasses / Models / Enums packages (any nesting under ui.main)
-keep class com.cryptium.coincurrently.ui.main.**.DataClasses.** { *; }
-keep class com.cryptium.coincurrently.ui.main.**.Models.** { *; }
-keep class com.cryptium.coincurrently.ui.main.**.Enums.** { *; }

# Data classes declared inside repository files (not under DataClasses/)
-keep class com.cryptium.coincurrently.ui.main.Repositories.PortfolioPiesData { *; }
-keep class com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.Repositories.CachedGlobalMarket { *; }
-keep class com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.Repositories.CachedCoinsList { *; }

# Sealed news category persisted via Gson
-keep class networking.NewsCategory { *; }
-keep class networking.NewsCategory$* { *; }

# Catch-all: every @Serializable type used with Gson / navigation / cache
-if @kotlinx.serialization.Serializable class **
-keep class <1> { *; }

# Nested enums stored via Gson (declared inside GlobalFunctions)
-keep class com.cryptium.coincurrently.ui.main.General.GlobalFunctions$* { *; }

# Portfolio companion touches Gson-backed fields; keep names stable
-keep class com.cryptium.coincurrently.ui.main.Repositories.PortfolioRepository { *; }
-keep class com.cryptium.coincurrently.ui.main.Repositories.PortfolioRepository$Companion { *; }

# kotlinx.serialization generated serializers for app + API models
-keep,includedescriptorclasses class networking.ResponseModels.**$$serializer { *; }
-keep,includedescriptorclasses class com.cryptium.coincurrently.ui.main.**$$serializer { *; }
-keep,includedescriptorclasses class networking.**$$serializer { *; }

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# kotlin.Result must retain its signature so ResultCallAdapterFactory matches
# Call<Result<T>>. If it does not, Gson deserializes the body as Result (object)
# and list endpoints that return JSON arrays crash fatally:
# "Expected BEGIN_OBJECT but was BEGIN_ARRAY".
-keep,allowobfuscation,allowshrinking class kotlin.Result
-keep,allowobfuscation,allowshrinking class kotlin.Result$Failure
-keep class networking.ResultCallAdapterFactory { *; }
-keep class networking.ResultCall { *; }
-keep,allowobfuscation interface networking.ResponseModels.ApiService
-keepclassmembers,allowobfuscation interface networking.ResponseModels.ApiService {
    <methods>;
}

# Kotlinx Serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class networking.ResponseModels.**$$serializer { *; }
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# App widgets / Glance receivers referenced from the manifest
-keep public class * extends android.appwidget.AppWidgetProvider
-keep class com.cryptium.coincurrently.ui.main.Widgets.** { *; }

# Enums used via reflection / name()
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# SignalR / Microsoft (optional reflective bits)
-dontwarn com.microsoft.signalr.**
-keep class com.microsoft.signalr.** { *; }

# OkHttp platform notes
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# SignalR pulls in SLF4J without a binder on Android
-dontwarn org.slf4j.impl.StaticLoggerBinder

# R8 can remove SDK_INT >= 31 guards when minSdk is 31. Keep GMS/Compose API-level
# helpers intact in release so sideloaded or vendor-modified runtimes stay guarded.
# (Class-scoped -dontoptimize is not supported by R8; use -keep on these small helpers.)
-keep class com.google.android.gms.common.util.PlatformVersion { *; }
-keep class com.google.android.gms.common.util.zzb { *; }
-keep class com.google.android.gms.common.util.zzc { *; }
-keep class com.google.android.gms.common.util.zzd { *; }
-keep class com.google.android.gms.common.util.zze { *; }
# GoogleApi constructors also gate API 30/31 Context attribution APIs behind SDK_INT.
-keep class com.google.android.gms.common.api.GoogleApi { *; }
-keep class com.google.android.gms.common.wrappers.AttributionSourceWrapper { *; }
-keep class androidx.compose.ui.text.font.FontWeightAdjustmentHelper { *; }
-keep class androidx.compose.ui.text.font.FontWeightAdjustmentHelperApi31 { *; }
-keepclassmembers class androidx.compose.ui.text.font.FontWeightAdjustmentHelper {
    public int getFontWeightAdjustment(android.content.Context);
}
-keep class com.cryptium.coincurrently.ui.main.General.ComposePlatformCompat { *; }
