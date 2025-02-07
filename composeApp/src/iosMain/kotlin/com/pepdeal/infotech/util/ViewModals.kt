package com.pepdeal.infotech.util

import com.pepdeal.infotech.categories.CategoriesViewModel
import com.pepdeal.infotech.favourite.FavoriteProductViewModal
import com.pepdeal.infotech.login.LoginViewModal
import com.pepdeal.infotech.product.ProductViewModal
import com.pepdeal.infotech.shop.ShopViewModal

object ViewModals {
    val shopViewModel by lazy { ShopViewModal() }
    val productViewModal by lazy { ProductViewModal() }
    val categoriesViewModel by lazy { CategoriesViewModel() }
    val loginViewModal by lazy { LoginViewModal() }
    val favoriteProductViewModal by lazy { FavoriteProductViewModal() }
}