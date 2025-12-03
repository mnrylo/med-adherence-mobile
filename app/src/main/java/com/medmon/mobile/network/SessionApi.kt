package com.medmon.mobile.network

import com.medmon.mobile.model.SessionPayload
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionApi {

    @POST("/api/v1/sessions/{session_id}/gestures")
    suspend fun sendSessionGestures(
        @Path("session_id") sessionId: String,
        @Body payload: SessionPayload
    )
}
