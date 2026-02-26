package com.syslabtech.usageaccess.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.syslabtech.usageaccess.data.DailyHistory
import com.syslabtech.usageaccess.data.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    var historyList by remember { mutableStateOf<List<DailyHistory>>(emptyList()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    val data = withContext(Dispatchers.IO) {
                        StorageHelper.getHistory(context)
                    }
                    historyList = data
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Usage History", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (historyList.isEmpty()) {
            item {
                Text("No history available yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            items(historyList) { daily ->
                HistoryDayCard(daily)
            }
        }
    }
}

@Composable
fun HistoryDayCard(daily: DailyHistory) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(daily.dateString, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (expanded) {
                daily.usageList.forEach { usage ->
                    UsageItemRow(usage)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                daily.usageList.take(5).forEach { usage ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(usage.appName, modifier = Modifier.weight(1f))
                        Text(formatMillis(usage.usageTimeMillis))
                    }
                }
                if (daily.usageList.size > 5) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "...and ${daily.usageList.size - 5} more apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
