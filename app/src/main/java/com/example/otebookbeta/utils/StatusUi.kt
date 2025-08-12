package com.example.otebookbeta.utils

import androidx.annotation.ColorRes
import com.example.otebookbeta.R

object StatusUi {
    @ColorRes
    fun colorResFor(status: String?): Int = when (status?.trim()) {
        "Активный" -> R.color.green
        "Пассивный", "Тёплый" -> R.color.orange
        "Потерянный" -> R.color.red
        "Холодный" -> R.color.blue
        else -> android.R.color.transparent
    }
}
