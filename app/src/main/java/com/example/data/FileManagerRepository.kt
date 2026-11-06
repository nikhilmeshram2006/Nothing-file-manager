package com.example.data

import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import com.example.crypto.VaultCryptoManager
import com.example.model.CloudAccount
import com.example.model.CloudSyncStatus
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.StorageInfo
import com.example.model.VaultSecurityState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FileManagerRepository(private val context: Context) {

    private val _currentPath = MutableStateFlow<String>("")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _storageInfo = MutableStateFlow(StorageInfo(64L * 1024 * 1024 * 1024, 28L * 1024 * 1024 * 1024, 36L * 1024 * 1024 * 1024))
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    private val _recentFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val recentFiles: StateFlow<List<FileItem>> = _recentFiles.asStateFlow()

    private val _vaultFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val vaultFiles: StateFlow<List<FileItem>> = _vaultFiles.asStateFlow()

    private val _cloudFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val cloudFiles: StateFlow<List<FileItem>> = _cloudFiles.asStateFlow()

    private val _cloudAccount = MutableStateFlow(CloudAccount())
    val cloudAccount: StateFlow<CloudAccount> = _cloudAccount.asStateFlow()

    private val _vaultSecurity = MutableStateFlow(VaultSecurityState())
    val vaultSecurity: StateFlow<VaultSecurityState> = _vaultSecurity.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    init {
        initializeStorage()
    }

    private fun initializeStorage() {
        val rootDir = getBaseStorageDirectory()
        _currentPath.value = rootDir.absolutePath
        seedInitialFilesIfEmpty(rootDir)
        refreshAllSync()
    }

    fun getBaseStorageDirectory(): File {
        val extDir = Environment.getExternalStorageDirectory()
        return if (extDir != null && extDir.canRead()) {
            extDir
        } else {
            val appStorage = File(context.filesDir, "internal_storage")
            if (!appStorage.exists()) appStorage.mkdirs()
            appStorage
        }
    }

    private fun seedInitialFilesIfEmpty(root: File) {
        val seededMarker = File(context.filesDir, ".seeded_files_v2")
        if (seededMarker.exists()) return

        try {
            val docsFolder = File(root, "Documents").apply { mkdirs() }
            val downloadFolder = File(root, "Download").apply { mkdirs() }
            val picturesFolder = File(root, "Pictures").apply { mkdirs() }
            val musicFolder = File(root, "Music").apply { mkdirs() }
            val nothingProjectFolder = File(docsFolder, "Nothing_OS_Specs").apply { mkdirs() }

            File(docsFolder, "Nothing_Design_Principles.pdf").writeText("%PDF-1.4 Minimalist Dot-Matrix Design Language & Typography")
            File(docsFolder, "Financial_Report_Q3_Encrypted.docx").writeText("Confidential quarterly roadmap and unit economics.")
            File(nothingProjectFolder, "Glyph_Interface_v3.kt").writeText("// Nothing OS Glyph Composer Interface\nval ledStrips = 5\nfun pulseRed() { /* dot matrix */ }")
            File(nothingProjectFolder, "Hardware_Schematics.cad").writeText("NOTHING DUAL TRANSPARENT CHASSIS SPECS")

            File(downloadFolder, "wallpaper_dot_matrix_dark.png").writeBytes(ByteArray(245000))
            File(downloadFolder, "firmware_nos_3.0_beta.zip").writeBytes(ByteArray(1250000))
            File(downloadFolder, "Nothing_Community_Pass.pdf").writeText("Member ID: NTH-84920-ALPHA")

            File(picturesFolder, "Camera_Glyph_Night.jpg").writeBytes(ByteArray(850000))
            File(picturesFolder, "Phone2a_Minimal_Render.png").writeBytes(ByteArray(540000))
            File(picturesFolder, "Nothing_Store_Soho.jpg").writeBytes(ByteArray(720000))

            File(musicFolder, "Swedish_House_Mafia_Glyph_Soundtrack.mp3").writeBytes(ByteArray(3400000))
            File(musicFolder, "Nothing_Dot_Ringtone.ogg").writeBytes(ByteArray(420000))

            // Seed sample encrypted vault files
            val vaultDir = VaultCryptoManager.getVaultDirectory(context)
            val sampleEncrypted1 = File(vaultDir, "Passport_Copy.pdf.nvault")
            val sampleEncrypted2 = File(vaultDir, "Crypto_Recovery_Seed_Phrases.txt.nvault")
            if (!sampleEncrypted1.exists()) {
                val encBytes = VaultCryptoManager.encrypt("CONFIDENTIAL PASSPORT DATA: N-74920199".toByteArray(), "1920")
                sampleEncrypted1.writeBytes(encBytes)
            }
            if (!sampleEncrypted2.exists()) {
                val encBytes = VaultCryptoManager.encrypt("Seed words: nothing dot matrix minimal glyph transparency future audio red".toByteArray(), "1920")
                sampleEncrypted2.writeBytes(encBytes)
            }

            seededMarker.createNewFile()
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun refreshAllSync() {
        refreshStorageInfo()
        refreshRecentFiles()
        refreshVaultFiles()
        refreshCloudFiles()
    }

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        refreshAllSync()
    }

    private fun refreshStorageInfo() {
        val root = getBaseStorageDirectory()
        val total = root.totalSpace.takeIf { it > 0 } ?: (64L * 1024 * 1024 * 1024)
        val free = root.freeSpace.takeIf { it > 0 } ?: (36L * 1024 * 1024 * 1024)
        val used = (total - free).coerceAtLeast(0L)
        _storageInfo.value = StorageInfo(total, used, free)
    }

    suspend fun listFilesInDirectory(path: String): List<FileItem> = withContext(Dispatchers.IO) {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

        val files = dir.listFiles() ?: return@withContext emptyList()
        files.map { file ->
            mapFileToItem(file)
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase(Locale.ROOT) }))
    }

    fun navigateTo(path: String) {
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            _currentPath.value = path
        }
    }

    fun navigateUp(): Boolean {
        val current = File(_currentPath.value)
        val parent = current.parentFile
        val base = getBaseStorageDirectory()
        if (parent != null && (current.absolutePath != base.absolutePath && current.absolutePath != "/")) {
            _currentPath.value = parent.absolutePath
            return true
        }
        return false
    }

    suspend fun getFilesForCategory(category: FileCategory): List<FileItem> = withContext(Dispatchers.IO) {
        when (category) {
            FileCategory.SAFE_VAULT -> _vaultFiles.value
            FileCategory.CLOUD -> _cloudFiles.value
            else -> {
                val allFiles = mutableListOf<FileItem>()
                scanRecursive(getBaseStorageDirectory(), allFiles, 4)
                allFiles.filter { it.category == category && !it.isDirectory }
                    .sortedByDescending { it.lastModified }
            }
        }
    }

    private fun scanRecursive(dir: File, list: MutableList<FileItem>, maxDepth: Int) {
        if (maxDepth <= 0) return
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.name.startsWith(".")) continue
            val item = mapFileToItem(child)
            if (!item.isDirectory) {
                list.add(item)
            } else if (list.size < 300) {
                scanRecursive(child, list, maxDepth - 1)
            }
        }
    }

    private fun refreshRecentFiles() {
        val all = mutableListOf<FileItem>()
        scanRecursive(getBaseStorageDirectory(), all, 4)
        val sorted = all.filter { !it.isDirectory }
            .sortedByDescending { it.lastModified }
            .take(40)
        _recentFiles.value = sorted
    }

    private fun refreshVaultFiles() {
        val vaultDir = VaultCryptoManager.getVaultDirectory(context)
        val files = vaultDir.listFiles() ?: emptyArray()
        val list = files.map { file ->
            val cleanName = file.name.removeSuffix(".nvault")
            FileItem(
                id = file.absolutePath,
                name = cleanName,
                path = file.absolutePath,
                sizeBytes = file.length(),
                lastModified = file.lastModified(),
                isDirectory = false,
                category = FileCategory.SAFE_VAULT,
                isEncrypted = true,
                isCloudSynced = false,
                mimeType = getMimeType(cleanName)
            )
        }.sortedByDescending { it.lastModified }
        _vaultFiles.value = list
        _vaultSecurity.value = _vaultSecurity.value.copy(encryptedItemCount = list.size)
    }

    private fun refreshCloudFiles() {
        val cloudDocs = listOf(
            FileItem(
                id = "cloud_1",
                name = "Nothing_Whitepaper_2026.pdf",
                path = "cloud://documents/Nothing_Whitepaper_2026.pdf",
                sizeBytes = 4L * 1024 * 1024 + 200 * 1024,
                lastModified = System.currentTimeMillis() - 1000 * 3600 * 2,
                category = FileCategory.DOCUMENTS,
                isCloudSynced = true,
                isEncrypted = true,
                mimeType = "application/pdf"
            ),
            FileItem(
                id = "cloud_2",
                name = "Glyph_Ringtone_Matrix_Pack.zip",
                path = "cloud://audio/Glyph_Ringtone_Matrix_Pack.zip",
                sizeBytes = 18L * 1024 * 1024,
                lastModified = System.currentTimeMillis() - 1000 * 3600 * 24,
                category = FileCategory.ARCHIVES,
                isCloudSynced = true,
                isEncrypted = true,
                mimeType = "application/zip"
            ),
            FileItem(
                id = "cloud_3",
                name = "Identity_Credentials_Backup.nvault",
                path = "cloud://vault/Identity_Credentials_Backup.nvault",
                sizeBytes = 512 * 1024,
                lastModified = System.currentTimeMillis() - 1000 * 3600 * 48,
                category = FileCategory.SAFE_VAULT,
                isCloudSynced = true,
                isEncrypted = true,
                mimeType = "application/octet-stream"
            ),
            FileItem(
                id = "cloud_4",
                name = "Phone_3_Camera_Presets.dng",
                path = "cloud://photos/Phone_3_Camera_Presets.dng",
                sizeBytes = 28L * 1024 * 1024,
                lastModified = System.currentTimeMillis() - 1000 * 3600 * 72,
                category = FileCategory.IMAGES,
                isCloudSynced = true,
                isEncrypted = false,
                mimeType = "image/x-adobe-dng"
            ),
            FileItem(
                id = "cloud_5",
                name = "NothingOS_Widget_Kit_Spec.json",
                path = "cloud://dev/NothingOS_Widget_Kit_Spec.json",
                sizeBytes = 145 * 1024,
                lastModified = System.currentTimeMillis() - 1000 * 3600 * 96,
                category = FileCategory.DOCUMENTS,
                isCloudSynced = true,
                isEncrypted = true,
                mimeType = "application/json"
            )
        )
        _cloudFiles.value = cloudDocs
    }

    suspend fun createNewFolder(parentPath: String, folderName: String): Boolean = withContext(Dispatchers.IO) {
        val sanitized = folderName.trim()
        if (sanitized.isEmpty()) return@withContext false
        val newDir = File(parentPath, sanitized)
        if (!newDir.exists()) {
            val created = newDir.mkdirs()
            refreshAll()
            created
        } else {
            false
        }
    }

    suspend fun createNewTextFile(parentPath: String, fileName: String, content: String): Boolean = withContext(Dispatchers.IO) {
        val fullFileName = if (fileName.contains('.')) fileName.trim() else "${fileName.trim()}.txt"
        val file = File(parentPath, fullFileName)
        val written = try {
            file.writeText(content)
            true
        } catch (_: Exception) {
            false
        }
        refreshAll()
        written
    }

    suspend fun deleteFile(item: FileItem): Boolean = withContext(Dispatchers.IO) {
        val file = File(item.path)
        val deleted = if (file.isDirectory) file.deleteRecursively() else file.delete()
        refreshAll()
        deleted
    }

    suspend fun renameFile(item: FileItem, newName: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(item.path)
        val target = File(file.parentFile, newName.trim())
        val renamed = file.renameTo(target)
        refreshAll()
        renamed
    }

    suspend fun encryptFileToVault(item: FileItem, pin: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(item.path)
            if (!srcFile.exists() || srcFile.isDirectory) return@withContext false
            val data = srcFile.readBytes()
            val encryptedBytes = VaultCryptoManager.encrypt(data, pin)
            val vaultDir = VaultCryptoManager.getVaultDirectory(context)
            val destFile = File(vaultDir, "${srcFile.name}.nvault")
            destFile.writeBytes(encryptedBytes)
            refreshAll()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun decryptFileFromVault(item: FileItem, pin: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(item.path)
            if (!file.exists()) return@withContext null
            val encryptedBytes = file.readBytes()
            VaultCryptoManager.decrypt(encryptedBytes, pin)
        } catch (_: Exception) {
            null
        }
    }

    fun unlockVault(enteredPin: String): Boolean {
        if (enteredPin == _vaultSecurity.value.currentPin) {
            _vaultSecurity.value = _vaultSecurity.value.copy(
                isUnlocked = true,
                lastUnlockedTime = System.currentTimeMillis()
            )
            return true
        }
        return false
    }

    fun unlockVaultWithBiometrics(): Boolean {
        _vaultSecurity.value = _vaultSecurity.value.copy(
            isUnlocked = true,
            lastUnlockedTime = System.currentTimeMillis()
        )
        return true
    }

    fun lockVault() {
        _vaultSecurity.value = _vaultSecurity.value.copy(isUnlocked = false)
    }

    fun setVaultPin(newPin: String) {
        _vaultSecurity.value = _vaultSecurity.value.copy(currentPin = newPin, hasPin = true)
    }

    suspend fun triggerCloudSync() = withContext(Dispatchers.IO) {
        _isCloudSyncing.value = true
        kotlinx.coroutines.delay(1800) // Realistic smooth sync animation
        _cloudAccount.value = _cloudAccount.value.copy(
            lastSyncedTime = System.currentTimeMillis(),
            usedQuotaBytes = _cloudAccount.value.usedQuotaBytes + 124 * 1024
        )
        _isCloudSyncing.value = false
    }

    private fun mapFileToItem(file: File): FileItem {
        val mime = getMimeType(file.name)
        val cat = categorizeFile(file.name, file.isDirectory, mime)
        return FileItem(
            id = file.absolutePath,
            name = file.name,
            path = file.absolutePath,
            sizeBytes = if (file.isDirectory) 0L else file.length(),
            lastModified = file.lastModified(),
            isDirectory = file.isDirectory,
            category = cat,
            mimeType = mime
        )
    }

    private fun categorizeFile(name: String, isDir: Boolean, mimeType: String): FileCategory {
        if (isDir) return FileCategory.DOCUMENTS
        val ext = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return when {
            ext in listOf("jpg", "jpeg", "png", "webp", "gif", "svg", "bmp", "heic", "dng") || mimeType.startsWith("image/") -> FileCategory.IMAGES
            ext in listOf("mp4", "mkv", "avi", "mov", "webm", "flv") || mimeType.startsWith("video/") -> FileCategory.VIDEOS
            ext in listOf("mp3", "wav", "ogg", "flac", "aac", "m4a") || mimeType.startsWith("audio/") -> FileCategory.AUDIO
            ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "json", "xml", "csv", "kt", "java") -> FileCategory.DOCUMENTS
            ext in listOf("zip", "rar", "7z", "tar", "gz", "apk", "xapk") -> FileCategory.ARCHIVES
            name.contains("download", ignoreCase = true) -> FileCategory.DOWNLOADS
            else -> FileCategory.DOCUMENTS
        }
    }

    private fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: when (ext) {
            "kt", "java", "json", "xml", "md", "txt" -> "text/plain"
            "pdf" -> "application/pdf"
            "apk" -> "application/vnd.android.package-archive"
            "nvault" -> "application/octet-stream"
            else -> "application/octet-stream"
        }
    }
}
