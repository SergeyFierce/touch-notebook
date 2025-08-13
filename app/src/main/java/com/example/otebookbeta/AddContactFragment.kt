package com.example.otebookbeta

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.otebookbeta.data.Contact
import com.example.otebookbeta.databinding.FragmentAddContactBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.google.i18n.phonenumbers.PhoneNumberUtil

@AndroidEntryPoint
class AddContactFragment : BaseFragment() {

    private var _binding: FragmentAddContactBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddContactViewModel by viewModels()
    private val availableTags = listOf("Новичок", "Напомнить", "VIP")

    override val shouldRequestFocusOnEndIconClick: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initForm()
        setupFocusValidation()

        binding.cancelButton.setOnClickListener {
            try {
                findNavController().navigateUp()
                Log.d("AddContactFragment", "Navigated up on cancel")
            } catch (e: Exception) {
                Log.e("AddContactFragment", "Cancel navigation error: ${e.message}", e)
                Toast.makeText(requireContext(), "Ошибка навигации: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.addButton.setOnClickListener {
            if (validateForm()) {
                saveContact()
            } else {
                showAggregateErrors()
            }
        }
    }

    private fun initForm() {
        val socialMediaItems = arrayOf(
            "Не выбрано", "Telegram", "Instagram", "ВК", "Одноклассники",
            "TikTok", "Facebook", "WhatsApp", "Skype", "MAX", "Другое"
        )
        val socialMediaAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, socialMediaItems
        )
        binding.socialMediaInput.setAdapter(socialMediaAdapter)
        setupAutoCompleteField(binding.socialMediaInput, binding.socialMediaLayout)

        val categoryItems = arrayOf("Партнёры", "Клиенты", "Потенциальные")
        val categoryAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, categoryItems
        )
        binding.categoryInput.setAdapter(categoryAdapter)
        setupAutoCompleteField(binding.categoryInput, binding.categoryLayout)

        // Статус выключен до выбора категории
        binding.statusInput.isEnabled = false
        binding.statusLayout.isEnabled = false
        binding.statusLayout.hint = "Статус (выберите категорию сначала)"

        binding.categoryInput.setOnItemClickListener { _, _, _, _ ->
            updateStatusDropdown()
            binding.statusInput.isEnabled = true
            binding.statusLayout.isEnabled = true
            binding.statusLayout.hint = "Статус"
        }

