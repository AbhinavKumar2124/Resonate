package com.adon.resonate

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ResonateTheme {
                JsonFetchScreen()
            }
        }
    }
}

@Composable
fun JsonFetchScreen() {
    var result by remember { mutableStateOf("Press button to fetch JSON") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    MaterialTheme{
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(result, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            result = fetchJson()
                            loading = false
                        }
                    },
                    enabled = !loading
                ) {
                    Text(if(loading) "Loading..." else "Fetch JSON")
                }
            }
        }
    }
}
suspend fun fetchJson(): String {
    return withContext(Dispatchers.IO ) {
        URL("https://jsonplaceholder.typicode.com/todos/1").readText()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ResonateTheme {
        JsonFetchScreen()
    }
}