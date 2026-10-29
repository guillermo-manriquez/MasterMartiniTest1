package com.example.mastermartini.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
 * @param badgeSeenCount last count the user has seen; the badge bounces when the count
 *   is higher (kept by the caller because this bar leaves composition on hidden screens).
 */
@Composable
fun BottomBar(
    currentDestination: NavDestination?,
    quoteItemCount: Int,
    badgeSeenCount: Int,
    onBadgeSeen: (Int) -> Unit,
    onTabSelected: (TopLevelTab) -> Unit,
) {
    NavigationBar {
        TopLevelTab.entries.forEach { tab ->
            val selected = currentDestination.isTab(tab)
            val badgeDescription = if (tab == TopLevelTab.QUOTE && quoteItemCount > 0) {
                pluralStringResource(R.plurals.cd_quote_badge, quoteItemCount, quoteItemCount)
            } else {
                null
            }
            NavigationBarItem(
                selected = selected,
                // NavigationBarItem hides the icon (and so the badge) from screen readers when a
                // label is shown, so the count is announced on the item: "Cotización, 2 productos…"
                modifier = Modifier.semantics { badgeDescription?.let { stateDescription = it } },
                onClick = { onTabSelected(tab) },
                label = { Text(stringResource(tab.labelRes)) },
                icon = {
                    val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                    if (tab == TopLevelTab.QUOTE && quoteItemCount > 0) {
                        BadgedBox(badge = { BouncingBadge(quoteItemCount, badgeSeenCount, onBadgeSeen) }) {
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

/** Badge that bounces when the count grows, so the user sees where added products went. */
@Composable
private fun BouncingBadge(count: Int, seenCount: Int, onSeen: (Int) -> Unit) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(count) {
        if (seenCount >= 0 && count > seenCount) {
            scale.animateTo(1.6f, tween(durationMillis = 140))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
        onSeen(count)
    }
    Badge(
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    ) { Text(count.toString()) }
}
