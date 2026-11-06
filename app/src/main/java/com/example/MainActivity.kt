package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.screens.MainScreen
import com.example.ui.theme.NothingFilesTheme
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.NothingFilesViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NothingFilesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentTab by viewModel.currentTab.collectAsState()
            val selectedCategory by viewModel.selectedCategory.collectAsState()

            // Storage Permission Launcher
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                val allGranted = results.values.all { it }
                viewModel.setStoragePermissionGranted(allGranted)
                if (allGranted) {
                    viewModel.showFeedback("Storage Access Granted // Local Only")
                } else {
                    viewModel.showFeedback("App Storage Sandbox Active")
                }
            }

            // Check initial permission
            LaunchedEffect(Unit) {
                val hasPermission = permissions.all {
                    ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED
                }
                viewModel.setStoragePermissionGranted(hasPermission)
            }

            // Predictive Back navigation handler
            BackHandler(enabled = (currentTab == MainTab.CATEGORIES && selectedCategory != null) || currentTab == MainTab.STORAGE) {
                if (currentTab == MainTab.CATEGORIES && selectedCategory != null) {
                    viewModel.selectCategory(null)
                } else if (currentTab == MainTab.STORAGE) {
                    val didNavigateUp = viewModel.repository.navigateUp()
                    if (didNavigateUp) {
                        viewModel.loadCurrentDirectory()
                    } else {
                        viewModel.setTab(MainTab.RECENTS)
                    }
                }
            }

            NothingFilesTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        viewModel = viewModel,
                        onRequestStoragePermission = {
                            permissionLauncher.launch(permissions)
                        }
                    )
                }
            }
        }
    }
}
