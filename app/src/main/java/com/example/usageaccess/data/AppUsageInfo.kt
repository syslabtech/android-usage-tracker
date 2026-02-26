package com.example.usageaccess.data

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val firstTimeStamp: Long = 0L,
    val lastTimeStamp: Long = 0L,
    val lastTimeUsed: Long = 0L,
    val totalTimeVisible: Long = 0L,
    val lastTimeVisible: Long = 0L,
    val totalTimeForegroundServiceUsed: Long = 0L,
    val lastTimeForegroundServiceUsed: Long = 0L
)
