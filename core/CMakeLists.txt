cmake_minimum_required(VERSION 3.22)
project(bpm_core LANGUAGES C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

# Paths
set(AUBIO_DIR ${CMAKE_SOURCE_DIR}/aubio)

# Include aubio headers
include_directories(
    ${AUBIO_DIR}/src
    ${AUBIO_DIR}/build/src
    ${AUBIO_DIR}
    ${CMAKE_SOURCE_DIR}/include
)

# Collect sources
file(GLOB_RECURSE AUBIO_SOURCES
    "${AUBIO_DIR}/src/*.c"
)

file(GLOB_RECURSE BPM_SOURCES
    "${CMAKE_SOURCE_DIR}/src/*.c"
    "${CMAKE_SOURCE_DIR}/src/*.cpp"
)

# Detect if building for Android or another platform
if (ANDROID)
    message(STATUS "Building for Android — creating shared library (.so)")
    add_library(bpm_core SHARED
        ${AUBIO_SOURCES}
        ${BPM_SOURCES}
    )
else()
    message(STATUS "Building for non-Android — creating static library (.a)")
    add_library(bpm_core STATIC
        ${AUBIO_SOURCES}
        ${BPM_SOURCES}
    )
endif()

target_include_directories(bpm_core PUBLIC
    ${AUBIO_DIR}/src
    ${AUBIO_DIR}/build/src
    ${AUBIO_DIR}
    ${CMAKE_SOURCE_DIR}/include
)

# I am linking Android system libraries if building for Android must use this, ok I don't know if it's the best way but it works
if (ANDROID)
    target_link_libraries(bpm_core
        log
        android
    )
endif()
