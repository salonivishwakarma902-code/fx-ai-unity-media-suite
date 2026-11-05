package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.OracleBackendTemplates
import com.example.data.OracleServerConfig
import com.example.data.OracleTelemetry
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSync
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeuralViolet
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianGlassBorder
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextSecondary

@Composable
fun OracleDashboardScreen(
    telemetry: OracleTelemetry,
    serverConfig: OracleServerConfig,
    terminalLogs: List<String>,
    isSyncing: Boolean,
    onTestConnection: () -> Unit,
    onTriggerFullSync: () -> Unit,
    onClearServerCache: () -> Unit,
    onRestartDaemon: () -> Unit,
    onToggleAutoBackup: (Boolean) -> Unit,
    onDownloadScript: (String, String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var selectedScriptTab by remember { mutableStateOf("server.js") }
    var copiedFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("oracle_dashboard_screen"),
        contentPadding = PaddingValues(14.dp, 10.dp, 14.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Node Banner
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Oracle Cloud 200GB Free Forever Control",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "${telemetry.serverVersion} • Endpoint: ${serverConfig.serverUrl}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanPrimary
                    )
                    Text(
                        text = "Status: ${telemetry.modeLabel} • Latency: ${telemetry.latencyMs}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSync
                    )
                }
            }
        }

        // 2. Live Server Telemetry & 200GB Storage Ring Gauges
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Live 200GB Block Volume & ARM Ampere Telemetry",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TelemetryRingGauge(
                            label = "200GB Block",
                            valueText = "${String.format("%.1f", telemetry.usedStorageGb)} GB",
                            subText = "${String.format("%.1f", telemetry.freeStorageGb)} GB Free",
                            progress = (telemetry.usedStorageGb / telemetry.totalStorageGb).coerceIn(0.05f, 1f),
                            color = EmeraldSync
                        )
                        TelemetryRingGauge(
                            label = "Ampere 4-OCPU",
                            valueText = "${telemetry.cpuLoadPercent}%",
                            subText = "ARM64 Load",
                            progress = (telemetry.cpuLoadPercent / 100f).coerceIn(0.05f, 1f),
                            color = CyanPrimary
                        )
                        TelemetryRingGauge(
                            label = "24GB RAM",
                            valueText = "${String.format("%.1f", telemetry.ramUsedGb)} GB",
                            subText = "of 24.0 GB",
                            progress = (telemetry.ramUsedGb / telemetry.ramTotalGb).coerceIn(0.05f, 1f),
                            color = SunsetAmber
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Containers: ${telemetry.activeContainers} Active (PM2 + Nginx + SQLite)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "↑ ${telemetry.uploadThroughputMbps} Mbps  ↓ ${telemetry.downloadThroughputMbps} Mbps",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary
                        )
                    }
                }
            }
        }

        // 3. Remote Server Controls & Wi-Fi Auto-Backup Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Remote Server Controls (रिमोट सर्वर कंट्रोल)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTestConnection,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("oracle_test_connection_button")
                        ) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Health", color = ObsidianBg, style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = onTriggerFullSync,
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSync),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("oracle_full_sync_button")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSyncing) "Syncing…" else "Full Sync", color = ObsidianBg, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClearServerCache,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("oracle_clear_cache_button")
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = SunsetAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Cache", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = onRestartDaemon,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("oracle_restart_daemon_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = NeuralViolet, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restart PM2", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Backup New Photos over Wi-Fi",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Automatically sync captured & imported photos to your 200GB Oracle Block Volume",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = serverConfig.autoBackupWifi,
                            onCheckedChange = onToggleAutoBackup,
                            colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = EmeraldSync)
                        )
                    }
                }
            }
        }

        // 4. Embedded Real-Time Server Terminal Console
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF050811)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldSync, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "root@oracle-ampere-200gb:~# pm2 logs fx-ai-unity",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSync
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        terminalLogs.takeLast(8).forEach { logLine ->
                            Text(
                                text = logLine,
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }

        // 5. Downloadable & Copyable Full Backend Server Code (server.js + install-oracle-server.sh)
        item {
            val activeCode = if (selectedScriptTab == "server.js") {
                OracleBackendTemplates.NODE_SERVER_JS
            } else {
                OracleBackendTemplates.INSTALL_ORACLE_SH
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Self-Hosted Backend Server Code (Copy / 1-Click Download)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Production Node.js + Express + Multer + SQLite3 server & automated Ubuntu 200GB installer",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("server.js", "install-oracle-server.sh").forEach { tabName ->
                            FilterChip(
                                selected = selectedScriptTab == tabName,
                                onClick = {
                                    selectedScriptTab = tabName
                                    copiedFeedback = null
                                },
                                label = { Text(tabName, fontFamily = JetBrainsMonoFontFamily) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.25f),
                                    selectedLabelColor = CyanPrimary
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(activeCode))
                                copiedFeedback = "Copied $selectedScriptTab to clipboard!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_backend_script_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy $selectedScriptTab", color = ObsidianBg, style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = {
                                onDownloadScript(selectedScriptTab, activeCode)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SunsetAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_backend_script_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download File", color = ObsidianBg, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    if (copiedFeedback != null) {
                        Text(
                            text = copiedFeedback!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldSync
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF050811))
                            .border(1.dp, ObsidianGlassBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = activeCode,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBAE6FD)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryRingGauge(
    label: String,
    valueText: String,
    subText: String,
    progress: Float,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(92.dp)
        ) {
            Canvas(modifier = Modifier.size(82.dp)) {
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.White)
        Text(text = subText, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
