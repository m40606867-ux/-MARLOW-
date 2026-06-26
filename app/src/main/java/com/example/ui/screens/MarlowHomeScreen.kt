package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.MarlowChat
import com.example.viewmodel.MarlowViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarlowHomeScreen(
    viewModel: MarlowViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    val chats by viewModel.chatHistory.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Touch feedback state
    var touchReactionText by remember { mutableStateOf("") }
    var touchCount by remember { mutableIntStateOf(0) }

    // Scroll to bottom on new message
    LaunchedEffect(chats.size) {
        if (chats.isNotEmpty()) {
            listState.animateScrollToItem(chats.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Dark slate backdrop
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Custom Marlow Header Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E)) // Pulsing green status online
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Ŋ|MARLOW✓",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }

                Row {
                    IconButton(
                        onClick = { viewModel.clearChats() },
                        modifier = Modifier.testTag("clear_chats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Clear logs",
                            tint = Color.LightGray
                        )
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log out",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main interaction viewport (Left: Marlow sticker, Right: Quick status buttons)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .testTag("marlow_avatar_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column: Interactive Sticker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable {
                                touchCount++
                                viewModel.marlowMood = "Cheeky"
                                touchReactionText = when (touchCount % 5) {
                                    1 -> "Ouch! Hey, tickles, ya habibi! 😂"
                                    2 -> "Bro, what are you poking me for? Go pray or play some games! 🎮"
                                    3 -> "Tapped me 3 times? We are officially married now, wallah. 💍"
                                    4 -> "Stop touching my forehead, my brain chips are heating up!"
                                    else -> "Dynamic high-five! Let's check your addictions tab."
                                }
                            }
                    ) {
                        // Pulse animation for thinking state
                        val transition = rememberInfiniteTransition(label = "Pulse")
                        val scale by if (viewModel.isThinking) {
                            transition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.05f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "ThinkingScale"
                            )
                        } else {
                            remember { mutableStateOf(1f) }
                        }

                        Image(
                            painter = painterResource(id = MarlowStickers.getStickerForMood(viewModel.marlowMood)),
                            contentDescription = "Marlow active mood",
                            modifier = Modifier
                                .size(110.dp)
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mood: ${viewModel.marlowMood}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right Column: Settings & Voice toggles
                    Column(
                        modifier = Modifier.weight(1.8f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (touchReactionText.isNotEmpty()) touchReactionText else "Yo! Marlow is in the house. Your loyal friend is here to keep you on track.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            lineHeight = 16.sp,
                            maxLines = 3
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Voice Continuous Listening Toggle
                            Button(
                                onClick = { viewModel.isContinuousListening = !viewModel.isContinuousListening },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewModel.isContinuousListening) Color(0xFF22C55E) else Color(0xFF475569)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("voice_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isContinuousListening) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = "Voice Input Toggle",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Voice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Screen Scanner Overlay button
                            Button(
                                onClick = { viewModel.scanScreen(apiKey) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("scan_screen_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan Screen",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Interactive screen-scan laser/results overlay banner
            AnimatedVisibility(
                visible = viewModel.isScanningScreen || viewModel.scannedResponseText.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("screen_scan_result"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF312E81))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Screenshot,
                                contentDescription = "Screen scanner",
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (viewModel.isScanningScreen) "SCANNING PHONE SCREEN..." else "SCREEN ANALYSIS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC7D2FE),
                                letterSpacing = 1.sp
                            )
                        }

                        if (viewModel.isScanningScreen) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF6366F1)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Active App Context: ${viewModel.activeAppOnScreen}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.scannedResponseText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alerts / API Info
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                Text(
                    text = "⚠️ Gemini API key not configured. Using friendly localized offline answers instead. Add key in AI Studio Secrets to unlock dynamic bilingual jokes!",
                    color = Color(0xFFFBBF24),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Live Equalizer bar animation if voice is active
            if (viewModel.isContinuousListening) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .background(Color(0xFF22C55E).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Pulsing Equalizer",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continuous Voice Active (Arabic & English support)...",
                        fontSize = 11.sp,
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Chats Viewport
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (chats.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(bottom = 40.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Empty chat",
                                tint = Color.LightGray.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Start chatting with Marlow",
                                color = Color.LightGray.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Ask about gaming strategies, global trends, or chat in Arabic!",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    items(chats) { chat ->
                        ChatBubbleItem(chat)
                    }
                }
            }

            // Input panel
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = viewModel.textInput,
                        onValueChange = { viewModel.textInput = it },
                        placeholder = {
                            Text(
                                "Vent to Marlow, ask strategies, or say 'Marhaba'...",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            viewModel.sendMessage(apiKey)
                            focusManager.clearFocus()
                        }),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    FloatingActionButton(
                        onClick = {
                            viewModel.sendMessage(apiKey)
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("send_chat_button"),
                        shape = CircleShape,
                        containerColor = Color(0xFF0288D1),
                        contentColor = Color.White
                    ) {
                        if (viewModel.isThinking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(chat: MarlowChat) {
    val isUser = chat.sender == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) Color(0xFF0288D1) else Color(0xFF334155)
    val textColors = Color.White

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = chat.text,
                color = textColors,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}
