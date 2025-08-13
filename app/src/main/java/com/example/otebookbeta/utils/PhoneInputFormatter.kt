package com.example.otebookbeta.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Formats Russian phone numbers as "+7 (XXX) XXX-XX-XX" while typing.
 * Also validates the number and shows error in the given layout.
 */
fun setupPhoneInput(
    editText: EditText,
    layout: TextInputLayout,
    onTextChanged: (() -> Unit)? = null
) {
    editText.addTextChangedListener(object : TextWatcher {
        private var isFormatting = false
        private var lastFormatted = ""
        private val maxLength = 18 // +7 (XXX) XXX-XX-XX

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isFormatting) return
            isFormatting = true

            val input = s.toString().replace("[^0-9]".toRegex(), "")
            var digits = when {
                input.isEmpty() -> ""
                input.startsWith("8") -> "7${input.substring(1)}"
                input.startsWith("9") && input.length <= 10 -> "7$input"
                input.startsWith("7") -> input
                else -> "7$input"
            }

            if (digits == "7" && (s?.length ?: 0) <= 3 && lastFormatted.isNotEmpty()) {
                digits = ""
            }

            val formatted = buildFormattedPhone(digits)
                .let { if (it.length > maxLength) it.substring(0, maxLength) else it }
            val cursorPosition = (editText.selectionStart + (formatted.length - (s?.length ?: 0)))
                .coerceIn(0, formatted.length)

            if (formatted != lastFormatted) {
                lastFormatted = formatted
                editText.setText(formatted)
                editText.setSelection(cursorPosition)
            }

            val cleanPhone = digits
            val isValid = cleanPhone.isEmpty() || (cleanPhone.length == 11 && cleanPhone.startsWith("7"))
            layout.error = if (!isValid && cleanPhone.isNotEmpty())
                "Неверный формат российского телефона (+7 (XXX) XXX-XX-XX)"
            else null

            isFormatting = false
            onTextChanged?.invoke()
        }
    })
}

private fun buildFormattedPhone(digits: String): String {
    if (digits.isEmpty()) return ""

    val sb = StringBuilder("+7")
    sb.append(" (")

    if (digits.length > 1) {
        sb.append(digits.substring(1, minOf(4, digits.length)))
        if (digits.length >= 4) sb.append(")")
    }

    if (digits.length > 4) {
        sb.append(" ")
        sb.append(digits.substring(4, minOf(7, digits.length)))
    }

    if (digits.length > 7) {
        sb.append("-")
        sb.append(digits.substring(7, minOf(9, digits.length)))
    }

    if (digits.length > 9) {
        sb.append("-")
        sb.append(digits.substring(9, minOf(11, digits.length)))
    }

    return sb.toString()
}
