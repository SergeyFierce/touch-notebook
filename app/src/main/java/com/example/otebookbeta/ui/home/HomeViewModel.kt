package com.example.otebookbeta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otebookbeta.data.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ContactRepository
) : ViewModel() {

    private val _partnersCount = MutableStateFlow(0)
    val partnersCount: StateFlow<Int> = _partnersCount

    private val _clientsCount = MutableStateFlow(0)
    val clientsCount: StateFlow<Int> = _clientsCount

    private val _potentialsCount = MutableStateFlow(0)
    val potentialsCount: StateFlow<Int> = _potentialsCount

    init {
        loadCounts()
    }

    private fun loadCounts() {
        val partners = repository.getContactCountByCategory("Партнёры").catch { emit(0) }
        val clients = repository.getContactCountByCategory("Клиенты").catch { emit(0) }
        val potentials = repository.getContactCountByCategory("Потенциальные").catch { emit(0) }

        viewModelScope.launch {
            combine(partners, clients, potentials) { p, c, pt -> Triple(p, c, pt) }
                .collect { (p, c, pt) ->
                    _partnersCount.value = p
                    _clientsCount.value = c
                    _potentialsCount.value = pt
                }
        }
    }
}
