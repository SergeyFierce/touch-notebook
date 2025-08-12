package com.example.otebookbeta.utils

import kotlin.math.abs

object StringUtils {

    /**
     * Возвращает корректную форму слова для счётчика в заголовках списков:
     * 1 партнёр / 2 партнёра / 5 партнёров
     * 1 клиент / 2 клиента / 5 клиентов
     * 1 потенциальный / 2 потенциальных / 5 потенциальных
     */
    fun getCategoryWord(count: Int, category: String): String {
        val n = abs(count)
        return when (category) {
            "Партнёры" -> ruPlural(n, one = "партнёр", few = "партнёра", many = "партнёров")
            "Клиенты" -> ruPlural(n, one = "клиент", few = "клиента", many = "клиентов")
            "Потенциальные" -> ruPlural(n, one = "потенциальный", few = "потенциальных", many = "потенциальных")
            else -> ruPlural(n, one = "контакт", few = "контакта", many = "контактов")
        }
    }

    fun getCategorySingular(category: String): String {
        return when (category) {
            "Партнёры" -> "Партнёр"
            "Клиенты" -> "Клиент"
            "Потенциальные" -> "Потенциальный"
            else -> category
        }
    }

    /**
     * Русская плюрализация по правилам:
     * - 11..14  → many
     * - остаток %10 == 1 → one
     * - остаток %10 in 2..4 → few
     * - иначе → many
     */
    private fun ruPlural(count: Int, one: String, few: String, many: String): String {
        val mod100 = count % 100
        if (mod100 in 11..14) return many
        return when (count % 10) {
            1 -> one
            2, 3, 4 -> few
            else -> many
        }
    }
}
