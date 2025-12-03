package com.medmon.mobile.model


data class GestureEventPayload(
    val timestamp: String,   // ISO 8601: 2025-11-26T19:00:01.200Z
    val window_id: Int,
    val label: String,       // "G1", "G2", "G3", "G4", "G5", "T"
    val confidence: Float
)
