package com.inspectavision.ui.screens

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
import java.text.SimpleDateFormat
import java.util.*

data class SavedFinding(
    val id: String,
    val category: String,
    val location: String,
    val severity: String,
    val description: String,
    val recommendation: String,
    val createdAt: Date
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindingsListScreen(
    onNavigateBack: () -> Unit
) {
    // Mock data - would come from Room database in production
    var findings by remember {
        mutableStateOf(
            listOf(
                SavedFinding(
                    id = "1",
                    category = "Roof",
                    location = "Southwest corner",
                    severity = "maintenance",
                    description = "Missing asphalt shingles with exposed underlayment visible",
                    recommendation = "Repair by licensed roofing contractor",
                    createdAt = Date()
                ),
                SavedFinding(
                    id = "2",
                    category = "Electrical",
                    location = "Main panel",
                    severity = "safety",
                    description = "Double-tapped breaker observed on circuit 12",
                    recommendation = "Correct by licensed electrician immediately",
                    createdAt = Date(System.currentTimeMillis() - 86400000)
                ),
                SavedFinding(
                    id = "3",
                    category = "Plumbing",
                    location = "Under kitchen sink",
                    severity = "minor",
                    description = "Minor water staining on cabinet bottom",
                    recommendation = "Monitor for active leakage",
                    createdAt = Date(System.currentTimeMillis() - 172800000)
                )
            )
        )
    }
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedSeverity by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Findings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { /* Export */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add manual finding */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Finding")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryChip(
                    label = "Total",
                    count = findings.size,
                    color = MaterialTheme.colorScheme.primary
                )
                SummaryChip(
                    label = "Safety",
                    count = findings.count { it.severity == "safety" },
                    color = MaterialTheme.colorScheme.error
                )
                SummaryChip(
                    label = "Major",
                    count = findings.count { it.severity == "major" },
                    color = Color(0xFFFF9800)
                )
                SummaryChip(
                    label = "Maintenance",
                    count = findings.count { it.severity == "maintenance" },
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Divider()
            
            if (findings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ListOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No Findings Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "AI-analyzed issues will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val filteredFindings = findings.filter {
                    selectedSeverity == null || it.severity == selectedSeverity
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFindings, key = { it.id }) { finding ->
                        FindingListItem(
                            finding = finding,
                            onClick = { /* View details */ },
                            onDelete = { findings = findings.filter { it.id != finding.id } }
                        )
                    }
                }
            }
        }
        
        // Filter Dialog
        if (showFilterDialog) {
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                title = { Text("Filter by Severity") },
                content = {
                    Column {
                        listOf(null, "safety", "major", "minor", "maintenance", "info").forEach { severity ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSeverity = severity
                                        showFilterDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedSeverity == severity,
                                    onClick = {
                                        selectedSeverity = severity
                                        showFilterDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(severity?.uppercase() ?: "All")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFilterDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun SummaryChip(label: String, count: Int, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .width(80.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
fun FindingListItem(
    finding: SavedFinding,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = finding.category,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = when (finding.severity) {
                            "safety" -> MaterialTheme.colorScheme.error
                            "major" -> Color(0xFFFF9800)
                            "maintenance" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    ) {
                        Text(
                            text = finding.severity.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Text(
                    text = finding.location,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = finding.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Text(
                    text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(finding.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
