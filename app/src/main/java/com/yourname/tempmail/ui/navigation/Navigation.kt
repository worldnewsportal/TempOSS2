package com.yourname.tempmail.ui.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourname.tempmail.MainActivity
import com.yourname.tempmail.di.AppContainer
import com.yourname.tempmail.ui.TempmailAppViewModel
import com.yourname.tempmail.ui.UiEvent
import com.yourname.tempmail.ui.create.CreateScreen
import com.yourname.tempmail.ui.home.HomeScreen
import com.yourname.tempmail.ui.inbox.InboxScreen
import com.yourname.tempmail.ui.inbox.ReaderScreen
import com.yourname.tempmail.ui.onboarding.OnboardingScreen
import com.yourname.tempmail.ui.settings.SettingsScreen
import com.yourname.tempmail.ui.theme.TempmailTheme
import com.yourname.tempmail.ui.theme.ThemeMode
import kotlinx.coroutines.flow.collectLatest

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CREATE = "create"
    const val SETTINGS = "settings"
    const val INBOX = "inbox/{mailboxId}"
    const val READER = "inbox/{mailboxId}/message/{messageId}"

    fun inbox(mailboxId: Long) = "inbox/$mailboxId"
    fun reader(mailboxId: Long, messageId: Long) = "inbox/$mailboxId/message/$messageId"
}

@Composable
fun TempmailNavHost(
    container: AppContainer,
    appViewModel: TempmailAppViewModel,
) {
    val navController = rememberNavController()
    val locale by appViewModel.locale.collectAsStateWithLifecycle()
    val themeKey by appViewModel.themeMode.collectAsStateWithLifecycle()
    val onboardingDone by appViewModel.onboardingDone.collectAsStateWithLifecycle()

    val themeMode = remember(themeKey) {
        ThemeMode.entries.firstOrNull { it.key == themeKey } ?: ThemeMode.SYSTEM
    }

    LaunchedEffect(appViewModel) {
        appViewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.RestartActivity -> {
                    val context = navController.context
                    context.startActivity(
                        Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                }
                is UiEvent.NavigateCreate -> navController.navigate(Routes.CREATE)
                is UiEvent.NavigateSettings -> navController.navigate(Routes.SETTINGS)
                is UiEvent.NavigateMailbox -> navController.navigate(Routes.inbox(event.id))
                is UiEvent.NavigateMessage -> navController.navigate(Routes.reader(event.mailboxId, event.messageId))
                is UiEvent.NavigateMailboxList -> navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
                is UiEvent.NavigateBack -> navController.popBackStack()
                is UiEvent.ShowSnackbar, null -> {}
            }
            if (event != null && event !is UiEvent.RestartActivity) appViewModel.consumeEvent()
        }
    }

    TempmailTheme(themeMode = themeMode) {
        NavHost(
            navController = navController,
            startDestination = if (onboardingDone) Routes.HOME else Routes.ONBOARDING,
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onDone = {
                        if (!appViewModel.adsConsentShown.value) {
                            appViewModel.setAdsConsent(accepted = false, hasReviewed = true)
                        }
                        appViewModel.finishOnboarding()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    container = container,
                    onOpenMailbox = { appViewModel.emit(UiEvent.NavigateMailbox(it.id)) },
                    onCreate = { appViewModel.emit(UiEvent.NavigateCreate) },
                    onSettings = { appViewModel.emit(UiEvent.NavigateSettings) },
                )
            }
            composable(Routes.CREATE) {
                CreateScreen(
                    container = container,
                    onDone = { appViewModel.emit(UiEvent.NavigateBack) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    container = container,
                    appViewModel = appViewModel,
                    onBack = { appViewModel.emit(UiEvent.NavigateBack) },
                )
            }
            composable(
                route = Routes.INBOX,
                arguments = listOf(navArgument("mailboxId") { type = NavType.LongType }),
            ) { entry ->
                val mailboxId = entry.arguments?.getLong("mailboxId") ?: 0L
                InboxScreen(
                    container = container,
                    mailboxId = mailboxId,
                    onMessage = { msgId -> appViewModel.emit(UiEvent.NavigateMessage(mailboxId, msgId)) },
                    onBack = { appViewModel.emit(UiEvent.NavigateBack) },
                )
            }
            composable(
                route = Routes.READER,
                arguments = listOf(
                    navArgument("mailboxId") { type = NavType.LongType },
                    navArgument("messageId") { type = NavType.LongType },
                ),
            ) { entry ->
                ReaderScreen(
                    container = container,
                    mailboxId = entry.arguments?.getLong("mailboxId") ?: 0L,
                    messageId = entry.arguments?.getLong("messageId") ?: 0L,
                    onBack = { appViewModel.emit(UiEvent.NavigateBack) },
                )
            }
        }
    }
}