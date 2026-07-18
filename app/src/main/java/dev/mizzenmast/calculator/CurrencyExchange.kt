package dev.mizzenmast.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mizzenmast.calculator.components.KeypadAmountField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration.Companion.milliseconds


// -----------------------------------------------------------------------------
// NETWORK
// -----------------------------------------------------------------------------

private suspend fun fetchRatesForBase(
    base: String
): Map<String, Double> = withContext(Dispatchers.IO) {

    val connection =
        URL("https://open.er-api.com/v6/latest/$base")
            .openConnection() as HttpURLConnection

    connection.connectTimeout = 8000
    connection.readTimeout = 8000

    try {
        val json = JSONObject(
            connection.inputStream
                .bufferedReader()
                .readText()
        )

        if (json.optString("result") != "success") {
            error("API returned an error")
        }

        val ratesObject = json.getJSONObject("rates")
        val map = LinkedHashMap<String, Double>()

        ratesObject.keys().forEach { key ->
            map[key] = ratesObject.getDouble(key)
        }

        map
    } finally {
        connection.disconnect()
    }
}


// -----------------------------------------------------------------------------
// CURRENCY DATA
// -----------------------------------------------------------------------------

private val favoriteCurrencies = listOf(
    "USD",
    "EUR",
    "GBP",
    "TZS",
    "UGX"
)

private val flagMap = mapOf(
    "USD" to "🇺🇸",
    "EUR" to "🇪🇺",
    "GBP" to "🇬🇧",
    "KES" to "🇰🇪",
    "TZS" to "🇹🇿",
    "UGX" to "🇺🇬",
    "JPY" to "🇯🇵",
    "CNY" to "🇨🇳",
    "INR" to "🇮🇳",
    "AUD" to "🇦🇺",
    "CAD" to "🇨🇦",
    "CHF" to "🇨🇭",
    "ZAR" to "🇿🇦",
    "NGN" to "🇳🇬",
    "GHS" to "🇬🇭",
    "AED" to "🇦🇪",
    "SAR" to "🇸🇦",
    "BRL" to "🇧🇷",
    "MXN" to "🇲🇽",
    "SEK" to "🇸🇪",
    "NOK" to "🇳🇴",
    "SGD" to "🇸🇬",
    "HKD" to "🇭🇰",
    "NZD" to "🇳🇿",
    "RWF" to "🇷🇼",
    "ETB" to "🇪🇹",
    "EGP" to "🇪🇬",
    "MAD" to "🇲🇦",
    "XOF" to "🌍",
    "XAF" to "🌍"
)

private fun flag(code: String): String {
    return flagMap[code] ?: "💱"
}

private val currencyNames = mapOf(
    "USD" to "US Dollar",
    "EUR" to "Euro",
    "GBP" to "British Pound",
    "KES" to "Kenyan Shilling",
    "TZS" to "Tanzanian Shilling",
    "UGX" to "Ugandan Shilling",
    "JPY" to "Japanese Yen",
    "CNY" to "Chinese Yuan",
    "INR" to "Indian Rupee",
    "AUD" to "Australian Dollar",
    "CAD" to "Canadian Dollar",
    "CHF" to "Swiss Franc",
    "ZAR" to "South African Rand",
    "NGN" to "Nigerian Naira",
    "GHS" to "Ghanaian Cedi",
    "AED" to "UAE Dirham",
    "SAR" to "Saudi Riyal",
    "BRL" to "Brazilian Real",
    "MXN" to "Mexican Peso",
    "SEK" to "Swedish Krona",
    "NOK" to "Norwegian Krone",
    "SGD" to "Singapore Dollar",
    "HKD" to "Hong Kong Dollar",
    "NZD" to "New Zealand Dollar",
    "RWF" to "Rwandan Franc",
    "ETB" to "Ethiopian Birr",
    "EGP" to "Egyptian Pound",
    "MAD" to "Moroccan Dirham"
)

private fun currencyName(code: String): String {
    return currencyNames[code] ?: code
}


// -----------------------------------------------------------------------------
// HELPERS
// -----------------------------------------------------------------------------

private fun elapsedLabel(sinceMillis: Long): String {
    if (sinceMillis == 0L) return ""

    val seconds =
        (System.currentTimeMillis() - sinceMillis) / 1000

    return when {
        seconds < 60 -> "Updated just now"
        seconds < 3600 -> "Updated ${seconds / 60} min ago"
        else -> "Updated ${seconds / 3600}h ago"
    }
}


