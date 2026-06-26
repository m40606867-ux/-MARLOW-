package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.User
import com.example.data.VibeDatabase
import com.example.util.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = VibeDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val sharedPrefs = application.getSharedPreferences("marlow_auth_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _successState = MutableStateFlow<String?>(null)
    val successState: StateFlow<String?> = _successState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Restore persistent login session if available
        val savedEmail = sharedPrefs.getString("logged_in_user_email", null)
        if (savedEmail != null) {
            _currentUserEmail.value = savedEmail
            _isLoggedIn.value = true
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedPass = password.trim()
        val trimmedConfirm = confirmPassword.trim()

        if (trimmedEmail.isEmpty() || trimmedPass.isEmpty() || trimmedConfirm.isEmpty()) {
            _errorState.value = "All fields are required, habibi!"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _errorState.value = "Please enter a valid email address."
            return
        }

        if (trimmedPass.length < 6) {
            _errorState.value = "Password must be at least 6 characters long."
            return
        }

        if (trimmedPass != trimmedConfirm) {
            _errorState.value = "Passwords do not match."
            return
        }

        _isLoading.value = true
        _errorState.value = null
        _successState.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingUser = userDao.getUserByEmail(trimmedEmail)
                if (existingUser != null) {
                    withContext(Dispatchers.Main) {
                        _errorState.value = "User with this email already exists."
                        _isLoading.value = false
                    }
                    return@launch
                }

                // Register user securely
                val salt = SecurityUtil.generateSalt()
                val passwordHash = SecurityUtil.hashPassword(trimmedPass, salt)
                val newUser = User(
                    email = trimmedEmail,
                    passwordHash = passwordHash,
                    salt = salt
                )

                userDao.insertUser(newUser)

                // Persist session
                sharedPrefs.edit().putString("logged_in_user_email", trimmedEmail).apply()

                withContext(Dispatchers.Main) {
                    _currentUserEmail.value = trimmedEmail
                    _isLoggedIn.value = true
                    _successState.value = "Welcome to Marlow's world, $trimmedEmail!"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorState.value = "SignUp failed: ${e.localizedMessage ?: "Unknown error"}"
                    _isLoading.value = false
                }
            }
        }
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedPass = password.trim()

        if (trimmedEmail.isEmpty() || trimmedPass.isEmpty()) {
            _errorState.value = "Both email and password are required!"
            return
        }

        _isLoading.value = true
        _errorState.value = null
        _successState.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userDao.getUserByEmail(trimmedEmail)
                if (user == null) {
                    withContext(Dispatchers.Main) {
                        _errorState.value = "No account found with this email. Please sign up."
                        _isLoading.value = false
                    }
                    return@launch
                }

                val calculatedHash = SecurityUtil.hashPassword(trimmedPass, user.salt)
                if (calculatedHash == user.passwordHash) {
                    // Password matches, log user in
                    sharedPrefs.edit().putString("logged_in_user_email", trimmedEmail).apply()
                    withContext(Dispatchers.Main) {
                        _currentUserEmail.value = trimmedEmail
                        _isLoggedIn.value = true
                        _successState.value = "Welcome back, $trimmedEmail!"
                        _isLoading.value = false
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _errorState.value = "Incorrect password. Try again!"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorState.value = "Login failed: ${e.localizedMessage ?: "Unknown error"}"
                    _isLoading.value = false
                }
            }
        }
    }

    fun logout() {
        sharedPrefs.edit().remove("logged_in_user_email").apply()
        _currentUserEmail.value = null
        _isLoggedIn.value = false
        _errorState.value = null
        _successState.value = null
    }

    fun clearErrors() {
        _errorState.value = null
        _successState.value = null
    }
}
