package com.pepdeal.infotech.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.util.CategoriesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel():ViewModel() {
    private val _categories = MutableStateFlow<List<ProductCategories>>(emptyList())
    val categories: StateFlow<List<ProductCategories>> = _categories

    private val _subCategoriesMap = MutableStateFlow<Map<Int, List<SubCategory>>>(emptyMap())
    val subCategoriesMap: StateFlow<Map<Int, List<SubCategory>>> = _subCategoriesMap

    init {
        loadCategories()
    }

    private fun loadCategories() {
        // Simulate network or database fetch
        viewModelScope.launch {
            val categoriesList = CategoriesUtil.productCategories
            val subCategoriesMap = CategoriesUtil.subCategories.groupBy { it.categoryId }
            _categories.value = categoriesList
            _subCategoriesMap.value = subCategoriesMap
        }
    }
}