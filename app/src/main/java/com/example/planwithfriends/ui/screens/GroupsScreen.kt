@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.planwithfriends.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.planwithfriends.data.Group
import com.example.planwithfriends.R

@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel(factory = GroupsViewModel.Factory),
    settingsViewModel: SettingsViewModel,
    onGroupClick: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    val currentUsername = settingsViewModel.currentUser ?: "Guest"
    val pfpUri = settingsViewModel.profilePictureUri

    LaunchedEffect(key1 = currentUsername) {
        viewModel.refreshDataForCurrentUser()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Salut, $currentUsername!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    ProfileAvatar(
                        pfpUri = pfpUri,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp),
                        iconSize = 24.dp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showMenu = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_group_cd),
                        tint = Color.Black
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.create_new_group)) },
                        onClick = {
                            showMenu = false
                            showCreateDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.join_group)) },
                        onClick = {
                            showMenu = false
                            showJoinDialog = true
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.groupsList) { group ->
                GroupListItem(
                    group = group,
                    onClick = { onGroupClick(group.id, group.name) }
                )
            }
        }

        if (showCreateDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { groupName ->
                    viewModel.createGroup(
                        name = groupName,
                        username = currentUsername,
                        icon = pfpUri ?: "icon_person"
                    )
                    showCreateDialog = false
                }
            )
        }

        if (showJoinDialog) {
            JoinGroupDialog(
                onDismiss = { showJoinDialog = false },
                onJoin = { groupId ->
                    viewModel.joinGroup(
                        groupId = groupId,
                        username = currentUsername,
                        icon = pfpUri ?: "icon_person"
                    )
                    showJoinDialog = false
                }
            )
        }
    }
}

@Composable
fun GroupListItem(group: Group, onClick: () -> Unit) {
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
            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    val displayCount = minOf(group.memberCount, 3)

                    for (i in 0 until displayCount) {
                        val iconUri = group.memberIcons.getOrNull(i)

                        ProfileAvatar(
                            pfpUri = iconUri,
                            modifier = Modifier.size(32.dp),
                            iconSize = 20.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (group.memberCount > 3) {
                    Text(
                        text = "+${group.memberCount - 3}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    pfpUri: String?, // Permitem să fie null dacă nu există poză
    modifier: Modifier = Modifier, // Adăugăm modifier-ul pentru a seta mărimile corecte
    iconSize: Dp = 24.dp // Mărimea iconițelor fallback
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (pfpUri != null && pfpUri.startsWith("icon_")) {
            val icon = when (pfpUri) {
                "icon_face" -> Icons.Default.Face
                "icon_favorite" -> Icons.Default.Favorite
                "icon_home" -> Icons.Default.Home
                else -> Icons.Default.Person
            }
            Icon(
                imageVector = icon,
                contentDescription = "Avatar",
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.primary
            )
        } else if (!pfpUri.isNullOrBlank()) {
            // Coil se ocupă automat de descărcarea și afișarea linkului
            AsyncImage(
                model = pfpUri,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile",
                modifier = Modifier.fillMaxSize(),
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.create_new_group), fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text(stringResource(R.string.group_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (groupName.isNotBlank()) onCreate(groupName)
                }
            ) { Text(stringResource(R.string.action_create)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = Color.Gray) }
        }
    )
}

@Composable
fun JoinGroupDialog(
    onDismiss: () -> Unit,
    onJoin: (groupId: String) -> Unit
) {
    var groupId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.join_group), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.join_group_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = groupId,
                    onValueChange = { groupId = it },
                    label = { Text(stringResource(R.string.group_id_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (groupId.isNotBlank()) onJoin(groupId)
                }
            ) { Text(stringResource(R.string.action_join)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = Color.Gray) }
        }
    )
}