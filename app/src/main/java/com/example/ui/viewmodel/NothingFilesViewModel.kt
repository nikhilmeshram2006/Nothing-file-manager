package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileManagerRepository
import com.example.model.CloudAccount
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.StorageInfo
import com.example.model.VaultSecurityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class MainTab(val label: String, val dotTag: String) {
    RECENTS("Recent", "01"),
    CATEGORIES("Categories", "02"),
    STORAGE("Internal", "03"),
    CLOUD("Cloud", "04"),
    VAULT("Vault", "05")
}

sealed class ActiveDialog {
    data object None : ActiveDialog()
    data class CreateFolder(val parentPath: String) : ActiveDialog()
    data class CreateFile(val parentPath: String) : ActiveDialog()
    data class RenameFile(val item: FileItem) : ActiveDialog()
    data class EncryptFile(val item: FileItem) : ActiveDialog()
    data class FileDetails(val item: FileItem) : ActiveDialog()
    data class PreviewFile(val item: FileItem, val content: String?) : ActiveDialog()
    data object VaultAuth : ActiveDialog()
    data object PermissionRationale : ActiveDialog()
    data object SecuritySettings : ActiveDialog()
}

class NothingFilesViewModel(application: Application) : AndroidViewModel(application) {

    val repository = FileManagerRepository(application)

    private val _currentTab = MutableStateFlow(MainTab.RECENTS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<FileCategory?>(null)
    val selectedCategory: StateFlow<FileCategory?> = _selectedCategory.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _activeDialog = MutableStateFlow<ActiveDialog>(ActiveDialog.None)
    val activeDialog: StateFlow<ActiveDialog> = _activeDialog.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _hasStoragePermission = MutableStateFlow(false)
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    private val _directoryFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val directoryFiles: StateFlow<List<FileItem>> = _directoryFiles.asStateFlow()

    private val _categoryFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val categoryFiles: StateFlow<List<FileItem>> = _categoryFiles.asStateFlow()

    val currentPath: StateFlow<String> = repository.currentPath
    val storageInfo: StateFlow<StorageInfo> = repository.storageInfo
    val recentFiles: StateFlow<List<FileItem>> = repository.recentFiles
    val vaultFiles: StateFlow<List<FileItem>> = repository.vaultFiles
    val cloudFiles: StateFlow<List<FileItem>> = repository.cloudFiles
    val cloudAccount: StateFlow<CloudAccount> = repository.cloudAccount
    val vaultSecurity: StateFlow<VaultSecurityState> = repository.vaultSecurity
    val isCloudSyncing: StateFlow<Boolean> = repository.isCloudSyncing

    // Filtered lists based on search query
    val filteredRecentFiles: StateFlow<List<FileItem>> = combine(recentFiles, searchQuery) { files, query ->
        if (query.isBlank()) files else files.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDirectoryFiles: StateFlow<List<FileItem>> = combine(directoryFiles, searchQuery) { files, query ->
        if (query.isBlank()) files else files.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCategoryFiles: StateFlow<List<FileItem>> = combine(categoryFiles, searchQuery) { files, query ->
        if (query.isBlank()) files else files.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCurrentDirectory()
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
        if (tab == MainTab.STORAGE) {
            loadCurrentDirectory()
        } else if (tab == MainTab.VAULT && !vaultSecurity.value.isUnlocked) {
            _activeDialog.value = ActiveDialog.VaultAuth
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun setStoragePermissionGranted(granted: Boolean) {
        _hasStoragePermission.value = granted
        if (granted) {
            viewModelScope.launch {
                repository.refreshAll()
                loadCurrentDirectory()
            }
        }
    }

    fun selectCategory(category: FileCategory?) {
        _selectedCategory.value = category
        if (category != null) {
            viewModelScope.launch {
                _categoryFiles.value = repository.getFilesForCategory(category)
            }
        }
    }

    fun loadCurrentDirectory() {
        viewModelScope.launch {
            val list = repository.listFilesInDirectory(repository.currentPath.value)
            _directoryFiles.value = list
        }
    }

    fun navigateToFolder(path: String) {
        repository.navigateTo(path)
        loadCurrentDirectory()
    }

    fun navigateUp() {
        val navigated = repository.navigateUp()
        if (navigated) {
            loadCurrentDirectory()
        }
    }

    fun showDialog(dialog: ActiveDialog) {
        _activeDialog.value = dialog
    }

    fun dismissDialog() {
        _activeDialog.value = ActiveDialog.None
    }

    fun showFeedback(message: String) {
        _feedbackMessage.value = message
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val success = repository.createNewFolder(repository.currentPath.value, name)
            if (success) {
                showFeedback("Folder created: $name")
                loadCurrentDirectory()
            } else {
                showFeedback("Failed to create folder")
            }
            dismissDialog()
        }
    }

    fun createFile(name: String, content: String) {
        viewModelScope.launch {
            val success = repository.createNewTextFile(repository.currentPath.value, name, content)
            if (success) {
                showFeedback("Document created: $name")
                loadCurrentDirectory()
            } else {
                showFeedback("Failed to create document")
            }
            dismissDialog()
        }
    }

    fun renameFile(item: FileItem, newName: String) {
        viewModelScope.launch {
            val success = repository.renameFile(item, newName)
            if (success) {
                showFeedback("Renamed to $newName")
                loadCurrentDirectory()
                if (_selectedCategory.value != null) {
                    selectCategory(_selectedCategory.value)
                }
            } else {
                showFeedback("Rename failed")
            }
            dismissDialog()
        }
    }

    fun deleteFile(item: FileItem) {
        viewModelScope.launch {
            val success = repository.deleteFile(item)
            if (success) {
                showFeedback("Deleted: ${item.name}")
                loadCurrentDirectory()
                if (_selectedCategory.value != null) {
                    selectCategory(_selectedCategory.value)
                }
            } else {
                showFeedback("Deletion failed")
            }
        }
    }

    fun encryptFile(item: FileItem, pin: String) {
        viewModelScope.launch {
            val success = repository.encryptFileToVault(item, pin)
            if (success) {
                showFeedback("Encrypted with AES-256 into Vault")
                loadCurrentDirectory()
            } else {
                showFeedback("Encryption failed")
            }
            dismissDialog()
        }
    }

    fun unlockVault(pin: String): Boolean {
        val success = repository.unlockVault(pin)
        if (success) {
            showFeedback("Vault unlocked // Authenticated")
            dismissDialog()
        } else {
            showFeedback("Incorrect PIN // Access Denied")
        }
        return success
    }

    fun unlockVaultBiometrics() {
        repository.unlockVaultWithBiometrics()
        showFeedback("Biometric Authenticated // Vault Unlocked")
        dismissDialog()
    }

    fun lockVault() {
        repository.lockVault()
        showFeedback("Vault Locked // Zero Knowledge Safe")
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.triggerCloudSync()
            showFeedback("Cloud Sync Complete // Encrypted")
        }
    }

    fun previewFile(item: FileItem) {
        viewModelScope.launch {
            val file = File(item.path)
            val content = if (item.isEncrypted) {
                val decrypted = repository.decryptFileFromVault(item, vaultSecurity.value.currentPin)
                if (decrypted != null) String(decrypted, Charsets.UTF_8) else "[Encrypted Payload - Authenticate with PIN]"
            } else if (!file.isDirectory && file.exists() && file.length() < 256 * 1024) {
                try {
                    file.readText()
                } catch (_: Exception) {
                    "[Binary file preview not supported as text]"
                }
            } else {
                null
            }
            _activeDialog.value = ActiveDialog.PreviewFile(item, content)
        }
    }
}
