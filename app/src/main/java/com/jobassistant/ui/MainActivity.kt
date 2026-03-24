package com.jobassistant.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jobassistant.ui.navigation.AppNavigation
import com.jobassistant.ui.theme.JobAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    /**
     * Phase 8 (8.3): holds the image URI received from a system share-sheet intent.
     * Jetpack Compose state — recomposition happens automatically when this changes.
     */
    private var sharedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Handle share intent delivered at launch time
        sharedImageUri = extractSharedImageUri(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            JobAssistantTheme(appTheme = uiState.selectedTheme) {
                AppNavigation(
                    isOnboardingComplete = uiState.isOnboardingComplete,
                    sharedImageUri = sharedImageUri,
                    onSharedImageConsumed = { sharedImageUri = null }
                )
            }
        }
    }

    /**
     * Phase 8 (8.3): called by the OS when the app is already running and the user shares
     * another image. Uses [android:launchMode] default (standard) — a new task is started,
     * so this fires reliably for share intents.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedImageUri = extractSharedImageUri(intent)
    }

    /** Extracts an image [Uri] from an ACTION_SEND intent, or returns null for other intents. */
    private fun extractSharedImageUri(intent: Intent?): Uri? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (!intent.type.orEmpty().startsWith("image/")) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }
}
