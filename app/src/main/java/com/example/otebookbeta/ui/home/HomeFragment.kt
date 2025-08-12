package com.example.otebookbeta.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.otebookbeta.HomeViewModel
import com.example.otebookbeta.R
import com.example.otebookbeta.databinding.FragmentHomeBinding
import com.example.otebookbeta.utils.StringUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.partnersCount.collect { count ->
                        val word = StringUtils.getCategoryWord(count, "Партнёры")
                        binding.partnersCount.text = "$count $word"
                    }
                }
                launch {
                    viewModel.clientsCount.collect { count ->
                        val word = StringUtils.getCategoryWord(count, "Клиенты")
                        binding.clientsCount.text = "$count $word"
                    }
                }
                launch {
                    viewModel.potentialsCount.collect { count ->
                        val word = StringUtils.getCategoryWord(count, "Потенциальные")
                        binding.potentialsCount.text = "$count $word"
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.partnersCard.setOnClickListener { navigateToCategory("Партнёры") }
        binding.clientsCard.setOnClickListener { navigateToCategory("Клиенты") }
        binding.potentialsCard.setOnClickListener { navigateToCategory("Потенциальные") }
    }

    private fun navigateToCategory(category: String) {
        val bundle = Bundle().apply {
            putString("category", category)
        }
        findNavController().navigate(
            R.id.action_nav_home_to_contactListFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}