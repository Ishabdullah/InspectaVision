cmake_minimum_required(VERSION 3.22.1)

project("inspectavision" VERSION 1.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# llama.cpp options
set(GGML_NATIVE OFF CACHE BOOL "Disable native optimizations" FORCE)
set(GGML_METAL OFF CACHE BOOL "Disable Metal (iOS/Mac)" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "Disable OpenMP" FORCE)
set(GGML_CUDA OFF CACHE BOOL "Disable CUDA" FORCE)
set(GGML_VULKAN OFF CACHE BOOL "Disable Vulkan" FORCE)
set(GGML_HIP OFF CACHE BOOL "Disable HIP" FORCE)
set(GGML_RPC OFF CACHE BOOL "Disable RPC" FORCE)
set(GGML_BUILD_TESTS OFF CACHE BOOL "Disable tests" FORCE)
set(GGML_BUILD_EXAMPLES OFF CACHE BOOL "Disable examples" FORCE)

# Add llama.cpp as a subdirectory
add_subdirectory(llama.cpp)

# Build the JNI library
add_library(inspectavision-native SHARED
    jni_bridge.cpp
)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)

# Include directories
target_include_directories(inspectavision-native PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/include
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/ggml/include
)

# Link libraries
target_link_libraries(inspectavision-native
    llama
    ggml
    ${log-lib}
    ${android-lib}
)

# Compiler flags
target_compile_options(inspectavision-native PRIVATE
    -Wall
    -Wextra
    -Wno-unused-parameter
    -fexceptions
    -frtti
)

# Android specific flags
if(CMAKE_SYSTEM_NAME STREQUAL "Android")
    target_compile_options(inspectavision-native PRIVATE
        -fdata-sections
        -ffunction-sections
    )
endif()
