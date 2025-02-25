package com.pepdeal.infotech.shop.editShop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.fonts.FontUtils.fontMap
import com.pepdeal.infotech.shop.shopDetails.ShopDetailsRepo
import com.pepdeal.infotech.shop.modal.ShopStatusMaster
import com.pepdeal.infotech.util.ColorUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.FontResource

class EditShopDetailsViewModal() : ViewModel() {
    private val shopDetailsRepo = ShopDetailsRepo()
    private val repo = EditShopDetailsRepo()

    private val _shopLoading = MutableStateFlow(false)
    val shopLoading: StateFlow<Boolean> get() = _shopLoading

    private val _isUploaded = MutableStateFlow(false)
    val isUploaded: StateFlow<Boolean> get() = _isUploaded

    private val _shopDetails = MutableStateFlow(ShopMaster())
    val shopDetails: StateFlow<ShopMaster> get() = _shopDetails

    private val _shopServices = MutableStateFlow(ShopStatusMaster())
    val shopServices: StateFlow<ShopStatusMaster> get() = _shopServices

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

    private val _selectedFonts = MutableStateFlow<Pair<String, FontResource>?>(null)
    val selectedFonts: StateFlow<Pair<String, FontResource>?> = _selectedFonts

    fun updateSelectedBackgroundColor(colorName: String, colorCode: String) {
        _selectedBackGroundColorName.value = colorName
        _selectedBackGroundColorCode.value = colorCode
    }

    fun updateSelectedFontColor(colorName: String, colorCode: String) {
        _selectedFontColorName.value = colorName
        _selectedFontColorCode.value = colorCode
    }

    fun updateTheTypeOfColor(forScreen: String) {
        _forScreen.value = forScreen
    }

    fun updateTheShopFontFamily(fontName: String, fontResources: FontResource) {
        _selectedFonts.value = Pair(fontName, fontResources)
    }

    fun fetchShopDetails(shopId: String) {
        _shopLoading.value = true
        viewModelScope.launch {
            val shopDetails = shopDetailsRepo.fetchShopDetails(shopId = shopId)
            if (shopDetails != null) {
                _shopDetails.value = shopDetails
                _selectedBackGroundColorName.value =
                    ColorUtil.colorMap.entries.find { it.value == shopDetails.bgColourId }?.key
                        ?: "Default"
                _selectedBackGroundColorCode.value = _shopDetails.value.bgColourId
                _selectedFontColorName.value =
                    ColorUtil.colorMap.entries.find { it.value == _shopDetails.value.fontColourId }?.key

                _selectedFontColorCode.value = _shopDetails.value.fontColourId

                _selectedFonts.value = _shopDetails.value.fontStyleId?.let { fontStyleId ->
                    fontMap[fontStyleId]?.let { fontResource ->
                        Pair(
                            fontStyleId,
                            fontResource
                        ) // Ensure the correct order (String, FontResource)
                    }
                }

                _shopLoading.value = false
            }
        }
    }

    fun fetchShopServices(shopId: String) {
        viewModelScope.launch {
            val shopServices = repo.fetchShopServices(shopId)
            _shopServices.value = shopServices
        }
    }

    fun updateShopDetails(shopStatusMaster: ShopStatusMaster, shopMaster: ShopMaster) {
        _isUploaded.value = true
        viewModelScope.launch {
            val status = repo.insertOrUpdateShopServices(shopStatusMaster)
            repo.updateShopDetails(shopMaster)
            _isUploaded.value = false
        }
    }

//    fun updateShopDetails(shopMaster: ShopMaster){
//
//    }

    fun reset() {
        _shopDetails.value = ShopMaster()
        _shopServices.value = ShopStatusMaster()
        _isUploaded.value = false
        _forScreen.value = null
        _selectedBackGroundColorName.value = null
        _selectedBackGroundColorCode.value = null
        _selectedFontColorName.value = null
        _selectedFontColorCode.value = null
        _selectedFonts.value = null
    }
}