        // Если категорию стерли — выключить статус
        binding.categoryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) {
                    binding.statusInput.isEnabled = false
                    binding.statusLayout.isEnabled = false
                    binding.statusLayout.hint = "Статус (выберите категорию сначала)"
                    binding.statusInput.setText("")
                    binding.statusLayout.error = null
                }
            }
        })

        updateStatusDropdown()

        updateTags()
        binding.newbieCheckbox.setOnCheckedChangeListener { _, _ -> updateTags() }
        binding.remindCheckbox.setOnCheckedChangeListener { _, _ -> updateTags() }
        binding.vipCheckbox.setOnCheckedChangeListener { _, _ -> updateTags() }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.dateInput.setText(dateFormat.format(Date()))
        binding.dateInput.isEnabled = true

        binding.dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.dateInput.setText(dateFormat.format(calendar.time))
                    binding.dateLayout.error = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // --- Телефон: сразу "+7 " и маска +7 (999) 999-99-99 ---
        if (binding.phoneInput.text.isNullOrBlank()) {
            binding.phoneInput.setText("+7 ")
            binding.phoneInput.setSelection(binding.phoneInput.text?.length ?: 0)
        }
        binding.phoneInput.addTextChangedListener(RuPhoneMaskWatcher { text, hasError ->
            binding.phoneLayout.error = if (hasError) "Неверный формат российского телефона (+7 (XXX) XXX-XX-XX)" else null
        })
    }

    private fun setupFocusValidation() {
        binding.fullNameInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateName() }
        binding.phoneInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validatePhone() }
        binding.emailInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateEmail() }
        binding.professionInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateProfession() }
        binding.cityInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateCity() }
        binding.categoryInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateCategory() }
        binding.statusInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateStatus() }
        binding.dateInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateDate() }
    }

    private fun updateStatusDropdown() {
        val category = binding.categoryInput.text.toString()
        val statusItems = when (category) {
            "Партнёры", "Клиенты" -> arrayOf("Активный", "Пассивный", "Потерянный")
            "Потенциальные" -> arrayOf("Холодный", "Тёплый", "Потерянный")
            else -> arrayOf()
        }
        val statusAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, statusItems
        )
        binding.statusInput.setAdapter(statusAdapter)
        binding.statusInput.setText("", false)
        binding.statusLayout.error = null
    }

    private fun setupAutoCompleteField(
        autoCompleteTextView: AutoCompleteTextView,
        textInputLayout: TextInputLayout
    ) {
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
            Log.d("AddContactFragment", "${textInputLayout.hint} dropdown shown on field click")
        }
        textInputLayout.setEndIconOnClickListener {
            autoCompleteTextView.showDropDown()
            if (shouldRequestFocusOnEndIconClick) {
                autoCompleteTextView.requestFocus()
                Log.d("AddContactFragment", "${textInputLayout.hint} focus requested on end icon click")
            }
            Log.d("AddContactFragment", "${textInputLayout.hint} dropdown shown on end icon click")
        }
    }

    private fun updateTags() {
        binding.newbieCheckbox.setCompoundDrawablesWithIntrinsicBounds(
            if (binding.newbieCheckbox.isChecked) R.drawable.ic_dot_blue else 0, 0, 0, 0
        )
        binding.remindCheckbox.setCompoundDrawablesWithIntrinsicBounds(
            if (binding.remindCheckbox.isChecked) R.drawable.ic_dot_purple else 0, 0, 0, 0
        )
        binding.vipCheckbox.setCompoundDrawablesWithIntrinsicBounds(
            if (binding.vipCheckbox.isChecked) R.drawable.ic_dot_yellow else 0, 0, 0, 0
        )
        val any = binding.newbieCheckbox.isChecked || binding.remindCheckbox.isChecked || binding.vipCheckbox.isChecked
        val colorRes = if (any) R.color.purple_500 else android.R.color.transparent
        binding.classificationCard.setStrokeColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    private fun validateName(): Boolean {
        val name = binding.fullNameInput.text?.toString()?.trim()
        return when {
            name.isNullOrBlank() -> { binding.fullNameLayout.error = "Поле ФИО обязательно"; false }
            name.length < 2 || name.length > 100 -> { binding.fullNameLayout.error = "ФИО должно быть от 2 до 100 символов"; false }
            !name.matches(Regex("^[А-Яа-яA-Za-z\\s\\-']+$")) -> { binding.fullNameLayout.error = "ФИО может содержать только буквы, пробелы, дефисы и апострофы"; false }
            else -> { binding.fullNameLayout.error = null; true }
        }
    }

    private fun validatePhone(): Boolean {
        val text = binding.phoneInput.text?.toString().orEmpty()
        val digits = text.replace("[^0-9]".toRegex(), "")
        // Пустое поле — это только "+7 " (т.е. единственная цифра 7)
        if (digits == "7" || digits.isEmpty()) {
            binding.phoneLayout.error = null
            return true
        }
        val ok = digits.length == 11 && digits.startsWith("7")
        binding.phoneLayout.error = if (ok) null else "Неверный формат российского телефона (+7 (XXX) XXX-XX-XX)"
        return ok
    }

    private fun validateEmail(): Boolean {
        val email = binding.emailInput.text?.toString()?.trim()
        if (email.isNullOrBlank()) { binding.emailLayout.error = null; return true }
        val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        binding.emailLayout.error = if (ok) null else "Неверный формат email"
        return ok
    }

    private fun validateProfession(): Boolean {
        val profession = binding.professionInput.text?.toString()?.trim()
        if (profession.isNullOrBlank()) { binding.professionLayout.error = null; return true }
        return when {
            profession.length !in 2..50 -> { binding.professionLayout.error = "Профессия должна быть от 2 до 50 символов"; false }
            !profession.matches(Regex("^[А-Яа-яA-Za-z\\s\\-]+$")) -> { binding.professionLayout.error = "Профессия может содержать только буквы, пробелы и дефисы"; false }
            else -> { binding.professionLayout.error = null; true }
        }
    }

    private fun validateCity(): Boolean {
        val city = binding.cityInput.text?.toString()?.trim()
        if (city.isNullOrBlank()) { binding.cityLayout.error = null; return true }
        return when {
            city.length !in 2..50 -> { binding.cityLayout.error = "Город должен быть от 2 до 50 символов"; false }
            !city.matches(Regex("^[А-Яа-яA-Za-z\\s\\-]+$")) -> { binding.cityLayout.error = "Город может содержать только буквы, пробелы и дефисы"; false }
            else -> { binding.cityLayout.error = null; true }
        }
    }

    private fun validateCategory(): Boolean {
        val ok = !binding.categoryInput.text.isNullOrBlank()
        binding.categoryLayout.error = if (ok) null else "Поле Категория обязательно"
        return ok
    }

    private fun validateStatus(): Boolean {
        val ok = !binding.statusInput.text.isNullOrBlank()
        binding.statusLayout.error = if (ok) null else "Поле Статус обязательно"
        return ok
    }

    private fun validateDate(): Boolean {
        val ok = !binding.dateInput.text.isNullOrBlank()
        binding.dateLayout.error = if (ok) null else "Поле Дата добавления обязательно"
        return ok
    }

    private fun showAggregateErrors() {
        val errors = mutableListOf<String>()
        if (!validateName()) errors.add("Введите корректное ФИО")
        if (!validatePhone()) errors.add("Неверный телефон")
        if (!validateEmail()) errors.add("Неверный email")
        if (!validateCategory()) errors.add("Выберите категорию")
        if (!validateStatus()) errors.add("Выберите статус")
        if (!validateDate()) errors.add("Укажите дату")
        if (!validateProfession()) errors.add("Проверьте «Профессию»")
        if (!validateCity()) errors.add("Проверьте «Город»")

        if (errors.isNotEmpty()) {
            Snackbar.make(binding.root, errors.joinToString("; "), Snackbar.LENGTH_LONG)
                .setAnchorView(binding.addButton)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
        }
    }

    private fun validateForm(): Boolean {
        return validateName() and validatePhone() and validateEmail() and
                validateCategory() and validateStatus() and validateDate() and
                validateProfession() and validateCity()
    }

    private fun saveContact() {
        val selectedTags = mutableListOf<String>()
        if (binding.newbieCheckbox.isChecked) selectedTags.add("Новичок")
        if (binding.remindCheckbox.isChecked) selectedTags.add("Напомнить")
        if (binding.vipCheckbox.isChecked) selectedTags.add("VIP")
        val tagsString = if (selectedTags.isNotEmpty()) selectedTags.joinToString(", ") else null

        val contact = Contact(
            name = binding.fullNameInput.text.toString().trim(),
            phone = binding.phoneInput.text.toString(),
            email = binding.emailInput.text?.toString()?.trim(),
            socialMedia = binding.socialMediaInput.text?.toString()?.trim(),
            city = binding.cityInput.text?.toString()?.trim(),
            age = binding.ageInput.text?.toString()?.toIntOrNull(),
            profession = binding.professionInput.text?.toString()?.trim(),
            category = binding.categoryInput.text.toString().trim(),
            status = binding.statusInput.text.toString().trim(),
            tags = tagsString,
            comment = binding.commentInput.text?.toString()?.takeIf { it.isNotBlank() }?.trim(),
            dateAdded = binding.dateInput.text.toString()
        )

        Log.d("AddContactFragment", "Saving contact with category: ${contact.category}, tags: ${contact.tags}")
        try {
            viewModel.saveContact(contact)
            Snackbar.make(binding.root, "Контакт добавлен!", Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.addButton)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
            if (isAdded) {
                findNavController().navigate(R.id.action_addContactFragment_to_nav_home)
                Log.d("AddContactFragment", "Navigated to nav_home after saving contact")
            }
        } catch (e: Exception) {
            Log.e("AddContactFragment", "Error saving contact: ${e.message}", e)
            Toast.makeText(requireContext(), "Ошибка при сохранении контакта: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** Маска телефона РФ: всегда "+7 " + (999) 999-99-99 *///////
private class RuPhoneMaskWatcher(
    private val onValidate: (text: String, hasError: Boolean) -> Unit
) : TextWatcher {
    private var isFormatting = false
    private val maxLen = 18 // длина строки вида +7 (999) 999-99-99

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting) return
        isFormatting = true

        val rawDigits = (s?.toString() ?: "").replace("[^0-9]".toRegex(), "")
        // гарантируем ведущую "7"
        val withCountry = when {
            rawDigits.isEmpty() -> "7"
            rawDigits[0] == '8' -> "7" + rawDigits.substring(1)
            rawDigits[0] != '7' -> "7$rawDigits"
            else -> rawDigits
        }
        // 10 национальных цифр после "7"
        val national = withCountry.drop(1).take(10)

        val formatted = formatRu(national)
        if (formatted != s.toString()) {
            s?.replace(0, s.length, formatted)
        }

        val hasError = national.isNotEmpty() && national.length != 10
        onValidate(formatted, hasError)

        isFormatting = false
    }

    private fun formatRu(national: String): String {
        if (national.isEmpty()) return "+7 "
        val sb = StringBuilder("+7 ")
        sb.append("(").append(national.substring(0, min(3, national.length)))
        if (national.length >= 3) sb.append(")")
        if (national.length > 3) {
            sb.append(" ")
            sb.append(national.substring(3, min(6, national.length)))
        }
        if (national.length > 6) {
            sb.append("-")
            sb.append(national.substring(6, min(8, national.length)))
        }
        if (national.length > 8) {
            sb.append("-")
            sb.append(national.substring(8, min(10, national.length)))
        }
        return if (sb.length > maxLen) sb.substring(0, maxLen) else sb.toString()
    }
}