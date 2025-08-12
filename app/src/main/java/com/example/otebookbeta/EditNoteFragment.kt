package com.example.otebookbeta

import android.app.DatePickerDialog
import android.os.Bundle
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
import com.example.otebookbeta.data.Note
import com.example.otebookbeta.databinding.FragmentEditNoteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditNoteFragment : BaseFragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditNoteViewModel by viewModels()
    private val args: EditNoteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Загружаем заметку
        viewModel.loadNote(args.noteId)
        initForm()
        setupButtonListeners()
        setupObservers()
    }

    private fun initForm() {
        // Настройка DatePicker
        binding.dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
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
                android.util.Log.e("EditNoteFragment", "Ошибка навигации: ${e.message}", e)
                Toast.makeText(requireContext(), "Ошибка навигации: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.saveButton.setOnClickListener {
            if (validateForm()) {
                saveNote()
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.note.collect { note ->
                    if (note != null) {
                        binding.noteContentInput.setText(note.content)
                        binding.dateInput.setText(note.dateAdded)
                    }
                }
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
            id = args.noteId,
            contactId = args.contactId,
            content = binding.noteContentInput.text.toString(),
            dateAdded = binding.dateInput.text.toString()
        )

        try {
            viewModel.updateNote(note)
            Snackbar.make(binding.root, "Заметка обновлена!", Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.saveButton)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
            if (isAdded) {
                findNavController().navigateUp()
            }
        } catch (e: Exception) {
            android.util.Log.e("EditNoteFragment", "Ошибка при сохранении заметки: ${e.message}", e)
            Toast.makeText(requireContext(), "Ошибка при сохранении заметки: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun deleteNote() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить заметку")
            .setMessage("Вы уверены, что хотите удалить эту заметку?")
            .setPositiveButton("Удалить") { _, _ ->
                try {
                    viewModel.deleteNote(args.noteId)
                    Snackbar.make(binding.root, "Заметка удалена!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                        .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        .show()
                    if (isAdded) {
                        findNavController().navigateUp() // Возврат без анимации
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EditNoteFragment", "Ошибка при удалении заметки: ${e.message}", e)
                    Toast.makeText(requireContext(), "Ошибка при удалении заметки: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

}