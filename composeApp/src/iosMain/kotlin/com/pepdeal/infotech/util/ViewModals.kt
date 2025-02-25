package com.pepdeal.infotech.util

import com.pepdeal.infotech.ProfileScreenViewModal
import com.pepdeal.infotech.categories.CategoriesViewModel
import com.pepdeal.infotech.favourite.FavoriteProductViewModal
import com.pepdeal.infotech.login.LoginViewModal
import com.pepdeal.infotech.product.AddNewProductViewModal
import com.pepdeal.infotech.product.ListAllProductViewModal
import com.pepdeal.infotech.product.ProductViewModal
import com.pepdeal.infotech.product.UpdateProductViewModal
import com.pepdeal.infotech.shop.OpenYourShopViewModal
import com.pepdeal.infotech.shop.editShop.EditShopDetailsViewModal
import com.pepdeal.infotech.shop.shopDetails.ShopDetailsViewModal
import com.pepdeal.infotech.shop.ShopViewModal
import com.pepdeal.infotech.shopVideo.favShopVideo.FavoriteShopVideoViewModal
import com.pepdeal.infotech.shopVideo.ShopVideosViewModal
import com.pepdeal.infotech.shopVideo.UploadShopVideoViewModal
import com.pepdeal.infotech.superShop.SuperShopViewModal
import com.pepdeal.infotech.tickets.SellerTicketViewModal
import com.pepdeal.infotech.tickets.TicketViewModal
import com.pepdeal.infotech.user.PersonalInfoViewModal

object ViewModals {
    val shopViewModel by lazy { ShopViewModal() }
    val productViewModal by lazy { ProductViewModal() }
    val categoriesViewModel by lazy { CategoriesViewModel() }
    val loginViewModal by lazy { LoginViewModal() }
    val favoriteProductViewModal by lazy { FavoriteProductViewModal() }
    val customerTicketViewModal by lazy {TicketViewModal()}
    val sellerTicketViewModal by lazy { SellerTicketViewModal() }
    val shopDetailsViewModal by lazy { ShopDetailsViewModal() }
    val editShopViewModal by lazy { EditShopDetailsViewModal() }
    val personalInfoViewModal by lazy { PersonalInfoViewModal() }
    val superShopViewModal by lazy { SuperShopViewModal() }
    val shopVideosViewModal by lazy { ShopVideosViewModal() }
    val favoriteShopVideoViewModal by lazy { FavoriteShopVideoViewModal() }
    val profileScreenViewModal by lazy { ProfileScreenViewModal() }
    val openYOurShopViewModal by lazy { OpenYourShopViewModal() }
    val addNewProductViewModal by lazy { AddNewProductViewModal() }
    val updateProductViewModal by lazy { UpdateProductViewModal() }
    val listAllProductViewModal by lazy { ListAllProductViewModal() }
    val uploadShopVideoViewModal by lazy { UploadShopVideoViewModal() }
}