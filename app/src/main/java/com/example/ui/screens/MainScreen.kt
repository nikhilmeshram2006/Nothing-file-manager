package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.NothingCreateFileDialog
import com.example.ui.components.NothingCreateFolderDialog
import com.example.ui.components.NothingDotMatrixGlyph
import com.example.ui.components.NothingEncryptDialog
import com.example.ui.components.NothingFileDetailsModal
import com.example.ui.components.NothingFilePreviewModal
import com.example.ui.components.NothingPermissionRationaleModal
import com.example.ui.components.NothingRenameDialog
import com.example.ui.components.NothingTopBar
import com.example.ui.components.NothingVaultAuthModal
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingWhite
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.NothingFilesViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    viewModel: NothingFilesViewModel,
    onRequestStoragePermission: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    val recentFiles by viewModel.filteredRecentFiles.collectAsState()
    val directoryFiles by viewModel.filteredDirectoryFiles.collectAsState()
    val categoryFiles by viewModel.filteredCategoryFiles.collectAsState()
    val cloudFiles by viewModel.cloudFiles.collectAsState()
    val cloudAccount by viewModel.cloudAccount.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()
    val vaultFiles by viewModel.vaultFiles.collectAsState()
    val vaultSecurity by viewModel.vaultSecurity.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()

    // Auto dismiss feedback pill after 2.5s
    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(2500)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NothingTopBar(
                title = when (currentTab) {
                    MainTab.RECENTS -> "NOTHING // RECENT"
                    MainTab.CATEGORIES -> if (selectedCategory != null) selectedCategory!!.title else "NOTHING // CATEGORIES"
                    MainTab.STORAGE -> "NOTHING // STORAGE"
                    MainTab.CLOUD -> "NOTHING // CLOUD"
                    MainTab.VAULT -> "NOTHING // SAFE"
                },
                subtitle = when (currentTab) {
                    MainTab.RECENTS -> "Recent documents & storage status"
                    MainTab.CATEGORIES -> "Sorted by media & format"
                    MainTab.STORAGE -> currentPath
                    MainTab.CLOUD -> "Zero-knowledge cross-device sync"
                    MainTab.VAULT -> "AES-256-GCM protected vault"
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                isGridView = isGridView,
                onToggleGridView = { viewModel.toggleGridView() },
                onBack = if (currentTab == MainTab.CATEGORIES && selectedCategory != null) {
                    { viewModel.selectCategory(null) }
                } else null
            )
        },
        bottomBar = {
            // Nothing Minimal Segmented Dock
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(26.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { viewModel.setTab(tab) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("nav_tab_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(NothingRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = tab.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab Content
            when (currentTab) {
                MainTab.RECENTS -> {
                    RecentTab(
                        viewModel = viewModel,
                        recentFiles = recentFiles,
                        storageInfo = storageInfo,
                        isGridView = isGridView,
                        onNavigateToStorage = { viewModel.setTab(MainTab.STORAGE) }
                    )
                }
                MainTab.CATEGORIES -> {
                    CategoriesTab(
                        viewModel = viewModel,
                        selectedCategory = selectedCategory,
                        categoryFiles = categoryFiles,
                        isGridView = isGridView,
                        onCategoryClick = { viewModel.selectCategory(it) },
                        onBackToCategories = { viewModel.selectCategory(null) }
                    )
                }
                MainTab.STORAGE -> {
                    InternalStorageTab(
                        viewModel = viewModel,
                        currentPath = currentPath,
                        files = directoryFiles,
                        isGridView = isGridView
                    )
                }
                MainTab.CLOUD -> {
                    CloudTab(
                        viewModel = viewModel,
                        cloudAccount = cloudAccount,
                        cloudFiles = cloudFiles,
                        isSyncing = isCloudSyncing
                    )
                }
                MainTab.VAULT -> {
                    VaultTab(
                        viewModel = viewModel,
                        vaultSecurity = vaultSecurity,
                        vaultFiles = vaultFiles
                    )
                }
            }

            // Floating Feedback Pill (Nothing Style)
            AnimatedVisibility(
                visible = feedbackMessage != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                feedbackMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, NothingRed.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .testTag("feedback_pill"),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NothingRed)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = msg.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs Manager
    when (val dialog = activeDialog) {
        is ActiveDialog.CreateFolder -> {
            NothingCreateFolderDialog(
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { folderName -> viewModel.createFolder(folderName) }
            )
        }
        is ActiveDialog.CreateFile -> {
            NothingCreateFileDialog(
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { name, content -> viewModel.createFile(name, content) }
            )
        }
        is ActiveDialog.RenameFile -> {
            NothingRenameDialog(
                item = dialog.item,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { newName -> viewModel.renameFile(dialog.item, newName) }
            )
        }
        is ActiveDialog.EncryptFile -> {
            NothingEncryptDialog(
                item = dialog.item,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { pin -> viewModel.encryptFile(dialog.item, pin) }
            )
        }
        is ActiveDialog.FileDetails -> {
            NothingFileDetailsModal(
                item = dialog.item,
                onDismiss = { viewModel.dismissDialog() }
            )
        }
        is ActiveDialog.PreviewFile -> {
            NothingFilePreviewModal(
                item = dialog.item,
                content = dialog.content,
                onDismiss = { viewModel.dismissDialog() }
            )
        }
        is ActiveDialog.VaultAuth -> {
            NothingVaultAuthModal(
                onDismiss = { viewModel.dismissDialog() },
                onPinSubmit = { pin -> viewModel.unlockVault(pin) },
                onBiometricSuccess = { viewModel.unlockVaultBiometrics() }
            )
        }
        is ActiveDialog.PermissionRationale -> {
            NothingPermissionRationaleModal(
                onDismiss = { viewModel.dismissDialog() },
                onRequestPermission = onRequestStoragePermission
            )
        }
        else -> Unit
    }
}
