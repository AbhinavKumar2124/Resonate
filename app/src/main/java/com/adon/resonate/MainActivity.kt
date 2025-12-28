package com.adon.resonate

import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.adon.resonate.ui.theme.ResonateTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.math.PI
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ResonateTheme {
                MicLoopScreen()
            }
        }
    }
}

@Composable
fun MicLoopScreen() {
    var running by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val isPermissionGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        isPermissionGranted.value = isGranted
        if (isGranted) {
            running = true
            scope.launch { startMicLoopback() }
        }
        else {
            Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    MaterialTheme{
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (!running) {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        } else {
                            running = false
                            isPlaying = false
                        }
                    }
                ) {
                    Text(if (running) "Stop Mic Loop" else "Start Mic Loop")
                }
            }
        }
    }
}

var track: AudioTrack? = null
var isPlaying = false
@androidx.annotation.RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
suspend fun startMicLoopback() = withContext(Dispatchers.IO ) {
    val sampleRate = 48000
    val channelIn = AudioFormat.CHANNEL_IN_MONO
    val channelOut = AudioFormat.CHANNEL_OUT_MONO
    val format = AudioFormat.ENCODING_PCM_16BIT

    val minRecBuf = AudioRecord.getMinBufferSize(sampleRate, channelIn, format)
    val minPlayBuf = AudioTrack.getMinBufferSize(sampleRate, channelOut, format)

    val record = AudioRecord.Builder()
        .setAudioSource(MediaRecorder.AudioSource.MIC)
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(format)
                .setChannelMask(channelIn)
                .build()
        )
        .setBufferSizeInBytes(minRecBuf * 2)
        .build()

    track = AudioTrack.Builder()
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(format)
                .setChannelMask(channelOut)
                .build()
        )
        .setBufferSizeInBytes(minPlayBuf * 2)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    val buffer = ShortArray(2048)

    record.startRecording()
    track?.play()
    isPlaying = true

    try {
        while(isPlaying) {
            val read = record.read(buffer, 0, buffer.size)
            if (read > 0) {
                track?.write(buffer, 0, read)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            record.stop()
        }
        record.release()

        if (track?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            track?.stop()
        }
        track?.release()
        track = null
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ResonateTheme {
        MicLoopScreen()
    }
}