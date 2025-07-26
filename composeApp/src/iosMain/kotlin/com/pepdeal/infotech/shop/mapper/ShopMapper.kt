package com.pepdeal.infotech.shop.mapper

import com.pepdeal.infotech.core.databaseUtils.DatabaseDocument
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.shop.modal.ShopMaster

object ShopMapper{
    fun DatabaseDocument.toShopMaster(): ShopMaster? {
        val fields = this.fields ?: return null
        return ShopMaster(
            shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopName = (fields["shopName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopMobileNo = (fields["shopMobileNo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopAddress = (fields["shopAddress"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopAddress2 = (fields["shopAddress2"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopArea = (fields["shopArea"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            city = (fields["city"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            state = (fields["state"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            pinCode = (fields["pinCode"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopDescription = (fields["shopDescription"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            bgColourId = (fields["bgColourId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            fontSizeId = (fields["fontSizeId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            fontStyleId = (fields["fontStyleId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            fontColourId = (fields["fontColourId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopActive = (fields["shopActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            flag = (fields["flag"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            latitude = (fields["latitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            longitude = (fields["longitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            shopStatus = (fields["shopStatus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values
                ?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                .orEmpty(),
            isVerified = (fields["isVerified"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            showNumber = (fields["showNumber"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
            geoHash = (fields["geoHash"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
        )
    }
}