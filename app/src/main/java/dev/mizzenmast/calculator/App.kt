package dev.mizzenmast.calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

private val screenTitles = listOf("SmartCalc", "Unit Converter", "Currency Exchange")
private val tabLabels = listOf("Calculator", "Converter", "Exchange")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState { tabLabels.size }
    val scope = rememberCoroutineScope()
    var exchangeRefreshTick by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitles[pagerState.currentPage]) },
                actions = {
                    if (pagerState.currentPage == 2) {
                        IconButton(onClick = { exchangeRefreshTick++ }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh rates")
                        }
                    } else {
                        IconButton(onClick = { /* reserved for future settings/menu */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabLabels.forEachIndexed { index, label ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(label) }
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> CalculatorScreen()
                    1 -> UnitConverterScreen()
                    else -> CurrencyExchangeScreen(refreshTrigger = exchangeRefreshTick)
                }
            }
        }
    }
}