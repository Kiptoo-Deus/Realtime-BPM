package com.example.pulsemeter

import android.content.Context
import android.media.*
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsemeter.ui.theme.PulseMeterTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.withContext

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

    private var processingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PulseMeterTheme {
                var bpm by remember { mutableStateOf(0f) }
                var isProcessing by remember { mutableStateOf(false) }

                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        val mime = contentResolver.getType(uri) ?: ""
                        if (mime.startsWith("audio/")) {
                            isProcessing = true
                            processingJob?.cancel()
                            processingJob = processAudioFile(uri, this) { detectedBpm ->
                                bpm = detectedBpm
                                isProcessing = false
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Please select a valid audio file",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = if (isProcessing) "Detecting BPM..." else "Pulse Meter",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF5C6BC0)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (bpm > 0f) "%.1f BPM".format(bpm) else "-- BPM",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Select Audio File", fontSize = 18.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        processingJob?.cancel()
        releaseBpmDetector()
    }

    private fun processAudioFile(
        uri: Uri,
        context: Context,
        onBpmDetected: (Float) -> Unit
    ): Job {
        return lifecycleScope.launch(Dispatchers.IO) {
            try {
                val extractor = MediaExtractor()
                context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                    extractor.setDataSource(fd.fileDescriptor)
                }

                var trackIndex = -1
                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                    if (mime.startsWith("audio/")) {
                        trackIndex = i
                        extractor.selectTrack(i)
                        break
                    }
                }

                if (trackIndex == -1) throw IllegalArgumentException("No audio track found")

                val format = extractor.getTrackFormat(trackIndex)
                val codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
                codec.configure(format, null, null, 0)
                codec.start()

                val allSamples = mutableListOf<Short>()
                val bufferInfo = MediaCodec.BufferInfo()
                var isEOS = false

                while (!isEOS) {
                    val inputIndex = codec.dequeueInputBuffer(10000)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex)!!
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }

                    var outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                    while (outputIndex >= 0) {
                        val outputBuffer = codec.getOutputBuffer(outputIndex)!!
                        val bytes = ByteArray(bufferInfo.size)
                        outputBuffer.get(bytes, 0, bufferInfo.size)

                        val shorts = ShortArray(bufferInfo.size / 2)
                        for (i in shorts.indices) {
                            shorts[i] = ((bytes[i * 2 + 1].toInt() shl 8) or (bytes[i * 2].toInt() and 0xFF)).toShort()
                        }
                        allSamples.addAll(shorts.toList())

                        codec.releaseOutputBuffer(outputIndex, false)
                        outputIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                    }
                }

                codec.stop()
                codec.release()
                extractor.release()

                // Detect BPM using native method
                initBpmDetector(format.getInteger(MediaFormat.KEY_SAMPLE_RATE), allSamples.size)
                val bpm = processAudioBuffer(allSamples.toShortArray())
                releaseBpmDetector()

                withContext(Dispatchers.Main) {
                    onBpmDetected(bpm)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
