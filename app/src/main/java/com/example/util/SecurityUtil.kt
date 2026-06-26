package com.example.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtil {
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(Base64.decode(salt, Base64.NO_WRAP))
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        // Run multiple hashing rounds (iterations) to enhance brute-force resilience
        var result = hashedBytes
        for (i in 1..1000) {
            md.reset()
            result = md.digest(result)
        }
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }
}
