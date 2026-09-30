package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.ProviderConfigEntity
import com.example.ui.theme.NyintAmber
import com.example.ui.theme.NyintCardBorderDark
import com.example.ui.theme.NyintCyan
import com.example.ui.theme.NyintEmerald
import com.example.ui.theme.NyintRose
import com.example.ui.theme.NyintViolet
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val providerConfigs by viewModel.providerConfigs.collectAsState()

    var notificationsEnabled by remember { mutableStateOf(viewModel.repository.preferences.notificationsEnabled) }

    // Permission Launchers
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(
            context,
            if (granted) "Microphone permission granted" else "Microphone permission denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(
            context,
            if (granted) "Camera permission granted" else "Camera permission denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings & Management",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Configure AI providers, keys, language, and system permissions",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Account Profile Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NyintCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(NyintCyan.copy(alpha = 0.2f))
                            .border(1.dp, NyintCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = NyintCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.repository.preferences.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = viewModel.repository.preferences.userEmail,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NyintEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Pro AI Studio Workspace",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NyintEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: AI Providers & API Key Center
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = NyintCyan, modifier = Modifier.size(18.dp))
                Text(
                    text = "AI Providers & API Keys",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter your personal API keys. Keys are stored locally on your device.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Provider 1: Gemini
        item {
            val geminiConfig = providerConfigs.find { it.providerId == "GEMINI" }
            ProviderKeyCard(
                providerName = "Google Gemini",
                providerId = "GEMINI",
                color = NyintCyan,
                currentKey = geminiConfig?.apiKey ?: "",
                isConnected = geminiConfig?.isConnected ?: false,
                statusMessage = uiState.testConnectionStatus["GEMINI"] ?: geminiConfig?.statusMessage ?: "Gemini Ready",
                isTesting = uiState.isTestingKey["GEMINI"] == true,
                onSaveKey = { key -> viewModel.saveApiKey("GEMINI", key) },
                onTestConnection = { key -> viewModel.testProviderConnection("GEMINI", key) },
                onDisconnect = { viewModel.disconnectProvider("GEMINI") }
            )
        }

        // Provider 2: OpenAI
        item {
            val openAiConfig = providerConfigs.find { it.providerId == "OPENAI" }
            ProviderKeyCard(
                providerName = "ChatGPT / OpenAI",
                providerId = "OPENAI",
                color = NyintEmerald,
                currentKey = openAiConfig?.apiKey ?: "",
                isConnected = openAiConfig?.isConnected ?: false,
                statusMessage = uiState.testConnectionStatus["OPENAI"] ?: openAiConfig?.statusMessage ?: "Enter OpenAI API key",
                isTesting = uiState.isTestingKey["OPENAI"] == true,
                onSaveKey = { key -> viewModel.saveApiKey("OPENAI", key) },
                onTestConnection = { key -> viewModel.testProviderConnection("OPENAI", key) },
                onDisconnect = { viewModel.disconnectProvider("OPENAI") }
            )
        }

        // Provider 3: Claude
        item {
            val claudeConfig = providerConfigs.find { it.providerId == "CLAUDE" }
            ProviderKeyCard(
                providerName = "Claude / Anthropic",
                providerId = "CLAUDE",
                color = NyintAmber,
                currentKey = claudeConfig?.apiKey ?: "",
                isConnected = claudeConfig?.isConnected ?: false,
                statusMessage = uiState.testConnectionStatus["CLAUDE"] ?: claudeConfig?.statusMessage ?: "Enter Claude API key",
                isTesting = uiState.isTestingKey["CLAUDE"] == true,
                onSaveKey = { key -> viewModel.saveApiKey("CLAUDE", key) },
                onTestConnection = { key -> viewModel.testProviderConnection("CLAUDE", key) },
                onDisconnect = { viewModel.disconnectProvider("CLAUDE") }
            )
        }

        // Section: Myanmar-First & Localization
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = NyintViolet, modifier = Modifier.size(18.dp))
                Text(
                    text = "Language & Localization",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NyintCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Myanmar Unicode Mode (မြန်မာစာ)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.isMyanmarLanguage) "Active: Prompts & voice prioritize Myanmar" else "Active: English standard",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = uiState.isMyanmarLanguage,
                        onCheckedChange = { viewModel.setLanguage(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NyintViolet)
                    )
                }
            }
        }

        // Section: Device Permissions
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = NyintCyan, modifier = Modifier.size(18.dp))
                Text(
                    text = "System Permissions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Explainable access for microphone voice and camera vision",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NyintCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Microphone
                    PermissionRow(
                        icon = Icons.Default.Mic,
                        title = "Microphone",
                        description = "Enables real-time voice speech-to-text",
                        isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
                        onRequest = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    )

                    // Camera
                    PermissionRow(
                        icon = Icons.Default.CameraAlt,
                        title = "Camera",
                        description = "Take photos for instant multimodal AI analysis",
                        isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
                        onRequest = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }
        }

        // Section: Android APK & Export Information
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NyintEmerald, modifier = Modifier.size(18.dp))
                Text(
                    text = "Android APK & Phone Installation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Real compiled Android binary artifact & mobile installation instructions",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NyintEmerald.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "A Nyint AI APK (Debug)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NyintEmerald.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "COMPILED • 25 MB",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NyintEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "• Package ID: com.aistudio.anyintai.nkvxp",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Text(
                            text = "• Version: 1.0 (Version Code: 1)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Target SDK: Android 15/16 (API 36, minSdk 24)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Output: app/build/outputs/apk/debug/app-debug.apk",
                            fontSize = 11.sp,
                            color = NyintCyan,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "📱 How to Install on Your Android Phone:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NyintAmber
                            )
                            Text(
                                text = "1. Tap the AI Studio Settings menu (top right) and select 'Export Project' or 'Download APK'.\n" +
                                        "2. When prompted 'File might be harmful', tap 'Download anyway'.\n" +
                                        "3. Open your phone's Downloads folder and tap 'app-debug.apk'.\n" +
                                        "4. If prompted 'Install unknown apps', tap Settings -> toggle 'Allow from this source'.\n" +
                                        "5. Tap 'Install' to launch A Nyint AI natively!",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProviderKeyCard(
    providerName: String,
    providerId: String,
    color: Color,
    currentKey: String,
    isConnected: Boolean,
    statusMessage: String,
    isTesting: Boolean,
    onSaveKey: (String) -> Unit,
    onTestConnection: (String) -> Unit,
    onDisconnect: () -> Unit
) {
    var keyInput by remember(currentKey) { mutableStateOf(currentKey) }
    var showRawKey by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NyintCardBorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) NyintEmerald else color)
                    )
                    Text(
                        text = providerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isConnected) NyintEmerald.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isConnected) "CONNECTED" else "STANDBY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) NyintEmerald else Color.Gray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Key Input with Masking
            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text("API Key") },
                placeholder = { Text("sk-... or AI Studio key") },
                visualTransformation = if (showRawKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showRawKey = !showRawKey }) {
                        Icon(
                            imageVector = if (showRawKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle key visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_input_$providerId"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = color,
                    unfocusedBorderColor = NyintCardBorderDark
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status message
            Text(
                text = statusMessage,
                fontSize = 12.sp,
                color = if (isConnected) NyintEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onSaveKey(keyInput.trim())
                        onTestConnection(keyInput.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("test_connection_$providerId"),
                    enabled = !isTesting
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Testing...", color = Color.Black, fontSize = 12.sp)
                    } else {
                        Text("Save & Test", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (keyInput.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            keyInput = ""
                            onDisconnect()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NyintRose),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Disconnect", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NyintCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = NyintCyan, modifier = Modifier.size(18.dp))
            }

            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (isGranted) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = NyintEmerald.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "GRANTED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NyintEmerald,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        } else {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = NyintCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Allow", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
