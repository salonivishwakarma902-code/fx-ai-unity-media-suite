package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AlbumsScreen
import com.example.ui.CameraCaptureModal
import com.example.ui.OpenSourceReadmeDialog
import com.example.ui.OracleDashboardScreen
import com.example.ui.OracleSetupGuideScreen
import com.example.ui.PhotoLightboxDialog
import com.example.ui.PhotosTimelineScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSync
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianGlassBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AiStatusBanner
import com.example.viewmodel.MediaSuiteViewModel

enum class AppDestination(val route: String, val label: String, val icon: ImageVector) {
    PHOTOS("photos", "Photos", Icons.Default.PhotoLibrary),
    ALBUMS("albums", "Albums", Icons.Default.Collections),
    ORACLE("oracle", "Oracle 200GB", Icons.Default.CloudQueue),
    GUIDE("guide", "Setup Guide", Icons.Default.MenuBook)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CRITICAL: Configure Firebase App Check with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FxAiUnityApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FxAiUnityApp(viewModel: MediaSuiteViewModel = viewModel()) {
    val activePhotos by viewModel.activePhotos.collectAsStateWithLifecycle()
    val filteredPhotos by viewModel.filteredPhotos.collectAsStateWithLifecycle()
    val trashedPhotos by viewModel.trashedPhotos.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val expandedTerms by viewModel.aiExpandedTerms.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPhotoIds by viewModel.selectedPhotoIds.collectAsStateWithLifecycle()
    val serverConfig by viewModel.serverConfig.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
    val aiStatus by viewModel.aiStatus.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(AppDestination.PHOTOS) }
    var showCameraModal by rememberSaveable { mutableStateOf(false) }
    var lightboxPhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    var showReadmeDialog by rememberSaveable { mutableStateOf(false) }

