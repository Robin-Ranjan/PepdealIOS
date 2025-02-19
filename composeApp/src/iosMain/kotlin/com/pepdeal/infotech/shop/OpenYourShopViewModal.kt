package com.pepdeal.infotech.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.shopDetails.OpenYourShopRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.FontResource

class OpenYourShopViewModal():ViewModel() {

    private val repo = OpenYourShopRepo()

    private val _registerShopResponse = MutableStateFlow<Pair<Boolean, String>?>(null)
    val registerShopResponse: StateFlow<Pair<Boolean, String>?> = _registerShopResponse

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    fun registerShop(shopMaster: ShopMaster){
        _isUploading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val status = repo.registerShop(shopMaster)
            _registerShopResponse.value = status
            _isUploading.value = false
            println(status)
        }
    }

//    fun changeUserShopStatus(userId:String){
//        viewModelScope.launch(Dispatchers.IO) {
//            userMasterRepo.changeUserShopStatus(userId,
//                onSuccess = {
//
//                })
//        }
//    }

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

    fun updateTheShopFontFamily(fontName:String,fontResources: FontResource){
        _selectedFonts.value = Pair(fontName,fontResources)
    }

    fun reset(){
        _registerShopResponse.value = null
        _isUploading.value = false
    }
}