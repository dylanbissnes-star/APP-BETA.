package com.example.glassplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.glassplayer.ui.PlayerScreen
import com.example.glassplayer.ui.theme.GlassPlayerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    private val audioPermission =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            viewModel.loadTracks(result[audioPermission] == true || hasAudioPermission())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GlassPlayerTheme { PlayerScreen(viewModel) }
        }

        if (savedInstanceState == null) {
            if (hasAudioPermission()) {
                viewModel.loadTracks(true)
            } else {
                val perms = mutableListOf(audioPermission)
                if (Build.VERSION.SDK_INT >= 33) perms += Manifest.permission.POST_NOTIFICATIONS
                requestPermissions.launch(perms.toTypedArray())
            }
        }
    }

    private fun hasAudioPermission() =
        ContextCompat.checkSelfPermission(this, audioPermission) == PackageManager.PERMISSION_GRANTED
}
