package com.pepdeal.infotech.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TicketViewModal :ViewModel() {

    private val customerTicketRepo = TicketRepo()
    private val _ticketProduct =
        MutableStateFlow<List<ProductTicket>>(emptyList())
    val ticketProduct: StateFlow<List<ProductTicket>> get() = _ticketProduct

    private var currentProductTicketList: MutableList<ProductTicket> = mutableListOf()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun getAllTicketProduct(userId:String){
        _isLoading.value = true
        viewModelScope.launch {
            customerTicketRepo.getTicketForCustomerFlow(userId)
                .collect{ ticket ->
                    currentProductTicketList.add(ticket)
                    _ticketProduct.value = currentProductTicketList.toList()
                    if(_isLoading.value) _isLoading.value = false
                }
        }
    }

    fun resetTicket(){
        _ticketProduct.value = emptyList()
        currentProductTicketList = mutableListOf()
    }

}