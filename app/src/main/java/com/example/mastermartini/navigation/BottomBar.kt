package com.example.mastermartini.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.mastermartini.R

enum class TopLevelTab(
    val route: Route,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    CATALOG(Route.Catalog, R.string.nav_catalog, Icons.Filled.Storefront, Icons.Outlined.Storefront),
    QUOTE(Route.Quote, R.string.nav_quote, Icons.Filled.RequestQuote, Icons.Outlined.RequestQuote),
    CONTACT(Route.Contact, R.string.nav_contact, Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent),
    PROFILE(Route.Profile, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person),
}

fun NavDestination?.isTab(tab: TopLevelTab): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.route::class) } == true

fun NavDestination?.isAnyTab(): Boolean = TopLevelTab.entries.any { isTab(it) }

/**
 * @param quoteItemCount number of products in the draft quote, shown as a badge
 *   on the Cotización tab when greater than zero.
 */
@Composable
fun BottomBar(
    currentDestination: NavDestination?,
    quoteItemCount: Int,
    onTabSelected: (TopLevelTab) -> Unit,
) {
    NavigationBar {
        TopLevelTab.entries.forEach { tab ->
            val selected = currentDestination.isTab(tab)
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                label = { Text(stringResource(tab.labelRes)) },
                icon = {
                    val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                    if (tab == TopLevelTab.QUOTE && quoteItemCount > 0) {
                        BadgedBox(badge = { Badge { Text(quoteItemCount.toString()) } }) {
                            Icon(icon, contentDescription = null)
                        }
                    } else {
                        Icon(icon, contentDescription = null)
                    }
                },
            )
        }
    }
}
