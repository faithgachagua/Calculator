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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var expression by remember { mutableStateOf("") }
    var memory by remember { mutableDoubleStateOf(0.0) }

    val result = remember(expression) { evaluateExpression(expression) }

    fun clear() { expression = "" }
    fun backspace() { if (expression.isNotEmpty()) expression = expression.dropLast(1) }
    fun equals() { result?.let { expression = formatResult(it) } }

    fun appendOperator(op: String) {
        val last = expression.lastOrNull()
        when {
            expression.isEmpty() -> if (op == "-") expression = "-" // allow leading negative
            last != null && last in "+-×÷" -> {
                // Replace a trailing operator instead of stacking two — unless this is a
                // "-" right after another operator, which means a negative number, e.g. "5×-3"
                expression = if (op == "-" && last != '-') expression + op
                else expression.dropLast(1) + op
            }
            else -> expression += op
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression.ifEmpty { " " },
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = result?.let { formatResult(it) } ?: "0",
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { memory = 0.0 }) { Text("MC") }
            TextButton(onClick = { expression += formatResult(memory) }) { Text("MR") }
            TextButton(onClick = { result?.let { memory += it } }) { Text("M+") }
            TextButton(onClick = { result?.let { memory -= it } }) { Text("M-") }
        }

        val rows = listOf(
            listOf("C", "⌫", "(", ")"),
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "-"),
            listOf("±", "0", ".", "+"),
        )

        Column(modifier = Modifier.padding(8.dp)) {
            for (row in rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (label in row) {
                        CalcButton(
                            label = label,
                            modifier = Modifier.weight(1f).aspectRatio(1.3f).padding(4.dp),
                            onClick = {
                                when (label) {
                                    "C" -> clear()
                                    "⌫" -> backspace()
                                    "±" -> expression =
                                        if (expression.startsWith("-")) expression.removePrefix("-")
                                        else "-$expression"
                                    "+", "-", "×", "÷" -> appendOperator(label)
                                    "." -> {
                                        val segment = expression.takeLastWhile { it.isDigit() || it == '.' }
                                        if (!segment.contains(".")) expression += "."
                                    }
                                    else -> expression += label
                                }
                            }
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton(
                    label = "%",
                    modifier = Modifier.weight(1f).aspectRatio(1.3f).padding(4.dp),
                    onClick = { expression += "%" }
                )
                CalcButton(
                    label = "=",
                    modifier = Modifier.weight(3f).aspectRatio(3.9f).padding(4.dp),
                    highlighted = true,
                    onClick = { equals() }
                )
            }
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val isOperator = label in setOf("÷", "×", "-", "+", "=", "%")
    val containerColor = when {
        highlighted -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        highlighted -> MaterialTheme.colorScheme.onPrimary
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        label == "C" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Text(label, fontSize = 22.sp)
    }
}