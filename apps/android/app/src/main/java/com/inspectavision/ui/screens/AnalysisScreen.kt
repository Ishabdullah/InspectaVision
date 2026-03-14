package com.inspectavision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.inspectavision.llm.LlamaCppBridge
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    imageUri: String,
    llamaBridge: LlamaCppBridge,
    onNavigateBack: () -> Unit,
    onSaveFinding: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf("") }
    var currentToken by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Roof") }
    
    val categories = listOf("Roof", "Exterior", "Basement/Foundation", "Heating", "Cooling", "Plumbing", "Electrical", "Fireplace", "Attic/Insulation", "Interior")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Analysis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSaveFinding() },
                        enabled = analysisResult.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Image Preview
            AsyncImage(
                model = imageUri,
                contentDescription = "Inspection Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            
            // Category Selector
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontal = false
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            
            Divider()
            
            // Analysis Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Analyze Button
                if (analysisResult.isBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "AI-Powered Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Analyze this photo using the local LLM to identify potential issues",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Button(
                                onClick = {
                                    isAnalyzing = true
                                    analysisResult = ""
                                    currentToken = ""
                                    
                                    val prompt = buildPrompt(selectedCategory)
                                    
                                    scope.launch {
                                        llamaBridge.generate(
                                            prompt = prompt,
                                            maxTokens = 512,
                                            temperature = 0.3f,
                                            topP = 0.9f,
                                            topK = 40,
                                            onToken = { token ->
                                                currentToken += token
                                                analysisResult = currentToken
                                            },
                                            onComplete = { success ->
                                                isAnalyzing = false
                                                if (!success) {
                                                    analysisResult = "Analysis was stopped or failed."
                                                }
                                            }
                                        )
                                    }
                                },
                                enabled = !isAnalyzing && llamaBridge.isLoaded()
                            ) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Analyzing...")
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Run AI Analysis")
                                }
                            }
                            
                            if (!llamaBridge.isLoaded()) {
                                Text(
                                    text = "Load a model first in Model Selector",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                } else {
                    // Results Display
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Analysis Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        isAnalyzing = true
                                        currentToken = ""
                                        analysisResult = ""
                                        // Re-run analysis
                                    },
                                    enabled = !isAnalyzing
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Retry")
                                }
                                IconButton(
                                    onClick = {
                                        llamaBridge.stopGeneration()
                                        isAnalyzing = false
                                    },
                                    enabled = isAnalyzing
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                                }
                            }
                        }
                        
                        // Streaming Output
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (isAnalyzing) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                
                                Text(
                                    text = analysisResult,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        // Extracted Findings (Mock parsing)
                        if (!isAnalyzing && analysisResult.isNotBlank()) {
                            Text(
                                text = "Extracted Findings",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            val findings = parseFindings(analysisResult)
                            findings.forEach { finding ->
                                FindingCard(finding = finding)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FindingCard(finding: Finding) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (finding.severity) {
                "safety" -> MaterialTheme.colorScheme.errorContainer
                "major" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                "maintenance" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = finding.location,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (finding.severity) {
                        "safety" -> Color.Red
                        "major" -> Color(0xFFFF9800)
                        "maintenance" -> Color(0xFFFFC107)
                        else -> Color(0xFF4CAF50)
                    }
                ) {
                    Text(
                        text = finding.severity.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
            
            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Recommendation: ${finding.recommendation}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class Finding(
    val location: String,
    val severity: String,
    val description: String,
    val recommendation: String
)

fun parseFindings(text: String): List<Finding> {
    // Simple parsing - in production would use structured output
    return listOf(
        Finding(
            location = "Observed Area",
            severity = "maintenance",
            description = text.take(200),
            recommendation = "Further evaluation recommended"
        )
    )
}

fun buildPrompt(category: String): String {
    return """
You are a professional home inspector. Analyze the described condition for the $category category.

Provide your analysis in the following format:

**Location:** [Specific location of the issue]
**Severity:** [safety/major/minor/maintenance/info]
**Description:** [Detailed professional description]
**Recommendation:** [Clear repair or evaluation recommendation]

Describe what you would typically look for in a $category inspection and common issues found.
""".trimIndent()
}
