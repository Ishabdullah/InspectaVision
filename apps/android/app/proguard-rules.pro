# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt

# Keep llama.cpp native methods
-keepclassmembers class com.inspectavision.llm.LlamaCppBridge {
    private native boolean nativeInitialize(java.lang.String, int, int, int);
    private native boolean nativeIsLoaded();
    private native java.lang.String nativeGetModelInfo();
    private native void nativeGenerate(java.lang.String, int, float, float, int, com.inspectavision.llm.TokenCallback, com.inspectavision.llm.CompletionCallback);
    private native void nativeStopGeneration();
    private native boolean nativeIsGenerating();
    private native void nativeFree();
    private native java.lang.String nativeGetMemoryInfo();
}

# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes
-keepclassmembers class com.inspectavision.** {
    *** Companion;
}
-keepclasseswithmembers class com.inspectavision.** {
    *** Companion;
}

# Keep Compose
-keep class androidx.compose.** { *; }
