package com.pepdeal.infotech.superShop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.product.FavProductWithImages
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

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> get() = _isEmpty

    private var currentSuperShopList: MutableList<SuperShopsWithProduct> = mutableListOf()

    fun fetchSuperShop(userId:String){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repo.getSuperShopWithProduct(userId)
                    .collect{ newShop->
                        newShop?.let {
                            currentSuperShopList.add(newShop)
                            _superShop.value = currentSuperShopList.toList()
//                            _superShop.update { oldList->
//                                (oldList + newShop).distinctBy { it.shop.shopId }
//                            }
                            if(_isLoading.value) _isLoading.value = false
                        } ?: run {
                            println("isEmpty true")
                            _isLoading.value = false
                            _isEmpty.value = true
                        }
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
                if(_superShop.value.isEmpty()) _isEmpty.value = true
            }
        }
    }

    fun reset(){
        _superShop.value = emptyList()
        currentSuperShopList = mutableListOf()
        _isEmpty.value = false
        _isLoading.value = false
    }
}