package com.adon.resonate

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                ToneScreen()
            }
        }
    }
}

@Composable
fun ToneScreen() {
    var playing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    MaterialTheme{
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (!playing) {
                            playing = true
                            scope.launch { playTone() }
                        } else {
                            playing = false
                            isPlaying = false
                        }
                    }
                ) {
                    Text(if (playing) "Stop Tone" else "Play 440 Hz Tone")
                }
            }
        }
    }
}

var audioTrack: AudioTrack? = null
var isPlaying = false
suspend fun playTone() = withContext(Dispatchers.IO ) {
    val sampleRate = 48000
    val frequency = 440.0
    val amplitude = 0.3

    val bufferSize = 960 //20ms buffer
    val buffer = ShortArray(bufferSize)

    audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(sampleRate).setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(bufferSize * 2)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    audioTrack?.play()
    isPlaying = true

    var phase = 0.0
    val increment = 2.0 * Math.PI * frequency / sampleRate
    while (isPlaying) {
        for (i in buffer.indices) {
            buffer[i] = (sin(phase) * amplitude * Short.MAX_VALUE).toInt().toShort()
            phase += increment
            if (phase > 2 * PI) phase -= 2 * PI
        }
        audioTrack?.write(buffer, 0, bufferSize)
    }
    audioTrack?.stop()
    audioTrack?.release()
    audioTrack = null
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ResonateTheme {
        ToneScreen()
    }
}