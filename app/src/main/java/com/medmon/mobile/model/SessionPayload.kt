package com.medmon.mobile.model

data class SessionPayload(
    val patient_id: String,
    val session_id: String,
    val phone_id: String,
    val model_version: String,
    val start_time: String,   // ISO 8601
    val end_time: String,     // ISO 8601
    val gestures: List<GestureEventPayload>
)