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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    // Stări locale pentru a bifa cerința "customize colors, language"
    var isDarkTheme by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("English", "Română", "Français")
    var selectedLanguage by remember { mutableStateOf(languages[0]) }

    // Folosim fundalul în funcție de temă (simulare schimbare culori)
    val backgroundColor = if (isDarkTheme) Color(0xFF303030) else Color(0xFFF0F4C3)
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Poza de Profil
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            tint = if (isDarkTheme) Color.LightGray else Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 2. Setare Limbă (Language Dropdown)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLanguage,
                onValueChange = {},
                readOnly = true,
                label = { Text("Language", color = textColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languages.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedLanguage = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Setare Temă (Customize colors)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dark Theme",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { isDarkTheme = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFF48FB1), // Rozul din temă
                    checkedTrackColor = Color(0xFFFCE4EC)
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. Log Out
        TextButton(onClick = { /* Acțiune delogare */ }) {
            Text(
                text = "Log Out",
                color = Color.Red,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}