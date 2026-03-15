#include <jni.h>
#include <android/log.h>
#include <string>
#include <thread>
#include <atomic>
#include <functional>
#include <sstream>
#include <vector>
#include <cstring>

#include "llama.h"

#define LOG_TAG "InspectaVision-LLM"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Global model and context pointers
static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;
static std::atomic<bool> g_is_loading(false);
static std::atomic<bool> g_is_generating(false);
static std::atomic<bool> g_stop_generation(false);

// Callback for token streaming
static std::function<void(const std::string&)> g_token_callback;
static std::function<void(bool)> g_completion_callback;

// Helper to convert jstring to std::string
static std::string jstring_to_string(JNIEnv* env, jstring jstr) {
    if (!jstr) return "";
    const char* chars = env->GetStringUTFChars(jstr, nullptr);
    std::string result(chars);
    env->ReleaseStringUTFChars(jstr, chars);
    return result;
}

// Helper to convert std::string to jstring
static jstring string_to_jstring(JNIEnv* env, const std::string& str) {
    return env->NewStringUTF(str.c_str());
}

extern "C" {

// JNI_OnLoad
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnLoad called");
    return JNI_VERSION_1_6;
}

// Initialize the LLM with a model path
JNIEXPORT jboolean JNICALL
Java_com_inspectavision_llm_LlamaCppBridge_nativeInitialize(
    JNIEnv* env,
    jobject thiz,
    jstring model_path,
    jint n_ctx,
    jint n_threads,
    jint n_gpu_layers
) {
    LOGI("Initializing LLM...");
    
    bool expected = false;
    if (!g_is_loading.compare_exchange_strong(expected, true)) {
        LOGE("LLM already loading or loaded");
        return JNI_FALSE;
    }
    
    std::string model_path_str = jstring_to_string(env, model_path);
    LOGI("Model path: %s", model_path_str.c_str());
    
    // Free existing model and context
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_free_model(g_model);
        g_model = nullptr;
    }
    
    // Model parameters
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = n_gpu_layers;
    
    // Context parameters
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = static_cast<uint32_t>(n_ctx);
    ctx_params.n_batch = 512;
    ctx_params.n_threads = static_cast<uint32_t>(n_threads);
    ctx_params.use_mmap = true;
    ctx_params.use_mlock = false;
    
    // Load the model
    LOGI("Loading model with n_ctx=%d, n_threads=%d", n_ctx, n_threads);
    g_model = llama_load_model_from_file(model_path_str.c_str(), model_params);
    
    if (!g_model) {
        LOGE("Failed to load model: %s", model_path_str.c_str());
        g_is_loading = false;
        return JNI_FALSE;
    }
    
    // Create context
    g_ctx = llama_new_context_with_model(g_model, ctx_params);
    
    if (!g_ctx) {
        LOGE("Failed to create context");
        llama_free_model(g_model);
        g_model = nullptr;
        g_is_loading = false;
        return JNI_FALSE;
    }
    
    LOGI("LLM initialized successfully");
    g_is_loading = false;
    return JNI_TRUE;
}

// Check if model is loaded
JNIEXPORT jboolean JNICALL
Java_com_inspectavision_llm_LlamaCppBridge_nativeIsLoaded(
    JNIEnv* env,
    jobject thiz
) {
    return (g_model != nullptr && g_ctx != nullptr) ? JNI_TRUE : JNI_FALSE;
}

// Get model info
JNIEXPORT jstring JNICALL
Java_com_inspectavision_llm_LlamaCppBridge_nativeGetModelInfo(
    JNIEnv* env,
    jobject thiz
) {
    if (!g_model) {
        return string_to_jstring(env, "No model loaded");
    }
    
    std::stringstream ss;
    ss << "Model loaded successfully\n";
    ss << "Vocab type: " << llama_vocab_type(g_model) << "\n";
    
    return string_to_jstring(env, ss.str());
}

