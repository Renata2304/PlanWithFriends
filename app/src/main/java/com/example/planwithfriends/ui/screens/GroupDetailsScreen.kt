package com.example.planwithfriends.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as PlanWithFriendsApplication

    val viewModel: GroupDetailsViewModel = viewModel(
        key = groupId,
        factory = GroupDetailsViewModel.provideFactory(
            eventsRepository = app.container.eventsRepository,
            groupsRepository = app.container.groupsRepository,
            groupId = groupId
        )
    )

    val events by viewModel.groupEvents.collectAsState()
    val group by viewModel.currentGroup.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }

    val toastMessage = stringResource(R.string.code_copied, groupId)

    val currentUsername = settingsViewModel.currentUser ?: "Guest"
    val pfpUri = settingsViewModel.profilePictureUri ?: "icon_person"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.navigate_back_cd))
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
                            text = { Text(text = stringResource(R.string.leave_group), color = Color.Red, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMenu = false
                                viewModel.leaveCurrentGroup(
                                    userId = currentUsername,
                                    userIcon = pfpUri
                                )
                                onNavigateBack()
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            val members = group?.members?.filter { it.isNotBlank() && it != "null" } ?: emptyList()
            val memberIcons = group?.memberIcons?.filter { it.isNotBlank() && it != "null" } ?: emptyList()

            if (members.isNotEmpty() || memberIcons.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val count = maxOf(members.size, memberIcons.size)
                    items(count) { index ->
                        val username = members.getOrNull(index) ?: "Unknown"
                        val iconUri = memberIcons.getOrNull(index) ?: ""

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(64.dp)
                        ) {
                            MemberAvatar(pfpUri = iconUri)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = username,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_events_in_group),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(events.size) { index ->
                        val event = events[index]
                        GroupEventCard(
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
                GroupEditEventDialog(
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

@Composable
fun MemberAvatar(pfpUri: String) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (pfpUri.startsWith("icon_")) {
            val icon = when (pfpUri) {
                "icon_face" -> Icons.Default.Face
                "icon_favorite" -> Icons.Default.Favorite
                "icon_home" -> Icons.Default.Home
                else -> Icons.Default.Person
            }
            Icon(
                imageVector = icon,
                contentDescription = "Member Avatar",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else if (pfpUri.isNotBlank()) {
            AsyncImage(
                model = pfpUri,
                contentDescription = "Member Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile",
                modifier = Modifier.size(32.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun GroupEventCard(title: String, time: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onTertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiary
            )
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
        GroupTimePickerDial(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupEditEventDialog(
    initialTitle: String,
    initialTime: String,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (title: String, time: String) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var time by remember { mutableStateOf(initialTime) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.edit_event), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.date_format_label, selectedDate),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.event_title_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { },
                        label = { Text(stringResource(R.string.event_time_hint)) },
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
                    if (title.isNotBlank()) {
                        onSave(title, time)
                    }
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.action_delete), color = Color.Red)
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel), color = Color.Gray)
                }
            }
        }
    )

    if (showTimePicker) {
        GroupTimePickerDial(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupTimePickerDial(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = java.util.Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(java.util.Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(java.util.Calendar.MINUTE),
        is24Hour = true,
    )

    GroupTimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(timePickerState) }
    ) {
        TimePicker(
            state = timePickerState,
        )
    }
}

@Composable
private fun GroupTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(stringResource(R.string.action_ok))
            }
        },
        text = { content() }
    )
}