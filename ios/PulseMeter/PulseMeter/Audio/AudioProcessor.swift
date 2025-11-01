//
//  AudioProcessor.swift
//  PulseMeter
//
//  Created by Joel on 01/11/2025.
//

import Foundation
import AVFoundation
import Combine
import SwiftUI

final class AudioProcessor: ObservableObject {
    private let engine = AVAudioEngine()
    @Published var bpm: Float = 0.0
    @Published var isRunning = false

    private var mockTimer: Timer?

    func start() {
        #if targetEnvironment(simulator)
        print("Running on Simulator using mock BPM data (no audio input).")
        startMockBPM()
        return
        #endif

        let input = engine.inputNode
        let format = input.inputFormat(forBus: 0)

        // Verify format before continuing
        guard format.sampleRate > 0, format.channelCount > 0 else {
            print("Invalid input format: \(format)")
            return
        }

        input.removeTap(onBus: 0)
        input.installTap(onBus: 0, bufferSize: 1024, format: format) { [weak self] buffer, _ in
            guard let self else { return }

            // Access sample data
            _ = buffer.floatChannelData?[0]

            DispatchQueue.main.async {
                // Placeholder BPM logic — replace with aubio later
                self.bpm = Float.random(in: 90...130)
            }
        }

        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.record, options: [.mixWithOthers])
            try session.setActive(true)
            try engine.start()
            DispatchQueue.main.async { self.isRunning = true }
            print("Engine started")
        } catch {
            print("Audio error:", error)
        }
    }

    func stop() {
        #if targetEnvironment(simulator)
        stopMockBPM()
        #else
        engine.stop()
        #endif

        DispatchQueue.main.async { self.isRunning = false }
        print("Engine stopped")
    }

    // MARK: - Mock BPM Generator (Simulator)

    private func startMockBPM() {
        isRunning = true
        mockTimer = Timer.scheduledTimer(withTimeInterval: 0.8, repeats: true) { [weak self] _ in
            guard let self else { return }
            withAnimation(.easeInOut(duration: 0.3)) {
                self.bpm = Float.random(in: 80...150)
            }
        }
    }

    private func stopMockBPM() {
        mockTimer?.invalidate()
        mockTimer = nil
        bpm = 0
    }
}
