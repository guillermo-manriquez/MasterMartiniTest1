package com.example.mastermartini.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mastermartini.R
import com.example.mastermartini.model.ProductCategory

/** "Todos" plus the four website categories; the selected chip shows a check mark. */
@Composable
fun CategoryChips(
    selected: ProductCategory?,
    onSelected: (ProductCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options: List<ProductCategory?> = listOf(null) + ProductCategory.entries
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options, key = { it?.name ?: "ALL" }) { category ->
            val isSelected = category == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(category) },
                label = { Text(stringResource(category?.labelRes ?: R.string.catalog_all)) },
                // Check mark when selected, otherwise the category icon ("Todos" has none)
                leadingIcon = (if (isSelected) Icons.Filled.Check else category?.icon)?.let { icon ->
                    { Icon(icon, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                },
            )
        }
    }
}
