package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.FileItem
import com.example.ui.components.NothingDotMatrixGlyph
import com.example.ui.components.NothingFileRow
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingWhite
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.NothingFilesViewModel
import java.io.File

@Composable
fun InternalStorageTab(
    viewModel: NothingFilesViewModel,
    currentPath: String,
    files: List<FileItem>,
    isGridView: Boolean
) {
    // Generate Breadcrumb segments
    val breadcrumbSegments = remember(currentPath) {
        val root = viewModel.repository.getBaseStorageDirectory().absolutePath
        val relative = if (currentPath.startsWith(root)) {
            currentPath.removePrefix(root).trim('/')
        } else {
            currentPath.trim('/')
        }

        val list = mutableListOf<Pair<String, String>>()
        list.add(Pair("HOME", root))

        if (relative.isNotEmpty()) {
            val parts = relative.split('/')
            var accPath = root
            for (part in parts) {
                accPath = "$accPath/$part"
                list.add(Pair(part.uppercase(), accPath))
            }
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("storage_tab_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Breadcrumb Trail
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    breadcrumbSegments.forEachIndexed { index, (segmentName, fullPath) ->
                        val isLast = index == breadcrumbSegments.lastIndex
                        Text(
                            text = segmentName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLast) NothingRed else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    if (!isLast) viewModel.navigateToFolder(fullPath)
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .testTag("breadcrumb_$segmentName")
                        )
                        if (!isLast) {
                            Text(
                                text = " / ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // Action Toolbar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateUp() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .testTag("storage_up_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Up directory",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "${files.size} ITEMS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.showDialog(ActiveDialog.CreateFolder(currentPath)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("new_folder_action")
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ FOLDER", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { viewModel.showDialog(ActiveDialog.CreateFile(currentPath)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("new_file_action")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ NOTE", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Files and Folders Listing
        if (files.isEmpty()) {
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
                            text = "[ FOLDER IS EMPTY ]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Use + FOLDER or + NOTE to add files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(files, key = { it.id }) { item ->
                NothingFileRow(
                    item = item,
                    onClick = {
                        if (item.isDirectory) {
                            viewModel.navigateToFolder(item.path)
                        } else {
                            viewModel.previewFile(item)
                        }
                    },
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
