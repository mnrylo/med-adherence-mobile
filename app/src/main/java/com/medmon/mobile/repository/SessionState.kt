package com.medmon.mobile.repository

import com.medmon.mobile.model.GestureEventPayload

data class SessionState(
    val isSessionActive: Boolean = false,
    val sessionId: String = "N/A",
    val startTime: String? = null,
    val endTime: String? = null,
    val gestures: List<GestureEventPayload> = emptyList(),
    val pendingSessions: Int = 0
)
