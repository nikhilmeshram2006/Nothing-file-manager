package com.example.model

enum class FileCategory(
    val title: String,
    val description: String,
    val dotTag: String
) {
    IMAGES("Images", "Photos, screenshots, graphics", "IMG"),
    VIDEOS("Videos", "Screen records, clips, movies", "VID"),
    AUDIO("Audio", "Music, recordings, voice notes", "AUD"),
    DOCUMENTS("Documents", "PDFs, spreadsheets, text files", "DOC"),
    ARCHIVES("Archives", "ZIP, RAR, 7z & installer APKs", "ARC"),
    DOWNLOADS("Downloads", "Browser downloads & cached media", "DL"),
    SAFE_VAULT("Encrypted Vault", "End-to-end encrypted safe files", "SEC"),
    CLOUD("Cloud Drive", "Cross-device synced documents", "CLD")
}

data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isDirectory: Boolean = false,
    val category: FileCategory = FileCategory.DOCUMENTS,
    val isEncrypted: Boolean = false,
    val isCloudSynced: Boolean = false,
    val isStarred: Boolean = false,
    val mimeType: String = "application/octet-stream",
    val contentPreview: String? = null
) {
    val formattedSize: String
        get() = when {
            isDirectory -> "Folder"
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> String.format("%.1f KB", sizeBytes / 1024.0)
            sizeBytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0))
        }

    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "").uppercase()
}

data class StorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long
) {
    val usedPercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedTotal: String
        get() = formatBytes(totalBytes)

    val formattedUsed: String
        get() = formatBytes(usedBytes)

    val formattedFree: String
        get() = formatBytes(freeBytes)

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}

enum class CloudSyncStatus {
    SYNCED,
    SYNCING,
    OFFLINE_ENCRYPTED,
    PENDING
}

data class CloudAccount(
    val accountName: String = "nikhil.meshram@nothing.tech",
    val tier: String = "NOTHING CLOUD // PRO",
    val encryptionType: String = "AES-256-GCM (Zero Knowledge)",
    val totalQuotaBytes: Long = 25L * 1024 * 1024 * 1024, // 25 GB
    val usedQuotaBytes: Long = 7L * 1024 * 1024 * 1024 + 400 * 1024 * 1024, // 7.4 GB
    val lastSyncedTime: Long = System.currentTimeMillis() - 1000 * 60 * 14,
    val syncedDevices: List<String> = listOf("Phone (2)", "Ear (2)", "Nothing Laptop (1)")
) {
    val usedRatio: Float
        get() = (usedQuotaBytes.toFloat() / totalQuotaBytes.toFloat()).coerceIn(0f, 1f)
}

data class VaultSecurityState(
    val isUnlocked: Boolean = false,
    val hasPin: Boolean = true,
    val currentPin: String = "1920",
    val biometricAvailable: Boolean = true,
    val autoLockDurationSeconds: Int = 30,
    val encryptedItemCount: Int = 0,
    val lastUnlockedTime: Long = 0L
)
