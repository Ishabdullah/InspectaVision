package com.inspectavision.llm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * JNI Bridge for llama.cpp inference
 * Provides local LLM inference for inspection analysis
 */
class LlamaCppBridge {
    
    companion object {
        private const val TAG = "LlamaCppBridge"
        private const val DEFAULT_CTX_SIZE = 4096
        private const val DEFAULT_THREADS = 4
        private const val DEFAULT_GPU_LAYERS = 0 // CPU only by default
        
        init {
            System.loadLibrary("inspectavision-native")
        }
    }
    
    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()
    
    private val _currentResponse = MutableStateFlow("")
    val currentResponse: StateFlow<String> = _currentResponse.asStateFlow()
    
    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * Initialize the LLM with a model file
     * @param context Android context
     * @param modelPath Path to the GGUF model file
     * @param ctxSize Context size (default: 4096)
     * @param threads Number of CPU threads (default: 4)
     * @param gpuLayers Number of layers to offload to GPU (default: 0)
     */
    suspend fun initialize(
        context: Context,
        modelPath: String,
        ctxSize: Int = DEFAULT_CTX_SIZE,
        threads: Int = DEFAULT_THREADS,
        gpuLayers: Int = DEFAULT_GPU_LAYERS
    ): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Initializing LLM with model: $modelPath")
        
        // Validate model file exists
        val modelFile = File(modelPath)
        if (!modelFile.exists()) {
            Log.e(TAG, "Model file not found: $modelPath")
            _errorMessage.value = "Model file not found: $modelPath"
            return@withContext false
        }
        
        _errorMessage.value = null
        val result = nativeInitialize(modelPath, ctxSize, threads, gpuLayers)
        
        if (result) {
            _isLoaded.value = true
            Log.i(TAG, "LLM initialized successfully")
        } else {
            _errorMessage.value = "Failed to initialize LLM. Check logcat for details."
            Log.e(TAG, "LLM initialization failed")
        }
        
        result
    }
    
    /**
     * Check if model is loaded
     */
    fun isLoaded(): Boolean = nativeIsLoaded()
    
    /**
     * Get model information
     */
    fun getModelInfo(): String = nativeGetModelInfo()
    
    /**
     * Generate a response with streaming
     * @param prompt Input prompt
     * @param maxTokens Maximum tokens to generate
     * @param temperature Sampling temperature (0.0-1.0)
     * @param topP Top-p sampling
     * @param topK Top-k sampling
     * @param onToken Callback for each generated token
     * @param onComplete Callback when generation completes
     */
    fun generate(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f,
        topP: Float = 0.9f,
        topK: Int = 40,
        onToken: (String) -> Unit = {},
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (!isLoaded()) {
            Log.e(TAG, "Cannot generate: model not loaded")
            onComplete(false)
            return
        }
        
        _currentResponse.value = ""
        _isGenerating.value = true
        
        nativeGenerate(
            prompt = prompt,
            maxTokens = maxTokens,
            temperature = temperature,
            topP = topP,
            topK = topK,
            tokenCallback = object : TokenCallback {
                override fun onToken(token: String) {
                    _currentResponse.value += token
                    onToken(token)
                }
            },
            completionCallback = object : CompletionCallback {
                override fun onComplete(success: Boolean) {
                    _isGenerating.value = false
                    onComplete(success)
                }
            }
        )
    }
    
    /**
     * Stop ongoing generation
     */
    fun stopGeneration() {
        nativeStopGeneration()
    }
    
    /**
     * Check if currently generating
     */
    fun isGenerating(): Boolean = nativeIsGenerating()
    
    /**
     * Free model resources
     */
    suspend fun free() = withContext(Dispatchers.IO) {
        nativeFree()
        _isLoaded.value = false
        _isGenerating.value = false
        _currentResponse.value = ""
    }
    
    /**
     * Get memory usage information
     */
    fun getMemoryInfo(): String = nativeGetMemoryInfo()
    
    /**
     * Find GGUF model files in a directory
     */
    fun findModelFiles(directory: File): List<File> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }
        
        return directory.listFiles { file ->
            file.extension.equals("gguf", ignoreCase = true)
        }?.sortedBy { it.name } ?: emptyList()
    }
    
    /**
     * Get default models directory path
     */
    fun getDefaultModelsPath(): String {
        // Try common locations for GGUF models
        val possiblePaths = listOf(
            "/storage/emulated/0/Download/models",
            "/storage/emulated/0/models",
            "/sdcard/models",
            android.os.Environment.getExternalStorageDirectory().absolutePath + "/models"
        )
        
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists() && file.isDirectory) {
                return path
            }
        }
        
        // Fallback to app's external files directory
        return ""
    }
}

/**
 * Callback interface for token streaming
 */
interface TokenCallback {
    fun onToken(token: String)
}

/**
 * Callback interface for generation completion
 */
interface CompletionCallback {
    fun onComplete(success: Boolean)
}
