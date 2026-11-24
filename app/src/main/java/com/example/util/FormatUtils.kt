package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val indianLocale = Locale("en", "IN")

    fun formatRupees(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(indianLocale).apply {
            minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            maximumFractionDigits = 2
        }
        return "₹${formatter.format(amount)}"
    }

    fun formatKg(kg: Double): String {
        return String.format(Locale.US, "%.2f kg", kg)
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDateShort(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun formatFileTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
        return sdf.format(Date())
    }
}
