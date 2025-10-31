cmake_minimum_required(VERSION 3.10)
project(bpm_core)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

add_subdirectory(aubio)

add_library(bpm_core STATIC
    src/bpm_detector.cpp
)

target_include_directories(bpm_core
    PUBLIC include
    PRIVATE aubio/src
)

target_link_libraries(bpm_core
    PUBLIC aubio
)
