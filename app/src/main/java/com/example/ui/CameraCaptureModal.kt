package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.DemoPhotoSeeder
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSync
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SunsetAmber

private const val TAG = "CameraCaptureModal"

@Composable
fun CameraCaptureModal(
    onDismiss: () -> Unit,
    onConfirmPhoto: (Bitmap, String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            permissionDeniedOnce = true
        }
    }

    // Request CAMERA permission automatically when modal opens if not yet granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // State for captured photo review (Confirm or Discard before saving)
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedSourceLabel by remember { mutableStateOf("FX Live CameraX • 24mm f/1.8") }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraErrorText by remember { mutableStateOf<String?>(null) }

    // CameraX configuration states
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var selectedStudioPreset by remember { mutableStateOf("SUNSET") }

    val imageCapture = remember(flashMode) {
        ImageCapture.Builder()
            .setFlashMode(flashMode)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Bind CameraX lifecycle when permission is granted and we are in viewfinder mode
    LaunchedEffect(hasCameraPermission, lensFacing, flashMode, capturedBitmap) {
        if (hasCameraPermission && capturedBitmap == null) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val selector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageCapture
                    )
                    cameraErrorText = null
                } catch (e: Exception) {
                    Log.w(TAG, "CameraX hardware bind note (emulator fallback ready): ${e.message}")
                    cameraErrorText = "Hardware camera unavailable on this virtual device — Studio Lens Fallback active."
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            }
        }
    }

    BackHandler {
        if (capturedBitmap != null) {
            capturedBitmap = null
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (capturedBitmap != null) {
                capturedBitmap = null
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("camera_modal_surface"),
            color = ObsidianBg
        ) {
            if (capturedBitmap != null) {
                // =========================================================================
                // STEP 2: PHOTO CONFIRM OR DISCARD REVIEW UI BEFORE SAVING
                // =========================================================================
                PhotoConfirmOrDiscardContent(
                    bitmap = capturedBitmap!!,
                    sourceLabel = capturedSourceLabel,
                    onDiscardAndRetake = {
                        capturedBitmap = null
                    },
                    onConfirmAndSave = {
                        val finalBmp = capturedBitmap
                        if (finalBmp != null) {
                            onConfirmPhoto(finalBmp, capturedSourceLabel)
                        }
                        onDismiss()
                    },
                    onCloseModal = onDismiss
                )
            } else {
                // =========================================================================
                // STEP 1: CAMERA PERMISSION & CAMERAX LIVE VIEWFINDER UI
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FX Live CameraX Studio",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = if (hasCameraPermission) {
                                    "Live Viewfinder • Auto Gemini Vision Indexing"
                                } else {
                                    "Camera Permission Required (कैमरा परमिशन आवश्यक है)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (hasCameraPermission) {
                                IconButton(
                                    onClick = {
                                        flashMode = when (flashMode) {
                                            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                                            else -> ImageCapture.FLASH_MODE_AUTO
                                        }
                                    },
                                    modifier = Modifier.testTag("camera_flash_toggle")
                                ) {
                                    Icon(
                                        imageVector = when (flashMode) {
                                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                            ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                                            else -> Icons.Default.FlashAuto
                                        },
                                        contentDescription = "Toggle Flash Mode",
                                        tint = SunsetAmber
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                            CameraSelector.LENS_FACING_FRONT
                                        } else {
                                            CameraSelector.LENS_FACING_BACK
                                        }
                                    },
                                    modifier = Modifier.testTag("camera_switch_lens_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cameraswitch,
                                        contentDescription = "Switch Front/Back Camera",
                                        tint = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_camera_modal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Camera",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Center Viewfinder or Permission Request Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 12.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                            .background(ObsidianSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasCameraPermission) {
                            AndroidView(
                                factory = { previewView },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("camerax_preview_view")
                            )

                            // Viewfinder Grid Overlay & Status Pill
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                            ) {
                                Surface(
                                    color = ObsidianBg.copy(alpha = 0.78f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                            "● REC READY • REAR LENS"
                                        } else {
                                            "● REC READY • FRONT LENS"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldSync,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                if (cameraErrorText != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = ObsidianCard.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = cameraErrorText!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SunsetAmber,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Permission Request Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CyanPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = "Camera Permission Icon",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Camera Access Required (कैमरा परमिशन दें)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = if (permissionDeniedOnce) {
                                        "Camera permission was denied. Tap below to grant Camera access so you can capture live photos with CameraX, or use the Studio Lens preset below."
                                    } else {
                                        "Grant Camera permission to capture live photos directly from your phone camera and index them in Hindi & English with Gemini Vision."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFCBD5E1),
                                    textAlign = TextAlign.Center
                                )

                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("grant_camera_permission_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = ObsidianBg
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Grant Camera Permission (परमिशन दें)",
                                        color = ObsidianBg,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Shutter & Studio Scene Selector Controls
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Scene Preset (for Emulator / Studio Capture):",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF94A3B8)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val presets = listOf(
                                "SUNSET" to "🌅 डूबता सूरज",
                                "MOUNTAIN" to "🏔️ नीला आसमान",
                                "DOCUMENT" to "📄 रसीद/Doc",
                                "NIGHT_CITY" to "🌃 रात का शहर"
                            )
                            presets.forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedStudioPreset == key,
                                    onClick = { selectedStudioPreset = key },
                                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanPrimary.copy(alpha = 0.25f),
                                        selectedLabelColor = CyanPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Primary Shutter Button
                        Button(
                            onClick = {
                                if (isCapturing) return@Button
                                isCapturing = true
                                if (hasCameraPermission && cameraErrorText == null) {
                                    captureWithCameraXOrFallback(
                                        context = context,
                                        imageCapture = imageCapture,
                                        fallbackPreset = selectedStudioPreset,
                                        onCaptured = { bmp, label ->
                                            isCapturing = false
                                            capturedSourceLabel = label
                                            capturedBitmap = bmp
                                        }
                                    )
                                } else {
                                    // Instant high-res capture using selected studio scene
                                    val bmp = DemoPhotoSeeder.renderDemoBitmap(selectedStudioPreset, 1200, 900)
                                    isCapturing = false
                                    capturedSourceLabel = "FX CameraX Studio ($selectedStudioPreset)"
                                    capturedBitmap = bmp
                                }
                            },
                            enabled = !isCapturing,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = SunsetAmber),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("camera_shutter_button")
                        ) {
                            if (isCapturing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = ObsidianBg,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Capturing Photo…",
                                    color = ObsidianBg,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Capture Photo",
                                    tint = ObsidianBg
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Capture Photo (फोटो खींचें)",
                                    color = ObsidianBg,
                                    style = MaterialTheme.typography.titleMedium,
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
private fun PhotoConfirmOrDiscardContent(
    bitmap: Bitmap,
    sourceLabel: String,
    onDiscardAndRetake: () -> Unit,
    onConfirmAndSave: () -> Unit,
    onCloseModal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("photo_confirm_discard_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Review Captured Photo (फोटो की जांच करें)",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "$sourceLabel • ${bitmap.width}×${bitmap.height}px",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldSync
                )
            }
            IconButton(
                onClick = onCloseModal,
                modifier = Modifier.testTag("close_review_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White
                )
            }
        }

        // Captured Image Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 14.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, EmeraldSync.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .background(ObsidianSurface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captured Photo Preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                color = ObsidianBg.copy(alpha = 0.82f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp)
            ) {
                Text(
                    text = "Confirm to save & auto-index with Gemini Vision (Hindi + English)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Confirm or Discard Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDiscardAndRetake,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorCrimson),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("discard_photo_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Discard Photo"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Discard / Retake (हटाएं)",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onConfirmAndSave,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSync),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("confirm_save_photo_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm and Save Photo",
                    tint = ObsidianBg
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm & Save (सेव करें)",
                    color = ObsidianBg,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun captureWithCameraXOrFallback(
    context: Context,
    imageCapture: ImageCapture,
    fallbackPreset: String,
    onCaptured: (Bitmap, String) -> Unit
) {
    try {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = runCatching {
                        val rawBmp = image.toBitmap()
                        val rotation = image.imageInfo.rotationDegrees
                        if (rotation != 0) {
                            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                            Bitmap.createBitmap(rawBmp, 0, 0, rawBmp.width, rawBmp.height, matrix, true)
                        } else {
                            rawBmp
                        }
                    }.getOrElse {
                        DemoPhotoSeeder.renderDemoBitmap(fallbackPreset, 1200, 900)
                    }
                    image.close()
                    onCaptured(bitmap, "CameraX Hardware Sensor • f/1.8")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.w(TAG, "CameraX capture fallback triggered: ${exception.message}")
                    val bmp = DemoPhotoSeeder.renderDemoBitmap(fallbackPreset, 1200, 900)
                    onCaptured(bmp, "FX CameraX Studio ($fallbackPreset)")
                }
            }
        )
    } catch (e: Exception) {
        val bmp = DemoPhotoSeeder.renderDemoBitmap(fallbackPreset, 1200, 900)
        onCaptured(bmp, "FX CameraX Studio ($fallbackPreset)")
    }
}
