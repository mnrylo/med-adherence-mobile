package com.medmon.mobile.model

data class ImuWindow(
    val session_id: String,
    val window_id: Int,
    val fs: Int,               // 100
    val n_samples: Int,        // 75
    val timestamp: String,     // ISO 8601
    val data: List<List<Float>> // 75 x 6 (AccX,AccY,AccZ,GyrX,GyrY,GyrZ)
)
