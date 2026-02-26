package com.example.usageaccess.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyHistory(
    val dateString: String,
    val usageList: List<AppUsageInfo>
)

object StorageHelper {
    private const val FILE_NAME = "usage_history.json"

    fun saveTodayUsage(context: Context, usageList: List<AppUsageInfo>) {
        if (usageList.isEmpty()) return
        
        val file = File(context.filesDir, FILE_NAME)
        val jsonArray = if (file.exists()) {
            try {
                JSONArray(file.readText())
            } catch (e: Exception) {
                JSONArray()
            }
        } else {
            JSONArray()
        }

        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val retainedArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
            try {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getString("date") != todayDateString) {
                    retainedArray.put(obj)
                }
            } catch (e: Exception) {
                // Ignore malformed entries
            }
        }

        val todayObj = JSONObject()
        todayObj.put("date", todayDateString)
        val appsArray = JSONArray()
        usageList.forEach {
            val appObj = JSONObject()
            appObj.put("packageName", it.packageName)
            appObj.put("appName", it.appName)
            appObj.put("usageTimeMillis", it.usageTimeMillis)
            appObj.put("firstTimeStamp", it.firstTimeStamp)
            appObj.put("lastTimeStamp", it.lastTimeStamp)
            appObj.put("lastTimeUsed", it.lastTimeUsed)
            appObj.put("totalTimeVisible", it.totalTimeVisible)
            appObj.put("lastTimeVisible", it.lastTimeVisible)
            appObj.put("totalTimeForegroundServiceUsed", it.totalTimeForegroundServiceUsed)
            appObj.put("lastTimeForegroundServiceUsed", it.lastTimeForegroundServiceUsed)
            appsArray.put(appObj)
        }
        todayObj.put("apps", appsArray)

        retainedArray.put(todayObj)
        file.writeText(retainedArray.toString(2))
    }

    fun getHistory(context: Context): List<DailyHistory> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()

        val historyList = mutableListOf<DailyHistory>()
        try {
            val jsonArray = JSONArray(file.readText())
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val dateStr = obj.getString("date")
                val appsArray = obj.getJSONArray("apps")
                val usageList = mutableListOf<AppUsageInfo>()
                for (j in 0 until appsArray.length()) {
                    val appObj = appsArray.getJSONObject(j)
                    usageList.add(
                        AppUsageInfo(
                            packageName = appObj.getString("packageName"),
                            appName = appObj.getString("appName"),
                            usageTimeMillis = appObj.getLong("usageTimeMillis"),
                            firstTimeStamp = appObj.optLong("firstTimeStamp", 0L),
                            lastTimeStamp = appObj.optLong("lastTimeStamp", 0L),
                            lastTimeUsed = appObj.optLong("lastTimeUsed", 0L),
                            totalTimeVisible = appObj.optLong("totalTimeVisible", 0L),
                            lastTimeVisible = appObj.optLong("lastTimeVisible", 0L),
                            totalTimeForegroundServiceUsed = appObj.optLong("totalTimeForegroundServiceUsed", 0L),
                            lastTimeForegroundServiceUsed = appObj.optLong("lastTimeForegroundServiceUsed", 0L)
                        )
                    )
                }
                historyList.add(DailyHistory(dateStr, usageList))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return historyList.reversed()
    }
}
