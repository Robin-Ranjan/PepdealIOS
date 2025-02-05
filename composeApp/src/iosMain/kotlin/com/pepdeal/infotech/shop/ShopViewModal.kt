package com.pepdeal.infotech.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.ShopRepo
import com.pepdeal.infotech.ShopWithProducts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.FontResource

class ShopViewModal :ViewModel() {
    private val shopRepo = ShopRepo()
    private val _shops = MutableStateFlow<List<ShopWithProducts>>(emptyList())
    val shops: StateFlow<List<ShopWithProducts>> = _shops.asStateFlow()

    private val _isLoading = MutableStateFlow(false) // Loading state
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var lastShopId: String? = null

    fun loadMoreShops() {
        if (_isLoading.value) return // Prevent duplicate loading

        _isLoading.value = true
        viewModelScope.launch {
            try {
                shopRepo.getActiveShopsFlowPaginationEmitWithFilter(lastShopId).collect { newShops ->
                        lastShopId = newShops.shop.shopId // Update last shopId for pagination
                        _shops.update { oldList->
                            (oldList + newShops).distinctBy { it.shop.shopId }
                        }
                    _isLoading.value = false
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    private val _forScreen = MutableStateFlow<String?>(null)
    val forScreen: StateFlow<String?> = _forScreen

    private val _selectedBackGroundColorName = MutableStateFlow<String?>(null)
    val selectedBackGroundColorName: StateFlow<String?> = _selectedBackGroundColorName

    private val _selectedBackGroundColorCode = MutableStateFlow<String?>(null)
    val selectedBackGroundColorCode: StateFlow<String?> = _selectedBackGroundColorCode

    private val _selectedFontColorName = MutableStateFlow<String?>(null)
    val selectedFontColorName: StateFlow<String?> = _selectedFontColorName

    private val _selectedFontColorCode = MutableStateFlow<String?>(null)
    val selectedFontColorCode: StateFlow<String?> = _selectedFontColorCode

    private val _selectedFonts = MutableStateFlow<Pair<String,FontResource>?>(null)
    val selectedFonts: StateFlow<Pair<String,FontResource>?> = _selectedFonts

    fun updateSelectedBackgroundColor(colorName: String,colorCode:String) {
        _selectedBackGroundColorName.value = colorName
        _selectedBackGroundColorCode.value = colorCode
    }

    fun updateSelectedFontColor(colorName: String,colorCode: String){
        _selectedFontColorName.value = colorName
        _selectedFontColorCode.value = colorCode
    }

    fun updateTheTypeOfColor(forScreen:String){
        _forScreen.value = forScreen
    }

    fun updateTheShopFontFamily(fontName:String,fontResources:FontResource){
        _selectedFonts.value = Pair(fontName,fontResources)
    }
}