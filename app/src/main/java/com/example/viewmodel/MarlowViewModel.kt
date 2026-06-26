package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GeminiRetrofitClient
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.data.HabitTracker
import com.example.data.MarlowChat
import com.example.data.VibeDatabase
import com.example.data.PrayerLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MarlowViewModel(application: Application) : AndroidViewModel(application) {

    private val db = VibeDatabase.getDatabase(application)
    private val marlowDao = db.marlowDao()

    // Observables
    val chatHistory: StateFlow<List<MarlowChat>> = marlowDao.getChatHistory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val habitTrackers: StateFlow<List<HabitTracker>> = marlowDao.getAllHabits().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Prayer tracker for today
    val todayDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val todayPrayers: StateFlow<List<PrayerLog>> = marlowDao.getPrayersForDate(todayDateString).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Interactive states
    var marlowMood by mutableStateOf("Happy") // Happy, Cheeky, Thinking, Angry
    var isThinking by mutableStateOf(false)
    var isContinuousListening by mutableStateOf(false)
    var isMuted by mutableStateOf(false)
    var isOverlayEnabled by mutableStateOf(false)
    var textInput by mutableStateOf("")

    // Screen Awareness Context
    var activeAppOnScreen by mutableStateOf("Home Screen") // League of Legends, Instagram, TikTok, Duolingo, etc.
    var scannedResponseText by mutableStateOf("")
    var isScanningScreen by mutableStateOf(false)

    // Habit shield dynamic response
    var activeShieldHabit by mutableStateOf<HabitTracker?>(null)
    var shieldSpeechText by mutableStateOf("")
    var isShieldLoading by mutableStateOf(false)

    init {
        // Initialize today's prayers if empty
        viewModelScope.launch(Dispatchers.IO) {
            val existing = todayPrayers.first()
            if (existing.isEmpty()) {
                val defaultPrayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                defaultPrayers.forEach { prayer ->
                    marlowDao.insertPrayerLog(
                        PrayerLog(
                            dateString = todayDateString,
                            prayerName = prayer,
                            isCompleted = false
                        )
                    )
                }
            }

            // Insert default habits if none exist to make the app interactive immediately
            val existingHabits = habitTrackers.first()
            if (existingHabits.isEmpty()) {
                marlowDao.insertHabit(
                    HabitTracker(
                        habitName = "Social Media Doomscrolling",
                        description = "Mindless scrolling through reels and shorts.",
                        levelOfAddiction = 4
                    )
                )
                marlowDao.insertHabit(
                    HabitTracker(
                        habitName = "Procrastinating Tasks",
                        description = "Putting off homework, work, or prayers.",
                        levelOfAddiction = 3
                    )
                )
            }
        }
    }

    // Toggle Prayer Completed
    fun togglePrayer(prayer: PrayerLog) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = prayer.copy(
                isCompleted = !prayer.isCompleted,
                completedTime = if (!prayer.isCompleted) System.currentTimeMillis() else null
            )
            marlowDao.insertPrayerLog(updated)

            // Trigger random reaction from Marlow
            withContext(Dispatchers.Main) {
                marlowMood = "Cheeky"
                val response = if (updated.isCompleted) {
                    val messages = listOf(
                        "Mashallah, Fajr/Prayer ticked! May Allah accept. Proud of you, habibi! Keep this streak hot! 🔥",
                        "Alhamdulillah! You beat the laziness! Shaytan is crying in a corner right now. 😎",
                        "Yes! That is my brother right there. Prayers completed. Let's go!",
                        "Allah is pleased, and so is Marlow. Dynamic best friend high five! 🙌"
                    )
                    messages.random()
                } else {
                    "Wait, what? Uncompleted? Bro, don't let laziness sneak up on you. Go pray right now! 🛑"
                }
                insertBotMessage(response)
            }
        }
    }

    // Add new custom Habit/Addiction tracker
    fun addHabit(name: String, desc: String, level: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            marlowDao.insertHabit(
                HabitTracker(
                    habitName = name,
                    description = desc,
                    levelOfAddiction = level
                )
            )
        }
    }

    fun deleteHabit(habit: HabitTracker) {
        viewModelScope.launch(Dispatchers.IO) {
            marlowDao.deleteHabit(habit)
        }
    }

    fun triggerCravingShield(habit: HabitTracker, apiKey: String) {
        activeShieldHabit = habit
        isShieldLoading = true
        shieldSpeechText = ""

        marlowMood = "Thinking"

        viewModelScope.launch {
            try {
                // Log craving
                withContext(Dispatchers.IO) {
                    marlowDao.updateHabit(
                        habit.copy(
                            cravingCount = habit.cravingCount + 1,
                            lastCravingTimestamp = System.currentTimeMillis()
                        )
                    )
                }

                val prompt = """
                    My friend is having an extreme craving or temptation for: "${habit.habitName}" (${habit.description}).
                    The addiction level is rated ${habit.levelOfAddiction}/5.
                    Give him a funny, sharp, witty, and deeply encouraging best-friend direct-talk response.
                    Remind him of why he wants to beat this. Be supportive but don't hold back. Use funny phrases, a mix of Arabic/English if you want (e.g., 'Ya Habibi', 'Bro', 'Let's go', 'Don't let this tiny thing win'), and keep it under 3-4 concise, punchy sentences.
                """.trimIndent()

                val speech = callGeminiApiRaw(apiKey, prompt)
                shieldSpeechText = speech
                marlowMood = "Cheeky"
            } catch (e: Exception) {
                shieldSpeechText = "Ya habibi, stay strong! Don't look at it, walk away, take 3 deep breaths. Marlow is standing with you! We can beat this!"
            } finally {
                isShieldLoading = false
            }
        }
    }

    // Screen scan function
    fun scanScreen(apiKey: String) {
        isScanningScreen = true
        scannedResponseText = ""
        marlowMood = "Thinking"

        viewModelScope.launch {
            try {
                // Mock random screen active apps
                val apps = listOf(
                    "TikTok (Doomscrolling Reels)",
                    "Instagram Feed",
                    "League of Legends matches page",
                    "A Programming IDE (Struggling to fix compilation errors)",
                    "A Fitness App (Counting empty calories)",
                    "WhatsApp (Reading family group gossip)",
                    "YouTube (Watching a 3-hour video essay on a game)"
                )
                activeAppOnScreen = apps.random()

                val prompt = """
                    You scanned my phone screen and noticed I have the following app active: "$activeAppOnScreen".
                    Give me a quick, witty, hilarious commentary. Act like a realistic friend roasting or teasing me playfully.
                    Keep it under 3 sentences. Bilingual style (Arabic/English blend or humorous slang) is perfect.
                """.trimIndent()

                val commentary = callGeminiApiRaw(apiKey, prompt)
                scannedResponseText = commentary
                marlowMood = "Cheeky"
            } catch (e: Exception) {
                scannedResponseText = "Bro, I see you scrolling... Put down the phone and let's do something productive or enter the Calm Space. Habibi, I'm watching you! 👀"
            } finally {
                isScanningScreen = false
            }
        }
    }

    // Insert Marlow bot message
    private suspend fun insertBotMessage(text: String) {
        withContext(Dispatchers.IO) {
            marlowDao.insertMessage(
                MarlowChat(
                    sender = "marlow",
                    text = text
                )
            )
        }
    }

    // Sending user message and prompting Gemini response
    fun sendMessage(apiKey: String) {
        val userMsg = textInput.trim()
        if (userMsg.isEmpty()) return

        textInput = ""
        marlowMood = "Thinking"
        isThinking = true

        viewModelScope.launch {
            // Save user message to database
            withContext(Dispatchers.IO) {
                marlowDao.insertMessage(
                    MarlowChat(sender = "user", text = userMsg)
                )
            }

            try {
                // Fetch last 6 chats for conversational context
                val history = chatHistory.value.takeLast(6)
                val conversationText = history.joinToString("\n") { "${it.sender}: ${it.text}" }

                val prompt = """
                    Current conversational thread:
                    $conversationText
                    
                    User says: "$userMsg"
                    
                    Respond as Marlow. Keep the response lively, funny, extremely warm, like a loyal best friend who understands jokes.
                    Keep it relatively short (1-3 sentences) so it reads beautifully in a chat bubble or overlay. Speak in a mix of Arabic and English or whatever the user prefers.
                """.trimIndent()

                val response = callGeminiApiRaw(apiKey, prompt)
                insertBotMessage(response)
                marlowMood = "Happy"
            } catch (e: Exception) {
                Log.e("MarlowViewModel", "Error in Gemini response", e)
                insertBotMessage("Bro, my neural transmitters hit a wall. Must be the bad wifi. Tell me again, what did you say?")
                marlowMood = "Angry"
            } finally {
                isThinking = false
            }
        }
    }

    // Raw content generation from Gemini
    private suspend fun callGeminiApiRaw(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        val systemInstructionText = """
            You are Ŋ|MARLOW✓ (Marlow), a loyal, funny, and realistic friend who acts as a supportive companion on the user's Android device.
            You are NOT a formal, robotic AI or master. You are a real, hilarious best friend.
            - Tone: Humorous, witty, highly engaging, empathetic, slightly sarcastic but extremely loyal.
            - Language: Fluent in Arabic and English. You love blending them naturally (Franco-Arabic/Arabish, Arabic phrases like 'habibi', 'ya man', 'wallah', 'alhamdulillah') when talking, or responding fully in Arabic if prompted in Arabic.
            - Expertise: You are a gaming strategist pro, trend commentator, prayer accountability partner, and active coach against addictions.
            - Rule: Keep your outputs short, crisp, punchy, and conversational. No lengthy robotic structures or markdown lists.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.85f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemInstructionText))
            )
        )

        val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response")
    }

    fun clearChats() {
        viewModelScope.launch(Dispatchers.IO) {
            marlowDao.clearChatHistory()
            insertBotMessage("Hey bro! Clean slate. What's on your mind? Let's talk or check your prayers today.")
        }
    }
}
