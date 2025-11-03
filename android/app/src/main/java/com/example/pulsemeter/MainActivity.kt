package com.example.pulsemeter

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.pulsemeter.ui.theme.PulseMeterTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("bpm_core")
            System.loadLibrary("native-lib")
        }
    }

    // Native functions
    external fun initBpmDetector(sampleRate: Int, bufferSize: Int)
    external fun processAudioBuffer(data: ShortArray): Float
    external fun releaseBpmDetector()

    private var recordingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionAndStart()
    }

    private fun requestPermissionAndStart() {
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) startAudioProcessing()
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            startAudioProcessing()
        } else {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startAudioProcessing() {
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        initBpmDetector(sampleRate, bufferSize)

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        recorder.startRecording()

        setContent {
            PulseMeterTheme {
                var bpm by remember { mutableStateOf(0f) }

                LaunchedEffect(Unit) {
                    recordingJob = CoroutineScope(Dispatchers.Default).launch {
                        val buffer = ShortArray(bufferSize)
                        while (isActive) {
                            val read = recorder.read(buffer, 0, buffer.size)
                            if (read > 0) {
                                val newBpm = processAudioBuffer(buffer)
                                if (newBpm > 0) bpm = newBpm
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(
                        text = "Current BPM: %.1f".format(bpm),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recordingJob?.cancel()
        releaseBpmDetector()
    }
}

@Preview
@Composable
fun PreviewPulseMeter() {
    PulseMeterTheme {
        Text("Current BPM: --")
    }
}
