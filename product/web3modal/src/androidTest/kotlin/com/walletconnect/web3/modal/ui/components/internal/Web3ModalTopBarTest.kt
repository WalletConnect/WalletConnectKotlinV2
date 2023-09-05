package com.walletconnect.web3.modal.ui.components.internal

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.walletconnect.web3.modal.ui.components.internal.commons.BackArrowIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.components.internal.commons.TestTags
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class Web3ModalTopBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun web3ModalTopBar_titleShouldBeShown() {
        composeTestRule.setContent {
            ProvideWeb3ModalThemeComposition {
                Web3ModalTopBar(title = "Title", startIcon = { BackArrowIcon { } }, onCloseIconClick = {})
            }
        }

        composeTestRule.onNodeWithTag(TestTags.TITLE).assertExists()
        composeTestRule.onNodeWithText("Title").assertExists()
    }

    @Test
    fun web3ModalTopBar_onCloseCallbackIsTriggered() {
        var isClicked = false

        composeTestRule.setContent {
            ProvideWeb3ModalThemeComposition {
                Web3ModalTopBar(title = "Title", startIcon = { BackArrowIcon { } }, onCloseIconClick = { isClicked = true})
            }

        }
        composeTestRule.onNodeWithContentDescription(ContentDescription.CLOSE.description).performClick()

        assertEquals(isClicked, true)
    }
}