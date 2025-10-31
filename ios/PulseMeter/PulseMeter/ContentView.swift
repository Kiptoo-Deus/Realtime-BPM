//
//  ContentView.swift
//  PulseMeter
//
//  Created by Joel on 01/11/2025.
//

import SwiftUI
import AVFAudio

struct ContentView: View {
    @StateObject private var processor = AudioProcessor()

    var body: some View {
        ZStack {
           
            RadialGradient(
                gradient: Gradient(colors: [.purple.opacity(0.3), .black]),
                center: .center,
                startRadius: 50,
                endRadius: 400
            )
            .ignoresSafeArea()

            VStack(spacing: 60) {
                Text("PulseMeter")
                    .font(.largeTitle.bold())
                    .foregroundStyle(.white)
                    .shadow(radius: 10)

                ZStack {
                    Circle()
                        .fill(.white.opacity(0.15))
                        .frame(width: 250, height: 250)
                        .shadow(radius: 20)

                    Circle()
                        .strokeBorder(.blue.opacity(0.7), lineWidth: 6)
                        .frame(width: 250, height: 250)
                        .scaleEffect(1 + 0.03 * sin(CGFloat(processor.bpm) / 2))
                        .animation(.easeInOut(duration: 0.3), value: processor.bpm)

                    Text(String(format: "%.1f", processor.bpm))
                        .font(.system(size: 72, weight: .heavy, design: .rounded))
                        .foregroundStyle(.white)
                        .shadow(radius: 8)
                }

                Button(action: {
                    processor.isRunning ? processor.stop() : processor.start()
                }) {
                    HStack(spacing: 12) {
                        Image(systemName: processor.isRunning ? "stop.fill" : "play.fill")
                            .font(.title3)
                        Text(processor.isRunning ? "Stop" : "Start")
                            .font(.headline)
                    }
                    .padding(.horizontal, 40)
                    .padding(.vertical, 16)
                    .background(processor.isRunning ? Color.red : Color.blue)
                    .foregroundColor(.white)
                    .clipShape(Capsule())
                    .shadow(radius: 10)
                    .animation(.easeInOut, value: processor.isRunning)
                }
            }
        }
    }
}

#Preview { ContentView() }
