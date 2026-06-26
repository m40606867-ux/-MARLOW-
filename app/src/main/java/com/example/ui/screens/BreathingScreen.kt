package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class BreathingPhase(val label: String, val instruction: String, val color: Color, val duration: Int) {
    INHALE("Inhale", "Fill your lungs with fresh energy...", Color(0xFF0288D1), 4),
    HOLD_IN("Hold", "Suspend your breath, find stillness...", Color(0xFF7B1FA2), 4),
    EXHALE("Exhale", "Release all tension and stress...", Color(0xFF388E3C), 4),
    HOLD_OUT("Hold", "Rest in pure quiet awareness...", Color(0xFFD81B60), 4)
}

@Composable
fun BreathingScreen(
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var secondsRemaining by remember { mutableIntStateOf(BreathingPhase.INHALE.duration) }
    var totalCyclesCompleted by remember { mutableIntStateOf(0) }

    // Breathing scale for animating the circle
    var targetScale by remember { mutableFloatStateOf(0.4f) }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 4000),
        label = "BreathingScale"
    )

    // Breathing cycle coroutine
    LaunchedEffect(isRunning, currentPhase) {
        if (isRunning) {
            // Update physical targets based on phase
            targetScale = when (currentPhase) {
                BreathingPhase.INHALE -> 1.0f
                BreathingPhase.HOLD_IN -> 1.0f
                BreathingPhase.EXHALE -> 0.4f
                BreathingPhase.HOLD_OUT -> 0.4f
            }

            secondsRemaining = currentPhase.duration
            while (secondsRemaining > 0) {
                delay(1000)
                secondsRemaining--
            }

            // Move to next phase
            currentPhase = when (currentPhase) {
                BreathingPhase.INHALE -> BreathingPhase.HOLD_IN
                BreathingPhase.HOLD_IN -> BreathingPhase.EXHALE
                BreathingPhase.EXHALE -> BreathingPhase.HOLD_OUT
                BreathingPhase.HOLD_OUT -> {
                    totalCyclesCompleted++
                    BreathingPhase.INHALE
                }
            }
        } else {
            // Reset state
            targetScale = 0.4f
            secondsRemaining = BreathingPhase.INHALE.duration
            currentPhase = BreathingPhase.INHALE
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Cosmic slate dark backdrop
                        Color(0xFF1E293B),
                        Color(0xFF334155)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "Calm Icon",
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Calm Space",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
            }
            Text(
                text = "Lower your heart rate instantly with Box Breathing",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Centered Breathing Circle Portal
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Background ambient breathing glow
                Box(
                    modifier = Modifier
                        .size((280 * animatedScale).dp)
                        .blur(50.dp)
                        .alpha(0.35f)
                        .clip(CircleShape)
                        .background(currentPhase.color)
                )

                // Outer pulsing ring
                Canvas(
                    modifier = Modifier.size(280.dp)
                ) {
                    drawCircle(
                        color = currentPhase.color.copy(alpha = 0.2f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 6f)
                    )
                }

                // Inner Solid Expanding Portal Circle
                Surface(
                    modifier = Modifier
                        .size((220 * animatedScale).dp)
                        .shadow(16.dp, CircleShape)
                        .testTag("breathing_circle"),
                    shape = CircleShape,
                    color = currentPhase.color,
                    tonalElevation = 12.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = if (isRunning) currentPhase.label.uppercase() else "READY",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            if (isRunning) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$secondsRemaining s",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Interactive controls and descriptive cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .testTag("breathing_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRunning) currentPhase.instruction else "Box breathing involves 4 seconds of inhalation, 4 seconds of retention, 4 seconds of exhalation, and 4 seconds of emptiness.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp),
                        lineHeight = 22.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CYCLES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$totalCyclesCompleted completed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                isRunning = !isRunning
                                if (!isRunning) {
                                    totalCyclesCompleted = 0
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Color(0xFFE11D48) else Color(0xFF4F46E5)
                            ),
                            modifier = Modifier
                                .width(130.dp)
                                .height(46.dp)
                                .testTag("breathing_control_button")
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Stop" else "Start",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRunning) "Stop" else "Start",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Informative help banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF334155).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Regular box breathing can lower cortisol, reduce anxiety levels, and balance autonomic blood pressure instantly.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
