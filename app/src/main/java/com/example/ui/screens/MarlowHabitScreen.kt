package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.HabitTracker
import com.example.viewmodel.MarlowViewModel
import java.util.concurrent.TimeUnit

@Composable
fun MarlowHabitScreen(
    viewModel: MarlowViewModel,
    modifier: Modifier = Modifier
) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    val habits by viewModel.habitTrackers.collectAsState()
    val focusManager = LocalFocusManager.current

    // Add Habit modal states
    var showAddDialog by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var newHabitDesc by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableIntStateOf(3) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Match the immersive dark palette
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield Icon",
                        tint = Color(0xFFEF4444), // Active reddish protective shield
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Habit Shield",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .background(Color(0xFF1E293B), CircleShape)
                        .testTag("add_habit_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Habit",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = "Marlow is your active defense opponent against addictions.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Dynamic Active Shield Dialog Pop-Up
            AnimatedVisibility(
                visible = viewModel.activeShieldHabit != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                viewModel.activeShieldHabit?.let { activeHabit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .testTag("craving_shield_response"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                        border = BorderStroke(1.5.dp, Color(0xFFEF4444))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.OfflineBolt,
                                        contentDescription = "Active Craving",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "MARLOW CRAVING COUNTER!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFCA5A5),
                                        letterSpacing = 1.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.activeShieldHabit = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Battling: ${activeHabit.habitName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (viewModel.isShieldLoading) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Marlow is warming up his roasts...", color = Color.White, fontSize = 12.sp)
                                }
                            } else {
                                Text(
                                    text = viewModel.shieldSpeechText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            // Habit Lists
            if (habits.isEmpty()) {
                EmptyHabitsView()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(habits) { habit ->
                        HabitItemCard(
                            habit = habit,
                            onCravingClick = { viewModel.triggerCravingShield(habit, apiKey) },
                            onDelete = { viewModel.deleteHabit(habit) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    // Add Habit Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Fight a New Addiction", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("What bad habit is it?") },
                        placeholder = { Text("e.g. Smoking, Procrastinating, TikTok") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newHabitDesc,
                        onValueChange = { newHabitDesc = it },
                        label = { Text("Why do you want to quit?") },
                        placeholder = { Text("e.g. Waste of hours, brain fog, expensive") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text(
                            text = "Addiction Level: $selectedLevel/5",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = selectedLevel.toFloat(),
                            onValueChange = { selectedLevel = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFEF4444),
                                activeTrackColor = Color(0xFFEF4444)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newHabitName.trim().isNotEmpty()) {
                            viewModel.addHabit(newHabitName, newHabitDesc, selectedLevel)
                            newHabitName = ""
                            newHabitDesc = ""
                            selectedLevel = 3
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Shield Up! 🛡️")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun EmptyHabitsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Empty Shield",
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No active battles yet",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add a habit, addiction, or screen time trap to begin fighting it side-by-side with Marlow.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun HabitItemCard(
    habit: HabitTracker,
    onCravingClick: () -> Unit,
    onDelete: () -> Unit
) {
    // Calculate dynamic clean duration
    val cleanMillis = System.currentTimeMillis() - habit.startTimestamp
    val daysClean = TimeUnit.MILLISECONDS.toDays(cleanMillis)
    val hoursClean = TimeUnit.MILLISECONDS.toHours(cleanMillis) % 24
    val minutesClean = TimeUnit.MILLISECONDS.toMinutes(cleanMillis) % 60

    val cleanTimeFormatted = when {
        daysClean > 0 -> "$daysClean d, $hoursClean h clean"
        hoursClean > 0 -> "$hoursClean hr, $minutesClean min clean"
        else -> "$minutesClean min clean"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = habit.habitName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Tracker",
                        tint = Color.Gray.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = habit.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "SOBER TIMER", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = cleanTimeFormatted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF22C55E) // Vibrant green timer
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "CRAVINGS DEFEATED", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${habit.cravingCount} times",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Craving shield launch button!
            Button(
                onClick = onCravingClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("craving_button_${habit.id}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Defend Craving",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("CRAVING ATK! MARLOW DEFEND", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
