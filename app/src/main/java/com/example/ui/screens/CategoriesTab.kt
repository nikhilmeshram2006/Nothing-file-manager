package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.ui.components.NothingCategoryTile
import com.example.ui.components.NothingDotMatrixGlyph
import com.example.ui.components.NothingFileGridCard
import com.example.ui.components.NothingFileRow
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingWhite
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.NothingFilesViewModel
import java.util.Locale

@Composable
fun CategoriesTab(
    viewModel: NothingFilesViewModel,
    selectedCategory: FileCategory?,
    categoryFiles: List<FileItem>,
    isGridView: Boolean,
    onCategoryClick: (FileCategory) -> Unit,
    onBackToCategories: () -> Unit
) {
    AnimatedContent(
        targetState = selectedCategory,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
            } else {
                slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
            }
        },
        label = "category_transition"
    ) { category ->
        if (category == null) {
            // Category Hub Overview
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("categories_hub_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "EXPLORE BY FORMAT",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    // Bento 2-column Grid of Categories
                    val categories = listOf(
                        FileCategory.IMAGES,
                        FileCategory.VIDEOS,
                        FileCategory.AUDIO,
                        FileCategory.DOCUMENTS,
                        FileCategory.ARCHIVES,
                        FileCategory.DOWNLOADS,
                        FileCategory.SAFE_VAULT,
                        FileCategory.CLOUD
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (i in categories.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    NothingCategoryTile(
                                        category = categories[i],
                                        itemCount = when (categories[i]) {
                                            FileCategory.SAFE_VAULT -> viewModel.vaultFiles.value.size
                                            FileCategory.CLOUD -> viewModel.cloudFiles.value.size
                                            else -> -1
                                        },
                                        onClick = {
                                            if (categories[i] == FileCategory.SAFE_VAULT) {
                                                viewModel.setTab(MainTab.VAULT)
                                            } else if (categories[i] == FileCategory.CLOUD) {
                                                viewModel.setTab(MainTab.CLOUD)
                                            } else {
                                                onCategoryClick(categories[i])
                                            }
                                        }
                                    )
                                }
                                if (i + 1 < categories.size) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        NothingCategoryTile(
                                            category = categories[i + 1],
                                            itemCount = when (categories[i + 1]) {
                                                FileCategory.SAFE_VAULT -> viewModel.vaultFiles.value.size
                                                FileCategory.CLOUD -> viewModel.cloudFiles.value.size
                                                else -> -1
                                            },
                                            onClick = {
                                                if (categories[i + 1] == FileCategory.SAFE_VAULT) {
                                                    viewModel.setTab(MainTab.VAULT)
                                                } else if (categories[i + 1] == FileCategory.CLOUD) {
                                                    viewModel.setTab(MainTab.CLOUD)
                                                } else {
                                                    onCategoryClick(categories[i + 1])
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        } else {
            // Category Detail Browser
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("category_detail_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackToCategories,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .testTag("category_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.title.uppercase(Locale.ROOT),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${categoryFiles.size} FILES FOUND",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        NothingDotMatrixGlyph()
                    }
                }

                if (categoryFiles.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                NothingDotMatrixGlyph(dotColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "[ NO ${category.title.uppercase(Locale.ROOT)} FOUND ]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(categoryFiles, key = { it.id }) { item ->
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
    }
}
