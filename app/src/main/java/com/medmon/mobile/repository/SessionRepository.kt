package com.medmon.mobile.repository

import android.util.Log
import com.medmon.mobile.model.GestureEventPayload
import com.medmon.mobile.model.ImuWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.random.Random

import com.medmon.mobile.model.SessionPayload
import com.medmon.mobile.network.SessionApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object SessionRepository {

    // Ajuste o BASE_URL conforme seu backend:
    // - Emulador Android Studio falando com FastAPI rodando no host: http://10.0.2.2:8000
    // - Dispositivo físico na mesma rede: ex. http://192.168.0.10:8000
    private const val BASE_URL = "http://192.168.0.244:8000"

    // Valores fixos por enquanto (podemos puxar do backend depois)
    private const val PATIENT_ID = "P001"
    private const val PHONE_ID = "PHONE_GALAXY_S23"
    private const val MODEL_VERSION = "tflite_v1.0"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val sessionApi: SessionApi by lazy {
        retrofit.create(SessionApi::class.java)
    }

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state

    private val isoFormatter = DateTimeFormatter.ISO_INSTANT

    fun startSession() {
        val newSessionId = generateSessionId()
        val now = Instant.now().toString()

        _state.value = SessionState(
            isSessionActive = true,
            sessionId = newSessionId,
            startTime = now,
            gestures = emptyList(),
            pendingSessions = _state.value.pendingSessions
        )

        Log.d("SessionRepository", "Session started: $newSessionId")
    }

//    fun stopSession() {
//        val current = _state.value
//        if (!current.isSessionActive) return
//
//        val endTime = Instant.now().toString()
//
//        // Aqui entra a lógica futura: montar SessionPayload e tentar enviar ao backend.
//        // Se falhar, incrementar pendingSessions.
//
//        _state.value = current.copy(
//            isSessionActive = false,
//            endTime = endTime,
//            // Por enquanto, vamos simular que sempre fica 1 sessão pendente após stop:
//            pendingSessions = current.pendingSessions + 1
//        )
//
//        Log.d("SessionRepository", "Session stopped: ${current.sessionId}")
//    }

    private fun buildSessionPayload(state: SessionState): SessionPayload {
        val start = state.startTime ?: Instant.now().toString()
        val end = state.endTime ?: Instant.now().toString()

        return SessionPayload(
            patient_id = PATIENT_ID,
            session_id = state.sessionId,
            phone_id = PHONE_ID,
            model_version = MODEL_VERSION,
            start_time = start,
            end_time = end,
            gestures = state.gestures
        )
    }

    suspend fun stopSessionAndSend() {
        val current = _state.value
        if (!current.isSessionActive) {
            Log.d("SessionRepository", "No active session to stop.")
            return
        }

        val endTime = Instant.now().toString()
        val updated = current.copy(
            isSessionActive = false,
            endTime = endTime
        )

        // Atualiza estado antes de enviar
        _state.value = updated

        val payload = buildSessionPayload(updated)

        try {
            Log.d("SessionRepository", "Sending session to backend: ${updated.sessionId}")
            sessionApi.sendSessionGestures(updated.sessionId, payload)
            // Se envio ok, não incrementa pendente
            Log.d("SessionRepository", "Session sent successfully")
            _state.value = updated.copy(
                pendingSessions = updated.pendingSessions
            )
        } catch (e: Exception) {
            Log.e("SessionRepository", "Error sending session: ${e.message}", e)
            // Se falhar, marca como pendente
            _state.value = updated.copy(
                pendingSessions = updated.pendingSessions + 1
            )
        }
    }

    suspend fun sendTestGestureSession() {
        val now = Instant.now()
        val nowStr = now.toString()

        val testGesture = GestureEventPayload(
            timestamp = nowStr,
            window_id = 1,
            label = "G1",
            confidence = 0.99f
        )

        val sessionId = "S_TEST_${now.epochSecond}"

        val payload = SessionPayload(
            patient_id = PATIENT_ID,
            session_id = sessionId,
            phone_id = PHONE_ID,
            model_version = MODEL_VERSION,
            start_time = nowStr,
            end_time = nowStr,
            gestures = listOf(testGesture)
        )

        try {
            Log.d("SessionRepository", "Sending TEST session: $sessionId")
            sessionApi.sendSessionGestures(sessionId, payload)
            Log.d("SessionRepository", "Test session sent successfully")
        } catch (e: Exception) {
            Log.e("SessionRepository", "Error sending test session: ${e.message}", e)
        }
    }


    /**
     * Chamado pelo serviço quando uma nova janela IMU chegar do relógio.
     * Por enquanto vamos fazer uma classificação FAKE.
     * Depois trocamos isso para chamar o TFLite.
     */
    fun onImuWindowReceived(window: ImuWindow) {
        val current = _state.value
        if (!current.isSessionActive) {
            Log.d("SessionRepository", "Ignoring IMU window: no active session")
            return
        }

        // Classificação fake: escolhe um label qualquer e uma confidence aleatória
        val (label, confidence) = fakeClassify(window)

        val gesture = GestureEventPayload(
            timestamp = window.timestamp,
            window_id = window.window_id,
            label = label,
            confidence = confidence
        )

        val updatedGestures = current.gestures + gesture

        _state.value = current.copy(
            gestures = updatedGestures
        )

        Log.d("SessionRepository", "Gesture added: $gesture")
    }

    private fun fakeClassify(window: ImuWindow): Pair<String, Float> {
        val labels = listOf("G1", "G2", "G3", "G4", "G5", "T")
        val label = labels.random()
        val confidence = Random.nextDouble(0.5, 1.0).toFloat()
        return label to confidence
    }

    private fun generateSessionId(): String {
        val now = Instant.now()
        // Algo tipo: S_20251130T190000Z
        return "S_" + isoFormatter.format(now)
    }

    // Futuro: função para enviar sessões pendentes, etc.
    fun syncPendingSessions() {
        // TODO: implementar envio real ao backend
        Log.d("SessionRepository", "Syncing pending sessions (fake)")
        // Por enquanto zera:
        val current = _state.value
        _state.value = current.copy(pendingSessions = 0)
    }
}
