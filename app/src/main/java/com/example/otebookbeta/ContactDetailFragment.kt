package com.example.otebookbeta

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.otebookbeta.data.Contact
import com.example.otebookbeta.databinding.FragmentContactDetailBinding
import com.example.otebookbeta.utils.ContactDictionary
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class ContactDetailFragment : BaseFragment() {

    private var _binding: FragmentContactDetailBinding? = null
    private val binding get() = _binding!!
    private val args: ContactDetailFragmentArgs by navArgs()
    private val contactViewModel: ContactDetailViewModel by viewModels()
    private val noteViewModel: NoteListViewModel by viewModels()
    private var hasChanges = false
    private var originalContact: Contact? = null
    private lateinit var noteAdapter: NoteAdapter

    private val contact get() = contactViewModel.contact
    override val shouldRequestFocusOnEndIconClick: Boolean = true

    fun deleteContact() {
        contact.value?.let { contact ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Удалить контакт")
                .setMessage("Вы уверены, что хотите удалить контакт ${contact.name}?")
                .setPositiveButton("Удалить") { _, _ ->
                    try {
                        contactViewModel.deleteContact(contact.id)
                        Snackbar.make(binding.root, "Контакт удалён!", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                            .show()
                        if (isAdded) findNavController().navigateUp()
                    } catch (e: Exception) {
                        Log.e("ContactDetailFragment", "Ошибка при удалении контакта: ${e.message}", e)
                        Toast.makeText(requireContext(), "Ошибка при удалении контакта: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        } ?: run {
            Toast.makeText(requireContext(), "Контакт не найден", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteAdapter = NoteAdapter(
            onClick = { note ->
                val action = ContactDetailFragmentDirections
                    .actionContactDetailFragmentToEditNoteFragment(noteId = note.id, contactId = args.contactId)
                findNavController().navigate(action)
            },
            onDelete = { note ->
                noteViewModel.deleteNote(note.id)
                Snackbar.make(binding.root, "Заметка удалена", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    .show()
            }
        )
        binding.recentNotesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = noteAdapter
        }

        contactViewModel.loadContact(args.contactId)
        noteViewModel.loadNotes(args.contactId)
        initForm()
        setupObservers()
        setupFieldListeners()
        setupFocusValidation()
        setupButtons()

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("menu_delete_click")
            ?.observe(viewLifecycleOwner) { clicked ->
                if (clicked == true) {
                    deleteContact()
                    findNavController().currentBackStackEntry?.savedStateHandle?.set("menu_delete_click", false)
                }
            }
    }

    private fun setupButtons() {
        binding.actionCancel.setOnClickListener {
            if (hasChanges) {
                resetForm()
                hasChanges = false
                updateButtonsVisibility()
                Snackbar.make(binding.root, "Изменения отменены", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    .show()
            }
        }
        binding.actionSave.setOnClickListener {
            if (validateForm()) {
                saveContact()
                hasChanges = false
                updateButtonsVisibility()
            }
        }
        binding.actionAllNotes.setOnClickListener {
            val action = ContactDetailFragmentDirections
                .actionContactDetailFragmentToNoteListFragment(contactId = args.contactId)
            findNavController().navigate(action)
        }
    }

    private fun updateButtonsVisibility() {
        binding.actionButtonsContainer.visibility = if (hasChanges) View.VISIBLE else View.GONE
        binding.actionCancel.isEnabled = hasChanges
        binding.actionSave.isEnabled = hasChanges
    }

    private fun initForm() {
        (binding.socialMediaInput as MaterialAutoCompleteTextView)
            .setSimpleItems(ContactDictionary.socialNetworks())
        (binding.categoryInput as MaterialAutoCompleteTextView)
            .setSimpleItems(ContactDictionary.categories())

        binding.statusInput.isEnabled = false
        binding.statusLayout.isEnabled = false
        binding.statusLayout.hint = "Статус (выберите категорию сначала)"

        setupAutoCompleteField(binding.socialMediaInput as MaterialAutoCompleteTextView, binding.socialMediaLayout)
        setupAutoCompleteField(binding.categoryInput as MaterialAutoCompleteTextView, binding.categoryLayout)
        setupAutoCompleteField(binding.statusInput as MaterialAutoCompleteTextView, binding.statusLayout)

        binding.categoryInput.setOnItemClickListener { _, _, _, _ ->
            updateStatusDropdown()
            binding.statusInput.isEnabled = true
            binding.statusLayout.isEnabled = true
            binding.statusLayout.hint = "Статус"
        }

        binding.categoryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) {
                    binding.statusInput.isEnabled = false
                    binding.statusLayout.isEnabled = false
                    binding.statusLayout.hint = "Статус (выберите категорию сначала)"
                    binding.statusInput.setText("")
                    checkForChanges()
                }
            }
        })

        binding.statusInput.setOnItemClickListener { _, _, _, _ ->
            checkForChanges()
        }
        binding.statusInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { checkForChanges() }
        })
    }

    private fun updateStatusDropdown(resetSelection: Boolean = true) {
        val category = binding.categoryInput.text.toString()
        val statusItems = ContactDictionary.statusesForCategory(category)
        (binding.statusInput as MaterialAutoCompleteTextView).setSimpleItems(statusItems)
        if (resetSelection) {
            binding.statusInput.setText("")
            binding.statusLayout.error = null
        }
    }

    private fun setupAutoCompleteField(
        view: MaterialAutoCompleteTextView,
        textInputLayout: TextInputLayout
    ) {
        view.setOnClickListener { view.showDropDown() }
        textInputLayout.setEndIconOnClickListener {
            view.showDropDown()
            if (shouldRequestFocusOnEndIconClick) view.requestFocus()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                contactViewModel.contact.collect { c ->
                    if (c != null) {
                        originalContact = c
                        populateForm(c)
                        hasChanges = false
                        updateButtonsVisibility()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                noteViewModel.recentNotes.collect { notes ->
                    noteAdapter.submitList(notes)
                    binding.recentNotesRecyclerView.visibility =
                        if (notes.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun populateForm(contact: Contact) {
        binding.fullNameInput.setText(contact.name)
        binding.phoneInput.setText(contact.phone)
        binding.emailInput.setText(contact.email)
        (binding.socialMediaInput as MaterialAutoCompleteTextView)
            .setText(contact.socialMedia?.takeIf { it.isNotBlank() } ?: "Не выбрано", false)
        (binding.categoryInput as MaterialAutoCompleteTextView)
            .setText(contact.category.orEmpty(), false)
        (binding.statusInput as MaterialAutoCompleteTextView)
            .setText(contact.status.orEmpty(), false)
        binding.cityInput.setText(contact.city)
        binding.ageInput.setText(contact.age?.toString() ?: "")
        binding.professionInput.setText(contact.profession)
        binding.commentInput.setText(contact.comment)
        binding.dateInput.setText(contact.dateAdded)

        updateStatusDropdown(resetSelection = false)
        if (binding.categoryInput.text.isNotBlank()) {
            binding.statusInput.isEnabled = true
            binding.statusLayout.isEnabled = true
            binding.statusLayout.hint = "Статус"
        }
    }

    // ==== слушатели полей и форматирование телефона ====
    private fun setupFieldListeners() {
        // Имя
        binding.fullNameInput.addTextChangedListener(SimpleWatcher { checkForChanges() })


        // Остальные поля — просто трекаем изменения
        listOf(
            binding.emailInput, binding.socialMediaInput, binding.categoryInput, binding.statusInput,
            binding.cityInput, binding.ageInput, binding.professionInput, binding.commentInput, binding.dateInput
        ).forEach { it.addTextChangedListener(SimpleWatcher { checkForChanges() }) }

        // Теги
        binding.newbieCheckbox.setOnCheckedChangeListener { _, _ -> checkForChanges() }
        binding.remindCheckbox.setOnCheckedChangeListener { _, _ -> checkForChanges() }
        binding.vipCheckbox.setOnCheckedChangeListener { _, _ -> checkForChanges() }

        // Дата
        binding.dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    val df = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    binding.dateInput.setText(df.format(calendar.time))
                    checkForChanges()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    // ===============================================================

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

    private fun normalizePhone(s: String?): String = s?.replace("[^0-9]".toRegex(), "") ?: ""
    private fun norm(s: String?): String = s?.trim().orEmpty()
    private fun normNullable(s: String?): String? = s?.trim()?.takeIf { it.isNotEmpty() }

    private fun tagsFromUi(): String? {
        val selected = mutableListOf<String>()
        if (binding.newbieCheckbox.isChecked) selected.add("Новичок")
        if (binding.remindCheckbox.isChecked) selected.add("Напомнить")
        if (binding.vipCheckbox.isChecked) selected.add("VIP")
        return if (selected.isNotEmpty()) selected.joinToString(", ") else null
    }

    private fun checkForChanges() {
        val c = originalContact ?: return
        val changed =
            norm(c.name) != norm(binding.fullNameInput.text?.toString()) ||
                    normalizePhone(c.phone) != normalizePhone(binding.phoneInput.text?.toString()) ||
                    normNullable(c.email) != normNullable(binding.emailInput.text?.toString()) ||
                    normNullable(c.socialMedia) != normNullable(binding.socialMediaInput.text?.toString()) ||
                    normNullable(c.city) != normNullable(binding.cityInput.text?.toString()) ||
                    (c.age ?: 0) != (binding.ageInput.text?.toString()?.trim()?.toIntOrNull() ?: 0) ||
                    normNullable(c.profession) != normNullable(binding.professionInput.text?.toString()) ||
                    norm(c.category) != norm(binding.categoryInput.text?.toString()) ||
                    normNullable(c.status) != normNullable(binding.statusInput.text?.toString()) ||
                    normNullable(c.tags) != normNullable(tagsFromUi()) ||
                    normNullable(c.comment) != normNullable(binding.commentInput.text?.toString()) ||
                    norm(c.dateAdded) != norm(binding.dateInput.text?.toString())
        hasChanges = changed
        updateButtonsVisibility()
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
        val phone = binding.phoneInput.text?.toString()
        if (phone.isNullOrBlank()) { binding.phoneLayout.error = null; return true }
        val clean = normalizePhone(phone)
        return if (clean.length == 11 && clean.startsWith("7")) { binding.phoneLayout.error = null; true }
        else { binding.phoneLayout.error = "Неверный формат российского телефона (+7 (XXX) XXX-XX-XX)"; false }
    }

    private fun validateEmail(): Boolean {
        val email = binding.emailInput.text?.toString()?.trim()
        if (email.isNullOrBlank()) { binding.emailLayout.error = null; return true }
        val ok = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
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

    private fun validateForm(): Boolean {
        return validateName() and validatePhone() and validateEmail() and
                validateProfession() and validateCity() and
                validateCategory() and validateStatus() and validateDate()
    }

    private fun saveContact() {
        val contact = Contact(
            id = args.contactId,
            name = binding.fullNameInput.text.toString().trim(),
            phone = binding.phoneInput.text.toString(),
            email = binding.emailInput.text?.toString()?.trim(),
            socialMedia = binding.socialMediaInput.text?.toString()?.trim(),
            city = binding.cityInput.text?.toString()?.trim(),
            age = binding.ageInput.text?.toString()?.trim()?.toIntOrNull(),
            profession = binding.professionInput.text?.toString()?.trim(),
            category = binding.categoryInput.text.toString().trim(),
            status = binding.statusInput.text.toString().trim(),
            tags = tagsFromUi(),
            comment = binding.commentInput.text?.toString()?.takeIf { it.isNotBlank() }?.trim(),
            dateAdded = binding.dateInput.text.toString()
        )
        try {
            contactViewModel.saveContact(contact)
            Snackbar.make(binding.root, "Контакт обновлён!", Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.actionSave)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
        } catch (e: Exception) {
            Log.e("ContactDetailFragment", "Error saving contact: ${e.message}", e)
            Toast.makeText(requireContext(), "Ошибка при сохранении контакта: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun resetForm() {
        originalContact?.let { populateForm(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private class SimpleWatcher(val after: () -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable?) = after()
}

