package com.inspectavision

import android.app.Application
import android.util.Log
import com.inspectavision.llm.LlamaCppBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class InspectaVisionApplication : Application() {
    
    companion object {
        private const val TAG = "InspectaVisionApp"
        
        lateinit var instance: InspectaVisionApplication
            private set
        
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }
    
    val llamaBridge: LlamaCppBridge by lazy {
        LlamaCppBridge()
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Log.i(TAG, "InspectaVision Application started")
        
        // Initialize llama.cpp backend
        initLlamaBackend()
    }
    
    private fun initLlamaBackend() {
        appScope.launchInBackground {
            Log.d(TAG, "Initializing llama.cpp backend")
            // Backend is initialized on-demand when model is loaded
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        appScope.launchInBackground {
            llamaBridge.free()
        }
    }
}

// Extension for launching coroutines in background
fun CoroutineScope.launchInBackground(block: suspend () -> Unit) {
    kotlinx.coroutines.launch(Dispatchers.IO) {
        block()
    }
}
