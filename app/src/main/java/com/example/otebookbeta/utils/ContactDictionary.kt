package com.example.otebookbeta.utils

object ContactDictionary {

    fun categories(): Array<String> =
        arrayOf("Партнёры", "Клиенты", "Потенциальные")

    fun statusesForCategory(category: String?): Array<String> = when (category?.trim()) {
        "Партнёры", "Клиенты" -> arrayOf("Активный", "Пассивный", "Потерянный")
        "Потенциальные"       -> arrayOf("Холодный", "Тёплый", "Потерянный")
        else                  -> emptyArray()
    }

    fun socialNetworks(): Array<String> =
        arrayOf("Не выбрано", "Telegram", "Instagram", "ВК", "Одноклассники",
            "TikTok", "Facebook", "WhatsApp", "Skype", "MAX", "Другое")
}
