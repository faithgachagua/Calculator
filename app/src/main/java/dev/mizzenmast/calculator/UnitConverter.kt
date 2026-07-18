package dev.mizzenmast.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class UnitDef(val label: String, val toBase: (Double) -> Double, val fromBase: (Double) -> Double)
private data class UnitCategory(val name: String, val icon: ImageVector, val units: List<UnitDef>)

private val categories = listOf(
    UnitCategory(
        "Length", Icons.Default.Straighten, listOf(
            UnitDef("Meter (m)", { it }, { it }),
            UnitDef("Kilometer (km)", { it * 1000 }, { it / 1000 }),
            UnitDef("Centimeter (cm)", { it / 100 }, { it * 100 }),
            UnitDef("Mile (mi)", { it * 1609.344 }, { it / 1609.344 }),
            UnitDef("Yard (yd)", { it * 0.9144 }, { it / 0.9144 }),
            UnitDef("Foot (ft)", { it * 0.3048 }, { it / 0.3048 }),
            UnitDef("Inch (in)", { it * 0.0254 }, { it / 0.0254 }),
        )
    ),
    UnitCategory(
        "Area", Icons.Default.SquareFoot, listOf(
            UnitDef("Square Meter (m²)", { it }, { it }),
            UnitDef("Square Kilometer (km²)", { it * 1_000_000 }, { it / 1_000_000 }),
            UnitDef("Square Foot (ft²)", { it * 0.092903 }, { it / 0.092903 }),
            UnitDef("Acre", { it * 4046.86 }, { it / 4046.86 }),
            UnitDef("Hectare (ha)", { it * 10_000 }, { it / 10_000 }),
        )
    ),
    UnitCategory(
        "Volume", Icons.Default.WaterDrop, listOf(
            UnitDef("Liter (L)", { it }, { it }),
            UnitDef("Milliliter (mL)", { it / 1000 }, { it * 1000 }),
            UnitDef("Gallon (US)", { it * 3.78541 }, { it / 3.78541 }),
            UnitDef("Cup", { it * 0.24 }, { it / 0.24 }),
        )
    ),
    UnitCategory(
        "Speed", Icons.Default.Speed, listOf(
            UnitDef("Meters/sec (m/s)", { it }, { it }),
            UnitDef("Kilometers/hour (km/h)", { it / 3.6 }, { it * 3.6 }),
            UnitDef("Miles/hour (mph)", { it * 0.44704 }, { it / 0.44704 }),
            UnitDef("Knots (kn)", { it * 0.514444 }, { it / 0.514444 }),
        )
    ),
    UnitCategory(
        "Weight", Icons.Default.FitnessCenter, listOf(
            UnitDef("Kilogram (kg)", { it }, { it }),
            UnitDef("Gram (g)", { it / 1000 }, { it * 1000 }),
            UnitDef("Pound (lb)", { it * 0.453592 }, { it / 0.453592 }),
            UnitDef("Ounce (oz)", { it * 0.0283495 }, { it / 0.0283495 }),
        )
    ),
    UnitCategory(
        "Temperature", Icons.Default.Thermostat, listOf(
            UnitDef("Celsius (°C)", { it }, { it }),
            UnitDef("Fahrenheit (°F)", { (it - 32) * 5 / 9 }, { it * 9 / 5 + 32 }),
            UnitDef("Kelvin (K)", { it - 273.15 }, { it + 273.15 }),
        )
    ),
    UnitCategory(
        "Power", Icons.Default.Bolt, listOf(
            UnitDef("Watt (W)", { it }, { it }),
            UnitDef("Kilowatt (kW)", { it * 1000 }, { it / 1000 }),
            UnitDef("Horsepower (hp)", { it * 745.7 }, { it / 745.7 }),
            UnitDef("BTU/hour", { it * 0.293071 }, { it / 0.293071 }),
        )
    ),
    UnitCategory(
        "Pressure", Icons.Default.Compress, listOf(
            UnitDef("Pascal (Pa)", { it }, { it }),
            UnitDef("Kilopascal (kPa)", { it * 1000 }, { it / 1000 }),
            UnitDef("Bar", { it * 100_000 }, { it / 100_000 }),
            UnitDef("PSI", { it * 6894.76 }, { it / 6894.76 }),
            UnitDef("Atmosphere (atm)", { it * 101_325 }, { it / 101_325 }),
        )
    ),
)

private fun shortUnit(label: String): String =
    label.substringAfter("(", "").substringBefore(")").ifEmpty { label }

@Composable
fun UnitConverterScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<UnitCategory?>(null) }

    val category = selected
    if (category == null) {
        CategoryGrid(modifier = modifier, onSelect = { selected = it })
    } else {
        CategoryDetail(modifier = modifier, category = category, onBack = { selected = null })
    }
}

@Composable
private fun CategoryGrid(modifier: Modifier = Modifier, onSelect: (UnitCategory) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { cat ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                onClick = { onSelect(cat) }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(cat.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 10.dp))
                    Text(cat.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDetail(modifier: Modifier = Modifier, category: UnitCategory, onBack: () -> Unit) {
    var fromIndex by remember { mutableIntStateOf(0) }
    var toIndex by remember { mutableIntStateOf(1.coerceAtMost(category.units.lastIndex)) }
    var input by remember { mutableStateOf("100") }
    val recent = remember { mutableStateListOf<String>() }

    val fromUnit = category.units[fromIndex.coerceIn(category.units.indices)]
    val toUnit = category.units[toIndex.coerceIn(category.units.indices)]
    val result = input.toDoubleOrNull()?.let { toUnit.fromBase(fromUnit.toBase(it)) }

    LaunchedEffect(fromIndex, toIndex) {
        val r = result ?: return@LaunchedEffect
        val entry = "$input ${shortUnit(fromUnit.label)}  →  ${formatResult(r)} ${shortUnit(toUnit.label)}"
        if (recent.firstOrNull() != entry) {
            recent.add(0, entry)
            while (recent.size > 4) recent.removeAt(recent.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to categories")
            }
            Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(category.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))

        SectionCard {
            Text("From", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Dropdown(category.units.map { it.label }, fromIndex) { fromIndex = it }
            Spacer(Modifier.height(10.dp))
            KeypadAmountField(
                label = "Amount",
                value = input,
                onValueChange = { input = it },
                suffix = shortUnit(fromUnit.label)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilledIconButton(onClick = { val f = fromIndex; fromIndex = toIndex; toIndex = f }) {
                Icon(Icons.Default.SwapVert, contentDescription = "Swap units")
            }
        }

        SectionCard {
            Text("To", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Dropdown(category.units.map { it.label }, toIndex) { toIndex = it }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = result?.let { formatResult(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text(shortUnit(toUnit.label)) },
                textStyle = MaterialTheme.typography.titleMedium
            )
        }

        if (recent.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Row {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text("Recent Conversions", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(8.dp))
            SectionCard(padding = 0.dp) {
                LazyColumn {
                    items(recent) { entry ->
                        Text(
                            entry,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(padding: androidx.compose.ui.unit.Dp = 16.dp, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(padding)) { content() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Dropdown(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = options.getOrElse(selectedIndex) { "" },
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, label ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(index); expanded = false })
            }
        }
    }
}