package com.example.mastermartini.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mastermartini.R
import com.example.mastermartini.ui.screens.PlaceholderScreen
import com.example.mastermartini.ui.screens.auth.AuthGateScreen

private const val TRANSITION_MS = 300

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentDestination.isAnyTab(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                BottomBar(
                    currentDestination = currentDestination,
                    quoteItemCount = 0, // connected to the draft quote in T13
                    onTabSelected = { tab -> navController.navigateToTab(tab) },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.AuthGate,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { if (isBetweenTabs()) fadeIn(tween(TRANSITION_MS)) else slideIn(SlideDirection.Start) },
            exitTransition = { if (isBetweenTabs()) fadeOut(tween(TRANSITION_MS)) else slideOut(SlideDirection.Start) },
            popEnterTransition = { if (isBetweenTabs()) fadeIn(tween(TRANSITION_MS)) else slideIn(SlideDirection.End) },
            popExitTransition = { if (isBetweenTabs()) fadeOut(tween(TRANSITION_MS)) else slideOut(SlideDirection.End) },
        ) {
            composable<Route.AuthGate>(
                enterTransition = { EnterTransition.None },
                exitTransition = { fadeOut(tween(TRANSITION_MS)) },
            ) {
                AuthGateScreen(
                    onLoggedIn = { navController.navigateClearingStack(Route.Catalog) },
                    onLoggedOut = { navController.navigateClearingStack(Route.Login) },
                )
            }

            // Auth flow (no bottom bar)
            composable<Route.Login> {
                PlaceholderScreen(
                    title = stringResource(R.string.title_login),
                    actions = listOf(
                        stringResource(R.string.placeholder_go_register) to { navController.navigate(Route.Register) },
                        stringResource(R.string.placeholder_enter) to { navController.navigateClearingStack(Route.Catalog) },
                    ),
                )
            }
            composable<Route.Register> {
                PlaceholderScreen(
                    title = stringResource(R.string.title_register),
                    actions = listOf(
                        stringResource(R.string.placeholder_enter) to { navController.navigateClearingStack(Route.Catalog) },
                    ),
                )
            }

            // Main flow
            composable<Route.Catalog> {
                PlaceholderScreen(
                    title = stringResource(R.string.nav_catalog),
                    actions = listOf(
                        stringResource(R.string.placeholder_open_product) to {
                            navController.navigate(Route.ProductDetail(productId = 1))
                        },
                    ),
                )
            }
            composable<Route.ProductDetail> { entry ->
                val route = entry.toRoute<Route.ProductDetail>()
                PlaceholderScreen(
                    title = stringResource(R.string.title_product_detail) + " #${route.productId}",
                    actions = listOf(stringResource(R.string.action_back) to { navController.popBackStack() }),
                )
            }
            composable<Route.Quote> {
                PlaceholderScreen(title = stringResource(R.string.nav_quote))
            }
            composable<Route.Contact> {
                PlaceholderScreen(title = stringResource(R.string.nav_contact))
            }
            composable<Route.Profile> {
                PlaceholderScreen(
                    title = stringResource(R.string.nav_profile),
                    actions = listOf(
                        stringResource(R.string.placeholder_logout) to { navController.navigateClearingStack(Route.Login) },
                    ),
                )
            }
        }
    }
}

/** Replaces the whole back stack, so Back cannot return to auth (after login) or the app (after logout). */
fun NavHostController.navigateClearingStack(route: Route) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

/** Standard bottom-bar behaviour: one copy of each tab, each tab keeps its own state. */
fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.route) {
        popUpTo<Route.Catalog> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isBetweenTabs(): Boolean =
    initialState.destination.isAnyTab() && targetState.destination.isAnyTab()

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideIn(direction: SlideDirection): EnterTransition =
    slideIntoContainer(direction, tween(TRANSITION_MS)) + fadeIn(tween(TRANSITION_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOut(direction: SlideDirection): ExitTransition =
    slideOutOfContainer(direction, tween(TRANSITION_MS)) + fadeOut(tween(TRANSITION_MS))
