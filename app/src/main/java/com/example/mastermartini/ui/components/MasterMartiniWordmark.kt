package com.example.mastermartini.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermartini.R
import com.example.mastermartini.ui.theme.MasterMartiniTheme

/**
 * Text-based brand mark ("MASTER" over an italic "Martini").
 * The company's logo file is not copied; see issues/prd.md.
 */
@Composable
fun MasterMartiniWordmark(
    modifier: Modifier = Modifier,
    showTagline: Boolean = false,
) {
    val description = stringResource(R.string.wordmark_content_description)
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.BakeryDining,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(36.dp),
            )
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = stringResource(R.string.wordmark_master),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = stringResource(R.string.wordmark_martini),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (showTagline) {
            Text(
                text = stringResource(R.string.wordmark_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MasterMartiniWordmarkPreview() {
    MasterMartiniTheme {
        MasterMartiniWordmark(showTagline = true)
    }
}
