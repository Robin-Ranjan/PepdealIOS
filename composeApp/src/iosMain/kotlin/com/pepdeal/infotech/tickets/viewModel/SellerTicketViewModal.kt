package com.pepdeal.infotech.tickets.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.tickets.domain.SellerTicketRepository
import com.pepdeal.infotech.tickets.model.ProductTicket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SellerTicketViewModal(
    savedStateHandle: SavedStateHandle,
    private val sellerTicketRepo: SellerTicketRepository
) : ViewModel() {

    val shopId = savedStateHandle.toRoute<Routes.SellerTicketPage>().shopId

    private val _state = MutableStateFlow(SellerTicketState(shopId = shopId))
    val state = _state.asStateFlow()
        .onStart {
            getAllSellerTicketProduct(shopId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _state.value
        )


    fun onAction(action: Action) {
        when (action) {
            is Action.OnTicketStatusChange -> {
                changeTicketStatus(action.ticketId, action.status)
            }

            is Action.OnClearSnackBarMessage -> {
                _state.update { it.copy(snackBarMessage = null) }
            }
        }
    }

    private fun getAllSellerTicketProduct(shopId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            sellerTicketRepo.getTicketForSellerFlow(shopId)
                .collect { response ->
                    when (response) {
                        is AppResult.Success -> {
                            val data = response.data
                            _state.update {
                                it.copy(
                                    sellerTicketProduct = it.sellerTicketProduct + data,
                                    isLoading = false
                                )
                            }
                        }

                        is AppResult.Error -> {
                            if (response.error.type == DataError.RemoteType.EMPTY_RESULT) {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        snackBarMessage = SnackBarMessage.Error("No Ticket found for your shop")
                                    )
                                }
                            } else {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        snackBarMessage = SnackBarMessage.Error(response.error.message.toString())
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun changeTicketStatus(ticketId: String, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sellerTicketRepo.updateTicketMasterStatus(ticketId, status)
                .collect { response ->
                    when (response) {
                        is AppResult.Success -> {
                            val updatesList = _state.value.sellerTicketProduct.map {
                                if (it.ticket.ticketId == ticketId) {
                                    it.copy(ticket = it.ticket.copy(ticketStatus = status))
                                } else {
                                    it
                                }
                            }
                            _state.update { it.copy(sellerTicketProduct = updatesList) }
                        }

                        is AppResult.Error -> {
                            _state.update {
                                it.copy(
                                    snackBarMessage = SnackBarMessage.Error(response.error.message.toString())
                                )
                            }
                        }
                    }
                }

        }
    }

    data class SellerTicketState(
        val sellerTicketProduct: List<ProductTicket> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val shopId: String = "",
        val snackBarMessage: SnackBarMessage? = null
    ) {
        val isEmpty: Boolean
            get() = sellerTicketProduct.isEmpty()

    }

    sealed interface Action {
        data class OnTicketStatusChange(val ticketId: String, val status: String) : Action
        data object OnClearSnackBarMessage : Action
    }

}