package com.medmon.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medmon.mobile.model.GestureEventPayload
import com.medmon.mobile.repository.SessionRepository
import com.medmon.mobile.repository.SessionState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SessionUiState(
    val isSessionActive: Boolean = false,
    val sessionId: String = "N/A",
    val gestures: List<GestureEventPayload> = emptyList(),
    val lastGesture: GestureEventPayload? = null,
    val pendingSessions: Int = 0
)

class SessionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            SessionRepository.state.collect { sessionState ->
                _uiState.value = sessionState.toUiState()
            }
        }
    }

    fun startSession() {
        SessionRepository.startSession()
    }

    fun stopSession() {
        viewModelScope.launch {
            SessionRepository.stopSessionAndSend()
        }
    }

    fun sendTestGestureSession() {
        viewModelScope.launch {
            SessionRepository.sendTestGestureSession()
        }
    }

    fun syncPending() {
        SessionRepository.syncPendingSessions()
    }
}

private fun SessionState.toUiState(): SessionUiState {
    val last = gestures.lastOrNull()
    return SessionUiState(
        isSessionActive = isSessionActive,
        sessionId = sessionId,
        gestures = gestures,
        lastGesture = last,
        pendingSessions = pendingSessions
    )
}
