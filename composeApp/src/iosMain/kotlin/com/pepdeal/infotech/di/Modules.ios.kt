package com.pepdeal.infotech.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pepdeal.infotech.ProfileScreenViewModal
import com.pepdeal.infotech.categoriesProduct.repo.CategoryWiseProductRepository
import com.pepdeal.infotech.categoriesProduct.repo.CategoryWiseProductRepositoryImpl
import com.pepdeal.infotech.categoriesProduct.viewModel.CategoryWiseProductViewModal
import com.pepdeal.infotech.dataStore.createDataStore
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepositoryImpl
import com.pepdeal.infotech.favourite_product.viewModel.FavoriteProductViewModal
import com.pepdeal.infotech.login.LoginRepositoryImpl
import com.pepdeal.infotech.login.repository.LoginRepository
import com.pepdeal.infotech.login.viewModel.LoginViewModal
import com.pepdeal.infotech.placeAPI.repository.AddressRepository
import com.pepdeal.infotech.placeAPI.repository.AddressRepositoryImpl
import com.pepdeal.infotech.placeAPI.viewModel.LocationViewModel
import com.pepdeal.infotech.product.ListAllProductRepository
import com.pepdeal.infotech.product.listProduct.repository.ListAllProductRepositoryImpl
import com.pepdeal.infotech.product.listProduct.viewModel.ListAllProductViewModal
import com.pepdeal.infotech.product.producrDetails.ProductDetailsViewModal
import com.pepdeal.infotech.product.ProductViewModel
import com.pepdeal.infotech.product.productUseCases.ProductUseCase
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.product.repository.ProductRepositoryImpl
import com.pepdeal.infotech.product.repository.ProductSearchRepository
import com.pepdeal.infotech.product.repository.ProductSearchRepositoryImpl
import com.pepdeal.infotech.shop.repository.SearchShopRepository
import com.pepdeal.infotech.shop.repository.SearchShopRepositoryImpl
import com.pepdeal.infotech.shop.viewModel.ShopViewModel
import com.pepdeal.infotech.shop.repository.ShopRepository
import com.pepdeal.infotech.shop.repository.ShopRepositoryImpl
import com.pepdeal.infotech.shop.shopUseCases.ShopUseCase
import com.pepdeal.infotech.shopVideo.favShopVideo.repository.FavoriteShopVideoRepository
import com.pepdeal.infotech.shopVideo.favShopVideo.repository.FavoriteShopVideoRepositoryImpl
import com.pepdeal.infotech.shopVideo.favShopVideo.viewModel.FavoriteShopVideoViewModal
import com.pepdeal.infotech.superShop.repository.SuperShopRepository
import com.pepdeal.infotech.superShop.repository.SuperShopRepositoryImpl
import com.pepdeal.infotech.superShop.viewModel.SuperShopViewModal
import com.pepdeal.infotech.tickets.domain.SellerTicketRepository
import com.pepdeal.infotech.tickets.domain.TicketRepository
import com.pepdeal.infotech.tickets.repository.SellerTicketRepositoryImpl
import com.pepdeal.infotech.tickets.repository.TicketRepositoryImpl
import com.pepdeal.infotech.tickets.viewModel.SellerTicketViewModal
import com.pepdeal.infotech.tickets.viewModel.TicketViewModal
import com.pepdeal.infotech.user.repository.PersonalInfoRepository
import com.pepdeal.infotech.user.repository.PersonalInfoRepositoryImpl
import com.pepdeal.infotech.user.repository.UserRepository
import com.pepdeal.infotech.user.repository.UserRepositoryImpl
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<DataStore<Preferences>> { createDataStore() }
        single<HttpClientEngine> { Darwin.create() }
        singleOf(::SearchShopRepositoryImpl).bind<SearchShopRepository>()
        singleOf(::ProductSearchRepositoryImpl).bind<ProductSearchRepository>()

        singleOf(::TicketRepositoryImpl).bind<TicketRepository>()
        singleOf(::LoginRepositoryImpl).bind<LoginRepository>()
        singleOf(::PersonalInfoRepositoryImpl).bind<PersonalInfoRepository>()
        singleOf(::FavouriteProductRepositoryImpl).bind<FavouriteProductRepository>()
        singleOf(::SuperShopRepositoryImpl).bind<SuperShopRepository>()
        singleOf(::ShopRepositoryImpl).bind<ShopRepository>()
        singleOf(::ProductRepositoryImpl).bind<ProductRepository>()
        singleOf(::UserRepositoryImpl).bind<UserRepository>()
        singleOf(::ListAllProductRepositoryImpl).bind<ListAllProductRepository>()
        singleOf(::CategoryWiseProductRepositoryImpl).bind<CategoryWiseProductRepository>()
        singleOf(::SellerTicketRepositoryImpl).bind<SellerTicketRepository>()
        singleOf(::FavoriteShopVideoRepositoryImpl).bind<FavoriteShopVideoRepository>()
        singleOf(::AddressRepositoryImpl).bind<AddressRepository>()

        viewModel { ProductDetailsViewModal(get()) }
        viewModel { TicketViewModal(get(), get()) }
        viewModel { LoginViewModal(get(), get()) }
        viewModel { ProfileScreenViewModal(get(), get()) }
        viewModel { FavoriteProductViewModal(get(), get()) }
        viewModel { SuperShopViewModal(get(), get()) }
        viewModel { ListAllProductViewModal(get(), get()) }
        viewModel { CategoryWiseProductViewModal(get(), get(), get(), get()) }
        viewModel { SellerTicketViewModal(get(), get()) }
        viewModel { FavoriteShopVideoViewModal(get(), get()) }
        viewModel { ShopViewModel(get(), get()) }
        viewModelOf(::ProductViewModel)
        viewModelOf(::LocationViewModel)


//        Use Cases (business logic)
        single {
            ShopUseCase(
                shopRepository = get(),
                productRepository = get(),
                shopSearchRepository = get()
            )
        }

        single {
            ProductUseCase(
                productRepository = get(),
                favRepo = get(),
                productSearchRepository = get()
            )
        }
    }
