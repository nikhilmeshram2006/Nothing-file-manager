package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.StorageInfo
import com.example.ui.components.NothingDotMatrixGlyph
import com.example.ui.components.NothingFileGridCard
import com.example.ui.components.NothingFileRow
import com.example.ui.components.NothingStorageMeterCard
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingWhite
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.NothingFilesViewModel

@Composable
fun RecentTab(
    viewModel: NothingFilesViewModel,
    recentFiles: List<FileItem>,
    storageInfo: StorageInfo,
    isGridView: Boolean,
    onNavigateToStorage: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(recentFiles, selectedFilter) {
        when (selectedFilter) {
            "DOCS" -> recentFiles.filter { it.category == FileCategory.DOCUMENTS }
            "IMAGES" -> recentFiles.filter { it.category == FileCategory.IMAGES }
            "VAULT" -> recentFiles.filter { it.isEncrypted }
            else -> recentFiles
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("recent_tab_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Storage Overview Bento Card
        item {
            NothingStorageMeterCard(
                storageInfo = storageInfo,
                onManageStorage = onNavigateToStorage
            )
        }

        // Privacy & Security Mini Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NothingRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NothingRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PRIVACY SHIELD // ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = NothingRed
                        )
                        Text(
                            text = "Zero tracking. Local encryption keys on-device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Filter Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RECENT FILES",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${filteredList.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("ALL", "DOCS", "IMAGES", "VAULT")
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) NothingRed else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NothingRed else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("filter_chip_$filter"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) NothingWhite else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Files List / Grid
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        NothingDotMatrixGlyph(dotColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "[ NO RECENT FILES FOUND ]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                NothingFileRow(
                    item = item,
                    onClick = { viewModel.previewFile(item) },
                    onDetails = { viewModel.showDialog(ActiveDialog.FileDetails(item)) },
                    onRename = { viewModel.showDialog(ActiveDialog.RenameFile(item)) },
                    onEncrypt = { viewModel.showDialog(ActiveDialog.EncryptFile(item)) },
                    onDelete = { viewModel.deleteFile(item) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
