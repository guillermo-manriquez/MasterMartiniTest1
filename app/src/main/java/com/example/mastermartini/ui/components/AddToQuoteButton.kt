package com.example.mastermartini.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mastermartini.R
import com.example.mastermartini.data.repository.AddToQuoteResult
import com.example.mastermartini.ui.theme.SuccessGreen

/**
 * "Agregar a cotización" button. After a tap it briefly turns green and slides to
 * "✓ Agregado" or "✓ Cantidad actualizada" (driven by [feedback] from the ViewModel).
 */
@Composable
fun AddToQuoteButton(
    feedback: AddToQuoteResult?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container by animateColorAsState(
        targetValue = if (feedback != null) SuccessGreen else MaterialTheme.colorScheme.primary,
        label = "addToQuoteColor",
    )
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = Color.White),
    ) {
        AnimatedContent(
            targetState = feedback,
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            },
            label = "addToQuoteLabel",
        ) { state ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (state == null) Icons.Outlined.AddShoppingCart else Icons.Filled.CheckCircle,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(
                        when (state) {
                            null -> R.string.detail_add_to_quote
                            AddToQuoteResult.ADDED -> R.string.detail_added
                            AddToQuoteResult.QUANTITY_UPDATED -> R.string.detail_quantity_updated
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
