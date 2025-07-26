package com.pepdeal.infotech.util

import com.pepdeal.infotech.yourShop.YourShopViewModal
import com.pepdeal.infotech.categories.CategoriesViewModel
import com.pepdeal.infotech.product.addProduct.AddNewProductViewModal
import com.pepdeal.infotech.product.updateProduct.UpdateProductViewModal
import com.pepdeal.infotech.shop.OpenYourShopViewModal
import com.pepdeal.infotech.shop.editShop.EditShopDetailsViewModal
import com.pepdeal.infotech.shop.shopDetails.ShopDetailsViewModal
import com.pepdeal.infotech.shopVideo.ShopVideosViewModal
import com.pepdeal.infotech.shopVideo.uploadShopVideo.UploadShopVideoViewModal
import com.pepdeal.infotech.user.PersonalInfoViewModal

object ViewModals {
    val categoriesViewModel by lazy { CategoriesViewModel() }
    val shopDetailsViewModal by lazy { ShopDetailsViewModal() }
    val editShopViewModal by lazy { EditShopDetailsViewModal() }
    val personalInfoViewModal by lazy { PersonalInfoViewModal() }
    val shopVideosViewModal by lazy { ShopVideosViewModal() }
    val openYOurShopViewModal by lazy { OpenYourShopViewModal() }
    val addNewProductViewModal by lazy { AddNewProductViewModal() }
    val updateProductViewModal by lazy { UpdateProductViewModal() }
    val uploadShopVideoViewModal by lazy { UploadShopVideoViewModal() }
    val yourShopViewModal by lazy { YourShopViewModal() }
}
