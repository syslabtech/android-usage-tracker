package com.example.usageaccess.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import java.util.Calendar

object UsageStatsHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageStatsPermission(context: Context) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun getDailyUsage(context: Context): List<AppUsageInfo> {
        if (!hasUsageStatsPermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = context.packageManager

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        return usageStatsList.filter { it.totalTimeInForeground > 0 }
            .map {
                val appName = try {
                    val info = pm.getApplicationInfo(it.packageName, 0)
                    pm.getApplicationLabel(info).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    it.packageName
                }
                var totalTimeVisible = 0L
                var lastTimeVisible = 0L
                var totalTimeForegroundServiceUsed = 0L
                var lastTimeForegroundServiceUsed = 0L
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    totalTimeVisible = it.totalTimeVisible
                    lastTimeVisible = it.lastTimeVisible
                    totalTimeForegroundServiceUsed = it.totalTimeForegroundServiceUsed
                    lastTimeForegroundServiceUsed = it.lastTimeForegroundServiceUsed
                }
                
                AppUsageInfo(
                    packageName = it.packageName,
                    appName = appName,
                    usageTimeMillis = it.totalTimeInForeground,
                    firstTimeStamp = it.firstTimeStamp,
                    lastTimeStamp = it.lastTimeStamp,
                    lastTimeUsed = it.lastTimeUsed,
                    totalTimeVisible = totalTimeVisible,
                    lastTimeVisible = lastTimeVisible,
                    totalTimeForegroundServiceUsed = totalTimeForegroundServiceUsed,
                    lastTimeForegroundServiceUsed = lastTimeForegroundServiceUsed
                )
            }
            .sortedByDescending { it.usageTimeMillis }
    }
}
