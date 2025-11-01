//
//  BPMDetector.swift
//  PulseMeter
//
//  Created by Joel on 01/11/2025.
//

import Foundation


class BPMDetector {
    init(sampleRate: Int32 = 44100) {
        bpm_init(sampleRate)
    }

    deinit {
        bpm_terminate()
    }

    func process(samples: [Float]) -> Float {
        return bpm_process(samples, Int32(samples.count))
    }

    func reset() {
        bpm_reset()
    }
}
