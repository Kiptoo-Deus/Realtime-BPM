cmake_minimum_required(VERSION 3.22.1)
project(pulsemeter)


add_library(
        native-lib
        SHARED
        src/main/cpp/native-lib.cpp
)


target_include_directories(
        native-lib PRIVATE
        ${CMAKE_SOURCE_DIR}/../../core/include
)


add_library(bpm_core SHARED IMPORTED)
set_target_properties(bpm_core PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/arm64-v8a/libbpm_core.so
)

# Link Android's log library
find_library(log-lib log)

# Link everything together
target_link_libraries(
        native-lib
        bpm_core
        ${log-lib}
)
