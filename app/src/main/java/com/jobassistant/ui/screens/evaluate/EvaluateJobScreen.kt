package com.jobassistant.ui.screens.evaluate

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.ui.components.FitScoreRing
import java.util.UUID

private val EVAL_TABS = listOf("Paste Text", "Paste URL", "Screenshot")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluateJobScreen(
    onBack: () -> Unit = {},
    onNavigateToJobDetail: (String) -> Unit = {},
    viewModel: EvaluateJobViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ocrText by viewModel.ocrText.collectAsStateWithLifecycle()
    val resumeEmpty by viewModel.resumeEmpty.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processScreenshot(it, context) }
    }

    // Navigate to job detail when saved
    if (uiState is EvaluateJobUiState.Saved) {
        val jobId = (uiState as EvaluateJobUiState.Saved).jobId.toString()
        onNavigateToJobDetail(jobId)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluate Fit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // No-resume banner (15.4)
            if (resumeEmpty) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Upload your resume in Profile for accurate scoring",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            when (val state = uiState) {
                is EvaluateJobUiState.Idle, is EvaluateJobUiState.Analyzing -> {
                    val isAnalyzing = state is EvaluateJobUiState.Analyzing
                    InputSection(
                        ocrText = ocrText,
                        isAnalyzing = isAnalyzing,
                        onAnalyzePaste = { viewModel.analyzeFromPaste(it) },
                        onAnalyzeUrl = { viewModel.analyzeFromUrl(it) },
                        onPickScreenshot = { imagePickerLauncher.launch("image/*") },
                        onAnalyzeOcr = { viewModel.analyzeFromPaste(ocrText) }
                    )
                }

                is EvaluateJobUiState.Result -> {
                    ResultSection(
                        state = state,
                        onSaveJob = { company, role -> viewModel.saveJob(company, role) },
                        onReset = { viewModel.reset() }
                    )
                }

                is EvaluateJobUiState.Error -> {
                    ErrorSection(
                        message = state.message,
                        onRetry = { viewModel.reset() }
                    )
                }

                is EvaluateJobUiState.Saved -> Unit // handled above
            }
        }
    }
}

// ── Input section ─────────────────────────────────────────────────────────────

@Composable
private fun InputSection(
    ocrText: String,
    isAnalyzing: Boolean,
    onAnalyzePaste: (String) -> Unit,
    onAnalyzeUrl: (String) -> Unit,
    onPickScreenshot: () -> Unit,
    onAnalyzeOcr: () -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    var pasteText by remember { mutableStateOf("") }
    var urlText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabRow(
            selectedTabIndex = tabIndex,
            indicator = { tabPositions ->
                if (tabIndex < tabPositions.size) {
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[tabIndex])
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        ) {
            EVAL_TABS.forEachIndexed { index, label ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(label) }
                )
            }
        }

        if (isAnalyzing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Analyzing fit…", style = MaterialTheme.typography.bodySmall)
        } else {
            when (tabIndex) {
                0 -> { // Paste Text
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { if (it.length <= 4000) pasteText = it },
                        label = { Text("Paste job description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        maxLines = 14,
                        supportingText = { Text("${pasteText.length} / 4000") }
                    )
                    Button(
                        onClick = { onAnalyzePaste(pasteText) },
                        enabled = pasteText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("check_fit_button")
                    ) {
                        Text("Check Fit")
                    }
                }
                1 -> { // Paste URL
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text("Job posting URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = { onAnalyzeUrl(urlText) },
                        enabled = urlText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Check Fit")
                    }
                }
                2 -> { // Screenshot
                    Button(
                        onClick = onPickScreenshot,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pick Screenshot")
                    }
                    if (ocrText.isNotBlank()) {
                        OutlinedTextField(
                            value = ocrText,
                            onValueChange = {},
                            label = { Text("Extracted text") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            minLines = 4,
                            maxLines = 8
                        )
                        Button(
                            onClick = onAnalyzeOcr,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Check Fit")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ── Result section ────────────────────────────────────────────────────────────

@Composable
private fun ResultSection(
    state: EvaluateJobUiState.Result,
    onSaveJob: (String, String) -> Unit,
    onReset: () -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var roleTitle by remember { mutableStateOf("") }
    var prosExpanded by remember { mutableStateOf(true) }
    var consExpanded by remember { mutableStateOf(true) }
    var missingExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Score ring card — animates in
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.85f)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FitScoreRing(
                        score = state.analysis.score,
                        size = 96.dp,
                        strokeWidth = 10.dp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Fit Score", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Expandable sections
        ExpandableResultSection(
            title = "Strengths (${state.analysis.pros.size})",
            items = state.analysis.pros,
            expanded = prosExpanded,
            onToggle = { prosExpanded = !prosExpanded },
            itemColor = Color(0xFF2E7D32)
        )
        ExpandableResultSection(
            title = "Weaknesses (${state.analysis.cons.size})",
            items = state.analysis.cons,
            expanded = consExpanded,
            onToggle = { consExpanded = !consExpanded },
            itemColor = Color(0xFFF57F17)
        )
        ExpandableResultSection(
            title = "Missing Skills (${state.analysis.missingSkills.size})",
            items = state.analysis.missingSkills,
            expanded = missingExpanded,
            onToggle = { missingExpanded = !missingExpanded },
            itemColor = Color(0xFF1565C0)
        )

        // Save section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Want to track this opportunity?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = roleTitle,
                    onValueChange = { roleTitle = it },
                    label = { Text("Role Title (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { onSaveJob(companyName, roleTitle) },
                    modifier = Modifier.fillMaxWidth().testTag("save_and_track_button")
                ) {
                    Text("Save & Track")
                }
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth().testTag("start_over_button")
                ) {
                    Text("Start Over")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ExpandableResultSection(
    title: String,
    items: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit,
    itemColor: Color
) {
    if (items.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items.forEach { item ->
                        Text(
                            "• $item",
                            style = MaterialTheme.typography.bodySmall,
                            color = itemColor
                        )
                    }
                }
            }
        }
    }
}

// ── Saved state ───────────────────────────────────────────────────────────────
// (handled via LaunchedEffect navigation in parent — no separate composable needed)

// ── Error section ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("Try Again")
        }
    }
}
