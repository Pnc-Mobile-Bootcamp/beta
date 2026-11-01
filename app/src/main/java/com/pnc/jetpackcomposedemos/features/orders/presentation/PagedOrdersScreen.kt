package com.pnc.jetpackcomposedemos.features.orders.presentation


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.pnc.jetpackcomposedemos.R
import com.pnc.jetpackcomposedemos.features.orders.domain.totalsByMonth
import retrofit2.HttpException

@Composable
private fun errorText(error: Throwable): String =
    when ((error as? HttpException)?.code()) {
        401 -> stringResource(R.string.orders_error_expired)
        403 -> stringResource(R.string.orders_error_forbidden)
        null -> error.message ?: stringResource(R.string.orders_error_unknown)
        else -> stringResource(R.string.orders_error_server, (error as HttpException).code())
    }

@Composable
fun PagedOrdersScreen(
    useTwoPaneLayout: Boolean,
    viewModel: PagedOrdersViewModel = hiltViewModel()
) {
    // A LazyPagingItems looks like a list, but it asks the view model's flow for the next page when scrolled near the end.
    val orders = viewModel.orders.collectAsLazyPagingItems()
    val refresh = orders.loadState.refresh
    val append = orders.loadState.append

    val snackbarHostState = remember { SnackbarHostState() }
    val retryLabel = stringResource(R.string.common_retry)

    // If a refresh fails while orders are already on screen, keep them and say why.
    val refreshError = (refresh as? LoadState.Error)?.error
    val refreshErrorText = refreshError?.let { errorText(it) }
    LaunchedEffect(refreshError) {
        if (refreshErrorText != null && orders.itemCount > 0) {
            val result = snackbarHostState.showSnackbar(
                message = refreshErrorText,
                actionLabel = retryLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) orders.retry()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            orders.itemCount == 0 && refresh is LoadState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            orders.itemCount == 0 && refresh is LoadState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.common_error, errorText(refresh.error)),
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { orders.retry() }) {
                        Text(retryLabel)
                    }
                }
            }

            orders.itemCount == 0 -> {
                Text(
                    text = stringResource(R.string.orders_empty),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = refresh is LoadState.Loading,
                    onRefresh = { orders.refresh() }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val totals = remember(orders.itemCount) {
                                orders.itemSnapshotList.items.totalsByMonth()
                            }
                            OrdersChartCard(totals = totals)
                        }

                        items(
                            count = orders.itemCount,
                            key = orders.itemKey { order -> order.id }
                        ) { index ->
                            orders[index]?.let { order ->
                                OrderCard(
                                    order = order,
                                    useTwoPaneLayout = useTwoPaneLayout
                                )
                            }
                        }

                        // The footer row: a spinner while the next page loads, or a retry if it failed.
                        item {
                            when (append) {
                                is LoadState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                is LoadState.Error -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = errorText(append.error),
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { orders.retry() }) {
                                            Text(retryLabel)
                                        }
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}



