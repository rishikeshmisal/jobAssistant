# Room entities and DAOs
-keep class com.jobassistant.data.db.** { *; }

# Retrofit / Claude API response models
-keep class com.jobassistant.data.remote.model.** { *; }

# Domain models — serialized by Gson for JSON export (Phase 9)
-keep class com.jobassistant.domain.model.** { *; }
-keep enum com.jobassistant.domain.model.** { *; }

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# Gson — preserve generic type signatures so TypeToken works under R8 full mode
-keepattributes Signature
-keepattributes *Annotation*

# Gson internal classes needed for reflection-based serialization
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# PDFBox-Android — native text extraction library
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-dontwarn org.apache.pdfbox.**

# Google Gemini AI SDK
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# OkHttp + Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Kotlin Coroutines — prevent R8 from stripping dispatcher internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# WorkManager workers — R8 must not rename or remove Worker subclasses
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# Hilt — keep all generated component entry points
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-dontwarn dagger.**
