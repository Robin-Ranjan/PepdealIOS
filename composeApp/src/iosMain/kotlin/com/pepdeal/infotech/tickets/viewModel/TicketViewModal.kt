package com.pepdeal.infotech.tickets.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.tickets.domain.TicketRepository
import com.pepdeal.infotech.tickets.model.ProductTicket
import com.pepdeal.infotech.tickets.repository.TicketRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketViewModal(
    private val ticketRepository: TicketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val userId = savedStateHandle.toRoute<Routes.CustomerTicketPage>().userId

    private val _state = MutableStateFlow(CustomerTicketUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllTicketProduct(userId)
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _state.value
        )

    private fun getAllTicketProduct(userId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            ticketRepository.getTicketForCustomerFlow(userId)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            val ticket = result.data
                            _state.update {
                                it.copy(
                                    tickets = it.tickets + ticket,
                                    isLoading = false
                                )
                            }
                        }

                        is AppResult.Error -> {
                            if (result.error.type == DataError.RemoteType.EMPTY_RESULT) {
                                _state.update { it.copy(isLoading = false) }
                            } else {
                                _state.update {
                                    it.copy(
                                        error = result.error.message,
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                }

        }
    }
}

data class CustomerTicketUiState(
    val tickets: List<ProductTicket> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean = tickets.isEmpty()
}