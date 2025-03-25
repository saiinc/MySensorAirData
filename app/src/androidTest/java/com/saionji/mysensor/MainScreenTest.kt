package com.saionji.mysensor

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MainScreenTest: TestCase(Kaspresso.Builder.withComposeSupport()) {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    val mainScreen = MainScreen(composeTestRule)

    @Test
    fun checkTopBarText() {
        run {
            step("Check text") {
                mainScreen {
                    topBarTitle.assertTextContains("My Sensor")
                }
            }
        }
    }
}