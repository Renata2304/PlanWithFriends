package com.example.planwithfriends.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.planwithfriends.PlanWithFriendsApplication
import com.example.planwithfriends.data.Event
import com.example.planwithfriends.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    groupName: String,
    onNavigateBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as PlanWithFriendsApplication

    // AICI ERA EROAREA: Acum îi pasăm și groupsRepository!
    val viewModel: GroupDetailsViewModel = viewModel(
        key = groupId,
        factory = GroupDetailsViewModel.provideFactory(
            eventsRepository = app.container.eventsRepository,
            groupsRepository = app.container.groupsRepository,
            groupId = groupId
        )
    )

    val events by viewModel.groupEvents.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }

    val toastMessage = stringResource(R.string.code_copied, groupId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.group_menu_cd))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "${stringResource(R.string.copy_group_id)}: $groupId") },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(groupId))
                                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Părăsește grupul", color = Color.Red, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMenu = false
                                viewModel.leaveCurrentGroup(
                                    onSuccess = {
                                        onNavigateBack()
                                    }
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_group_event_cd), tint = Color.Black)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (events.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_events_in_group),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(events) { event ->
                        EventCard(
                            title = event.title,
                            time = stringResource(R.string.date_at_time, event.date, event.time),
                            onClick = { eventToEdit = event }
                        )
                    }
                }
            }

            if (showAddDialog) {
                GroupEventDialog(
                    initialDate = LocalDate.now(),
                    onDismiss = { showAddDialog = false },
                    onSave = { title, time, date ->
                        viewModel.addGroupEvent(title, time, date)
                        showAddDialog = false
                    }
                )
            }

            eventToEdit?.let { event ->
                EditEventDialog(
                    initialTitle = event.title,
                    initialTime = event.time,
                    selectedDate = try { LocalDate.parse(event.date) } catch (e: Exception) { LocalDate.now() },
                    onDismiss = { eventToEdit = null },
                    onSave = { newTitle, newTime ->
                        viewModel.updateGroupEvent(event, newTitle, newTime)
                        eventToEdit = null
                    },
                    onDelete = {
                        viewModel.deleteGroupEvent(event)
                        eventToEdit = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupEventDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (title: String, time: String, date: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(initialDate) }

    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.add_group_event_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.event_title_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date.toString(),
                        onValueChange = { },
                        label = { Text(stringResource(R.string.date_label)) },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent)
                            .clickable { showDatePicker = true }
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { },
                        label = { Text(stringResource(R.string.time_label)) },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent)
                            .clickable { showTimePicker = true }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && time.isNotBlank()) {
                        onSave(title, time, date.toString())
                    }
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = Color.Gray) }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        date = selectedDate
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        DialWithDialogExample(
            onConfirm = { timePickerState ->
                val h = timePickerState.hour.toString().padStart(2, '0')
                val m = timePickerState.minute.toString().padStart(2, '0')
                time = "$h:$m"
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}