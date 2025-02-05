package com.pepdeal.infotech.categories

data class ProductCategories(
    val id: Int,
    val name: String,
    var isSelected:Boolean = false
)

data class SubCategory(
    val id: Int,
    val categoryId: Int, // This links the subcategory to a category
    val name: String,
    val imageUrl: String,
    var isSelected: Boolean = false
)

