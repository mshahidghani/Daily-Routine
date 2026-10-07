package com.dailyroutine.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Routine(val id: Long, val name: String, val details: String, val time: String, val repeat: String, val category: String)

class MainActivity : ComponentActivity() {
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        if (Build.VERSION.SDK_INT >= 26) {
            val c = NotificationChannel("routine_tasks", "Routine Tasks", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(c)
        }
        setContent { MaterialTheme { DailyRoutineApp() } }
    }
}

@Composable
fun DailyRoutineApp() {
    var tasks by remember { mutableStateOf(listOf<Routine>()) }
    var adding by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Daily Routine") }) },
        floatingActionButton = { FloatingActionButton({ adding = true }) { Text("+") } }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Today", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            if (tasks.isEmpty()) Text("No routines yet. Create your first routine.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tasks, key = { it.id }) { t ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(t.name, style = MaterialTheme.typography.titleLarge)
                            Text(t.time)
                            Text(t.details)
                            Text("${t.category} • ${t.repeat}")
                        }
                    }
                }
            }
        }
    }
    if (adding) AddDialog(
        onDismiss = { adding = false },
        onSave = { tasks = tasks + it; adding = false }
    )
}

@Composable
fun AddDialog(onDismiss: () -> Unit, onSave: (Routine) -> Unit) {
    var name by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("6:00 PM") }
    var category by remember { mutableStateOf("Pray") }
    var repeat by remember { mutableStateOf("Every day") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Task name") })
                OutlinedTextField(details, { details = it }, label = { Text("Details") })
                OutlinedTextField(time, { time = it }, label = { Text("Time") })
                OutlinedTextField(category, { category = it }, label = { Text("Category") })
                OutlinedTextField(repeat, { repeat = it }, label = { Text("Repeat") })
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank(), onClick = {
                onSave(Routine(System.currentTimeMillis(), name, details, time, repeat, category))
            }) { Text("Save Task") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
