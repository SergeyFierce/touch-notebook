package com.example.otebookbeta.utils

import android.util.Patterns

object ValidationUtils {

    /** Возвращает текст ошибки или null, если всё ок */
    fun nameError(name: String?): String? {
        val n = name?.trim().orEmpty()
        if (n.isEmpty()) return "Поле ФИО обязательно"
        if (n.length !in 2..100) return "ФИО должно быть от 2 до 100 символов"
        if (!n.matches(Regex("^[А-Яа-яA-Za-z\\s\\-']+$")))
            return "ФИО может содержать только буквы, пробелы, дефисы и апострофы"
        return null
    }

    fun phoneError(phoneFormatted: String?): String? {
        val digits = phoneFormatted?.replace("[^0-9]".toRegex(), "").orEmpty()
        if (digits.isEmpty()) return null
        return if (digits.length == 11 && digits.startsWith("7")) null
        else "Неверный формат российского телефона (+7 (XXX) XXX-XX-XX)"
    }

    fun emailError(email: String?): String? {
        val e = email?.trim().orEmpty()
        if (e.isEmpty()) return null
        return if (Patterns.EMAIL_ADDRESS.matcher(e).matches()) null
        else "Неверный формат email"
    }

    fun professionError(text: String?): String? {
        val t = text?.trim().orEmpty()
        if (t.isEmpty()) return null
        if (t.length !in 2..50) return "Профессия должна быть от 2 до 50 символов"
        if (!t.matches(Regex("^[А-Яа-яA-Za-z\\s\\-]+$")))
            return "Профессия может содержать только буквы, пробелы и дефисы"
        return null
    }

    fun cityError(text: String?): String? {
        val t = text?.trim().orEmpty()
        if (t.isEmpty()) return null
        if (t.length !in 2..50) return "Город должен быть от 2 до 50 символов"
        if (!t.matches(Regex("^[А-Яа-яA-Za-z\\s\\-]+$")))
            return "Город может содержать только буквы, пробелы и дефисы"
        return null
    }

    fun categoryError(text: String?): String? =
        if (text.isNullOrBlank()) "Поле Категория обязательно" else null

    fun statusError(text: String?): String? =
        if (text.isNullOrBlank()) "Поле Статус обязательно" else null

    fun dateError(text: String?): String? =
        if (text.isNullOrBlank()) "Поле Дата добавления обязательно" else null
}
