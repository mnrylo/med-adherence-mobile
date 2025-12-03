package com.medmon.mobile.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.medmon.mobile.model.ImuWindow
import com.medmon.mobile.repository.SessionRepository
import org.json.JSONArray
import org.json.JSONObject

class ImuWindowListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        if (messageEvent.path == "/imu/window") {
            val json = String(messageEvent.data, Charsets.UTF_8)
            Log.d("ImuWindowListener", "Received IMU window: $json")

            val imuWindow = parseImuWindow(json)

            // 👉 Agora notificamos o SessionRepository
            SessionRepository.onImuWindowReceived(imuWindow)
        }
    }

    private fun parseImuWindow(json: String): ImuWindow {
        val obj = JSONObject(json)
        val sessionId = obj.getString("session_id")
        val windowId = obj.getInt("window_id")
        val fs = obj.getInt("fs")
        val nSamples = obj.getInt("n_samples")
        val timestamp = obj.getString("timestamp")

        val dataArray = obj.getJSONArray("data")
        val data = parseDataArray(dataArray)

        return ImuWindow(
            session_id = sessionId,
            window_id = windowId,
            fs = fs,
            n_samples = nSamples,
            timestamp = timestamp,
            data = data
        )
    }

    private fun parseDataArray(jsonArray: JSONArray): List<List<Float>> {
        val list = mutableListOf<List<Float>>()
        for (i in 0 until jsonArray.length()) {
            val row = jsonArray.getJSONArray(i)
            list.add(
                listOf(
                    row.getDouble(0).toFloat(),
                    row.getDouble(1).toFloat(),
                    row.getDouble(2).toFloat(),
                    row.getDouble(3).toFloat(),
                    row.getDouble(4).toFloat(),
                    row.getDouble(5).toFloat()
                )
            )
        }
        return list
    }
}
