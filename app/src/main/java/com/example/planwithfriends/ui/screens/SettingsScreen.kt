@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.planwithfriends.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.planwithfriends.R
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

suspend fun uploadImageToImgBB(base64Image: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = "5009c84ce24ceb12936ad621ba093069"

            val url = URL("https://api.imgbb.com/1/upload?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val postData = "image=" + URLEncoder.encode(base64Image, "UTF-8")
            connection.outputStream.write(postData.toByteArray())

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                return@withContext jsonObject.getJSONObject("data").getString("url") // Returnează link-ul pozei!
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, settingsViewModel: SettingsViewModel) {
    var expandedLanguage by remember { mutableStateOf(false) }
    var expandedTheme by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    var isUploading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUsername = settingsViewModel.currentUser
    val isLoggedIn = currentUsername != null

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { selectedUri ->
            selectedUri?.let { uri ->
                coroutineScope.launch {
                    isUploading = true
                    try {
                        if (currentUsername != null) {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()

                            if (originalBitmap != null) {
                                val maxDim = 500f
                                val scale = minOf(maxDim / originalBitmap.width.toFloat(), maxDim / originalBitmap.height.toFloat())
                                val width = (scale * originalBitmap.width).roundToInt()
                                val height = (scale * originalBitmap.height).roundToInt()

                                val scaledBitmap = originalBitmap.scale(width, height)

                                val outputStream = ByteArrayOutputStream()
                                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                                val byteArray = outputStream.toByteArray()

                                val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                                val imageUrl = uploadImageToImgBB(base64String)

                                if (imageUrl != null) {
                                    settingsViewModel.updateProfilePicture(imageUrl)
                                    Toast.makeText(context, "Poză actualizată cu succes!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Eroare la încărcare!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "A apărut o eroare.", Toast.LENGTH_SHORT).show()
                    } finally {
                        isUploading = false
                        showAvatarDialog = false
                    }
                }
            }
        }
    )

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(if (settingsViewModel.isDarkTheme) Color.DarkGray else Color.LightGray)
                .clickable(enabled = isLoggedIn && !isUploading) { showAvatarDialog = true },
            contentAlignment = Alignment.Center
        ) {
            val pfpUri = settingsViewModel.profilePictureUri

            if (isUploading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (pfpUri != null && pfpUri.startsWith("icon_")) {
                val icon = when (pfpUri) {
                    "icon_face" -> Icons.Default.Face
                    "icon_favorite" -> Icons.Default.Favorite
                    "icon_home" -> Icons.Default.Home
                    else -> Icons.Default.Person
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else if (pfpUri != null && pfpUri.startsWith("http")) {
                // Aici va intra automat link-ul de la ImgBB
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

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoggedIn) {
            Text(
                text = stringResource(R.string.logged_in_as, currentUsername!!),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = stringResource(R.string.please_login),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dark_theme),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            Switch(
                checked = settingsViewModel.isDarkTheme,
                onCheckedChange = { isDark -> settingsViewModel.toggleTheme(isDark) }
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

        if (showAvatarDialog) {
            AlertDialog(
                onDismissRequest = { showAvatarDialog = false },
                title = { Text(stringResource(R.string.choose_profile_picture)) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.choose_from_gallery))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.or_choose_icon), style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { settingsViewModel.updateProfilePicture("icon_face"); showAvatarDialog = false }) {
                                Icon(Icons.Default.Face, contentDescription = "Face", modifier = Modifier.size(40.dp))
                            }
                            IconButton(onClick = { settingsViewModel.updateProfilePicture("icon_person"); showAvatarDialog = false }) {
                                Icon(Icons.Default.Person, contentDescription = "Person", modifier = Modifier.size(40.dp))
                            }
                            IconButton(onClick = { settingsViewModel.updateProfilePicture("icon_favorite"); showAvatarDialog = false }) {
                                Icon(Icons.Default.Favorite, contentDescription = "Favorite", modifier = Modifier.size(40.dp))
                            }
                            IconButton(onClick = { settingsViewModel.updateProfilePicture("icon_home"); showAvatarDialog = false }) {
                                Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAvatarDialog = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
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
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Text
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.trim() },
                    label = { Text(stringResource(R.string.password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Password
                    )
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