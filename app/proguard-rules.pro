# ---- App ----
-keepattributes *Annotation*, JavascriptInterface
-keepclassmembers class com.yourname.tempmail.** { *; }

# ---- LevelPlay / ironSource (Unity Grow) ----
-keep class com.ironsource.** { *; }
-keep class com.unity3d.mediation.** { *; }
-keep class com.unity3d.unityads.** { *; }
-keep class com.unity3d.ads.** { *; }
-keep public interface com.unity3d.mediation.LevelPlayMediationAdListener { *; }
-keepclassmembers class com.unity3d.mediation.LevelPlayMediationListener { *; }
# Gson / adapter models used by the ad SDK network
-keep class com.google.gson.** { *; }
-keep class com.ironsource.adapters.** { *; }
-keep class com.bytedance.sdk.openadsdk.** { *; }
# finalized widgets referenced reflectively
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keepattributes Signature
-keepattributes Exceptions
-keepattributes EnclosingMethod, InnerClasses

# ---- OkHttp ----
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---- Retrofit ----
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep class retrofit2.** { *; }

# ---- Room ----
-keep class androidx.room.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# ---- jsoup is self-contained ----
-dontwarn org.jsoup.**

# ---- Coroutines ----
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepclassmembernames class kotlinx.coroutines.android.HandlerDispatcherFactory { *; }