package com.pepdeal.infotech.product.mapper

import com.pepdeal.infotech.core.databaseUtils.DatabaseDocument
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.product.ProductMaster

object ProductMapper {

    fun DatabaseDocument.toProductMaster(): ProductMaster? {
        val fields = this.fields ?: return null
        return ProductMaster(
            productId = (fields["productId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            productName = (fields["productName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            brandId = (fields["brandId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            brandName = (fields["brandName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            categoryId = (fields["categoryId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            subCategoryId = (fields["subCategoryId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            description = (fields["description"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            description2 = (fields["description2"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            specification = (fields["specification"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            warranty = (fields["warranty"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            sizeId = (fields["sizeId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            sizeName = (fields["sizeName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            color = (fields["color"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values?.map { it.stringValue }
                .orEmpty(),
            onCall = (fields["onCall"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            mrp = (fields["mrp"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            discountMrp = (fields["discountMrp"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            sellingPrice = (fields["sellingPrice"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            productActive = (fields["productActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            flag = (fields["flag"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopActive = (fields["shopActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopBlock = (fields["shopBlock"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopLongitude = (fields["shopLongitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopLatitude = (fields["shopLatitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
        )
    }
}