// Generate response with streaming
JNIEXPORT void JNICALL
Java_com_inspectavision_llm_LlamaCppBridge_nativeGenerate(
    JNIEnv* env,
    jobject thiz,
    jstring prompt,
    jint max_tokens,
    jfloat temperature,
    jfloat top_p,
    jint top_k,
    jobject token_callback,
    jobject completion_callback
) {
    if (!g_ctx) {
        LOGE("Context not initialized");
        return;
    }
    
    bool expected = false;
    if (!g_is_generating.compare_exchange_strong(expected, true)) {
        LOGE("Generation already in progress");
        return;
    }
    
    g_stop_generation = false;
    
    std::string prompt_str = jstring_to_string(env, prompt);
    LOGI("Generating response for prompt length: %zu", prompt_str.length());
    
    // Store callbacks as global for this generation
    JavaVM* vm;
    env->GetJavaVM(&vm);
    
    jobject global_token_cb = env->NewGlobalRef(token_callback);
    jobject global_completion_cb = env->NewGlobalRef(completion_callback);
    
    g_token_callback = [vm, global_token_cb](const std::string& token) {
        JNIEnv* env;
        if (vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            return;
        }
        
        jclass callback_class = env->GetObjectClass(global_token_cb);
        jmethodID method = env->GetMethodID(callback_class, "onToken", "(Ljava/lang/String;)V");
        
        if (method) {
            jstring jtoken = env->NewStringUTF(token.c_str());
            env->CallVoidMethod(global_token_cb, method, jtoken);
            env->DeleteLocalRef(jtoken);
        }
        
        vm->DetachCurrentThread();
    };
    
    g_completion_callback = [vm, global_completion_cb, global_token_cb](bool success) {
        JNIEnv* env;
        if (vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            return;
        }
        
        jclass callback_class = env->GetObjectClass(global_completion_cb);
        jmethodID method = env->GetMethodID(callback_class, "onComplete", "(Z)V");
        
        if (method) {
            env->CallVoidMethod(global_completion_cb, method, success ? JNI_TRUE : JNI_FALSE);
        }
        
        // Clean up
        env->DeleteGlobalRef(global_token_cb);
        env->DeleteGlobalRef(global_completion_cb);
        vm->DetachCurrentThread();
    };
    
    // Run generation in a separate thread
    std::thread([prompt_str, max_tokens, temperature, top_p, top_k]() {
        // Tokenize the prompt
        const int n_prompt_max = 4096;
        std::vector<llama_token> tokens(n_prompt_max);
        
        int32_t n_tokens = llama_tokenize(
            llama_get_model(g_ctx),
            prompt_str.c_str(),
            prompt_str.size(),
            tokens.data(),
            tokens.size(),
            true,  // add_special
            true   // parse_special
        );
        
        if (n_tokens < 0) {
            LOGE("Tokenization failed");
            if (g_completion_callback) {
                g_completion_callback(false);
            }
            g_is_generating = false;
            return;
        }
        
        tokens.resize(n_tokens);
        
        // Decode the prompt
        if (llama_decode(g_ctx, llama_batch_get_one(tokens.data(), n_tokens, 0, 0)) != 0) {
            LOGE("Failed to decode prompt");
            if (g_completion_callback) {
                g_completion_callback(false);
            }
            g_is_generating = false;
            return;
        }
        
        int32_t n_decode = 0;
        llama_token new_token_id;
        
        // Sampling parameters
        llama_sampling_params sparams;
        sparams.temp = temperature;
        sparams.top_p = top_p;
        sparams.top_k = top_k;
        sparams.penalty_last_n = 64;
        sparams.penalty_repeat = 1.0f;
        sparams.penalty_freq = 0.0f;
        sparams.penalty_present = 0.0f;
        
        llama_sampling_context* ctx_sampling = llama_sampling_init(sparams);
        
        // Generation loop
        while (n_decode < max_tokens && !g_stop_generation) {
            // Sample the next token
            new_token_id = llama_sampling_sample(ctx_sampling, g_ctx, nullptr, 0);
            
            // Check for EOS
            if (new_token_id == llama_token_eos(llama_get_model(g_ctx))) {
                break;
            }
            
            // Get token string
            std::vector<char> result(256);
            int n_chars = llama_token_to_piece(g_ctx, new_token_id, result.data(), result.size());
            if (n_chars > 0) {
                std::string token_str(result.data(), n_chars);
                
                // Call token callback
                if (g_token_callback) {
                    g_token_callback(token_str);
                }
            }
            
            // Update sampling context
            llama_sampling_accept(ctx_sampling, g_ctx, new_token_id, true);
            
            // Decode the new token
            if (llama_decode(g_ctx, llama_batch_get_one(&new_token_id, 1, 0, 0)) != 0) {
                LOGE("Decode failed");
                break;
            }
            
            n_decode++;
        }
        
        llama_sampling_free(ctx_sampling);
        
        LOGI("Generation completed, tokens decoded: %d", n_decode);
        
        if (g_completion_callback) {
            g_completion_callback(!g_stop_generation);
        }
        
        g_is_generating = false;
    }).detach();
}

// Stop generation
JNIEXPORT void JNICALL
Java_com_inspectavision_llm_LlamaCppBridge_nativeStopGeneration(
    JNIEnv* env,
    jobject thiz
) {
    LOGI("Stopping generation");
    g_stop_generation = true;
}

// Check if generating
JNIEXPORT jboolean JNICALL
Java_com_inspectavision_llm_LlamaCppBridge_nativeIsGenerating(
    JNIEnv* env,
    jobject thiz
) {
    return g_is_generating ? JNI_TRUE : JNI_FALSE;
}

// Free the model
JNIEXPORT void JNICALL
Java_com_inspectavision_llm_LlamaCppBridge_nativeFree(
    JNIEnv* env,
    jobject thiz
) {
    LOGI("Freeing LLM resources");
    
    g_stop_generation = true;
    
    // Wait for generation to stop
    while (g_is_generating) {
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }
    
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    
    if (g_model) {
        llama_free_model(g_model);
        g_model = nullptr;
    }
    
    LOGI("LLM resources freed");
}

// Get memory info
JNIEXPORT jstring JNICALL
Java_com_inspectavision_llm_LlamaCppBridge_nativeGetMemoryInfo(
    JNIEnv* env,
    jobject thiz
) {
    if (!g_ctx) {
        return string_to_jstring(env, "No context");
    }
    
    std::stringstream ss;
    ss << "Context size: " << llama_n_ctx(g_ctx) << "\n";
    ss << "KV cache used: " << llama_get_kv_cache_used_cells(g_ctx) << " cells";
    
    return string_to_jstring(env, ss.str());
}

} // extern "C"
