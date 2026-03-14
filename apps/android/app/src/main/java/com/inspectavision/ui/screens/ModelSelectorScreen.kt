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
        val pathsToScan = listOf(
            File(Environment.getExternalStorageDirectory(), "models"),
            File(Environment.getExternalStorageDirectory(), "Download/models"),
            File("/sdcard/models"),
            File(customPath).takeIf { customPath.isNotEmpty() && File(customPath).exists() }
        ).filterNotNull()
        
        val allModels = mutableListOf<File>()
        for (path in pathsToScan) {
            if (path.exists() && path.isDirectory) {
                allModels.addAll(llamaBridge.findModelFiles(path))
            }
        }
        modelFiles = allModels.distinctBy { it.absolutePath }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Model") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPathDialog = true }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Add Path")
                    }
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
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "GGUF Model Files",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Select a GGUF format model file. Recommended: 7B or smaller for mobile devices.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
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
                            text = "Place .gguf files in /sdcard/models or /sdcard/Download/models",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { showPathDialog = true }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Custom Path")
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
                                    context = android.content.ContextWrapper(
                                        androidx.compose.ui.platform.LocalContext.current
                                    ),
                                    modelPath = selectedModel!!.absolutePath
                                )
                                isLoading = false
                                if (success) {
                                    onModelLoaded()
                                } else {
                                    errorMessage = "Failed to load model. Check logcat for details."
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
                            Text("Load Model")
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
                title = { Text("Add Model Path") },
                text = {
                    OutlinedTextField(
                        value = customPath,
                        onValueChange = { customPath = it },
                        label = { Text("Directory Path") },
                        placeholder = { Text("/sdcard/MyModels") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPathDialog = false
                            scanForModels()
                        }
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

@Composable
fun ModelFileCard(
    file: File,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val fileSizeMb = file.length() / 1024.0 / 1024.0
    val fileSizeStr = String.format("%.1f MB", fileSizeMb)
    
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
                    }
                )
                Column {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = file.parent ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = fileSizeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
