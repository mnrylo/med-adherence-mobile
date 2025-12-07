package com.medmon.mobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medmon.mobile.model.GestureEventPayload
import com.medmon.mobile.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(sessionViewModel: SessionViewModel = viewModel()) {

    val uiState by sessionViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Por enquanto, vamos deixar conexão do relógio e backend fixas:
    val connectionWatch = "Unknown"
    val connectionBackend = "Offline"


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medication Ingestion Monitor") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            ConnectionSection(
                connectionWatch = connectionWatch,
                connectionBackend = connectionBackend
            )

            Spacer(modifier = Modifier.height(12.dp))

            SessionSection(
                isSessionActive = uiState.isSessionActive,
                sessionId = uiState.sessionId,
                onStart = { sessionViewModel.startSession(context) },
                onStop = { sessionViewModel.stopSession() },
                onTestSend = { sessionViewModel.sendTestGestureSession() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LastGestureSection(lastGesture = uiState.lastGesture)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Gestures Timeline",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            GesturesList(gestures = uiState.gestures)

            Spacer(modifier = Modifier.height(12.dp))

            PendingSessionsSection(
                pendingCount = uiState.pendingSessions,
                onSync = { sessionViewModel.syncPending() }
            )
        }
    }
}

@Composable
fun ConnectionSection(
    connectionWatch: String,
    connectionBackend: String
) {
    Column {
        Text("Connection Status", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Watch: $connectionWatch")
        Text("Backend: $connectionBackend")
    }
}

@Composable
fun SessionSection(
    isSessionActive: Boolean,
    sessionId: String,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTestSend: () -> Unit
) {

    Column {
        Text("Session", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Status: ${if (isSessionActive) "Active" else "Idle"}")
        Text("Session ID: $sessionId")
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onStart, enabled = !isSessionActive) {
                Text("Start Session")
            }
            Button(onClick = onStop, enabled = isSessionActive) {
                Text("Stop Session")
            }
            Button(onClick = onTestSend) {
                Text("Send Test")
            }
        }
    }
}

@Composable
fun LastGestureSection(lastGesture: GestureEventPayload?) {
    Column {
        Text("Last Gesture", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        if (lastGesture == null) {
            Text("No gestures yet.")
        } else {
            Text("Label: ${lastGesture.label} (${lastGesture.confidence})")
            Text("Window: ${lastGesture.window_id}")
            Text("Time: ${lastGesture.timestamp}")
        }
    }
}

@Composable
fun GesturesList(gestures: List<GestureEventPayload>) {
    if (gestures.isEmpty()) {
        Text("No gestures recorded in this session.")
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            items(gestures) { g ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(g.timestamp)
                    Text(g.label)
                    Text(String.format("%.2f", g.confidence))
                }
            }
        }
    }
}

@Composable
fun PendingSessionsSection(
    pendingCount: Int,
    onSync: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Pending sessions: $pendingCount")
        Button(onClick = onSync, enabled = pendingCount > 0) {
            Text("Sync")
        }
    }
}
