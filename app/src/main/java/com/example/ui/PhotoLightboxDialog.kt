package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSync
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianGlassBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoLightboxDialog(
    photos: List<PhotoEntity>,
    initialPhotoId: String,
    onDismiss: () -> Unit,
    onToggleFavorite: (PhotoEntity) -> Unit,
    onForceSyncOracle: (PhotoEntity) -> Unit,
    onExportPhoto: (PhotoEntity) -> Unit,
    onReAnalyzeWithGemini: (PhotoEntity) -> Unit,
    onDeletePhoto: (PhotoEntity) -> Unit,
    onTagClick: (String) -> Unit
) {
    if (photos.isEmpty()) {
        onDismiss()
        return
    }

    var currentIndex by remember(initialPhotoId) {
        mutableIntStateOf(photos.indexOfFirst { it.id == initialPhotoId }.coerceAtLeast(0))
    }
    val safeIndex = currentIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0))
    val currentPhoto = photos[safeIndex]

    var scale by remember(currentPhoto.id) { mutableFloatStateOf(1f) }
    var offset by remember(currentPhoto.id) { mutableStateOf(Offset.Zero) }
    var isSlideshowPlaying by remember { mutableStateOf(false) }
    var showInfoDrawer by remember { mutableStateOf(true) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offset = if (scale > 1f) offset + panChange else Offset.Zero
    }

    // Auto-advance when Slideshow mode is playing
    LaunchedEffect(isSlideshowPlaying, safeIndex, photos.size) {
        if (isSlideshowPlaying && photos.size > 1) {
            delay(3200L)
            currentIndex = (safeIndex + 1) % photos.size
        }
    }

    BackHandler(onBack = onDismiss)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("photo_lightbox_dialog"),
            color = ObsidianBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianSurface)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("lightbox_close_button")) {
                            Icon(Icons.Default.Close, contentDescription = "Close Lightbox", tint = Color.White)
                        }
                        Column {
                            Text(
                                text = currentPhoto.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "${safeIndex + 1} of ${photos.size} • ${currentPhoto.dateGroupLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Slideshow Play/Pause
                        IconButton(
                            onClick = { isSlideshowPlaying = !isSlideshowPlaying },
                            modifier = Modifier.testTag("lightbox_slideshow_button")
                        ) {
                            Icon(
                                imageVector = if (isSlideshowPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle Slideshow",
                                tint = if (isSlideshowPlaying) SunsetAmber else Color.White
                            )
                        }

                        // Zoom In/Out Toggle
                        IconButton(
                            onClick = {
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.2f
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (scale > 1.2f) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                                contentDescription = "Toggle Zoom",
                                tint = Color.White
                            )
                        }

                        // Favorite
                        IconButton(onClick = { onToggleFavorite(currentPhoto) }) {
                            Icon(
                                imageVector = if (currentPhoto.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (currentPhoto.isFavorite) SunsetAmber else Color.White
                            )
                        }

                        // Download / Export
                        IconButton(onClick = { onExportPhoto(currentPhoto) }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Photo",
                                tint = CyanPrimary
                            )
                        }

                        // Toggle AI & EXIF Info Drawer
                        IconButton(onClick = { showInfoDrawer = !showInfoDrawer }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Toggle Info Drawer",
                                tint = if (showInfoDrawer) SunsetAmber else Color.White
                            )
                        }

                        // Delete / Move to Trash
                        IconButton(
                            onClick = {
                                onDeletePhoto(currentPhoto)
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Photo",
                                tint = ErrorCrimson
                            )
                        }
                    }
                }

                // Interactive Zoomable Photo Canvas + Next/Prev Navigation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (showInfoDrawer) 0.48f else 0.9f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(currentPhoto.localFilePath),
                        contentDescription = currentPhoto.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentPhoto.id) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (scale > 1f) {
                                            scale = 1f
                                            offset = Offset.Zero
                                        } else {
                                            scale = 2.4f
                                        }
                                    }
                                )
                            }
                            .transformable(state = transformState)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )

                    if (photos.size > 1) {
                        IconButton(
                            onClick = {
                                isSlideshowPlaying = false
                                currentIndex = if (safeIndex - 1 < 0) photos.size - 1 else safeIndex - 1
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(8.dp)
                                .background(ObsidianBg.copy(alpha = 0.65f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Photo", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                isSlideshowPlaying = false
                                currentIndex = (safeIndex + 1) % photos.size
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(8.dp)
                                .background(ObsidianBg.copy(alpha = 0.65f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Photo", tint = Color.White)
                        }
                    }
                }

                // Bottom Trilingual AI Scene Description, Semantic Tags & EXIF Drawer
                if (showInfoDrawer) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.52f)
                            .background(ObsidianSurface)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cloud Sync & Gemini AI Re-Index Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onReAnalyzeWithGemini(currentPhoto) },
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetAmber),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lightbox_reanalyze_ai_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Re-Analyze AI (Gemini)", color = ObsidianBg, style = MaterialTheme.typography.labelLarge)
                            }

                            Button(
                                onClick = { onForceSyncOracle(currentPhoto) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentPhoto.isSyncedToOracle) EmeraldSync else CyanPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lightbox_sync_oracle_button")
                            ) {
                                Icon(
                                    imageVector = if (currentPhoto.isSyncedToOracle) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = ObsidianBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentPhoto.isSyncedToOracle) "Synced (200GB)" else "Sync to Oracle",
                                    color = ObsidianBg,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        // Trilingual AI Descriptions (Hindi Devanagari + Hinglish + English)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🇮🇳 हिंदी AI विवरण (Gemini Vision):",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SunsetAmber
                                )
                                Text(
                                    text = currentPhoto.descriptionHi,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🇮🇳 Hinglish Scene Summary:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyanPrimary
                                )
                                Text(
                                    text = currentPhoto.descriptionHinglish,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFE2E8F0)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🌐 English Visual Description:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EmeraldSync
                                )
                                Text(
                                    text = currentPhoto.descriptionEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        // OCR Text if detected
                        if (currentPhoto.ocrText.isNotBlank()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "📄 Extracted OCR Text Inside Image:",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CyanPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentPhoto.ocrText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Interactive Semantic Tags (Tap any tag to search)
                        Text(
                            text = "AI Semantic Tags (Tap any tag to filter timeline):",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currentPhoto.semanticTags.forEach { tag ->
                                AssistChip(
                                    onClick = {
                                        onTagClick(tag)
                                        onDismiss()
                                    },
                                    label = { Text("#$tag", style = MaterialTheme.typography.labelMedium) }
                                )
                            }
                        }

                        // EXIF & Storage Telemetry Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ObsidianGlassBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "EXIF Metadata & Dual-Storage Telemetry",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyanPrimary
                                )
                                Text(
                                    text = "Camera / Lens : ${currentPhoto.deviceModel} (${currentPhoto.iso} • ${currentPhoto.shutterSpeed})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Resolution    : ${currentPhoto.width} × ${currentPhoto.height} px • ${currentPhoto.formattedSize}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Location      : ${currentPhoto.locationLabel} • Album: ${currentPhoto.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Oracle Object : ${currentPhoto.oracleRemoteId ?: "Local Indexed Storage (Ready to Sync)"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentPhoto.isSyncedToOracle) EmeraldSync else SunsetAmber
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
