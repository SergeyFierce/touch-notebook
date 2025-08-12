package com.example.otebookbeta

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.otebookbeta.data.Note
import com.example.otebookbeta.databinding.FragmentAddNoteBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddNoteFragment : BaseFragment() {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddNoteViewModel by viewModels()
    private val args: AddNoteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initForm()
        setupButtonListeners()
    }

    private fun initForm() {
        // Устанавливаем текущую дату по умолчанию
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.dateInput.setText(dateFormat.format(Date()))

        // Настройка DatePicker
        binding.dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.dateInput.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButtonListeners() {
        binding.cancelButton.setOnClickListener {
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                android.util.Log.e("AddNoteFragment", "Ошибка навигации: ${e.message}", e)
                Toast.makeText(requireContext(), "Ошибка навигации: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.saveButton.setOnClickListener {
            if (validateForm()) {
                saveNote()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val content = binding.noteContentInput.text?.toString()
        if (content.isNullOrBlank()) {
            binding.noteContentLayout.error = "Введите текст заметки"
            isValid = false
        } else if (content.length > 500) {
            binding.noteContentLayout.error = "Текст заметки не должен превышать 500 символов"
            isValid = false
        } else {
            binding.noteContentLayout.error = null
        }

        val date = binding.dateInput.text?.toString()
        if (date.isNullOrBlank()) {
            binding.dateLayout.error = "Укажите дату добавления"
            isValid = false
        } else {
            binding.dateLayout.error = null
        }

        if (!isValid) {
            Snackbar.make(binding.root, "Проверьте введённые данные", Snackbar.LENGTH_LONG)
                .setAnchorView(binding.saveButton)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
        }

        return isValid
    }

    private fun saveNote() {
        val note = Note(
            contactId = args.contactId,
            content = binding.noteContentInput.text.toString(),
            dateAdded = binding.dateInput.text.toString()
        )

        try {
            viewModel.saveNote(note)
            Snackbar.make(binding.root, "Заметка добавлена!", Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.saveButton)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
            if (isAdded) {
                findNavController().navigateUp()
            }
        } catch (e: Exception) {
            android.util.Log.e("AddNoteFragment", "Ошибка при сохранении заметки: ${e.message}", e)
            Toast.makeText(requireContext(), "Ошибка при сохранении заметки: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}