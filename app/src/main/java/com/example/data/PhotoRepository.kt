package com.example.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.ai.PhotoAiAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PhotoRepository(
    private val context: Context,
    private val photoDao: PhotoDao
) {
    val activePhotos: Flow<List<PhotoEntity>> = photoDao.getActivePhotos()
    val trashedPhotos: Flow<List<PhotoEntity>> = photoDao.getTrashedPhotos()

    suspend fun initializeDemoPhotosIfEmpty() = withContext(Dispatchers.IO) {
        if (photoDao.getTotalPhotoCount() == 0) {
            val demoList = DemoPhotoSeeder.ensureDemoPhotosSeeded(context)
            photoDao.insertPhotos(demoList)
        }
    }

    suspend fun saveBitmapAsPhoto(
        bitmap: Bitmap,
        sourceDeviceLabel: String,
        aiAnalysis: PhotoAiAnalysis?
    ): PhotoEntity = withContext(Dispatchers.IO) {
        val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
        val id = "photo_${UUID.randomUUID().toString().take(8)}"
        val fileName = "${id}_${System.currentTimeMillis()}.jpg"
        val destFile = File(photosDir, fileName)

        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        val now = System.currentTimeMillis()
        val analysis = aiAnalysis ?: defaultAnalysisForCapture(sourceDeviceLabel)

        val entity = PhotoEntity(
            id = id,
            title = analysis.title,
            localFilePath = destFile.absolutePath,
            timestamp = now,
            dateGroupLabel = "Today",
            width = bitmap.width,
            height = bitmap.height,
            fileSizeBytes = destFile.length().coerceAtLeast(120_000L),
            deviceModel = sourceDeviceLabel,
            iso = "ISO 100",
            shutterSpeed = "1/250s",
            locationLabel = "Local Capture • FX Cloud",
            descriptionEn = analysis.descriptionEn,
            descriptionHi = analysis.descriptionHi,
            descriptionHinglish = analysis.descriptionHinglish,
            semanticTagsCsv = analysis.semanticTags.joinToString(","),
            ocrText = analysis.ocrText,
            category = analysis.category,
            dominantColorsCsv = analysis.dominantColors.joinToString(","),
            isFavorite = false,
            isTrashed = false,
            isSyncedToOracle = false,
            oracleRemoteId = null,
            isAiIndexed = aiAnalysis != null
        )
        photoDao.insertPhoto(entity)
        entity
    }

    suspend fun importUriPhoto(uri: Uri): Pair<PhotoEntity, Bitmap?> = withContext(Dispatchers.IO) {
        val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
        val id = "upload_${UUID.randomUUID().toString().take(8)}"
        val destFile = File(photosDir, "${id}.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        val bitmap = BitmapFactory.decodeFile(destFile.absolutePath)
        val width = bitmap?.width ?: 1200
        val height = bitmap?.height ?: 900
        val now = System.currentTimeMillis()

        val initialEntity = PhotoEntity(
            id = id,
            title = "Uploaded Photo (${SimpleDateFormat("HH:mm", Locale.US).format(Date(now))})",
            localFilePath = destFile.absolutePath,
            timestamp = now,
            dateGroupLabel = "Today",
            width = width,
            height = height,
            fileSizeBytes = destFile.length().coerceAtLeast(150_000L),
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            iso = "ISO 125",
            shutterSpeed = "1/180s",
            locationLabel = "Imported from Device",
            descriptionEn = "Analyzing with Gemini Vision…",
            descriptionHi = "जेमिनी विज़न द्वारा फोटो का विश्लेषण किया जा रहा है…",
            descriptionHinglish = "Gemini Vision photo ko analyze kar raha hai…",
            semanticTagsCsv = "uploaded,photo,local",
            ocrText = "",
            category = "Nature & Sunsets",
            dominantColorsCsv = "#38BDF8,#A855F7,#0F172A",
            isFavorite = false,
            isTrashed = false,
            isSyncedToOracle = false,
            oracleRemoteId = null,
            isAiIndexed = false
        )
        photoDao.insertPhoto(initialEntity)
        initialEntity to bitmap
    }

    suspend fun updateWithAiAnalysis(photoId: String, analysis: PhotoAiAnalysis) = withContext(Dispatchers.IO) {
        val current = photoDao.getPhotoById(photoId) ?: return@withContext
        val updated = current.copy(
            title = analysis.title,
            descriptionEn = analysis.descriptionEn,
            descriptionHi = analysis.descriptionHi,
            descriptionHinglish = analysis.descriptionHinglish,
            semanticTagsCsv = analysis.semanticTags.joinToString(","),
            ocrText = analysis.ocrText,
            category = analysis.category,
            dominantColorsCsv = analysis.dominantColors.joinToString(","),
            isAiIndexed = true
        )
        photoDao.updatePhoto(updated)
    }

    suspend fun toggleFavorite(photo: PhotoEntity) = withContext(Dispatchers.IO) {
        photoDao.setFavorite(photo.id, !photo.isFavorite)
    }

    suspend fun setFavoritesBatch(ids: Set<String>, favorite: Boolean) = withContext(Dispatchers.IO) {
        ids.forEach { id -> photoDao.setFavorite(id, favorite) }
    }

    suspend fun moveToTrashBatch(ids: Set<String>) = withContext(Dispatchers.IO) {
        ids.forEach { id -> photoDao.setTrashed(id, true) }
    }

    suspend fun restoreFromTrash(id: String) = withContext(Dispatchers.IO) {
        photoDao.setTrashed(id, false)
    }

    suspend fun deletePermanently(id: String) = withContext(Dispatchers.IO) {
        val photo = photoDao.getPhotoById(id)
        if (photo != null && !photo.id.startsWith("demo_")) {
            runCatching { File(photo.localFilePath).delete() }
        }
        photoDao.deletePermanently(id)
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        photoDao.emptyTrash()
    }

    suspend fun markSyncedToOracle(id: String, remoteId: String) = withContext(Dispatchers.IO) {
        photoDao.setSynced(id, true, remoteId)
    }

    suspend fun exportPhotoToDeviceGallery(photo: PhotoEntity): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val sourceFile = File(photo.localFilePath)
            if (!sourceFile.exists()) return@withContext false

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "FX_AI_${photo.id}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FX_AI_Unity")
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext false

            resolver.openOutputStream(uri)?.use { out ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun defaultAnalysisForCapture(device: String): PhotoAiAnalysis {
        return PhotoAiAnalysis(
            title = "Live Camera Capture (${SimpleDateFormat("HH:mm", Locale.US).format(Date())})",
            descriptionEn = "Captured live via $device and saved to FX AI Unity Dual-Storage timeline.",
            descriptionHi = "लाइव कैमरे द्वारा खींची गई तस्वीर, FX AI Unity टाइमलाइन में सुरक्षित।",
            descriptionHinglish = "Live camera se capture ki gayi photo, FX AI Unity timeline mein saved.",
            semanticTags = listOf("camera", "live capture", "कैमरा", "photo", "today"),
            ocrText = "",
            category = "People & Portraits",
            dominantColors = listOf("#38BDF8", "#F59E0B", "#10B981")
        )
    }
}
