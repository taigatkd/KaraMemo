package com.taigatkd.karamemo.ui.feature.song

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.KaraMemoActionIconButton
import com.taigatkd.karamemo.ui.components.KaraMemoSwipeToDeleteContainer
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.MidnightStage
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme
import com.taigatkd.karamemo.ui.theme.StageWhite
import java.math.BigDecimal

@Composable
fun SongItemRow(
    song: Song,
    onEdit: (Song) -> Unit,
    onDelete: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    modifier: Modifier = Modifier,
    playlistName: String? = null,
    showActions: Boolean = true,
    enableSwipeToDelete: Boolean = false,
    compactMetadata: Boolean = false,
    showArtistInfo: Boolean = true,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val favoriteDescription = if (song.isFavorite) {
        stringResource(R.string.action_unfavorite)
    } else {
        stringResource(R.string.action_favorite)
    }
    val hasScore = song.score != null
    val leadingBadgeBackground = if (hasScore) {
        MidnightStage
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    }
    val leadingBadgeContent = if (hasScore) {
        StageWhite
    } else {
        MaterialTheme.colorScheme.primary
    }

    val rowContent: @Composable () -> Unit = {
        Card(
            onClick = { onEdit(song) },
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = if (compactMetadata) 10.dp else 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = leadingBadgeBackground,
                            shape = CircleShape,
                        )
                        .size(38.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    song.score?.let { score ->
                        Text(
                            text = formatScore(score),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = leadingBadgeContent,
                            maxLines = 1,
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = leadingBadgeContent,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (compactMetadata) 5.dp else 6.dp),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (compactMetadata) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (showArtistInfo) {
                                MetadataBadge(
                                    text = song.artist,
                                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                                )
                            }

                            if (song.key != 0) {
                                MetadataBadge(
                                    text = stringResource(R.string.label_key, signedKey(song.key)),
                                    textColor = MaterialTheme.colorScheme.primary,
                                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                )
                            }

                            playlistName?.let { name ->
                                MetadataBadge(
                                    text = name,
                                    textColor = MaterialTheme.colorScheme.secondary,
                                    backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                                )
                            }
                        }
                    } else {
                        if (showArtistInfo) {
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (song.key != 0) {
                                MetadataBadge(
                                    text = stringResource(R.string.label_key, signedKey(song.key)),
                                    textColor = MaterialTheme.colorScheme.primary,
                                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                )
                            }

                            playlistName?.let { name ->
                                MetadataBadge(
                                    text = name,
                                    textColor = MaterialTheme.colorScheme.secondary,
                                    backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                                )
                            }
                        }
                    }

                    if (song.memo.isNotBlank()) {
                        Text(
                            text = song.memo,
                            style = if (compactMetadata) {
                                MaterialTheme.typography.labelMedium
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (showActions) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KaraMemoActionIconButton(
                            onClick = { onToggleFavorite(song) },
                            contentDescription = favoriteDescription,
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            iconTint = if (song.isFavorite) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                        if (!enableSwipeToDelete) {
                            KaraMemoActionIconButton(
                                onClick = { showDeleteDialog = true },
                                contentDescription = stringResource(R.string.action_delete),
                                imageVector = Icons.Default.Delete,
                                iconTint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                } else if (song.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }

    if (enableSwipeToDelete && showActions) {
        KaraMemoSwipeToDeleteContainer(
            modifier = modifier,
            onDeleteRequested = { showDeleteDialog = true },
            content = rowContent,
        )
    } else {
        rowContent()
    }

    if (showActions && showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.title_delete_song)) },
            text = { Text(stringResource(R.string.message_delete_song, song.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(song)
                    },
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun MetadataBadge(
    text: String,
    textColor: Color,
    backgroundColor: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .background(
                backgroundColor,
                MaterialTheme.shapes.small,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

private fun signedKey(value: Int): String = if (value > 0) "+$value" else value.toString()

private fun formatScore(value: Double): String =
    BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

@Preview(showBackground = true)
@Composable
private fun SongItemRowPreview() {
    KaraMemoTheme {
        SongItemRow(
            song = PreviewFixtures.songs.first(),
            playlistName = PreviewFixtures.playlistNamesById[PreviewFixtures.songs.first().playlistId],
            onEdit = {},
            onDelete = {},
            onToggleFavorite = {},
        )
    }
}
