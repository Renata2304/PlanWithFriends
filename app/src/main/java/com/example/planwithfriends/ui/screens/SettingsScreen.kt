@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.planwithfriends.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.planwithfriends.R

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, settingsViewModel: SettingsViewModel) {
    var expandedLanguage by remember { mutableStateOf(false) }
    var expandedTheme by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    val languages = stringArrayResource(R.array.languages_list)
    val selectedLanguage = settingsViewModel.currentLanguage ?: languages[0]

    val textColor = MaterialTheme.colorScheme.onBackground

    val themeMap = mapOf(
        "auto" to stringResource(id = R.string.theme_auto),
        "spring" to stringResource(id = R.string.theme_spring),
        "summer" to stringResource(id = R.string.theme_summer),
        "autumn" to stringResource(id = R.string.theme_autumn),
        "winter" to stringResource(id = R.string.theme_winter)
    )
    val themeKeys = themeMap.keys.toList()

    val selectedThemeDisplayText = themeMap[settingsViewModel.currentSeasonTheme] ?: themeMap["auto"]!!

    // Starea curenta a utilizatorului
    val currentUsername = settingsViewModel.currentUser
    val isLoggedIn = currentUsername != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Poza de Profil
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = stringResource(R.string.profile_picture_cd),
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            tint = if (settingsViewModel.isDarkTheme) Color.LightGray else Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Numele utilizatorului daca e logat
        if (isLoggedIn) {
            Text(
                text = stringResource(R.string.logged_in_as, currentUsername!!),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Setare Limba
        ExposedDropdownMenuBox(
            expanded = expandedLanguage,
            onExpandedChange = { expandedLanguage = !expandedLanguage }
        ) {
            OutlinedTextField(
                value = selectedLanguage,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.language_label), color = textColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguage) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedLanguage,
                onDismissRequest = { expandedLanguage = false }
            ) {
                languages.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            settingsViewModel.setLanguage(selectionOption)
                            expandedLanguage = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Setare Tema
        ExposedDropdownMenuBox(
            expanded = expandedTheme,
            onExpandedChange = { expandedTheme = !expandedTheme }
        ) {
            OutlinedTextField(
                value = selectedThemeDisplayText,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.color_theme), color = textColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTheme) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedTheme,
                onDismissRequest = { expandedTheme = false }
            ) {
                themeKeys.forEach { key ->
                    DropdownMenuItem(
                        text = { Text(themeMap[key]!!) },
                        onClick = {
                            settingsViewModel.setSeasonTheme(key)
                            expandedTheme = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Mod Intunecat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dark_theme_label),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Switch(
                checked = settingsViewModel.isDarkTheme,
                onCheckedChange = { isDark ->
                    settingsViewModel.toggleTheme(isDark)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.secondary,
                    checkedTrackColor = MaterialTheme.colorScheme.tertiary
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isLoggedIn) {
            TextButton(onClick = { settingsViewModel.logout() }) {
                Text(
                    text = stringResource(R.string.action_logout),
                    color = Color.Red,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Button(
                onClick = { showLoginDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.action_login),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (showLoginDialog) {
            AuthDialog(
                viewModel = settingsViewModel,
                onDismiss = { showLoginDialog = false }
            )
        }
    }
}

@Composable
fun AuthDialog(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.clearAuthError() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.login_register_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                viewModel.authErrorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.trim() },
                    label = { Text(stringResource(R.string.username_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.trim() },
                    label = { Text(stringResource(R.string.password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (viewModel.isAuthLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                TextButton(
                    onClick = {
                        if (username.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(username, password) { onDismiss() }
                        }
                    },
                    enabled = !viewModel.isAuthLoading
                ) { Text(stringResource(R.string.action_login_button)) }

                TextButton(
                    onClick = {
                        if (username.isNotBlank() && password.isNotBlank()) {
                            viewModel.register(username, password) { onDismiss() }
                        }
                    },
                    enabled = !viewModel.isAuthLoading
                ) { Text(stringResource(R.string.action_create_account)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !viewModel.isAuthLoading) {
                Text(stringResource(R.string.action_cancel), color = Color.Gray)
            }
        }
    )
}