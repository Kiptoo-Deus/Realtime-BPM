#include "bpm_detector.h"
#include <iostream>
#include <vector>

int main() {
    bpm_init(44100, 1024);
    std::vector<float> fake(44100);
    for (int i = 0; i < fake.size(); i += 22050) fake[i] = 1.0f;

    bpm_process(fake.data(), fake.size());
    std::cout << "Detected BPM: " << bpm_get_estimate() << std::endl;
    bpm_cleanup();
}
