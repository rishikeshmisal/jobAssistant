package com.jobassistant.ui.screens.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jobassistant.ui.MainViewModel
import com.jobassistant.ui.screens.profile.ProfileUiState
import com.jobassistant.ui.screens.profile.ProfileViewModel

private const val TOTAL_STEPS = 4

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var careerGoal by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }

    val profileUiState by profileViewModel.profileUiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step dot indicator at top
        StepDotIndicator(currentStep = currentStep, totalSteps = TOTAL_STEPS)

        Spacer(Modifier.height(32.dp))

        // Centered step content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                0 -> StepWelcome(
                    name = name,
                    onNameChange = { name = it },
                    onNext = { currentStep = 1 }
                )
                1 -> StepCareerGoal(
                    careerGoal = careerGoal,
                    onCareerGoalChange = { careerGoal = it },
                    keywords = keywords,
                    onKeywordsChange = { keywords = it },
                    onBack = { currentStep = 0 },
                    onNext = { currentStep = 2 }
                )
                2 -> StepResumeUpload(
                    profileUiState = profileUiState,
                    onResumeSelected = { uri, context ->
                        profileViewModel.onResumePicked(uri, context)
                    },
                    onSkip = { currentStep = 3 },
                    onNext = { currentStep = 3 },
                    onBack = { currentStep = 1 }
                )
                3 -> StepConnect(
                    onBack = { currentStep = 2 },
                    onGetStarted = {
                        mainViewModel.completeOnboarding(
                            name = name,
                            careerGoal = careerGoal,
                            keywords = keywords.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        )
                        onOnboardingComplete()
                    }
                )
            }
        }
    }
}

// ── Step dot indicator ────────────────────────────────────────────────────────

@Composable
private fun StepDotIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .then(
                        if (isActive) Modifier.width(24.dp) else Modifier.width(8.dp)
                    )
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    .animateContentSize(animationSpec = tween(200))
            )
        }
    }
}

// ── Step 0: Welcome ───────────────────────────────────────────────────────────

@Composable
private fun StepWelcome(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Icon(
        imageVector = Icons.Filled.WorkspacePremium,
        contentDescription = null,
        modifier = Modifier.size(96.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Welcome to Job Assistant",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Let's set up your profile",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Your name") },
        modifier = Modifier.fillMaxWidth().testTag("name_field"),
        singleLine = true
    )
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onNext,
        enabled = name.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Next")
    }
}

// ── Step 1: Career Goal ───────────────────────────────────────────────────────

@Composable
private fun StepCareerGoal(
    careerGoal: String,
    onCareerGoalChange: (String) -> Unit,
    keywords: String,
    onKeywordsChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Icon(
        imageVector = Icons.Filled.TrendingUp,
        contentDescription = null,
        modifier = Modifier.size(96.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = "What's your career goal?",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "This helps us tailor your job insights",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))
    OutlinedTextField(
        value = careerGoal,
        onValueChange = onCareerGoalChange,
        label = { Text("Career goal") },
        modifier = Modifier.fillMaxWidth().testTag("career_goal_field"),
        minLines = 3,
        maxLines = 5
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = keywords,
        onValueChange = onKeywordsChange,
        label = { Text("Keywords (comma-separated, optional)") },
        modifier = Modifier.fillMaxWidth().testTag("keywords_field"),
        singleLine = true
    )
    Spacer(Modifier.height(24.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Button(onClick = onNext, enabled = careerGoal.isNotBlank()) { Text("Next") }
    }
}

// ── Step 2: Resume Upload ─────────────────────────────────────────────────────

@Composable
private fun StepResumeUpload(
    profileUiState: ProfileUiState,
    onResumeSelected: (Uri, android.content.Context) -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onResumeSelected(it, context) }
    }

    val isLoading = profileUiState is ProfileUiState.ExtractingPdf ||
            profileUiState is ProfileUiState.AnalyzingIntent
    val resumeReady = profileUiState is ProfileUiState.PdfExtracted ||
            profileUiState is ProfileUiState.IntentAnalyzed

    Icon(
        imageVector = Icons.Filled.UploadFile,
        contentDescription = null,
        modifier = Modifier.size(96.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Upload Your Resume",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Upload a PDF resume so we can analyze your fit for each job. You can skip and add later.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))

    if (isLoading) {
        CircularProgressIndicator()
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (profileUiState is ProfileUiState.AnalyzingIntent)
                "Analyzing your career profile…" else "Extracting text…",
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        Button(
            onClick = { pdfLauncher.launch("application/pdf") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_upload_button")
        ) {
            Text(if (resumeReady) "Replace Resume" else "Choose PDF")
        }
    }

    if (resumeReady) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Resume uploaded! Tap Next to continue.",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("Next") }
    }

    Spacer(Modifier.height(8.dp))
    TextButton(
        onClick = onSkip,
        modifier = Modifier.fillMaxWidth().testTag("skip_resume_button")
    ) {
        Text("Skip for now")
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        TextButton(onClick = onBack) { Text("Back") }
    }
}

// ── Step 3: Connect Gmail ─────────────────────────────────────────────────────

@Composable
private fun StepConnect(
    onBack: () -> Unit,
    onGetStarted: () -> Unit
) {
    Icon(
        imageVector = Icons.Filled.Email,
        contentDescription = null,
        modifier = Modifier.size(96.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Connect Gmail",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "You can connect Gmail later to auto-import job applications from your inbox.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))
    Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) { Text("Get Started") }
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) { Text("Set up later") }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        TextButton(onClick = onBack) { Text("Back") }
    }
}
