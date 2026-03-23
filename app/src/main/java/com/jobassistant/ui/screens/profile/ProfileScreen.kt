package com.jobassistant.ui.screens.profile

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val profileUiState by viewModel.profileUiState.collectAsStateWithLifecycle()
    val gmailEmail by viewModel.gmailEmail.collectAsStateWithLifecycle()
    val gmailNeedsReauth by viewModel.gmailNeedsReauth.collectAsStateWithLifecycle()
    val reauthIntent by viewModel.reauthIntent.collectAsStateWithLifecycle()
    val userApiKey by viewModel.userApiKey.collectAsStateWithLifecycle()
    val exportUri by viewModel.exportUri.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var fullName by remember(profileState.fullName) { mutableStateOf(profileState.fullName) }
    var careerGoal by remember(profileState.careerGoal) { mutableStateOf(profileState.careerGoal) }
    var keywords by remember(profileState.keywords) {
        mutableStateOf(profileState.keywords.joinToString(", "))
    }
    var targetSalaryMin by remember(profileState.targetSalaryMin) {
        mutableIntStateOf(profileState.targetSalaryMin)
    }
    var targetSalaryMax by remember(profileState.targetSalaryMax) {
        mutableIntStateOf(profileState.targetSalaryMax)
    }
    var apiKeyDraft by remember { mutableStateOf("") }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onResumePicked(it, context) }
    }

    // Gap 1 — launch interactive re-auth when the ViewModel emits a PendingIntent.
    // After the user completes the consent screen (RESULT_OK), call authorize() again —
    // it will succeed silently and the ViewModel saves the new token.
    val reauthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.requestInteractiveReauth(context)
        }
    }

    LaunchedEffect(reauthIntent) {
        reauthIntent?.let { intent ->
            val sender = IntentSenderRequest.Builder(intent.intentSender).build()
            reauthLauncher.launch(sender)
            viewModel.consumeReauthIntent()
        }
    }

    // Sync field values when profile loads from DataStore
    LaunchedEffect(profileState) {
        fullName = profileState.fullName
        careerGoal = profileState.careerGoal
        keywords = profileState.keywords.joinToString(", ")
        targetSalaryMin = profileState.targetSalaryMin
        targetSalaryMax = profileState.targetSalaryMax
    }

    // 9.2 — fire share intent when export URI is ready
    LaunchedEffect(exportUri) {
        exportUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Job Data"))
            viewModel.consumeExportUri()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Resume section ───────────────────────────────────────────────────
        Text("Your Resume", style = MaterialTheme.typography.titleLarge)

        Button(
            onClick = { pdfLauncher.launch("application/pdf") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_resume_button")
        ) {
            Text("Upload PDF Resume")
        }

        when (val state = profileUiState) {
            is ProfileUiState.ExtractingPdf -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Text("Extracting text…", style = MaterialTheme.typography.bodySmall)
                }
            }

            is ProfileUiState.PdfExtracted -> {
                Text(
                    text = "Preview: ${state.previewText}…",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("resume_preview")
                )
            }

            is ProfileUiState.AnalyzingIntent -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Text("Analyzing career intent…", style = MaterialTheme.typography.bodySmall)
                }
            }

            is ProfileUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("profile_error")
                )
            }

            else -> Unit
        }

        if (profileState.resumeText.isNotBlank() && profileUiState !is ProfileUiState.ExtractingPdf) {
            Text(
                text = "Resume loaded (${profileState.resumeText.length} chars)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else if (profileState.resumeText.isBlank() && profileUiState is ProfileUiState.Idle) {
            // Gap 3 — prompt user to upload resume before AI features work
            Text(
                text = "Upload your resume to enable AI fit scoring and career analysis",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("no_resume_warning")
            )
        }

        HorizontalDivider()

        // ── Career Details section ───────────────────────────────────────────
        Text("Career Details", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_name_field"),
            singleLine = true
        )

        OutlinedTextField(
            value = careerGoal,
            onValueChange = { careerGoal = it },
            label = { Text("Career Goal") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("career_goal_field"),
            minLines = 2,
            maxLines = 4
        )

        OutlinedTextField(
            value = keywords,
            onValueChange = { keywords = it },
            label = { Text("Keywords (comma-separated)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("keywords_field"),
            singleLine = true
        )

        OutlinedTextField(
            value = if (targetSalaryMin > 0) targetSalaryMin.toString() else "",
            onValueChange = { targetSalaryMin = it.toIntOrNull() ?: 0 },
            label = { Text("Min Salary") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("salary_min_field"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = if (targetSalaryMax > 0) targetSalaryMax.toString() else "",
            onValueChange = { targetSalaryMax = it.toIntOrNull() ?: 0 },
            label = { Text("Max Salary") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("salary_max_field"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                viewModel.saveProfile(
                    fullName = fullName,
                    careerGoal = careerGoal,
                    keywords = keywords.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    targetSalaryMin = targetSalaryMin,
                    targetSalaryMax = targetSalaryMax
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_profile_button")
        ) {
            Text("Save Profile")
        }

        if (profileUiState is ProfileUiState.Saved) {
            Text(
                text = "Profile saved!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("profile_saved_text")
            )
        }

        // ── AI Career Summary section ────────────────────────────────────────
        val aiSummary = when (val state = profileUiState) {
            is ProfileUiState.IntentAnalyzed -> state.profile.goalMap
            else -> if (profileState.careerGoal.isNotBlank() && profileState.resumeText.isNotBlank())
                profileState.careerGoal else null
        }

        if (aiSummary != null) {
            HorizontalDivider()
            Text("AI Career Summary", style = MaterialTheme.typography.titleLarge)
            Text(
                text = aiSummary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("ai_career_summary")
            )
        }

        // ── Data section (9.2 Export) ────────────────────────────────────────
        HorizontalDivider()
        Text("Data", style = MaterialTheme.typography.titleLarge)

        Button(
            onClick = { viewModel.exportData(context) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("export_data_button")
        ) {
            Text("Export Data")
        }

        // ── API Settings section (9.4 BYOK) ─────────────────────────────────
        HorizontalDivider()
        Text("API Settings", style = MaterialTheme.typography.titleLarge)

        if (userApiKey != null) {
            Text(
                text = "Custom API key is set",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("api_key_set_label")
            )
        } else {
            // Gap 3 — warn users when no API key is configured (built-in key may be absent in production)
            Text(
                text = "No custom API key set — AI features use the built-in key which may be rate-limited",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("no_api_key_warning")
            )
        }

        OutlinedTextField(
            value = apiKeyDraft,
            onValueChange = { apiKeyDraft = it },
            label = { Text("Gemini API Key") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("api_key_field"),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = { viewModel.saveApiKey(apiKeyDraft) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_api_key_button"),
            enabled = apiKeyDraft.isNotBlank()
        ) {
            Text("Save API Key")
        }

        // ── Gmail section ─────────────────────────────────────────────────────
        HorizontalDivider()
        Text("Gmail Integration", style = MaterialTheme.typography.titleLarge)

        if (gmailEmail != null) {
            Text(
                text = "Connected: $gmailEmail",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("gmail_connected_email")
            )

            // Gap 1 — re-auth banner when access token has expired
            if (gmailNeedsReauth) {
                Text(
                    text = "Gmail access expired — tap below to reconnect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("gmail_reauth_banner")
                )
                Button(
                    onClick = { viewModel.requestInteractiveReauth(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gmail_reauth_button")
                ) {
                    Text("Re-authenticate Gmail")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.syncGmailNow(context) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gmail_sync_now_button")
                ) {
                    Text("Sync Now")
                }
                OutlinedButton(
                    onClick = { viewModel.disconnectGmail(context) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gmail_disconnect_button")
                ) {
                    Text("Disconnect")
                }
            }
        } else {
            if (profileUiState is ProfileUiState.GmailConnecting) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Text("Signing in…", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Button(
                    onClick = { viewModel.connectGmail(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gmail_connect_button")
                ) {
                    Text("Connect Gmail")
                }
                TextButton(
                    onClick = { /* skip */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set up later")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
