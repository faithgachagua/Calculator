package dev.mizzenmast.calculator

import java.util.Locale
import kotlin.math.roundToLong

// ---------------------------------------------------------------------------------
// Shunting-yard expression evaluator (no third-party math libs needed)
// ---------------------------------------------------------------------------------

private fun precedence(op: String) = when (op) {
    "+", "-" -> 1
    "×", "÷" -> 2
    "u-" -> 3
    else -> 0
}

private fun tokenize(expr: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < expr.length) {
        val c = expr[i]
        when {
            c.isDigit() || c == '.' -> {
                val start = i
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                tokens += expr.substring(start, i)
            }
            c in "+-×÷()%" -> {
                tokens += c.toString()
                i++
            }
            else -> i++ // ignore stray characters/spaces
        }
    }
    return tokens
}

/** Converts infix tokens to Reverse Polish Notation using the shunting-yard algorithm. */
private fun toRPN(tokens: List<String>): List<String> {
    val output = mutableListOf<String>()
    val ops = ArrayDeque<String>()
    var prev: String? = null

    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> output += token

            token == "(" -> ops.addLast(token)

            token == ")" -> {
                while (ops.isNotEmpty() && ops.last() != "(") output += ops.removeLast()
                if (ops.isNotEmpty()) ops.removeLast() // discard "("
            }

            // "%" behaves as an immediate postfix "divide by 100"
            token == "%" -> output += token

            // unary minus: "-" at the start, or right after another operator/"("
            token == "-" && (prev == null || prev in setOf("+", "-", "×", "÷", "(")) -> {
                ops.addLast("u-")
            }

            else -> { // binary operator
                while (ops.isNotEmpty() && ops.last() != "(" && precedence(ops.last()) >= precedence(token)) {
                    output += ops.removeLast()
                }
                ops.addLast(token)
            }
        }
        prev = token
    }
    while (ops.isNotEmpty()) output += ops.removeLast()
    return output
}

private fun evalRPN(rpn: List<String>): Double {
    val stack = ArrayDeque<Double>()
    for (token in rpn) {
        when (token) {
            "u-" -> stack.addLast(-stack.removeLast())
            "%" -> stack.addLast(stack.removeLast() / 100.0)
            "+", "-", "×", "÷" -> {
                val b = stack.removeLast()
                val a = stack.removeLast()
                stack.addLast(
                    when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "×" -> a * b
                        else -> if (b != 0.0) a / b else Double.NaN
                    }
                )
            }
            else -> stack.addLast(token.toDouble())
        }
    }
    return stack.lastOrNull() ?: 0.0
}

fun evaluateExpression(expr: String): Double? = try {
    if (expr.isBlank()) null else evalRPN(toRPN(tokenize(expr)))
} catch (e: Exception) {
    null
}

fun formatResult(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "Error"
    return if (value == value.roundToLong().toDouble() && kotlin.math.abs(value) < 1e15) {
        value.roundToLong().toString()
    } else {
        String.format(Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')
    }
}