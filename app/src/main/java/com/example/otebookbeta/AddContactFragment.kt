package com.example.otebookbeta

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.example.otebookbeta.utils.setupPhoneInput
import com.example.otebookbeta.utils.ValidationUtils

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
            if (checkInputs()) {
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
        // Телефон: форматирование +7 (999) 999-99-99
        if (binding.phoneInput.text.isNullOrBlank()) {
            binding.phoneInput.setSelection(binding.phoneInput.text?.length ?: 0)
        }
        setupPhoneInput(binding.phoneInput, binding.phoneLayout)
    }

    private fun setupFocusValidation() {
        binding.fullNameInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.fullNameLayout.error =
                ValidationUtils.nameError(binding.fullNameInput.text?.toString())
        }
        binding.phoneInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.phoneLayout.error =
                ValidationUtils.phoneError(binding.phoneInput.text?.toString())
        }
        binding.emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.emailLayout.error =
                ValidationUtils.emailError(binding.emailInput.text?.toString())
        }
        binding.professionInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.professionLayout.error =
                ValidationUtils.professionError(binding.professionInput.text?.toString())
        }
        binding.cityInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.cityLayout.error =
                ValidationUtils.cityError(binding.cityInput.text?.toString())
        }
        binding.categoryInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.categoryLayout.error =
                ValidationUtils.categoryError(binding.categoryInput.text?.toString())
        }
        binding.statusInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.statusLayout.error =
                ValidationUtils.statusError(binding.statusInput.text?.toString())
        }
        binding.dateInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.dateLayout.error =
                ValidationUtils.dateError(binding.dateInput.text?.toString())
        }
        binding.ageInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.ageLayout.error =
                ValidationUtils.ageError(binding.ageInput.text?.toString())
        }
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

    private fun checkInputs(): Boolean {
        val nameError = ValidationUtils.nameError(binding.fullNameInput.text?.toString())
        binding.fullNameLayout.error = nameError

        val phoneError = ValidationUtils.phoneError(binding.phoneInput.text?.toString())
        binding.phoneLayout.error = phoneError

        val emailError = ValidationUtils.emailError(binding.emailInput.text?.toString())
        binding.emailLayout.error = emailError

        val professionError = ValidationUtils.professionError(binding.professionInput.text?.toString())
        binding.professionLayout.error = professionError

        val cityError = ValidationUtils.cityError(binding.cityInput.text?.toString())
        binding.cityLayout.error = cityError

        val categoryError = ValidationUtils.categoryError(binding.categoryInput.text?.toString())
        binding.categoryLayout.error = categoryError

        val statusError = ValidationUtils.statusError(binding.statusInput.text?.toString())
        binding.statusLayout.error = statusError

        val dateError = ValidationUtils.dateError(binding.dateInput.text?.toString())
        binding.dateLayout.error = dateError

        val ageError = ValidationUtils.ageError(binding.ageInput.text?.toString())
        binding.ageLayout.error = ageError

        return listOf(
            nameError,
            phoneError,
            emailError,
            professionError,
            cityError,
            categoryError,
            statusError,
            dateError,
            ageError
        ).all { it == null }
    }

    private fun showAggregateErrors() {
        val errors = listOfNotNull(
            binding.fullNameLayout.error,
            binding.phoneLayout.error,
            binding.emailLayout.error,
            binding.categoryLayout.error,
            binding.statusLayout.error,
            binding.dateLayout.error,
            binding.professionLayout.error,
            binding.cityLayout.error,
            binding.ageLayout.error
        )

        if (errors.isNotEmpty()) {
            Snackbar.make(binding.root, errors.joinToString("; "), Snackbar.LENGTH_LONG)
                .setAnchorView(binding.addButton)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
        }
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