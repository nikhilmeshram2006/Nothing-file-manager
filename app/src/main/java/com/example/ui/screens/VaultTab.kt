package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.model.FileItem
import com.example.model.VaultSecurityState
import com.example.ui.components.NothingBiometricPad
import com.example.ui.components.NothingDotMatrixGlyph
import com.example.ui.components.NothingFileRow
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingWhite
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.NothingFilesViewModel
import java.util.Locale

@Composable
fun VaultTab(
    viewModel: NothingFilesViewModel,
    vaultSecurity: VaultSecurityState,
    vaultFiles: List<FileItem>
) {
    if (!vaultSecurity.isUnlocked) {
        // Locked State: Biometric Sensor Pad + PIN
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("vault_locked_view"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                NothingDotMatrixGlyph(dotColor = NothingRed, rows = 3, cols = 3, dotRadius = 3.dp, spacing = 4.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ENCRYPTED VAULT",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ZERO-KNOWLEDGE // END-TO-END ENCRYPTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingRed
                )
            }

            item {
                NothingBiometricPad(
                    onAuthenticated = { viewModel.unlockVaultBiometrics() },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Button(
                    onClick = { viewModel.showDialog(ActiveDialog.VaultAuth) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_pin_unlock_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("UNLOCK WITH PIN (DEFAULT: 1920)", style = MaterialTheme.typography.labelMedium)
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "SECURITY PROTOCOLS",
                            style = MaterialTheme.typography.labelSmall,
                            color = NothingRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• AES-256-GCM authenticated payload encryption\n• PBKDF2 key derivation with 10,000 rounds & 16-byte random salt\n• On-device biometric verification & anti-tampering protection\n• Auto-lock after inactivity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    } else {
        // Unlocked State: Explore Encrypted Documents
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("vault_unlocked_view"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, NothingRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(NothingRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, tint = NothingRed, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "VAULT UNLOCKED",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${vaultFiles.size} ENCRYPTED ITEMS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NothingRed
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.lockVault() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NothingRed,
                                contentColor = NothingWhite
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("vault_lock_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LOCK", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (vaultFiles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NothingDotMatrixGlyph()
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "[ VAULT HAS NO DOCUMENTS ]",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Select any file from Recents or Storage and choose 'Encrypt to Vault'",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(vaultFiles, key = { it.id }) { item ->
                    NothingFileRow(
                        item = item,
                        onClick = { viewModel.previewFile(item) },
                        onDetails = { viewModel.showDialog(ActiveDialog.FileDetails(item)) },
                        onRename = { viewModel.showDialog(ActiveDialog.RenameFile(item)) },
                        onEncrypt = { /* Already encrypted */ },
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