    // Zero-permission Android Photo Picker for multi-photo import
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importPhotosFromPicker(uris)
        }
    }

    // Handle system Back navigation on secondary tabs
    if (currentTab != AppDestination.PHOTOS) {
        BackHandler {
            currentTab = AppDestination.PHOTOS
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianSurface,
                    titleContentColor = Color.White
                ),
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FX AI Unity",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = CyanPrimary.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "200GB FREE CLOUD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Lite AI Media Suite • Gemini Vision Hindi/EN",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCameraModal = true },
                        modifier = Modifier.testTag("top_bar_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Open Live Camera",
                            tint = SunsetAmber
                        )
                    }
                    IconButton(
                        onClick = {
                            multiplePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("top_bar_upload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Upload Photos",
                            tint = CyanPrimary
                        )
                    }
                    IconButton(
                        onClick = { showReadmeDialog = true },
                        modifier = Modifier.testTag("top_bar_readme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Open Architecture README",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentTab == AppDestination.PHOTOS) {
                FloatingActionButton(
                    onClick = { showCameraModal = true },
                    containerColor = SunsetAmber,
                    contentColor = ObsidianBg,
                    modifier = Modifier.testTag("fab_live_camera")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture Live Photo"
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianSurface,
                tonalElevation = 8.dp
            ) {
                AppDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = currentTab == dest,
                        onClick = { currentTab = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.label
                            )
                        },
                        label = {
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianBg,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${dest.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Real-time AI & Cloud Status Banner
            AnimatedVisibility(visible = aiStatus !is AiStatusBanner.Idle) {
                AiStatusBannerBar(
                    status = aiStatus,
                    onDismiss = viewModel::dismissAiBanner
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (currentTab) {
                    AppDestination.PHOTOS -> {
                        PhotosTimelineScreen(
                            photos = filteredPhotos,
                            allPhotosCount = activePhotos.size,
                            searchQuery = searchQuery,
                            expandedTerms = expandedTerms,
                            selectedCategory = selectedCategory,
                            selectedPhotoIds = selectedPhotoIds,
                            telemetry = telemetry,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onTriggerAiExpand = viewModel::triggerGeminiQueryExpansion,
                            onSelectCategory = viewModel::selectCategory,
                            onPhotoClick = { photo -> lightboxPhotoId = photo.id },
                            onToggleSelectPhoto = viewModel::togglePhotoSelection,
                            onSelectAllVisible = viewModel::selectAllVisible,
                            onClearSelection = viewModel::clearSelection,
                            onBatchFavorite = viewModel::batchFavoriteSelected,
                            onBatchSyncOracle = viewModel::batchForceSyncSelectedToOracle,
                            onBatchExport = viewModel::batchExportSelectedToGallery,
                            onBatchTrash = viewModel::batchMoveSelectedToTrash,
                            onToggleFavoriteSingle = viewModel::toggleFavorite,
                            onOpenCamera = { showCameraModal = true },
                            onOpenPhotoPicker = {
                                multiplePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }

                    AppDestination.ALBUMS -> {
                        AlbumsScreen(
                            activePhotos = activePhotos,
                            trashedPhotos = trashedPhotos,
                            onOpenAlbumCategory = { categoryKey ->
                                viewModel.selectCategory(categoryKey)
                                currentTab = AppDestination.PHOTOS
                            },
                            onRestorePhoto = viewModel::restoreFromTrash,
                            onDeletePermanently = viewModel::deletePermanently,
                            onEmptyTrash = viewModel::emptyTrash
                        )
                    }

                    AppDestination.ORACLE -> {
                        OracleDashboardScreen(
                            telemetry = telemetry,
                            serverConfig = serverConfig,
                            terminalLogs = terminalLogs,
                            isSyncing = isSyncing,
                            onTestConnection = { viewModel.refreshOracleTelemetry(silent = false) },
                            onTriggerFullSync = viewModel::triggerFullOracleSync,
                            onClearServerCache = viewModel::clearOracleServerCache,
                            onRestartDaemon = viewModel::restartOracleMediaDaemon,
                            onToggleAutoBackup = viewModel::toggleAutoBackupWifi,
                            onDownloadScript = viewModel::exportScriptFileToDownloads
                        )
                    }

                    AppDestination.GUIDE -> {
                        OracleSetupGuideScreen(
                            serverConfig = serverConfig,
                            onSelectLanguage = viewModel::setGuideLanguage,
                            onVerifyAndConnect = viewModel::verifyAndConnectOracleServer
                        )
                    }
                }
            }
        }
    }

    // CameraX Live Viewfinder + Confirm/Discard Modal
    if (showCameraModal) {
        CameraCaptureModal(
            onDismiss = { showCameraModal = false },
            onConfirmPhoto = { bitmap, sourceLabel ->
                viewModel.confirmAndSaveCapturedPhoto(bitmap, sourceLabel)
            }
        )
    }

    // Full-Screen Lightbox Viewer
    if (lightboxPhotoId != null) {
        val listForLightbox = if (filteredPhotos.isNotEmpty()) filteredPhotos else activePhotos
        PhotoLightboxDialog(
            photos = listForLightbox,
            initialPhotoId = lightboxPhotoId!!,
            onDismiss = { lightboxPhotoId = null },
            onToggleFavorite = viewModel::toggleFavorite,
            onForceSyncOracle = viewModel::forceSyncSinglePhoto,
            onExportPhoto = viewModel::exportSinglePhoto,
            onReAnalyzeWithGemini = viewModel::reAnalyzePhotoWithGemini,
            onDeletePhoto = viewModel::moveToTrash,
            onTagClick = { tag ->
                viewModel.updateSearchQuery(tag)
                currentTab = AppDestination.PHOTOS
            }
        )
    }

    // Open-Source README & Architecture Modal
    if (showReadmeDialog) {
        OpenSourceReadmeDialog(onDismiss = { showReadmeDialog = false })
    }
}

@Composable
private fun AiStatusBannerBar(
    status: AiStatusBanner,
    onDismiss: () -> Unit
) {
    val (accentColor, text) = when (status) {
        is AiStatusBanner.Working -> SunsetAmber to status.message
        is AiStatusBanner.Success -> EmeraldSync to status.message
        is AiStatusBanner.Error -> ErrorCrimson to status.message
        AiStatusBanner.Idle -> return
    }

    Surface(
        color = ObsidianCard,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (status is AiStatusBanner.Working) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = accentColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Banner",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
