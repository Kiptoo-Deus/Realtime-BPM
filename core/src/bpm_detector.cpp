#include "bpm_detector.h"

extern "C" {
#include "/Users/joel/Documents/GitHub/realtime-bpm/core/aubio/src/aubio.h"
#include "/Users/joel/Documents/GitHub/realtime-bpm/core/aubio/src/tempo/tempo.h"
#include "/Users/joel/Documents/GitHub/realtime-bpm/core/aubio/src/fvec.h"
}

static aubio_tempo_t* tempo = nullptr;
static fvec_t* input = nullptr;
static int samplerate = 44100;
static int buffer_size = 1024;
static int hop_size = 512;

void bpm_init(int sr) {
    samplerate = sr;
    buffer_size = 1024;
    hop_size = 512;

    if (tempo) del_aubio_tempo(tempo);
    if (input) del_fvec(input);

    tempo = new_aubio_tempo("default", buffer_size, hop_size, samplerate);
    input = new_fvec(hop_size);
}

float bpm_process(const float* samples, int length) {
    if (!tempo || !input) return 0.0f;

    int frames = length < hop_size ? length : hop_size;
    for (int i = 0; i < frames; i++) {
        fvec_set_sample(input, samples[i], i);
    }

    aubio_tempo_do(tempo, input, nullptr);
    return aubio_tempo_get_bpm(tempo);
}

void bpm_reset(void) {
    if (tempo) {
        del_aubio_tempo(tempo);
        tempo = new_aubio_tempo("default", buffer_size, hop_size, samplerate);
    }
}

void bpm_terminate(void) {
    if (tempo) {
        del_aubio_tempo(tempo);
        tempo = nullptr;
    }
    if (input) {
        del_fvec(input);
        input = nullptr;
    }
}
