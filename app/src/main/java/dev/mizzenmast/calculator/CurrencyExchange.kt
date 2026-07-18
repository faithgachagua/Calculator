package dev.mizzenmast.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration.Companion.milliseconds

// Free, no-key FX API covering ~160 currencies: https://www.exchangerate-api.com/docs/free
private suspend fun fetchRatesForBase(base: String): Map<String, Double> = withContext(Dispatchers.IO) {
    val connection = URL("https://open.er-api.com/v6/latest/$base").openConnection() as HttpURLConnection
    connection.connectTimeout = 8000
    connection.readTimeout = 8000
    try {
        val json = JSONObject(connection.inputStream.bufferedReader().readText())
        if (json.optString("result") != "success") error("API returned an error")
        val ratesObj = json.getJSONObject("rates")
        val map = LinkedHashMap<String, Double>()
        ratesObj.keys().forEach { key -> map[key] = ratesObj.getDouble(key) }
        map
    } finally {
        connection.disconnect()
    }
}

private val favoriteCurrencies = listOf("USD", "EUR", "GBP", "TZS", "UGX")

private val flagMap = mapOf(
    "USD" to "🇺🇸", "EUR" to "🇪🇺", "GBP" to "🇬🇧", "KES" to "🇰🇪", "TZS" to "🇹🇿", "UGX" to "🇺🇬",
    "JPY" to "🇯🇵", "CNY" to "🇨🇳", "INR" to "🇮🇳", "AUD" to "🇦🇺", "CAD" to "🇨🇦", "CHF" to "🇨🇭",
    "ZAR" to "🇿🇦", "NGN" to "🇳🇬", "GHS" to "🇬🇭", "AED" to "🇦🇪", "SAR" to "🇸🇦", "BRL" to "🇧🇷",
    "MXN" to "🇲🇽", "SEK" to "🇸🇪", "NOK" to "🇳🇴", "SGD" to "🇸🇬", "HKD" to "🇭🇰", "NZD" to "🇳🇿",
    "RWF" to "🇷🇼", "ETB" to "🇪🇹", "EGP" to "🇪🇬", "MAD" to "🇲🇦", "XOF" to "🌍", "XAF" to "🌍",
)
private fun flag(code: String) = flagMap[code] ?: "💱"

private val currencyNames = mapOf(
    "USD" to "US Dollar", "EUR" to "Euro", "GBP" to "British Pound", "KES" to "Kenyan Shilling",
    "TZS" to "Tanzanian Shilling", "UGX" to "Ugandan Shilling", "JPY" to "Japanese Yen",
    "CNY" to "Chinese Yuan", "INR" to "Indian Rupee", "AUD" to "Australian Dollar",
    "CAD" to "Canadian Dollar", "CHF" to "Swiss Franc", "ZAR" to "South African Rand",
    "NGN" to "Nigerian Naira", "GHS" to "Ghanaian Cedi", "AED" to "UAE Dirham",
    "SAR" to "Saudi Riyal", "BRL" to "Brazilian Real", "MXN" to "Mexican Peso",
    "SEK" to "Swedish Krona", "NOK" to "Norwegian Krone", "SGD" to "Singapore Dollar",
    "HKD" to "Hong Kong Dollar", "NZD" to "New Zealand Dollar", "RWF" to "Rwandan Franc",
    "ETB" to "Ethiopian Birr", "EGP" to "Egyptian Pound", "MAD" to "Moroccan Dirham",
)
private fun currencyName(code: String) = currencyNames[code] ?: code

