package com.pepdeal.infotech.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SellerTicketViewModal():ViewModel() {
    private val sellerTicketRepo = SellerTicketRepo()

    private val _sellerTicketProduct =
        MutableStateFlow<List<ProductTicket>>(emptyList())
    val sellerTicketProduct: StateFlow<List<ProductTicket>> get() = _sellerTicketProduct

    private var currentProductTicketList: MutableList<ProductTicket> = mutableListOf()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun getAllSellerTicketProduct(shopId:String){
        _isLoading.value = true
        viewModelScope.launch {
            sellerTicketRepo.getTicketForSellerFlow(shopId)
                .collect{ ticket->
                    currentProductTicketList.add(ticket)
                    _sellerTicketProduct.value = currentProductTicketList.toList()
                    if(_isLoading.value) _isLoading.value = false
                }
        }
    }

    fun resetTicket(){
        _sellerTicketProduct.value = emptyList()
        currentProductTicketList = mutableListOf()
    }

}