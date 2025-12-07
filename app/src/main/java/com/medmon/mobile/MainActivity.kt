package com.medmon.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.medmon.mobile.ui.MainScreen
import com.medmon.mobile.repository.SessionRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionRepository.initClassifier(applicationContext)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

