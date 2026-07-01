package com.motoapp.pro.util

object CentimosFormatter {
    fun format(cents: Long): String {
        val abs = kotlin.math.abs(cents)
        val reais = abs / 100
        val centavos = abs % 100
        val formatted = "R$ ${reais.let { it.formatWithSeparator() }},${centavos.toString().padStart(2, '0')}"
        return if (cents < 0) "-$formatted" else formatted
    }

    fun parse(value: String): Long {
        val cleaned = value.replace("R$ ", "").replace(".", "").replace(",", ".").trim()
        val parsed = cleaned.toDoubleOrNull() ?: 0.0
        return (parsed * 100).toLong()
    }

    fun formatShort(cents: Long): String {
        val abs = kotlin.math.abs(cents)
        return when {
            abs >= 100000 -> "R$ ${abs / 100000},${(abs % 100000) / 1000}mil"
            else -> format(cents)
        }
    }

    private fun Long.formatWithSeparator(): String {
        val str = this.toString()
        val sb = StringBuilder()
        var count = 0
        for (i in str.lastIndex downTo 0) {
            if (count > 0 && count % 3 == 0) sb.insert(0, '.')
            sb.insert(0, str[i])
            count++
        }
        return sb.toString()
    }
}
