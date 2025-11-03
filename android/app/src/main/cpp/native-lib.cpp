//
// Created by Joel on 03/11/2025.
//
#include <jni.h>
#include <vector>
#include "bpm_detector.h"

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_pulsemeter_MainActivity_initBpmDetector(JNIEnv*, jobject, jint sampleRate, jint bufferSize) {
    bpm_init(sampleRate);
}

JNIEXPORT jfloat JNICALL
Java_com_example_pulsemeter_MainActivity_processAudioBuffer(JNIEnv* env, jobject, jshortArray audioData) {
    jsize length = env->GetArrayLength(audioData);
    std::vector<jshort> buffer(length);
    env->GetShortArrayRegion(audioData, 0, length, buffer.data());

    std::vector<float> floatBuffer(length);
    for (int i = 0; i < length; i++) {
        floatBuffer[i] = buffer[i] / 32768.0f;
    }

    float bpm = bpm_process(floatBuffer.data(), length);
    return bpm;
}

JNIEXPORT void JNICALL
Java_com_example_pulsemeter_MainActivity_releaseBpmDetector(JNIEnv*, jobject) {
    bpm_reset();
}

} // extern "C"
