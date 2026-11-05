package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val localFilePath: String,
    val timestamp: Long,
    val dateGroupLabel: String,
    val width: Int,
    val height: Int,
    val fileSizeBytes: Long,
    val deviceModel: String,
    val iso: String,
    val shutterSpeed: String,
    val locationLabel: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val descriptionHinglish: String,
    val semanticTagsCsv: String,
    val ocrText: String,
    val category: String,
    val dominantColorsCsv: String,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false,
    val isSyncedToOracle: Boolean = false,
    val oracleRemoteId: String? = null,
    val isAiIndexed: Boolean = true
) {
    val semanticTags: List<String>
        get() = semanticTagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val dominantColors: List<String>
        get() = dominantColorsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val formattedSize: String
        get() {
            val kb = fileSizeBytes / 1024.0
            return if (kb >= 1024.0) {
                String.format("%.2f MB", kb / 1024.0)
            } else {
                String.format("%.0f KB", kb.coerceAtLeast(1.0))
            }
        }
}
