package com.example.otebookbeta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.otebookbeta.databinding.FragmentNoteListBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteListFragment : BaseFragment() {

    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NoteListViewModel by viewModels()
    private val args: NoteListFragmentArgs by navArgs()
    private var fab: FloatingActionButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val noteAdapter = NoteAdapter(
            onClick = { note ->
                val action = NoteListFragmentDirections.actionNoteListFragmentToEditNoteFragment(
                    noteId = note.id,
                    contactId = args.contactId
                )
                findNavController().navigate(action)
            },
            onDelete = { note ->
                viewModel.deleteNote(note.id)
                Snackbar.make(binding.root, "Заметка удалена", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    .show()
            }
        )

        binding.notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = noteAdapter
        }

        binding.addNoteButton.setOnClickListener {
            val action = NoteListFragmentDirections.actionNoteListFragmentToAddNoteFragment(
                contactId = args.contactId
            )
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notes.collect { notes ->
                    noteAdapter.submitList(notes)
                    binding.notesRecyclerView.visibility = if (notes.isEmpty()) View.GONE else View.VISIBLE
                    binding.notesListTitle.text = if (notes.isEmpty()) "Нет заметок" else "Заметки"
                }
            }
        }

        viewModel.loadNotes(args.contactId)
    }

}