private fun elapsedLabel(sinceMillis: Long): String {
    if (sinceMillis == 0L) return ""
    val seconds = (System.currentTimeMillis() - sinceMillis) / 1000
    return when {
        seconds < 60 -> "Updated just now"
        seconds < 3600 -> "Updated ${seconds / 60} min ago"
        else -> "Updated ${seconds / 3600}h ago"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyExchangeScreen(modifier: Modifier = Modifier, refreshTrigger: Int = 0) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var ratesForBase by remember { mutableStateOf<Map<String, Double>?>(null) }
    var from by remember { mutableStateOf("KES") }
    var to by remember { mutableStateOf("USD") }
    var amount by remember { mutableStateOf("100") }
    var previousRate by remember { mutableStateOf<Double?>(null) }
    var lastUpdated by remember { mutableLongStateOf(0L) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var tick by remember { mutableIntStateOf(0) }
    var pickerFor by remember { mutableStateOf<String?>(null) } // "from" | "to" | null

    fun refresh() {
        if (!isOnline(context)) {
            error = "No internet connection"
            loading = false
            return
        }
        loading = true
        error = null
        scope.launch {
            try {
                val newRates = fetchRatesForBase(from)
                previousRate = ratesForBase?.get(to)
                ratesForBase = newRates
                lastUpdated = System.currentTimeMillis()
            } catch (e: Exception) {
                error = "Couldn't load rates — ${e.message ?: "check your connection"}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(from, refreshTrigger) { refresh() }
    LaunchedEffect(Unit) { while (true) { delay(15_000.milliseconds); tick++ } }

    val rate = ratesForBase?.get(to)
    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val converted = rate?.let { it * amountValue }
    val trend = when {
        previousRate == null || rate == null -> null
        rate > previousRate!! -> "up"
        rate < previousRate!! -> "down"
        else -> "flat"
    }
    val availableCurrencies = ratesForBase?.keys?.sorted()
        ?: listOf("USD", "EUR", "GBP", "KES", "TZS", "UGX", "NGN", "ZAR", "INR", "JPY")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        KeypadAmountField(label = "Amount", value = amount, onValueChange = { amount = it })

        Spacer(Modifier.height(16.dp))
        Text("From", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        CurrencyRow(from) { pickerFor = "from" }

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            FilledIconButton(onClick = { val t = from; from = to; to = t }) {
                Icon(Icons.Default.SwapVert, contentDescription = "Swap currencies")
            }
        }

        Text("To", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        CurrencyRow(to) { pickerFor = "to" }

        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                when {
                    loading -> CircularProgressIndicator()
                    error != null -> Column {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { refresh() }) { Text("Retry") }
                    }
                    converted != null -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "= ${formatResult(converted)} $to",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            when (trend) {
                                "up" -> Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Rising", tint = MaterialTheme.colorScheme.secondary)
                                "down" -> Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = "Falling", tint = MaterialTheme.colorScheme.error)
                                "flat" -> Icon(Icons.AutoMirrored.Filled.TrendingFlat, contentDescription = "Steady", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("1 $from = ${formatResult(rate ?: 0.0)} $to", style = MaterialTheme.typography.bodySmall)
                        if (lastUpdated != 0L) {
                            val label = remember(tick, lastUpdated) { elapsedLabel(lastUpdated) }
                            Spacer(Modifier.height(2.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Favorites", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(48.dp)) {
            items(favoriteCurrencies) { code ->
                AssistChip(
                    onClick = { to = code },
                    label = { Text("${flag(code)} $code") },
                    shape = RoundedCornerShape(12.dp),
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }

    if (pickerFor != null) {
        CurrencyPickerSheet(
            currencies = availableCurrencies,
            selected = if (pickerFor == "from") from else to,
            onSelect = { code ->
                if (pickerFor == "from") from = code else to = code
                pickerFor = null
            },
            onDismiss = { pickerFor = null }
        )
    }
}

@Composable
private fun CurrencyRow(code: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(flag(code), fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(code, fontWeight = FontWeight.SemiBold)
                Text(currencyName(code), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    currencies: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, currencies) {
        if (query.isBlank()) currencies
        else currencies.filter {
            it.contains(query, ignoreCase = true) || currencyName(it).contains(query, ignoreCase = true)
        }
    }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Choose a currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by code or name") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.height(420.dp)) {
                items(filtered) { code ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (code == selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background)
                            .clickable { onSelect(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(flag(code), fontSize = 20.sp, modifier = Modifier.width(36.dp))
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        ) {
                            Text(code, fontWeight = FontWeight.SemiBold)
                            Text(currencyName(code), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (code == selected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}