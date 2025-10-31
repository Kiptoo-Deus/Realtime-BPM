#include "bpm_detector.h"
#include <core/aubio/src/aubio.h>
#include <mutex>

static aubio_tempo_t* tempo = nullptr;
static fvec_t* buffer = nullptr;
static std::mutex bpm_mutex;
static float last_bpm = -1.0f;

void bpm_init(int sample_rate, int buffer_size) {
    std::lock_guard<std::mutex> lock(bpm_mutex);
    if (tempo) bpm_cleanup();

    tempo = new_aubio_tempo("default", buffer_size, buffer_size/2, sample_rate);
    buffer = new_fvec(buffer_size/2);
}

void bpm_process(float* samples, int length) {
    std::lock_guard<std::mutex> lock(bpm_mutex);
    if (!tempo || !buffer) return;

    int hop_size = buffer->length;
    for (int i = 0; i + hop_size <= length; i += hop_size) {
        for (int j = 0; j < hop_size; j++)
            buffer->data[j] = samples[i + j];
        aubio_tempo_do(tempo, buffer);
        if (aubio_tempo_get_confidence(tempo) > 0.5f)
            last_bpm = aubio_tempo_get_bpm(tempo);
    }
}

float bpm_get_estimate(void) {
    std::lock_guard<std::mutex> lock(bpm_mutex);
    return last_bpm;
}

void bpm_cleanup(void) {
    std::lock_guard<std::mutex> lock(bpm_mutex);
    if (tempo) { del_aubio_tempo(tempo); tempo = nullptr; }
    if (buffer) { del_fvec(buffer); buffer = nullptr; }
    last_bpm = -1;
}
