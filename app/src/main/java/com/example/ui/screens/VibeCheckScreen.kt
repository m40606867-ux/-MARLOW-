package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.VibeThemeRegistry
import com.example.viewmodel.VibeViewModel

@Composable
fun VibeCheckScreen(
    viewModel: VibeViewModel,
    onNavigateToCalmSpace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val apiKey = BuildConfig.GEMINI_API_KEY

    // Choose ambient background based on last analyzed vibe or neutral
    val lastVibe = viewModel.lastAnalysisResult
    val themeColors = remember(lastVibe) {
        VibeThemeRegistry.getTheme(lastVibe?.colorTheme)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.BubbleChart,
                    contentDescription = "Vibe Icon",
                    tint = themeColors.primaryColor,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Vibe Check",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = themeColors.textOnBackground
                )
            }
            Text(
                text = "Your daily AI emotional sounding board",
                style = MaterialTheme.typography.bodyMedium,
                color = themeColors.textOnBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Alert block if API Key is not set or placeholder
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Warning",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "To enable AI analyses, please configure GEMINI_API_KEY in the Secrets Panel in AI Studio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF856404)
                        )
                    }
                }
            }

            // Journal block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .testTag("journal_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "How are you feeling?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2C3E50),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.currentInputText,
                        onValueChange = { viewModel.currentInputText = it },
                        placeholder = {
                            Text(
                                "Write down your thoughts, fears, or victories today...",
                                color = Color.Gray.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("journal_input"),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primaryColor,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.8f),
                            focusedContainerColor = Color(0xFFF9FAFC),
                            unfocusedContainerColor = Color(0xFFF9FAFC)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mood Slider (Sad to Happy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SentimentDissatisfied,
                                contentDescription = "Low mood",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mood: ${"%.0f".format(viewModel.moodSlider * 100)}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF34495E)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfied,
                            contentDescription = "High mood",
                            tint = themeColors.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Slider(
                        value = viewModel.moodSlider,
                        onValueChange = { viewModel.moodSlider = it },
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.primaryColor,
                            activeTrackColor = themeColors.primaryColor,
                            inactiveTrackColor = themeColors.primaryColor.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mood_slider")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Energy Slider (Tired to Excited)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Low energy",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Energy: ${"%.0f".format(viewModel.energySlider * 100)}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF34495E)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "High energy",
                            tint = Color(0xFFF39C12),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Slider(
                        value = viewModel.energySlider,
                        onValueChange = { viewModel.energySlider = it },
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.primaryColor,
                            activeTrackColor = themeColors.primaryColor,
                            inactiveTrackColor = themeColors.primaryColor.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("energy_slider")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Analyze button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.checkVibe(apiKey)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("analyze_button"),
                        enabled = !viewModel.isAnalyzing,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.primaryColor,
                            disabledContainerColor = themeColors.primaryColor.copy(alpha = 0.5f)
                        )
                    ) {
                        if (viewModel.isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Reading your energy...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Analyze",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Check my Vibe", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Error display if any
                    viewModel.errorState?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Last Analysis Result Display
            AnimatedVisibility(
                visible = lastVibe != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                if (lastVibe != null) {
                    val resultTheme = VibeThemeRegistry.getTheme(lastVibe.colorTheme)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp))
                            .testTag("analysis_result_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Header with dominant emotion and close button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(resultTheme.cardBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Emotion icon",
                                            tint = resultTheme.primaryColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "YOUR VIBE IS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = lastVibe.dominantEmotion,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = resultTheme.textOnBackground
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.lastAnalysisResult = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close report",
                                        tint = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Positivity Percentage Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Positivity score:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF2C3E50)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${lastVibe.positivityScore}%",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = resultTheme.primaryColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEFEFEF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(lastVibe.positivityScore / 100f)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(resultTheme.secondaryColor, resultTheme.primaryColor)
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // AI Insight Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = resultTheme.backgroundColor.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "AI Vibe Coach:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = resultTheme.textOnBackground,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Text(
                                        text = lastVibe.aiInsight,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF2C3E50),
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Activity idea
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelfImprovement,
                                    contentDescription = "Mindfulness Activity",
                                    tint = resultTheme.primaryColor,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Recommended Boost:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = resultTheme.textOnBackground
                                    )
                                    Text(
                                        text = lastVibe.activityIdea,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF34495E),
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            // If activity hints at breathing, show a quick link button
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavigateToCalmSpace,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = resultTheme.cardBackground),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelfImprovement,
                                    contentDescription = "Breathing Room",
                                    tint = resultTheme.primaryColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Enter Calm Space",
                                    color = resultTheme.textOnBackground,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
