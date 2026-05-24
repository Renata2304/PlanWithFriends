package com.example.planwithfriends.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.planwithfriends.data.Group

@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel(factory = GroupsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Navigare către ecranul de creare grup nou */ },
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crează Grup", tint = Color.Black)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.groupsList) { group ->
                GroupListItem(group = group)
            }
        }
    }
}

@Composable
fun GroupListItem(group: Group) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numele Grupului
        Text(
            text = group.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        // Secțiunea cu Avatare (Placeholder) și numărul de membri suplimentari
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Simulăm avatarele suprapuse din schiță
            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                repeat(minOf(group.memberCount, 3)) { // Arătăm maxim 3 avatare
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Indicator pentru restul membrilor (ex: +1, +2)
            if (group.memberCount > 3) {
                Text(
                    text = "+${group.memberCount - 3}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Blue // Culoarea specifică din schiță
                )
            }
        }
    }
}