package com.kyawzinlinn.app.alert.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

private const val SCREEN_MILLIS = 300
private const val FADE_IN_MILLIS = 250
private const val FADE_OUT_MILLIS = 120

private val DetailRoutes = setOf(
    NavigationDestination.AppDetail.route,
    NavigationDestination.AlarmConfig.route,
    NavigationDestination.Changelog.route,
)

private fun NavBackStackEntry.isDetail() = destination.route in DetailRoutes

typealias NavScope = AnimatedContentTransitionScope<NavBackStackEntry>

fun NavScope.appEnterTransition(): EnterTransition =
    if (targetState.isDetail()) {
        slideInHorizontally(tween(SCREEN_MILLIS)) { it / 4 } + fadeIn(tween(SCREEN_MILLIS))
    } else {
        fadeIn(tween(FADE_IN_MILLIS, delayMillis = FADE_OUT_MILLIS)) +
            scaleIn(tween(FADE_IN_MILLIS, delayMillis = FADE_OUT_MILLIS), initialScale = 0.98f)
    }

fun NavScope.appExitTransition(): ExitTransition =
    if (targetState.isDetail()) {
        slideOutHorizontally(tween(SCREEN_MILLIS)) { -it / 6 } + fadeOut(tween(SCREEN_MILLIS))
    } else {
        fadeOut(tween(FADE_OUT_MILLIS))
    }

fun NavScope.appPopEnterTransition(): EnterTransition =
    if (initialState.isDetail()) {
        slideInHorizontally(tween(SCREEN_MILLIS)) { -it / 6 } + fadeIn(tween(SCREEN_MILLIS))
    } else {
        fadeIn(tween(FADE_IN_MILLIS, delayMillis = FADE_OUT_MILLIS))
    }

fun NavScope.appPopExitTransition(): ExitTransition =
    if (initialState.isDetail()) {
        slideOutHorizontally(tween(SCREEN_MILLIS)) { it / 4 } + fadeOut(tween(SCREEN_MILLIS))
    } else {
        fadeOut(tween(FADE_OUT_MILLIS))
    }
