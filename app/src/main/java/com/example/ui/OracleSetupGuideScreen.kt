package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.GuideLanguage
import com.example.data.OracleBackendTemplates
import com.example.data.OracleServerConfig
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSync
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianGlassBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextSecondary

@Composable
fun OracleSetupGuideScreen(
    serverConfig: OracleServerConfig,
    onSelectLanguage: (GuideLanguage) -> Unit,
    onVerifyAndConnect: (String, String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val steps = remember(serverConfig.guideLanguage) {
        OracleBackendTemplates.getStepsForLanguage(serverConfig.guideLanguage)
    }

    var serverUrlInput by remember(serverConfig.serverUrl) { mutableStateOf(serverConfig.serverUrl) }
    var apiKeyInput by remember(serverConfig.apiKey) { mutableStateOf(serverConfig.apiKey) }
    var copiedStepId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("oracle_setup_guide_screen"),
        contentPadding = PaddingValues(14.dp, 10.dp, 14.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Language Switcher (🇮🇳 हिंदी | 🇮🇳 Hinglish | 🌐 English)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ObsidianGlassBorder, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Oracle 200GB Free Setup Wizard (हिंदी / English)",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Step-by-step interactive guide to claim your Forever-Free 200GB Block Volume + 24GB RAM Ampere VM",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GuideLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = serverConfig.guideLanguage == lang,
                                onClick = { onSelectLanguage(lang) },
                                label = {
                                    Text(
                                        text = lang.label,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SunsetAmber,
                                    selectedLabelColor = ObsidianBg
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("guide_lang_${lang.code}")
                            )
                        }
                    }
                }
            }
        }

        // Steps 1 to 5
        items(steps, key = { "step_${it.stepNumber}" }) { step ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ObsidianGlassBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = CyanPrimary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = step.badge,
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    Text(
                        text = step.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SunsetAmber
                    )

                    step.bullets.forEach { bullet ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldSync,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    if (step.terminalSnippet != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF050811))
                                .border(1.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = step.terminalSnippet,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldSync
                                )
                                Button(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(step.terminalSnippet))
                                        copiedStepId = step.stepNumber
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (copiedStepId == step.stepNumber) "Copied Command!" else "1-Click Copy Command",
                                        color = ObsidianBg,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Step 5 Connection Form
                    if (step.stepNumber == 5) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            label = { Text("Oracle Public IP / Server URL (e.g. http://152.67.184.210:8080)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ObsidianSurface,
                                unfocusedContainerColor = ObsidianSurface,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = ObsidianGlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("guide_server_url_input")
                        )

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Secret Bearer API Key") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ObsidianSurface,
                                unfocusedContainerColor = ObsidianSurface,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = ObsidianGlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("guide_api_key_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    serverUrlInput = "https://152.67.184.210:8080"
                                    apiKeyInput = "fx-oracle-200gb-free-forever-key-9941"
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Fill Demo Node", color = CyanPrimary)
                            }

                            Button(
                                onClick = { onVerifyAndConnect(serverUrlInput, apiKeyInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSync),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("verify_connect_server_button")
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Verify & Connect Server",
                                    color = ObsidianBg,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OpenSourceReadmeDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = ObsidianCard,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSync)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FX AI Unity — Open-Source Architecture",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close README", tint = Color.White)
                    }
                }

                Text(
                    text = "1. Dual-Storage Engine: Local Room SQLite Database + JPEG File System paired with an OkHttp REST Connector (`Bearer Token`) for your Oracle Cloud 200GB Always Free VM.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0)
                )
                Text(
                    text = "2. Multilingual Gemini Vision Pipeline: Uses Firebase AI Logic (`gemini-3.8-flash`) with App Check to generate structured JSON descriptions in Hindi (देवनागरी), Hinglish, and English + OCR text.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0)
                )
                Text(
                    text = "3. CameraX Live Studio: Includes runtime Camera permission flow, live hardware PreviewView + ImageCapture, and a Confirm or Discard review screen before saving.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0)
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Architecture Overview", color = ObsidianBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
