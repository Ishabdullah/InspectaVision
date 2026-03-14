package com.inspectavision.ui.screens

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inspectavision.llm.LlamaCppBridge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    llamaBridge: LlamaCppBridge,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var contextSize by remember { mutableIntStateOf(4096) }
    var threads by remember { mutableIntStateOf(4) }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var maxTokens by remember { mutableIntStateOf(512) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var modelInfo by remember { mutableStateOf("") }
    
    LaunchedEffect(llamaBridge.isLoaded) {
        if (llamaBridge.isLoaded) {
            modelInfo = llamaBridge.getModelInfo()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Model Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (llamaBridge.isLoaded) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (llamaBridge.isLoaded) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (llamaBridge.isLoaded) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                        Text(
                            text = if (llamaBridge.isLoaded) "Model Loaded" else "No Model Loaded",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (llamaBridge.isLoaded) {
                        Text(
                            text = modelInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = llamaBridge.getMemoryInfo(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Select a GGUF model from the Model Selector to begin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Inference Settings
            SettingsSection(title = "Inference Settings") {
                SettingsSlider(
                    label = "Context Size",
                    value = contextSize.toFloat(),
                    valueRange = 512f..8192f,
                    steps = 10,
                    valueText = "$contextSize tokens",
                    onValueChange = { contextSize = it.toInt() }
                )
                
                SettingsSlider(
                    label = "CPU Threads",
                    value = threads.toFloat(),
                    valueRange = 1f..8f,
                    steps = 6,
                    valueText = "$threads threads",
                    onValueChange = { threads = it.toInt() }
                )
                
                SettingsSlider(
                    label = "Temperature",
                    value = temperature,
                    valueRange = 0.1f..1.5f,
                    steps = 13,
                    valueText = String.format("%.1f", temperature),
                    onValueChange = { temperature = it }
                )
                
                SettingsSlider(
                    label = "Max Tokens",
                    value = maxTokens.toFloat(),
                    valueRange = 64f..2048f,
                    steps = 15,
                    valueText = "$maxTokens tokens",
                    onValueChange = { maxTokens = it.toInt() }
                )
            }
            
            // App Info
            SettingsSection(title = "Application") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About InspectaVision",
                    subtitle = "Version 1.0.0",
                    onClick = { showAboutDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Device Info",
                    subtitle = "Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}",
                    onClick = { }
                )
                
                SettingsItem(
                    icon = Icons.Default.Memory,
                    title = "Memory Info",
                    subtitle = if (llamaBridge.isLoaded) llamaBridge.getMemoryInfo() else "Load model to see memory usage",
                    onClick = { }
                )
            }
            
            // Actions
            SettingsSection(title = "Actions") {
                if (llamaBridge.isLoaded) {
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Unload Model",
                        subtitle = "Free memory by unloading the current model",
                        onClick = {
                            scope.launch {
                                llamaBridge.free()
                            }
                        },
                        destructive = true
                    )
                }
                
                SettingsItem(
                    icon = Icons.Default.BugReport,
                    title = "Export Logs",
                    subtitle = "Save debug logs for troubleshooting",
                    onClick = { /* Export logs */ }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("About InspectaVision") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Version 1.0.0")
                        Text("AI-powered home inspection analysis running locally on your device using llama.cpp and GGUF models.")
                        Divider()
                        Text(
                            text = "Powered by:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("• llama.cpp - High-performance LLM inference")
                        Text("• Google Gemini (optional) - Cloud-based analysis")
                        Text("• Jetpack Compose - Modern Android UI")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (destructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (destructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SettingsSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueText: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