// -----------------------------------------------------------------------------
// SCREEN
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyExchangeScreen(
    modifier: Modifier = Modifier,
    refreshTrigger: Int = 0
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var ratesForBase by remember {
        mutableStateOf<Map<String, Double>?>(null)
    }

    var from by remember {
        mutableStateOf("KES")
    }

    var to by remember {
        mutableStateOf("USD")
    }

    var amount by remember {
        mutableStateOf("100")
    }

    var previousRate by remember {
        mutableStateOf<Double?>(null)
    }

    var lastUpdated by remember {
        mutableLongStateOf(0L)
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var tick by remember {
        mutableIntStateOf(0)
    }

    var pickerFor by remember {
        mutableStateOf<String?>(null)
    }

    fun refresh() {
        if (!isOnline(context)) {
            error = "No internet connection"
            loading = false
            return
        }

        loading = true
        error = null
        ratesForBase = null

        scope.launch {
            try {
                val newRates = fetchRatesForBase(from)

                previousRate = ratesForBase?.get(to)
                ratesForBase = newRates
                lastUpdated = System.currentTimeMillis()

            } catch (e: Exception) {
                error =
                    "Couldn't load rates — ${e.message ?: "check your connection"}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(from, refreshTrigger) {
        refresh()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000.milliseconds)
            tick++
        }
    }

    val rate = ratesForBase?.get(to)

    val amountValue =
        amount.toDoubleOrNull() ?: 0.0

    val converted =
        rate?.let { it * amountValue }

    val trend = when {
        previousRate == null || rate == null -> null
        rate > previousRate!! -> "up"
        rate < previousRate!! -> "down"
        else -> "flat"
    }

    val availableCurrencies =
        ratesForBase?.keys?.sorted()
            ?: listOf(
                "USD",
                "EUR",
                "GBP",
                "KES",
                "TZS",
                "UGX",
                "NGN",
                "ZAR",
                "INR",
                "JPY"
            )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        // ---------------------------------------------------------------------
        // AMOUNT
        // ---------------------------------------------------------------------

        KeypadAmountField(
            label = "Amount",
            value = amount,
            onValueChange = { amount = it }
        )

        Spacer(Modifier.height(16.dp))

        // ---------------------------------------------------------------------
        // CURRENCY SELECTION
        // ---------------------------------------------------------------------

        CurrencySelectionCard(
            from = from,
            to = to,
            onFromClick = {
                pickerFor = "from"
            },
            onToClick = {
                pickerFor = "to"
            },
            onSwap = {
                val temp = from
                from = to
                to = temp
            }
        )

        Spacer(Modifier.height(16.dp))

        // ---------------------------------------------------------------------
        // RESULT
        // ---------------------------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "CONVERTED AMOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(Modifier.height(8.dp))

                when {
                    loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                refresh()
                            }
                        ) {
                            Text("Retry")
                        }
                    }

                    converted != null -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${formatResult(converted)} $to",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            when (trend) {
                                "up" -> Icon(
                                    Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = "Rising"
                                )

                                "down" -> Icon(
                                    Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = "Falling"
                                )

                                "flat" -> Icon(
                                    Icons.AutoMirrored.Filled.TrendingFlat,
                                    contentDescription = "Steady"
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "1 $from = ${formatResult(rate)} $to",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (lastUpdated != 0L) {
                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = remember(
                                    tick,
                                    lastUpdated
                                ) {
                                    elapsedLabel(lastUpdated)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---------------------------------------------------------------------
        // FAVORITES
        // ---------------------------------------------------------------------

        Text(
            text = "Favorites",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favoriteCurrencies) { code ->
                AssistChip(
                    onClick = {
                        to = code
                    },
                    label = {
                        Text("${flag(code)}  $code")
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // CURRENCY PICKER
    // -------------------------------------------------------------------------

    if (pickerFor != null) {
        CurrencyPickerSheet(
            currencies = availableCurrencies,
            selected = if (pickerFor == "from") from else to,
            onSelect = { code ->
                if (pickerFor == "from") {
                    from = code
                } else {
                    to = code
                }

                pickerFor = null
            },
            onDismiss = {
                pickerFor = null
            }
        )
    }
}


// -----------------------------------------------------------------------------
// CURRENCY SELECTION CARD
// -----------------------------------------------------------------------------

@Composable
private fun CurrencySelectionCard(
    from: String,
    to: String,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onSwap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            CurrencyRow(
                label = "FROM",
                code = from,
                onClick = onFromClick
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = onSwap,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap currencies"
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            CurrencyRow(
                label = "TO",
                code = to,
                onClick = onToClick
            )
        }
    }
}


// -----------------------------------------------------------------------------
// CURRENCY ROW
// -----------------------------------------------------------------------------

@Composable
private fun CurrencyRow(
    label: String,
    code: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.width(48.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = flag(code),
                fontSize = 24.sp
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = code,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Text(
                    text = currencyName(code),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}


// -----------------------------------------------------------------------------
// CURRENCY PICKER
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    currencies: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember {
        mutableStateOf("")
    }

    val filtered = remember(query, currencies) {
        if (query.isBlank()) {
            currencies
        } else {
            currencies.filter { code ->
                code.contains(query, ignoreCase = true) ||
                        currencyName(code)
                            .contains(query, ignoreCase = true)
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Choose a currency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            // Search
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    BasicTextField(
                        value = query,
                        onValueChange = {
                            query = it
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                horizontal = 10.dp,
                                vertical = 13.dp
                            ),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search by code or name",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            innerTextField()
                        }
                    )

                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    query = ""
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Currency list
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No currencies found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = filtered,
                        key = { it }
                    ) { code ->

                        val isSelected = code == selected

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme
                                            .secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme
                                            .surfaceVariant
                                    }
                                )
                                .clickable {
                                    onSelect(code)
                                }
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = flag(code),
                                fontSize = 22.sp,
                                modifier = Modifier.width(40.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = code,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = currencyName(code),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme
                                        .onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}