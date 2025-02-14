package com.pepdeal.infotech.superShop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.ShopWithProducts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SuperShopViewModal():ViewModel() {
    private val repo = SuperShopRepo()
    private val _superShop = MutableStateFlow<List<SuperShopsWithProduct>>(emptyList())
    val superShop : StateFlow<List<SuperShopsWithProduct>> get() = _superShop

    private val _isLoading = MutableStateFlow(false) // Loading state
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun fetchSuperShop(userId:String){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repo.getSuperShopWithProduct(userId)
                    .collect{ newShop->
                        _superShop.update { oldList->
                            (oldList + newShop).distinctBy { it.shop.shopId }
                        }
                        _isLoading.value = false
                    }
            }catch (e:Exception){
                println(e.message)
                e.printStackTrace()
            }
        }
    }

    fun removeSuperShop(userId: String,shopId:String){
        viewModelScope.launch {
            repo.removeSuperShop(userId,shopId){
                _superShop.value = _superShop.value.filterNot { it.shop.shopId == shopId }
            }
        }
    }

    fun reset(){
        _superShop.value = emptyList()
    }
}