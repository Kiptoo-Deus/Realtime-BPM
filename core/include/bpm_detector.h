#ifndef BPM_DETECTOR_H
#define BPM_DETECTOR_H

#ifdef __cplusplus
extern "C" {
#endif

void bpm_init(int sample_rate, int buffer_size); //I'm initializing the bpm detector
void bpm_process(float* samples, int length);
float bpm_get_estimate(void);
void bpm_cleanup(void);

#ifdef __cplusplus
}
#endif

#endif
