package com.pnc.jetpackcomposedemos.features.orders.presentation


import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pnc.jetpackcomposedemos.HiltTestActivity
import com.pnc.jetpackcomposedemos.features.auth.domain.SessionManager
import com.pnc.jetpackcomposedemos.features.auth.domain.User
import com.pnc.jetpackcomposedemos.testutil.builders.aMyOrder
import com.pnc.jetpackcomposedemos.testutil.builders.aMyOrderLine
import com.pnc.jetpackcomposedemos.testutil.builders.aMyOrdersResponse
import com.pnc.jetpackcomposedemos.testutil.fakes.FakeMyOrdersDataSource
import com.pnc.jetpackcomposedemos.ui.theme.KazooTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

@HiltAndroidTest
class PagedOrdersScreenIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var session: SessionManager
    @Inject
    lateinit var ordersApi: FakeMyOrdersDataSource

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun signInAs(id: String = "11000", name: String = "Cara Cole") {
        session.signIn(User(id = id, username = name, token = "test-token"))
    }

    private fun showScreen() {
        composeRule.setContent {
            KazooTheme { PagedOrdersScreen(useTwoPaneLayout = false) }
        }
    }

    // Paging loads on a background thread, which Compose cannot see on its own.
    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun httpError(code: Int) = HttpException(
        Response.error<Any>(code, "".toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun givenSignedInCustomerWithOrders_whenScreenOpens_thenTheOrdersAreListed() {
        ordersApi.response = aMyOrdersResponse(
            11000,
            aMyOrder(orderNumber = 1001, lines = listOf(aMyOrderLine(productName = "Road Bike"))),
            aMyOrder(orderNumber = 1002, lines = listOf(aMyOrderLine(lineId = 2, productName = "Helmet")))
        )
        signInAs()

        showScreen()

        waitForText("Order #:1001")
        composeRule.onNodeWithText("Order #:1002").assertIsDisplayed()
        composeRule.onNodeWithText("Road Bike").assertIsDisplayed()
        composeRule.onNodeWithText("Helmet").assertIsDisplayed()
    }

    @Test
    fun givenSignedInCustomer_whenScreenOpens_thenTheCustomersNameComesFromTheSession() {
        ordersApi.response = aMyOrdersResponse(11000, aMyOrder(1001), aMyOrder(1002))
        signInAs(name = "Cara Cole")

        showScreen()

        waitForText("Order #:1001")
        composeRule.onAllNodesWithText("Customer: Cara Cole").assertCountEquals(2)
    }

    @Test
    fun givenNoOrders_whenScreenOpens_thenTheEmptyMessageIsShown() {
        ordersApi.response = aMyOrdersResponse(11000)
        signInAs()

        showScreen()

        waitForText("No orders found")
    }

    @Test
    fun givenAnExpiredSession_whenScreenOpens_thenAnExplanationAndRetryAreShown() {
        ordersApi.failure = httpError(401)
        signInAs()

        showScreen()

        waitForText("Error: Your session has expired. Please sign in again.")
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun givenAFailedLoad_whenRetryIsTappedAfterTheServerRecovers_thenTheOrdersAppear() {
        ordersApi.failure = httpError(500)
        signInAs()
        showScreen()
        waitForText("Error: The server returned an error (500).")

        ordersApi.failure = null
        ordersApi.response = aMyOrdersResponse(11000, aMyOrder(1001))
        composeRule.onNodeWithText("Retry").performClick()

        waitForText("Order #:1001")
    }
}



