# ProGuard / R8 Configuration for VentureForge AI

# Keep line numbers for debugging production stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Moshi & JSON Model rules
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *

# VentureForge domain & entity models
-keep class com.example.domain.model.** { *; }
-keep class com.example.data.database.entity.** { *; }

# Google Mobile Ads SDK
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes Exceptions
