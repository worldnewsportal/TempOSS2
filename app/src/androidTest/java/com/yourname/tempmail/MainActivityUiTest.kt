package com.yourname.tempmail

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun firstLaunchShowsOnboarding() {
        // A fresh install shows the onboarding screen (rule: explain before first use).
        // Resolve the localized string so the test works under any device locale.
        val welcome = ApplicationProvider.getApplicationContext<android.content.Context>()
            .getString(R.string.welcome_title)
        composeRule.onNodeWithText(welcome, substring = true).assertExists()
    }
}
