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

# Collect aubio sources
file(GLOB_RECURSE AUBIO_SOURCES
    "${AUBIO_DIR}/src/*.c"
)


file(GLOB_RECURSE BPM_SOURCES
    "${CMAKE_SOURCE_DIR}/src/*.c"
    "${CMAKE_SOURCE_DIR}/src/*.cpp"
)

add_library(bpm_core STATIC
    ${AUBIO_SOURCES}
    ${BPM_SOURCES}
)

target_include_directories(bpm_core PUBLIC
    ${AUBIO_DIR}/src
    ${AUBIO_DIR}/build/src
    ${AUBIO_DIR}
    ${CMAKE_SOURCE_DIR}/include
)
