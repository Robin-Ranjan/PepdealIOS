package com.pepdeal.infotech.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SellerTicketViewModal() : ViewModel() {
    private val sellerTicketRepo = SellerTicketRepo()

    private val _sellerTicketProduct =
        MutableStateFlow<List<ProductTicket>>(emptyList())
    val sellerTicketProduct: StateFlow<List<ProductTicket>> get() = _sellerTicketProduct

    private var currentProductTicketList: MutableList<ProductTicket> = mutableListOf()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun getAllSellerTicketProduct(shopId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            sellerTicketRepo.getTicketForSellerFlow(shopId)
                .collect { ticket ->
                    currentProductTicketList.add(ticket)
                    // Sort by updatedAt descending (latest first)
                    val sortedList = currentProductTicketList.sortedByDescending {
                        it.ticket.updatedAt.toLongOrNull() ?: 0L
                    }
                    _sellerTicketProduct.value = sortedList
                    if (_isLoading.value) _isLoading.value = false
                }
        }
    }

    fun changeTicketStatus(ticketId: String, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sellerTicketRepo.updateTicketMasterStatus(ticketId, status,
                onSuccess = {
                    _sellerTicketProduct.update { currentList ->
                        currentList.map { productTicket ->
                            if (productTicket.ticket.ticketId == ticketId) {
                                // Create a new TicketMaster with the updated values
                                val updatedTicket = productTicket.ticket.copy(
                                    ticketStatus = status,
                                    updatedAt = Util.getCurrentTimeStamp() // Or any new timestamp you want to set
                                )
                                // Return a new ProductTicket with the updated ticket
                                productTicket.copy(ticket = updatedTicket)
                            } else {
                                productTicket
                            }
                        }
                    }
                    println(it)
                })
        }
    }

    fun resetTicket() {
        _sellerTicketProduct.value = emptyList()
        currentProductTicketList = mutableListOf()
    }

}