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
import androidx.compose.ui.unit.dp
import com.example.planwithfriends.R

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, settingsViewModel: SettingsViewModel) {
    var expandedLanguage by remember { mutableStateOf(false) }
    var expandedTheme by remember { mutableStateOf(false) }

    val languages = stringArrayResource(R.array.languages_list)
    val selectedLanguage = settingsViewModel.currentLanguage

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

        Spacer(modifier = Modifier.height(48.dp))

        // 2. Setare Limbă (Language Dropdown)
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

        // 3. Setare Temă Cromatică / Anotimp
        ExposedDropdownMenuBox(
            expanded = expandedTheme,
            onExpandedChange = { expandedTheme = !expandedTheme }
        ) {
            OutlinedTextField(
                value = selectedThemeDisplayText, // Afișăm traducerea!
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
                        text = { Text(themeMap[key]!!) }, // Afișăm opțiunile traduse
                        onClick = {
                            settingsViewModel.setSeasonTheme(key) // Salvăm codul intern!
                            expandedTheme = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Mod Întunecat (Dark Mode)
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

        // 5. Log Out
        TextButton(onClick = { /* Acțiune delogare */ }) {
            Text(
                text = stringResource(R.string.action_logout),
                color = Color.Red,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}