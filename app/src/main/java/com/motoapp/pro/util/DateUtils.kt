package com.motoapp.pro.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val fullFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    fun today(): String = apiFormat.format(Date())

    fun formatApi(date: Date): String = apiFormat.format(date)

    fun formatDisplay(dateStr: String): String {
        return try {
            val date = apiFormat.parse(dateStr) ?: return dateStr
            displayFormat.format(date)
        } catch (e: Exception) { dateStr }
    }

    fun formatFull(timestamp: Long): String = fullFormat.format(Date(timestamp))

    fun daysUntil(dateStr: String?): Int {
        if (dateStr == null) return 0
        return try {
            val due = apiFormat.parse(dateStr) ?: return 0
            val now = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
            ((due.time - now.time) / 86400000).toInt()
        } catch (e: Exception) { 0 }
    }

    fun inicioDoDia(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun inicioDaSemana(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return cal.timeInMillis
    }

    fun inicioDoMes(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return cal.timeInMillis
    }
}
