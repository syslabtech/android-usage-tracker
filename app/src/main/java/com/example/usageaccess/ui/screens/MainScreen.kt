package com.example.usageaccess.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.usageaccess.data.AppUsageInfo
import com.example.usageaccess.data.StorageHelper
import com.example.usageaccess.data.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    var hasPermission by remember { mutableStateOf(UsageStatsHelper.hasUsageStatsPermission(context)) }
    var usageList by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = UsageStatsHelper.hasUsageStatsPermission(context)
                if (hasPermission) {
                    coroutineScope.launch {
                        isLoading = true
                        val data = withContext(Dispatchers.IO) {
                            UsageStatsHelper.getDailyUsage(context)
                        }
                        usageList = data
                        withContext(Dispatchers.IO) {
                            StorageHelper.saveTodayUsage(context, data)
                        }
                        isLoading = false
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!hasPermission) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Usage Access Permission is Required")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { UsageStatsHelper.requestUsageStatsPermission(context) }) {
                Text("Grant Permission")
            }
        }
    } else {
        if (isLoading && usageList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Today's Usage", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(usageList) { usage ->
                    UsageItemRow(usage)
                }
            }
        }
    }
}

@Composable
fun UsageItemRow(usage: AppUsageInfo) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(usage.appName, style = MaterialTheme.typography.titleMedium)
                    Text(usage.packageName, style = MaterialTheme.typography.bodySmall)
                }
                Text(formatMillis(usage.usageTimeMillis), style = MaterialTheme.typography.bodyMedium)
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                
                UsageDetailRow("First Time Stamp", formatDateTime(usage.firstTimeStamp))
                UsageDetailRow("Last Time Stamp", formatDateTime(usage.lastTimeStamp))
                UsageDetailRow("Last Time Used", formatDateTime(usage.lastTimeUsed))
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    UsageDetailRow("Total Time Visible", formatMillis(usage.totalTimeVisible))
                    UsageDetailRow("Last Time Visible", formatDateTime(usage.lastTimeVisible))
                    UsageDetailRow("Foreground Service Used", formatMillis(usage.totalTimeForegroundServiceUsed))
                    UsageDetailRow("Last FG Service Used", formatDateTime(usage.lastTimeForegroundServiceUsed))
                }
            }
        }
    }
}

@Composable
fun UsageDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

fun formatMillis(millis: Long): String {
    if (millis == 0L) return "0m"
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
        "${hours}h ${minutes % 60}m"
    } else {
        "${minutes}m"
    }
}

fun formatDateTime(millis: Long): String {
    if (millis == 0L) return "N/A"
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(millis))
}
