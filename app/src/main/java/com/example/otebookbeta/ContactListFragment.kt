package com.example.otebookbeta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.otebookbeta.data.Contact
import com.example.otebookbeta.databinding.FragmentContactListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!
    private val args: ContactListFragmentArgs by navArgs()
    private val viewModel: ContactListViewModel by viewModels()

    private lateinit var adapter: ContactAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.categoryTitle.text = args.category
        setupRecyclerView()
        setupObservers()
        viewModel.loadContacts(args.category)
    }

    private fun setupRecyclerView() {
        adapter = ContactAdapter(
            onClick = { contact ->
                val bundle = Bundle().apply { putInt("contactId", contact.id) }
                findNavController().navigate(R.id.action_contactListFragment_to_contactDetailFragment, bundle)
            },
            onLongClick = { contact -> showDeleteConfirmationDialog(contact) }
        )
        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.contactsRecyclerView.adapter = adapter
        binding.contactsRecyclerView.itemAnimator = null
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoryTitle.collect { category -> binding.categoryTitle.text = category }
                }
                launch {
                    viewModel.contacts.collect { contacts ->
                        adapter.submitList(contacts)
                        binding.emptyState.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.contactsCountText.collect { countText -> binding.contactsCount.text = countText }
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(contact: Contact) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удаление контакта")
            .setMessage("Вы уверены, что хотите удалить контакт ${contact.name}?")
            .setPositiveButton("Удалить") { _, _ ->
                try {
                    viewModel.deleteContact(contact.id)
                    Snackbar.make(binding.root, "Контакт удалён!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
                        .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        .show()
                    // Больше НЕ уходим назад; список обновится из Flow
                } catch (e: Exception) {
                    Log.e("ContactListFragment", "Ошибка при удалении контакта: ${e.message}", e)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
