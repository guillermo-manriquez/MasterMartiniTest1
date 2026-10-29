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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mastermartini.ui.screens.auth.AuthGateScreen
import com.example.mastermartini.ui.screens.auth.LoginScreen
import com.example.mastermartini.ui.screens.auth.RegisterScreen
import com.example.mastermartini.ui.screens.catalog.CatalogScreen
import com.example.mastermartini.ui.screens.catalog.ProductDetailScreen
import com.example.mastermartini.ui.screens.contact.ContactScreen
import com.example.mastermartini.ui.screens.profile.ProfileScreen
import com.example.mastermartini.ui.screens.quote.QuoteScreen
import com.example.mastermartini.viewmodel.AppShellViewModel

private const val TRANSITION_MS = 300

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    shellViewModel: AppShellViewModel = viewModel(factory = AppShellViewModel.Factory),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val quoteItemCount by shellViewModel.quoteItemCount.collectAsStateWithLifecycle()

    // Kept here, outside the bar, so the badge can still bounce after returning from a
    // screen without the bar (e.g. adding from product detail). -1 = nothing seen yet, so a
    // quote restored at app start does not bounce.
    var badgeSeenCount by rememberSaveable { mutableIntStateOf(-1) }
    LaunchedEffect(quoteItemCount) {
        if (badgeSeenCount < 0 || quoteItemCount < badgeSeenCount) badgeSeenCount = quoteItemCount
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentDestination.isAnyTab(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                BottomBar(
                    currentDestination = currentDestination,
                    quoteItemCount = quoteItemCount,
                    badgeSeenCount = badgeSeenCount,
                    onBadgeSeen = { badgeSeenCount = it },
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
                .padding(innerPadding)
                // Tell child imePadding() this space is already reserved, so the keyboard
                // height is not added on top of the bottom bar
                .consumeWindowInsets(innerPadding),
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
                LoginScreen(
                    onLoggedIn = { navController.navigateClearingStack(Route.Catalog) },
                    onGoToRegister = { navController.navigate(Route.Register) { launchSingleTop = true } },
                )
            }
            composable<Route.Register> {
                RegisterScreen(
                    onRegistered = { navController.navigateClearingStack(Route.Catalog) },
                    onBack = { navController.popBackStack() },
                )
            }

            // Main flow
            composable<Route.Catalog> {
                CatalogScreen(onProductClick = { id -> navController.navigate(Route.ProductDetail(productId = id)) })
            }
            composable<Route.ProductDetail> {
                ProductDetailScreen(
                    onBack = { navController.popBackStack() },
                    onViewQuote = { navController.navigateToTab(TopLevelTab.QUOTE) },
                )
            }
            composable<Route.Quote> {
                QuoteScreen(onGoToCatalog = { navController.navigateToTab(TopLevelTab.CATALOG) })
            }
            composable<Route.Contact> {
                ContactScreen()
            }
            composable<Route.Profile> {
                ProfileScreen(
                    onProductClick = { id -> navController.navigate(Route.ProductDetail(productId = id)) },
                    onGoToCatalog = { navController.navigateToTab(TopLevelTab.CATALOG) },
                    onLoggedOut = {
                        navController.navigateClearingStack(Route.Login)
                        // Tab switching saved each tab's state under Catálogo; drop it so the next
                        // user does not get this user's screens (e.g. the pre-filled contact form)
                        navController.clearBackStack<Route.Catalog>()
                    },
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
