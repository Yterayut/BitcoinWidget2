# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep data classes for JSON parsing
-keep class com.teerayut.bitcoinwidget.ApiService$** { *; }
-keepclassmembers class com.teerayut.bitcoinwidget.ApiService$** { *; }

# Keep Gson TypeAdapters
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep model classes used with Gson
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep widget provider classes
-keep class * extends android.appwidget.AppWidgetProvider
-keep class com.teerayut.bitcoinwidget.BitcoinPriceWidget { *; }

# Keep activity classes
-keep class * extends android.app.Activity
-keep class * extends androidx.appcompat.app.AppCompatActivity

# Keep manifest classes
-keep class com.teerayut.bitcoinwidget.MainActivity { *; }
-keep class com.teerayut.bitcoinwidget.PopupActivity { *; }
-keep class com.teerayut.bitcoinwidget.RefreshIntervalActivity { *; }
-keep class com.teerayut.bitcoinwidget.PriceAlertsActivity { *; }
-keep class com.teerayut.bitcoinwidget.FeatureTestActivity { *; }
-keep class com.teerayut.bitcoinwidget.WidgetTestActivity { *; }

# Keep notification classes
-keep class com.teerayut.bitcoinwidget.alerts.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Optimization
-optimizationpasses 5
-allowaccessmodification
-dontpreverify