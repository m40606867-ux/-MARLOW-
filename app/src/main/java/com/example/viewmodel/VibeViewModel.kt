package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GeminiRetrofitClient
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.VibeAnalysisResponse
import com.example.data.VibeCheck
import com.example.data.VibeDatabase
import com.example.data.VibeRepository
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VibeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VibeRepository
    val allVibeChecks: StateFlow<List<VibeCheck>>

    init {
        val vibeCheckDao = VibeDatabase.getDatabase(application).vibeCheckDao()
        repository = VibeRepository(vibeCheckDao)
        allVibeChecks = repository.allVibeChecks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Form inputs
    var currentInputText by mutableStateOf("")
    var moodSlider by mutableFloatStateOf(0.5f)
    var energySlider by mutableFloatStateOf(0.5f)

    // UI States
    var isAnalyzing by mutableStateOf(false)
    var errorState by mutableStateOf<String?>(null)
    var lastAnalysisResult by mutableStateOf<VibeCheck?>(null)

    fun resetInputs() {
        currentInputText = ""
        moodSlider = 0.5f
        energySlider = 0.5f
        errorState = null
    }

    fun deleteVibe(vibe: VibeCheck) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(vibe)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    fun checkVibe(apiKey: String) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            errorState = "API Key is not configured. Please enter a valid Gemini API Key in the Secrets panel."
            return
        }

        isAnalyzing = true
        errorState = null

        val currentText = currentInputText.trim()
        val currentMood = moodSlider
        val currentEnergy = energySlider

        viewModelScope.launch {
            try {
                val responseText = callGeminiApi(apiKey, currentText, currentMood, currentEnergy)
                val analysis = parseVibeResponse(responseText, currentMood, currentEnergy)

                val vibeRecord = VibeCheck(
                    inputPrompt = if (currentText.isEmpty()) "[No journal notes, sliders check]" else currentText,
                    moodSlider = currentMood,
                    energySlider = currentEnergy,
                    positivityScore = analysis.positivityScore,
                    dominantEmotion = analysis.dominantEmotion,
                    aiInsight = analysis.aiInsight,
                    activityIdea = analysis.activityIdea,
                    colorTheme = analysis.colorTheme
                )

                // Save to database
                withContext(Dispatchers.IO) {
                    repository.insert(vibeRecord)
                }

                lastAnalysisResult = vibeRecord
                resetInputs()
            } catch (e: Exception) {
                Log.e("VibeViewModel", "Error analyzing vibe", e)
                errorState = "Failed to analyze vibe: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    private suspend fun callGeminiApi(
        apiKey: String,
        text: String,
        mood: Float,
        energy: Float
    ): String = withContext(Dispatchers.IO) {

        val systemInstructionText = """
            You are the expert Vibe Check Companion, an empathetic, intuitive, and supportive emotional companion.
            Your task is to analyze the user's emotional state based on their sliders and journal notes.
            
            You MUST return ONLY a valid JSON object matching this schema exactly:
            {
              "positivityScore": <Int between 0 and 100 representing emotional positivity>,
              "dominantEmotion": "<A short 1-2 word description of the core emotion, e.g., Peaceful, Inspired, Melancholic, Restless, Cozy, Stressed, Weary, Euphoric>",
              "aiInsight": "<A warm, validating, 2-3 sentence personalized analysis. Direct, kind, and supportive. Use 'you' to refer to the user.>",
              "activityIdea": "<A comforting, grounding, or fun action tailored for their current state, e.g., 'Unwind with a 2-minute breathing cycle in the Calm Space', 'Step outside for 5 minutes and listen to birds'>",
              "colorTheme": "<Choose exactly one: 'blue' (for peaceful/chill/wistful), 'orange' (for happy/excited), 'green' (for calm/balanced/focused), 'purple' (for creative/deep/introspective), 'pink' (for cozy/loving/grateful), 'grey' (for tired/exhausted/bored)>"
            }
            
            DO NOT output any markdown wrappers (such as ```json), additional commentary, or conversational filler. Return ONLY the raw JSON string.
        """.trimIndent()

        val promptText = """
            User Check-In State:
            - Mood Slider: ${"%.1f".format(mood * 10)} / 10.0 (where 0 is low/sad, 10 is happy)
            - Energy Slider: ${"%.1f".format(energy * 10)} / 10.0 (where 0 is tired/flat, 10 is energized/bouncing)
            - Notes: "${if (text.isEmpty()) "User did not leave any comments, just sliders." else text}"
            
            Analyze their vibe and respond with the requested JSON schema.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = promptText)))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemInstructionText))
            )
        )

        val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini API")
    }

    private fun parseVibeResponse(rawText: String, mood: Float, energy: Float): VibeAnalysisResponse {
        // Clean markdown wrapper blocks if any
        var cleaned = rawText.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.removeSuffix("```")
            }
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.removeSuffix("```")
            }
        }
        cleaned = cleaned.trim()

        return try {
            val adapter: JsonAdapter<VibeAnalysisResponse> =
                GeminiRetrofitClient.moshi.adapter(VibeAnalysisResponse::class.java)
            adapter.fromJson(cleaned) ?: throw Exception("Failed to deserialize JSON")
        } catch (e: Exception) {
            Log.e("VibeViewModel", "JSON deserialization failed, executing local fallback logic", e)
            // Local fallback logic based on sliders
            createLocalFallback(mood, energy)
        }
    }

    private fun createLocalFallback(mood: Float, energy: Float): VibeAnalysisResponse {
        val positivity = (mood * 100).toInt()
        val emotion: String
        val insight: String
        val activity: String
        val theme: String

        when {
            mood >= 0.7f && energy >= 0.7f -> {
                emotion = "Radiant & Energetic"
                insight = "You are feeling highly uplifted and bursting with positive energy today! It's a wonderful day to tackle creative projects or connect with friends."
                activity = "Start a creative project or share this vibrant energy with someone you care about."
                theme = "orange"
            }
            mood >= 0.7f && energy < 0.7f -> {
                emotion = "Peaceful & Content"
                insight = "Your mood is serene, but your physical energy is soft and relaxed. You are experiencing a cozy, gentle sense of gratitude and tranquility."
                activity = "Grab a soft blanket, brew some tea, and read a favorite chapter in your current book."
                theme = "pink"
            }
            mood < 0.4f && energy >= 0.6f -> {
                emotion = "Restless or Anxious"
                insight = "You have higher physical energy, but your emotions are feeling a bit heavy or tense. This contrast can sometimes feel like restlessness or anxiety."
                activity = "Unwind this tense energy in the Calm Space with a 3-minute rhythmic breathing cycle."
                theme = "purple"
            }
            mood < 0.4f && energy < 0.4f -> {
                emotion = "Weary & Tired"
                insight = "Both your emotional state and your energy levels are running low right now. This is a gentle signal from your body that it is time for restful recovery."
                activity = "Set aside all expectations for the day and let yourself rest. A warm shower or a short nap is perfect."
                theme = "grey"
            }
            else -> {
                // Balanced / Center
                emotion = "Balanced & Calm"
                insight = "You are in a centered, steady state of equilibrium. There is a quiet, grounded focus to your day, which is excellent for productivity and mindfulness."
                activity = "Try the rhythmic breathing animation in the Calm Space for 1 minute to stay aligned."
                theme = "green"
            }
        }

        return VibeAnalysisResponse(
            positivityScore = positivity,
            dominantEmotion = emotion,
            aiInsight = insight,
            activityIdea = activity,
            colorTheme = theme
        )
    }
}
