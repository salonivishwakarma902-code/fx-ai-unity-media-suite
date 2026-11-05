package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.OracleTelemetry
import com.example.data.PhotoEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSync
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.NeuralViolet
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianGlassBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextSecondary
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotosTimelineScreen(
    photos: List<PhotoEntity>,
    allPhotosCount: Int,
    searchQuery: String,
    expandedTerms: List<String>,
    selectedCategory: String,
    selectedPhotoIds: Set<String>,
    telemetry: OracleTelemetry,
    onSearchQueryChange: (String) -> Unit,
    onTriggerAiExpand: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onPhotoClick: (PhotoEntity) -> Unit,
    onToggleSelectPhoto: (String) -> Unit,
    onSelectAllVisible: () -> Unit,
    onClearSelection: () -> Unit,
    onBatchFavorite: () -> Unit,
    onBatchSyncOracle: () -> Unit,
    onBatchExport: () -> Unit,
    onBatchTrash: () -> Unit,
    onToggleFavoriteSingle: (PhotoEntity) -> Unit,
    onOpenCamera: () -> Unit,
    onOpenPhotoPicker: () -> Unit
) {
    val groupedByDate = photos.groupBy { it.dateGroupLabel }

    val quickQueries = listOf(
        "डूबता हुआ सूरज" to "🌅 डूबता हुआ सूरज",
        "doobta hua suraj" to "🌇 doobta hua suraj",
        "sunset" to "☀️ sunset",
        "नीला आसमान" to "🏔️ नीला आसमान",
        "मेरा डॉक्यूमेंट" to "📄 मेरा डॉक्यूमेंट",
        "रात का शहर" to "🌃 रात का शहर",
        "happy moment" to "✨ happy moment"
    )

    val categories = listOf(
        "All",
        "Nature & Sunsets",
        "Documents & Screenshots",
        "Night & Urban",
        "Favorites",
        "Synced"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Multi-Select Batch Action Toolbar
        AnimatedVisibility(visible = selectedPhotoIds.isNotEmpty()) {
            Surface(
                color = ObsidianCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .border(1.dp, CyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("batch_action_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClearSelection) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Selection", tint = Color.White)
                        }
                        Text(
                            text = "${selectedPhotoIds.size} Selected",
                            style = MaterialTheme.typography.titleMedium,
                            color = CyanPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = onSelectAllVisible, modifier = Modifier.testTag("batch_select_all_button")) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select All", tint = Color.White)
                        }
                        IconButton(onClick = onBatchFavorite, modifier = Modifier.testTag("batch_favorite_button")) {
                            Icon(Icons.Default.Favorite, contentDescription = "Favorite Selected", tint = SunsetAmber)
                        }
                        IconButton(onClick = onBatchSyncOracle, modifier = Modifier.testTag("batch_sync_oracle_button")) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Force Sync to Oracle", tint = EmeraldSync)
                        }
                        IconButton(onClick = onBatchExport, modifier = Modifier.testTag("batch_download_button")) {
                            Icon(Icons.Default.Download, contentDescription = "Save to Device", tint = CyanPrimary)
                        }
                        IconButton(onClick = onBatchTrash, modifier = Modifier.testTag("batch_delete_button")) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Move to Bin", tint = ErrorCrimson)
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("photos_timeline_list"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Multilingual Semantic Search Bar + Quick Hindi/Hinglish/English Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search \"डूबता हुआ सूरज\", \"doobta hua suraj\", \"मेरा डॉक्यूमेंट\"…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = CyanPrimary
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = onTriggerAiExpand,
                                        modifier = Modifier.testTag("ai_semantic_expand_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Expand with Gemini AI",
                                            tint = SunsetAmber
                                        )
                                    }
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                        modifier = Modifier.testTag("clear_search_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Search",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ObsidianCard,
                            unfocusedContainerColor = ObsidianSurface,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = ObsidianGlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("semantic_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Multilingual One-Tap Search Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickQueries.forEach { (queryValue, chipLabel) ->
                            val isSelected = searchQuery.equals(queryValue, ignoreCase = true)
                            AssistChip(
                                onClick = {
                                    if (isSelected) onSearchQueryChange("") else onSearchQueryChange(queryValue)
                                },
                                label = {
                                    Text(
                                        text = chipLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) ObsidianBg else Color.White
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) SunsetAmber else ObsidianCard
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = if (isSelected) SunsetAmber else ObsidianGlassBorder
                                ),
                                modifier = Modifier.testTag("search_chip_${queryValue.hashCode()}")
                            )
                        }
                    }

                    // Show Expanded Semantic Synset Pills when searching
                    if (searchQuery.isNotBlank() && expandedTerms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = NeuralViolet.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Multilingual AI Semantic Match (${photos.size} found)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = SunsetAmber
                                    )
                                    Text(
                                        text = "Matched concepts: ${expandedTerms.take(9).joinToString(" • ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE2E8F0),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                AssistChip(
                                    onClick = onTriggerAiExpand,
                                    label = { Text("AI Deep Expand", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = SunsetAmber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Dual-Storage Status & Quick Capture/Upload Strip
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (telemetry.isOnline) EmeraldSync else SunsetAmber)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = telemetry.modeLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Oracle Free: ${String.format("%.1f", telemetry.freeStorageGb)} GB / 200 GB • $allPhotosCount Photos Indexed",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onOpenCamera,
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetAmber),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("timeline_open_camera_button")
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    tint = ObsidianBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera", color = ObsidianBg, style = MaterialTheme.typography.labelLarge)
                            }

                            Button(
                                onClick = onOpenPhotoPicker,
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("timeline_upload_photos_button")
                            ) {
                                Icon(
                                    Icons.Default.UploadFile,
                                    contentDescription = "Upload",
                                    tint = ObsidianBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload", color = ObsidianBg, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // 3. Memories & Smart Category Filter Chips (when search query is empty)
            if (searchQuery.isBlank() && photos.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        Text(
                            text = "AI Memories & Highlights (यादगार पल)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(photos.take(4), key = { "mem_${it.id}" }) { memoryPhoto ->
                                MemoryHighlightCard(
                                    photo = memoryPhoto,
                                    onClick = { onPhotoClick(memoryPhoto) }
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Strip
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onSelectCategory(cat) },
                            label = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.22f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                    }
                }
            }

            // 4. Grouped Timeline Photo Grid (2-column responsive layout inside LazyColumn)
            if (photos.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "No matching photos found for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Try searching \"डूबता हुआ सूरज\", \"doobta hua suraj\", \"नीला आसमान\", \"मेरा डॉक्यूमेंट\", or tap Clear.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Button(
                            onClick = {
                                onSearchQueryChange("")
                                onSelectCategory("All")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Reset Search & Filters", color = ObsidianBg)
                        }
                    }
                }
            } else {
                groupedByDate.forEach { (dateLabel, groupPhotos) ->
                    item(key = "header_$dateLabel") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "${groupPhotos.size} item(s) • Long-press to multi-select",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    val chunkedRows = groupPhotos.chunked(2)
                    items(chunkedRows, key = { row -> "row_${dateLabel}_${row.first().id}" }) { rowPhotos ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPhotos.forEach { photo ->
                                PhotoTimelineGridCard(
                                    photo = photo,
                                    isSelected = selectedPhotoIds.contains(photo.id),
                                    inSelectionMode = selectedPhotoIds.isNotEmpty(),
                                    onClick = {
                                        if (selectedPhotoIds.isNotEmpty()) {
                                            onToggleSelectPhoto(photo.id)
                                        } else {
                                            onPhotoClick(photo)
                                        }
                                    },
                                    onLongClick = { onToggleSelectPhoto(photo.id) },
                                    onToggleFavorite = { onToggleFavoriteSingle(photo) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowPhotos.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryHighlightCard(
    photo: PhotoEntity,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(210.dp)
            .height(128.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, ObsidianGlassBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = File(photo.localFilePath),
            contentDescription = photo.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, ObsidianBg.copy(alpha = 0.88f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = photo.title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = photo.descriptionHi,
                style = MaterialTheme.typography.bodySmall,
                color = SunsetAmber,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoTimelineGridCard(
    photo: PhotoEntity,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) CyanPrimary else ObsidianGlassBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("photo_card_${photo.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            ) {
                AsyncImage(
                    model = File(photo.localFilePath),
                    contentDescription = photo.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-left Storage Badge (Synced to Oracle 200GB vs Local)
                Surface(
                    color = ObsidianBg.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (photo.isSyncedToOracle) Icons.Default.CloudDone else Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (photo.isSyncedToOracle) EmeraldSync else CyanPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (photo.isSyncedToOracle) "Oracle 200GB" else "Local DB",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (photo.isSyncedToOracle) EmeraldSync else CyanPrimary
                        )
                    }
                }

                // Top-right Favorite or Multi-select indicator
                IconButton(
                    onClick = {
                        if (inSelectionMode) onLongClick() else onToggleFavorite()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = CyanPrimary
                        )
                    } else {
                        Icon(
                            imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (photo.isFavorite) SunsetAmber else Color.White
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = photo.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = photo.descriptionHi,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = photo.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = SunsetAmber
                    )
                    Text(
                        text = photo.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
