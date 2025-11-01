#ifndef BPM_DETECTOR_H
#define BPM_DETECTOR_H

#ifdef __cplusplus
extern "C" {
#endif

void bpm_init(int samplerate);
float bpm_process(const float* samples, int length);
void bpm_reset(void);
void bpm_terminate(void);

#ifdef __cplusplus
}
#endif

#endif 
