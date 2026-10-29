package com.example.mastermartini.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.example.mastermartini.R

private val FavoriteRed = Color(0xFFC62828)

/** Heart toggle with a spring "pop" and a color change when it becomes a favorite. */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(1f) }
    // Pop only after the user's own tap, not when a saved favorite loads with the screen
    var tappedByUser by remember { mutableStateOf(false) }

    LaunchedEffect(isFavorite) {
        if (isFavorite && tappedByUser) {
            scale.animateTo(1.35f, tween(durationMillis = 110))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
        }
        tappedByUser = false
    }

    val tint by animateColorAsState(
        targetValue = if (isFavorite) FavoriteRed else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "favoriteTint",
    )

    IconButton(
        onClick = {
            tappedByUser = true
            onToggle()
        },
        modifier = modifier,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(if (isFavorite) R.string.cd_remove_favorite else R.string.cd_add_favorite),
            tint = tint,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        )
    }
}
