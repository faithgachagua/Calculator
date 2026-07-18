package dev.mizzenmast.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mizzenmast.calculator.components.KeypadAmountField

private data class UnitDef(
    val label: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

private data class UnitCategory(
    val name: String,
    val icon: ImageVector,
    val units: List<UnitDef>
)

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
private fun categoryAccent(category: UnitCategory): Color {
    val scheme = MaterialTheme.colorScheme

    return when (category.name) {
        "Length" -> Color(0xFF6B74E6)
        "Area" -> Color(0xFF66BB6A)
        "Volume" -> Color(0xFF42A5F5)
        "Speed" -> Color(0xFFFFA726)
        "Weight" -> Color(0xFFFFCA28)
        "Temperature" -> Color(0xFFAB47BC)
        "Power" -> Color(0xFFEF5350)
        "Pressure" -> Color(0xFF26A69A)
        else -> scheme.primary
    }
}

@Composable
fun UnitConverterScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<UnitCategory?>(null) }
    val category = selected

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (category == null) {
            CategoryGrid(
                modifier = Modifier,
                onSelect = { selected = it }
            )
        } else {
            CategoryDetail(
                modifier = Modifier,
                category = category,
                onBack = { selected = null }
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    modifier: Modifier = Modifier,
    onSelect: (UnitCategory) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Convert anything",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    color = scheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Quick and simple unit conversions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = scheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = categories,
                span = { category ->
                    if (category.name == "Volume") GridItemSpan(maxLineSpan) else GridItemSpan(1)
                }
            ) { category ->
                if (category.name == "Volume") {
                    VolumeCategoryCard(
                        category = category,
                        onClick = { onSelect(category) }
                    )
                } else {
                    CategoryCard(
                        category = category,
                        onClick = { onSelect(category) }
                    )
                }
            }

            item {
                Card(
                    onClick = { },
                    modifier = Modifier.aspectRatio(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = scheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(scheme.onSurfaceVariant.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = null,
                                tint = scheme.surface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "More",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "And more units",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: UnitCategory,
    onClick: () -> Unit
) {
    val accent = categoryAccent(category)
    val scheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = scheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = category.units
                        .take(3)
                        .joinToString(" • ") { shortUnit(it.label) },
                    style = MaterialTheme.typography.bodySmall,
                    color = accent,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun VolumeCategoryCard(
    category: UnitCategory,
    onClick: () -> Unit
) {
    val accent = categoryAccent(category)
    val scheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = category.units
                        .take(4)
                        .joinToString(" • ") { shortUnit(it.label) },
                    style = MaterialTheme.typography.bodySmall,
                    color = accent,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDetail(
    modifier: Modifier = Modifier,
    category: UnitCategory,
    onBack: () -> Unit
) {
    var fromIndex by remember { mutableIntStateOf(0) }
    var toIndex by remember {
        mutableIntStateOf(1.coerceAtMost(category.units.lastIndex))
    }
    var input by remember { mutableStateOf("100") }

    val recent = remember { mutableStateListOf<String>() }
    val fromUnit = category.units[fromIndex.coerceIn(category.units.indices)]
    val toUnit = category.units[toIndex.coerceIn(category.units.indices)]

    val result = input.toDoubleOrNull()?.let {
        toUnit.fromBase(fromUnit.toBase(it))
    }

    val accent = categoryAccent(category)
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(fromIndex, toIndex, input) {
        val r = result ?: return@LaunchedEffect
        val entry =
            "$input ${shortUnit(fromUnit.label)} → " +
                    "${formatResult(r)} ${shortUnit(toUnit.label)}"

        if (recent.firstOrNull() != entry) {
            recent.remove(entry)
            recent.add(0, entry)
            while (recent.size > 4) {
                recent.removeAt(recent.lastIndex)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        // Top App Bar Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = scheme.onBackground
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onBackground
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = scheme.onBackground
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))

                // Overlapping From/To Cards
                Box(contentAlignment = Alignment.Center) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ConversionPanel(
                            title = "From",
                            value = input,
                            accent = Color(0xFF5C6BC0), // Custom matching color from mockup
                            options = category.units.map { it.label },
                            selectedIndex = fromIndex,
                            onUnitSelected = { fromIndex = it },
                            onValueChange = { input = it },
                            isReadOnly = false
                        )

                        ConversionPanel(
                            title = "To",
                            value = result?.let(::formatResult) ?: "—",
                            accent = Color(0xFF43A047), // Green result text matching mockup
                            options = category.units.map { it.label },
                            selectedIndex = toIndex,
                            onUnitSelected = { toIndex = it },
                            isReadOnly = true
                        )
                    }

                    // Floating Swap Button
                    FilledIconButton(
                        onClick = {
                            val oldFrom = fromIndex
                            fromIndex = toIndex
                            toIndex = oldFrom
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(scheme.surface),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF5C6BC0)
                        )
                    ) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = "Swap units",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = scheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5C6BC0).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF5C6BC0),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "1 ${shortUnit(fromUnit.label)} = ${formatResult(fromUnit.toBase(1.0) / toUnit.toBase(1.0))} ${shortUnit(toUnit.label)}",
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Exact conversion",
                                color = Color(0xFF5C6BC0),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                if (recent.isNotEmpty()) {
                    Spacer(Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Conversions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onBackground
                        )
                        TextButton(onClick = { recent.clear() }) {
                            Text("Clear", color = Color(0xFF5C6BC0))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            items(recent) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5C6BC0).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = Color(0xFF5C6BC0),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${shortUnit(fromUnit.label)} → ${shortUnit(toUnit.label)}",
                                color = scheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Icon(
                                imageVector = Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Just now",
                                fontSize = 10.sp,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversionPanel(
    title: String,
    value: String,
    accent: Color,
    options: List<String>,
    selectedIndex: Int,
    onUnitSelected: (Int) -> Unit,
    onValueChange: ((String) -> Unit)? = null,
    isReadOnly: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReadOnly) Color(0xFFF1F8E9) else Color(0xFFF3E5F5).copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = title,
                    color = accent,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            if (isReadOnly) {
                Text(
                    text = value,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = accent
                )
            } else {
                KeypadAmountField(
                    label = "",
                    value = value,
                    onValueChange = onValueChange ?: {},
                    suffix = ""
                )
            }

            Spacer(Modifier.height(24.dp))

            // Custom Dropdown Box matching mockup
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                Card(
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = options.getOrElse(selectedIndex) { "" },
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium,
                            color = scheme.onSurface
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.SwapVert else Icons.Default.MoreHoriz,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEachIndexed { index, label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onUnitSelected(index)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
