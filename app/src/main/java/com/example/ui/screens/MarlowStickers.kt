package com.example.ui.screens

import com.example.R

object MarlowStickers {
    val Happy = R.drawable.img_marlow_happy_1782461352444
    val Cheeky = R.drawable.img_marlow_cheeky_1782461367192
    val Thinking = R.drawable.img_marlow_thinking_1782461378783

    fun getStickerForMood(mood: String): Int {
        return when (mood.lowercase().trim()) {
            "cheeky" -> Cheeky
            "thinking" -> Thinking
            else -> Happy
        }
    }
}
