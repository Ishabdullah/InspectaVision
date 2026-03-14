package com.inspectavision.ui.screens

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inspectavision.llm.LlamaCppBridge
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorScreen(
    llamaBridge: LlamaCppBridge,
    onModelLoaded: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var modelFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedModel by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPathDialog by remember { mutableStateOf(false) }
    var customPath by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        scanForModels()
    }
    
    fun scanForModels() {
        // Get external storage directories
        val externalDirs = context.getExternalFilesDirs(null)
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        
        // Paths to scan for GGUF models
        val pathsToScan = listOf(
            // Standard Downloads folder
            downloadDir,
            
            // Downloads/models subfolder
            File(downloadDir, "models"),
            
            // Root of external storage
            Environment.getExternalStorageDirectory(),
            
            // Common model locations
            File(Environment.getExternalStorageDirectory(), "models"),
            
            // App-specific external storage
            externalDirs.firstOrNull()?.parentFile?.parentFile,
            
            // Custom path from user
            File(customPath).takeIf { customPath.isNotEmpty() && File(customPath).exists() }
        ).filterNotNull().filter { it.exists() && it.isDirectory }
        
        val allModels = mutableListOf<File>()
        for (path in pathsToScan) {
            allModels.addAll(llamaBridge.findModelFiles(path))
            // Also scan subdirectories up to 2 levels deep
            path.listFiles { file -> file.isDirectory }?.forEach { dir ->
                allModels.addAll(llamaBridge.findModelFiles(dir))
                dir.listFiles { subDir -> subDir.isDirectory }?.forEach { subDir ->
                    allModels.addAll(llamaBridge.findModelFiles(subDir))
                }
            }
        }
        
        // Remove duplicates and sort by name
        modelFiles = allModels.distinctBy { it.absolutePath }.sortedBy { it.name.lowercase() }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Load GGUF Model") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { scanForModels() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            // Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "100% Offline - No Server Required",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Place .gguf files in your Downloads folder or /sdcard/models. The app runs entirely on your device using llama.cpp.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Quick access buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        customPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                        scanForModels()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Downloads")
                }
                OutlinedButton(
                    onClick = { 
                        customPath = "/sdcard/models"
                        scanForModels()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("/sdcard/models")
                }
            }
            
            if (modelFiles.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.FolderOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No GGUF Models Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Download a GGUF model and place it in:\n• /sdcard/Download/\n• /sdcard/models/",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Recommended: Llama-3.2-1B, Phi-3-mini, or Qwen2.5-1.5B (Q4_K_M quantization)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Button(
                            onClick = { showPathDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse Custom Location")
                        }
                    }
                }
            } else {
                // Model List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(modelFiles, key = { it.absolutePath }) { file ->
                        ModelFileCard(
                            file = file,
                            isSelected = selectedModel == file,
                            onClick = { selectedModel = file }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            
            // Load Button
            if (selectedModel != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val success = llamaBridge.initialize(
                                    context = context,
                                    modelPath = selectedModel!!.absolutePath,
                                    ctxSize = 4096,
                                    threads = 4,
                                    gpuLayers = 0
                                )
                                isLoading = false
                                if (success) {
                                    onModelLoaded()
                                } else {
                                    errorMessage = "Failed to load model. The file may be corrupted or too large for your device's RAM."
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Loading Model...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Model - ${formatFileSize(selectedModel!!.length())}")
                        }
                    }
                }
            }
        }
        
        // Error Snackbar
        errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
        
        // Path Dialog
        if (showPathDialog) {
            AlertDialog(
                onDismissRequest = { showPathDialog = false },
                title = { Text("Browse to Folder") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Enter the full path to a folder containing GGUF models:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = customPath,
                            onValueChange = { customPath = it },
                            label = { Text("Folder Path") },
                            placeholder = { Text("/sdcard/MyModels") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = customPath.isNotEmpty() && !File(customPath).exists()
                        )
                        if (customPath.isNotEmpty() && !File(customPath).exists()) {
                            Text(
                                text = "Path does not exist",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (File(customPath).exists() && File(customPath).isDirectory) {
                                showPathDialog = false
                                scanForModels()
                            }
                        },
                        enabled = File(customPath).exists() && File(customPath).isDirectory
                    ) {
                        Text("Scan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPathDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / 1024 / 1024} MB"
        else -> String.format("%.2f GB", bytes / 1024.0 / 1024.0 / 1024.0)
    }
}

@Composable
fun ModelFileCard(
    file: File,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val fileSizeMb = file.length() / 1024.0 / 1024.0
    val fileSizeStr = formatFileSize(file.length())
    val isLargeModel = fileSizeMb > 4000 // Warn if > 4GB
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(32.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fileSizeStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isLargeModel) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "Needs 8+ GB RAM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = file.parent ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
