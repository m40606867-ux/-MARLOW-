package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VibeCheck
import com.example.ui.theme.VibeThemeRegistry
import com.example.viewmodel.VibeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: VibeViewModel,
    modifier: Modifier = Modifier
) {
    val vibeHistory by viewModel.allVibeChecks.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Statistics calculations
    val averagePositivity = remember(vibeHistory) {
        if (vibeHistory.isEmpty()) 0
        else vibeHistory.map { it.positivityScore }.average().toInt()
    }

    val totalEntries = vibeHistory.size

    val topEmotion = remember(vibeHistory) {
        if (vibeHistory.isEmpty()) "None"
        else vibeHistory.groupBy { it.dominantEmotion }
            .maxByOrNull { it.value.size }?.key ?: "None"
    }

    // Emotion distribution for visual charts
    val emotionCounts = remember(vibeHistory) {
        vibeHistory.groupBy { it.dominantEmotion }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FA)) // Clean minimal warm-grey canvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History Icon",
                        tint = Color(0xFF2C3E50),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Vibe History",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF2C3E50)
                    )
                }

                if (vibeHistory.isNotEmpty()) {
                    IconButton(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear all",
                            tint = Color(0xFFC0392B),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Text(
                text = "Tracking your emotional trajectory over time",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (vibeHistory.isEmpty()) {
                EmptyHistoryView()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Summary Stats Card
                    item {
                        StatsSummaryCard(
                            avgPos = averagePositivity,
                            totalEntries = totalEntries,
                            topEmotion = topEmotion,
                            emotionCounts = emotionCounts
                        )
                    }

                    item {
                        Text(
                            text = "Timeline",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2C3E50),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // Vibe List items
                    items(vibeHistory) { vibe ->
                        VibeHistoryCard(
                            vibe = vibe,
                            onDelete = { viewModel.deleteVibe(vibe) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    // Confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Reset Vibe History?") },
            text = { Text("Are you sure you want to permanently delete all your logged vibe checks? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Clear All", color = Color(0xFFC0392B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun EmptyHistoryView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFECEFF1)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEmotions,
                contentDescription = "Empty",
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(54.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your journal is empty",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF2C3E50)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Check in your first vibe to see trends, emotional breakdowns, and historic logs here.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun StatsSummaryCard(
    avgPos: Int,
    totalEntries: Int,
    topEmotion: String,
    emotionCounts: List<Pair<String, Int>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .testTag("stats_summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Emotional Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2C3E50),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Positivity & Entry volume
            Row(modifier = Modifier.fillMaxWidth()) {
                // Avg positivity metric
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "$avgPos%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Avg Positivity",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Volume metric
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalEntries",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Logs",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Top Emotion metric
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3E5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = topEmotion.take(8) + if (topEmotion.length > 8) ".." else "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Top State",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (emotionCounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Emotion distribution:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                emotionCounts.forEach { (emotion, count) ->
                    val percentage = (count.toFloat() / totalEntries * 100).toInt()
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = emotion, fontSize = 12.sp, color = Color(0xFF34495E), fontWeight = FontWeight.Medium)
                            Text(text = "$count check-ins ($percentage%)", fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFF0F0F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(count.toFloat() / totalEntries)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF7B1FA2))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VibeHistoryCard(
    vibe: VibeCheck,
    onDelete: () -> Unit
) {
    val vibeTheme = VibeThemeRegistry.getTheme(vibe.colorTheme)
    val formattedDate = remember(vibe.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()).format(Date(vibe.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .testTag("vibe_history_item_${vibe.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with theme color tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(vibeTheme.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = vibe.dominantEmotion,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = vibeTheme.textOnBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = vibeTheme.cardBackground,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "${vibe.positivityScore}% Pos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = vibeTheme.textOnBackground,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_vibe_button_${vibe.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Delete",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = formattedDate,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Journal Prompt snippet
            Text(
                text = vibe.inputPrompt,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF34495E),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            // AI coach tiny tip if present
            if (vibe.aiInsight.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(vibeTheme.backgroundColor.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "AI Vibe Coach insight:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = vibeTheme.textOnBackground,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = vibe.aiInsight,
                            fontSize = 12.sp,
                            color = Color(0xFF2C3E50),
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
