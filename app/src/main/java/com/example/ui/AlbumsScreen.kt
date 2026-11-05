package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSync
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.NeuralViolet
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianGlassBorder
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextSecondary
import java.io.File

private data class SmartAlbumCardData(
    val key: String,
    val title: String,
    val hindiSubtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val photos: List<PhotoEntity>
)

@Composable
fun AlbumsScreen(
    activePhotos: List<PhotoEntity>,
    trashedPhotos: List<PhotoEntity>,
    onOpenAlbumCategory: (String) -> Unit,
    onRestorePhoto: (PhotoEntity) -> Unit,
    onDeletePermanently: (PhotoEntity) -> Unit,
    onEmptyTrash: () -> Unit
) {
    val smartAlbums = listOf(
        SmartAlbumCardData(
            key = "Nature & Sunsets",
            title = "Nature & Sunsets",
            hindiSubtitle = "प्रकृति और डूबता हुआ सूरज",
            icon = Icons.Default.WbSunny,
            accentColor = SunsetAmber,
            photos = activePhotos.filter { it.category == "Nature & Sunsets" }
        ),
        SmartAlbumCardData(
            key = "Documents & Screenshots",
            title = "Documents & Receipts",
            hindiSubtitle = "मेरा डॉक्यूमेंट और रसीद (OCR)",
            icon = Icons.Default.Description,
            accentColor = CyanPrimary,
            photos = activePhotos.filter { it.category == "Documents & Screenshots" }
        ),
        SmartAlbumCardData(
            key = "Night & Urban",
            title = "Night City & Cyber",
            hindiSubtitle = "रात का शहर और साइबर वर्कस्पेस",
            icon = Icons.Default.LocationCity,
            accentColor = NeuralViolet,
            photos = activePhotos.filter { it.category == "Night & Urban" }
        ),
        SmartAlbumCardData(
            key = "People & Portraits",
            title = "People & Live Camera",
            hindiSubtitle = "लोग और लाइव कैमरा शॉट्स",
            icon = Icons.Default.People,
            accentColor = EmeraldSync,
            photos = activePhotos.filter { it.category == "People & Portraits" }
        ),
        SmartAlbumCardData(
            key = "Favorites",
            title = "Favorites ★",
            hindiSubtitle = "पसंदीदा फोटोज़",
            icon = Icons.Default.Favorite,
            accentColor = SunsetAmber,
            photos = activePhotos.filter { it.isFavorite }
        ),
        SmartAlbumCardData(
            key = "Synced",
            title = "Oracle 200GB Cloud Synced",
            hindiSubtitle = "ओरेकल क्लाउड पर सुरक्षित",
            icon = Icons.Default.CloudDone,
            accentColor = EmeraldSync,
            photos = activePhotos.filter { it.isSyncedToOracle }
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("smart_albums_screen"),
        contentPadding = PaddingValues(14.dp, 10.dp, 14.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "AI Smart Albums & Memories (स्मार्ट एल्बम)",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "Automatically categorized by Gemini 3.8 Flash Vision across Hindi & English concepts",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        val rows = smartAlbums.chunked(2)
        items(rows) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { album ->
                    SmartAlbumTile(
                        album = album,
                        onClick = { onOpenAlbumCategory(album.key) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Trash / Bin Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trash / Bin (ट्रैश बिन • ${trashedPhotos.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Restore deleted photos or permanently purge from local storage",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (trashedPhotos.isNotEmpty()) {
                    Button(
                        onClick = onEmptyTrash,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorCrimson),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_trash_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Empty Bin")
                    }
                }
            }
        }

        if (trashedPhotos.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Trash / Bin is clean. Deleted photos appear here so you can restore them anytime.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(trashedPhotos, key = { "trash_${it.id}" }) { trashed ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AsyncImage(
                                model = File(trashed.localFilePath),
                                contentDescription = trashed.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trashed.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = trashed.formattedSize,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { onRestorePhoto(trashed) }) {
                                Icon(
                                    Icons.Default.RestoreFromTrash,
                                    contentDescription = "Restore Photo",
                                    tint = EmeraldSync
                                )
                            }
                            IconButton(onClick = { onDeletePermanently(trashed) }) {
                                Icon(
                                    Icons.Default.DeleteForever,
                                    contentDescription = "Delete Permanently",
                                    tint = ErrorCrimson
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
private fun SmartAlbumTile(
    album: SmartAlbumCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coverPhoto = album.photos.firstOrNull()

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, ObsidianGlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("album_tile_${album.key}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.15f)
        ) {
            if (coverPhoto != null) {
                AsyncImage(
                    model = File(coverPhoto.localFilePath),
                    contentDescription = album.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianCard)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianBg.copy(alpha = 0.25f),
                                ObsidianBg.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            Icon(
                imageVector = album.icon,
                contentDescription = null,
                tint = album.accentColor,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(26.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "${album.photos.size} Photos",
                    style = MaterialTheme.typography.labelSmall,
                    color = album.accentColor
                )
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = album.hindiSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
