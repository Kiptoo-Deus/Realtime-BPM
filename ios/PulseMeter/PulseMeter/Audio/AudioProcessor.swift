//
//  AudioProcessor.swift
//  PulseMeter
//
//  Created by Joel on 01/11/2025.
//
import Foundation
import AVFoundation
import Combine

final class AudioProcessor: ObservableObject {
    private let engine = AVAudioEngine()
    @Published var bpm: Float = 0.0
    @Published var isRunning = false

    func start() {
        let input = engine.inputNode
        let format = input.inputFormat(forBus: 0)

        input.removeTap(onBus: 0)
        input.installTap(onBus: 0, bufferSize: 1024, format: format) { [weak self] buffer, _ in
            guard let self else { return }
            _ = buffer.floatChannelData?[0]
            DispatchQueue.main.async {
                // Placeholder BPM — later we’ll call aubio here.
                self.bpm = Float.random(in: 90...130)
            }
        }

        do {
            try AVAudioSession.sharedInstance().setCategory(.record, options: [.mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
            try engine.start()
            DispatchQueue.main.async { self.isRunning = true }
            print("🎤 Engine started")
        } catch {
            print("Audio error:", error)
        }
    }

    func stop() {
        engine.stop()
        DispatchQueue.main.async { self.isRunning = false }
        print("Engine stopped")
    }
}

