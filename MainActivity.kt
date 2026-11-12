package com.shrii.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) {
            tts.language = Locale("hi", "IN")
            tts.voices?.firstOrNull { it.locale.language == "hi" && (it.name.contains("hia") || it.name.contains("female", true)) }?.let { tts.voice = it }
            tts.setPitch(0.95f)
        }
        val prefs = getSharedPreferences("shrii", Context.MODE_PRIVATE)
        setContent { MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Ink)) { Screen(prefs) } }
    }

    @Composable
    fun Screen(prefs: SharedPreferences) {
        var key by remember { mutableStateOf(prefs.getString("key", "") ?: "") }
        var input by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("idle") }
        val msgs = remember { mutableStateListOf<String>() }

        fun send(t: String) {
            if (t.isBlank()) return
            prefs.edit().putString("key", key).apply()
            msgs.add("Aap: $t"); input = ""; Agent.stop = false; state = "thinking"
            Agent.run(this@MainActivity, t, key,
                { m -> runOnUiThread { msgs.add("Shrii: $m"); tts.speak(m, TextToSpeech.QUEUE_FLUSH, null, "s") } },
                { s -> runOnUiThread { state = s } })
        }
        val mic = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
            r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { send(it) }
        }

        Column(Modifier.fillMaxSize().background(Ink).padding(16.dp).padding(top = 24.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("1. Accessibility") }
                Button({ startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }) { Text("2. Overlay") }
            }
            OutlinedTextField(key, { key = it }, Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Claude API key") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Orb(state, Modifier.fillMaxSize())
            }
            Text("Namaste, kya karna hai? ($state)", Modifier.align(Alignment.CenterHorizontally))
            LazyColumn(Modifier.weight(1f).padding(vertical = 8.dp)) { items(msgs) { Text(it, Modifier.padding(vertical = 4.dp)) } }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Command likho...") })
                Button({
                    mic.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN"))
                }) { Text("Mic") }
                Button({ send(input) }) { Text("Go") }
            }
        }
    }
}
