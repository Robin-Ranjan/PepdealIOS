package com.pepdeal.infotech.categoriesProduct.repo

import com.pepdeal.infotech.categoriesProduct.viewModel.CategoryWiseProductViewModal
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import kotlinx.coroutines.flow.Flow

interface CategoryWiseProductRepository {
    suspend fun getTheCategoryWiseProduct(
        subCategoryName: String,
        userId: String? = null
    ): Flow<AppResult<CategoryWiseProductViewModal.CategoryWiseProductModel, DataError.Remote>>
}