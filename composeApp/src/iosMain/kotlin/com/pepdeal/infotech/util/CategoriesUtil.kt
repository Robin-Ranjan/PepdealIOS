package com.pepdeal.infotech.util


import com.pepdeal.infotech.categories.ProductCategories
import com.pepdeal.infotech.categories.SubCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CategoriesUtil {

    fun getSubCategoriesList(categoryName: String): List<SubCategory> {
        val matchingCategory =
            productCategories.find { it.name.equals(categoryName, ignoreCase = true) }
        return matchingCategory?.let { category ->
            subCategories.filter { it.categoryId == category.id }
        } ?: emptyList()
    }

    // Function to create the map in a background thread using coroutines
    fun createCategorySubCategoryMapAsync(
        categories: List<ProductCategories>,
        subCategories: List<SubCategory>
    ) {
        val categoryIdToName = categories.associateBy({ it.id }, { it.name.lowercase() })

        subCategories.groupBy { it.categoryId }
            .mapKeys { (categoryId, _) -> categoryIdToName[categoryId] ?: "" }
            .filterKeys { it.isNotEmpty() }
    }

    // Function to get subcategories based on category name using the precomputed map
    fun getSubCategoriesByCategoryNameAsync(
        categoryName: String,
        categorySubCategoryMap: Map<String, List<SubCategory>>
    ) {
        categorySubCategoryMap[categoryName.lowercase()] ?: emptyList()
    }

    fun getSubCategoriesListById(categoryId: Int): List<SubCategory> {
        if(categoryId==-1) return emptyList()
        return subCategories.filter { it.categoryId == categoryId }
    }

    val productCategories = listOf(
        ProductCategories(1, "Electronics"),
        ProductCategories(2, "Fashion"),
        ProductCategories(3, "Home & Kitchen"),
        ProductCategories(4, "Sports & Outdoors"),
        ProductCategories(5, "Books & Media"),
        ProductCategories(6, "Health & Beauty"),
        ProductCategories(7, "Automotive"),
        ProductCategories(8, "Toys & Games"),
        ProductCategories(9, "Grocery"),
        ProductCategories(10, "Pet Supplies"),
        ProductCategories(11, "Personalized Gifts"),
    )

    val subCategories = listOf(
        // Electronics
        SubCategory(1, 1, "Mobiles", "https://fdn2.gsmarena.com/vv/bigpic/apple-iphone-14-pro.jpg"),
        SubCategory(2, 1, "Laptops", "https://pngimg.com/uploads/laptop/laptop_PNG101763.png"),
        SubCategory(
            3,
            1,
            "Cameras",
            "https://pngimg.com/uploads/photo_camera/photo_camera_PNG7828.png"
        ),
        SubCategory(4, 1, "Televisions", "https://www.freepngimg.com/thumb/tv/22378-2-tv.png"),
        SubCategory(
            5,
            1,
            "Headphones",
            "https://images.unsplash.com/photo-1516534775068-ba3e7458af70"
        ),
        SubCategory(
            6,
            1,
            "Wearables",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQagKLAztLqreW5osEfuR4ZeDgGwsu3-Lt8UA&s"
        ),
//        SubCategory(7, 1, "Gaming Consoles", "https://images.unsplash.com/photo-1600963767462-d1d4601d533b"),
        SubCategory(
            8,
            1,
            "Drones",
            "https://www.pngmart.com/files/6/Drone-PNG-Transparent-Image.png"
        ),
        SubCategory(
            9,
            1,
            "Smart Devices",
            "https://cdn0.iconfinder.com/data/icons/appliance-1-2/1024/smart_home6-1024.png"
        ),
        SubCategory(
            10,
            1,
            "Printers",
            "https://images-na.ssl-images-amazon.com/images/I/71Xjvp8czwL._SL1500_.jpg"
        ),

        // Fashion
        SubCategory(
            11,
            2,
            "Men's Clothing",
            "https://images.unsplash.com/photo-1512436991641-6745cdb1723f"
        ),
        SubCategory(
            12,
            2,
            "Women's Clothing",
            "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c"
        ),
        SubCategory(
            13,
            2,
            "Shoes",
            "https://images.freeimages.com/images/large-previews/fb0/shoes-1420565.jpg"
        ),
        SubCategory(
            14,
            2,
            "Accessories",
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b"
        ),
        SubCategory(15, 2, "Bags", "https://images.unsplash.com/photo-1512436991641-6745cdb1723f"),
        SubCategory(
            16,
            2,
            "Watches",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQagKLAztLqreW5osEfuR4ZeDgGwsu3-Lt8UA&s"
        ),
//        SubCategory(17, 2, "Jewelry", "https://images.unsplash.com/photo-1579586335094-4e5e873dd1bb"),
        SubCategory(
            18,
            2,
            "Sunglasses",
            "https://images.unsplash.com/photo-1522337660859-02fbefca4702"
        ),
//        SubCategory(19, 2, "Hats & Caps", "https://images.unsplash.com/photo-1542062703-ec1c66e5f3f7"),
        SubCategory(
            20,
            2,
            "Sportswear",
            "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"
        ),

        // Home & Kitchen
        SubCategory(
            21,
            3,
            "Furniture",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTrEwcyqrGyszFIPFsTWFLgk_STQkLsO_yl7Q&s"
        ),
        SubCategory(
            22,
            3,
            "Cookware",
            "https://images.unsplash.com/photo-1515376721779-7db6951da88d"
        ),
//        SubCategory(23, 3, "Home Decor", "https://images.unsplash.com/photo-1505692794403-eab5e3adf1b2"),
        SubCategory(24, 3, "Lighting", "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2"),
        SubCategory(
            25,
            3,
            "Bedding",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTrEwcyqrGyszFIPFsTWFLgk_STQkLsO_yl7Q&s"
        ),
        SubCategory(
            26,
            3,
            "Gardening",
            "https://images.unsplash.com/photo-1506748686214-e9df14d4d9d0"
        ),
//        SubCategory(27, 3, "Storage Solutions", "https://images.unsplash.com/photo-1592194996308-7d757625fe36"),
//        SubCategory(28, 3, "Cleaning Supplies", "https://images.unsplash.com/photo-1616627787931-3c29c86578ff"),
//        SubCategory(29, 3, "Dining", "https://images.unsplash.com/photo-1570561571408-11db3dfba1d7"),
//        SubCategory(30, 3, "Bath Accessories", "https://images.unsplash.com/photo-1597767780401-3c84e4e3b8f4"),

        // Sports & Outdoors
        SubCategory(
            31,
            4,
            "Fitness Equipment",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT3gXI1YSzenmEdZdC0Hup1Z_XCrjXS7tPuaA&s"
        ),
        SubCategory(
            32,
            4,
            "Cycling",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ2Hvd0OeJIt5xAB9_p4QnrF1L7ErsmnqGISw&s"
        ),
        SubCategory(
            33,
            4,
            "Camping & Hiking",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQD7TIhOdkx7dSLzXYHD0Heg29v2LLNim_tTg&s"
        ),
        SubCategory(
            34,
            4,
            "Water Sports",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQD7TIhOdkx7dSLzXYHD0Heg29v2LLNim_tTg&s"
        ),
        SubCategory(
            35,
            4,
            "Outdoor Clothing",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQD7TIhOdkx7dSLzXYHD0Heg29v2LLNim_tTg&s"
        ),
        SubCategory(
            36,
            4,
            "Sports Shoes",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQD7TIhOdkx7dSLzXYHD0Heg29v2LLNim_tTg&s"
        ),
//        SubCategory(37, 4, "Golf", "https://images.unsplash.com/photo-1530133532239-ed71f3e7de6a"),
//        SubCategory(38, 4, "Team Sports", "https://images.unsplash.com/photo-1598639166302-f228a03d0cb2"),
//        SubCategory(39, 4, "Winter Sports", "https://images.unsplash.com/photo-1518655061811-f0c8f5403e7b"),
//        SubCategory(40, 4, "Yoga & Pilates", "https://images.unsplash.com/photo-1606238882904-0e0d43b75c0d"),

        // Books & Media
        SubCategory(
            41,
            5,
            "Fiction Books",
            "https://images.unsplash.com/photo-1512820790803-83ca734da794"
        ),
        SubCategory(
            42,
            5,
            "Non-fiction Books",
            "https://images.unsplash.com/photo-1516979187457-637abb4f9353"
        ),
        SubCategory(
            43,
            5,
            "Comics & Manga",
            "https://images.unsplash.com/photo-1516979187457-637abb4f9353"
        ),
//        SubCategory(44, 5, "Magazines", "https://images.unsplash.com/photo-1561234468-2d3605e0b41a"),
//        SubCategory(45, 5, "Music CDs", "https://images.unsplash.com/photo-1511415512883-dcebaa9d4b0b"),
//        SubCategory(46, 5, "Movies & TV Shows", "https://images.unsplash.com/photo-1560303381-e94e7fa7fdfd"),
        SubCategory(
            47,
            5,
            "E-books",
            "https://images.unsplash.com/photo-1516979187457-637abb4f9353"
        ),
        SubCategory(
            48,
            5,
            "Children's Books",
            "https://images.unsplash.com/photo-1516979187457-637abb4f9353"
        ),
//        SubCategory(49, 5, "Educational", "https://images.unsplash.com/photo-1533850595622-b4088e63c15b"),
//        SubCategory(50, 5, "Audio Books", "https://images.unsplash.com/photo-1553798395-2ea636b7f374"),

        // Health & Beauty
        SubCategory(
            51,
            6,
            "Skincare",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTC46BfED5FmboeS-zmqMBMKWNfLUC6ST7BMw&s"
        ),
        SubCategory(
            52,
            6,
            "Haircare",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTC46BfED5FmboeS-zmqMBMKWNfLUC6ST7BMw&s"
        ),
//        SubCategory(53, 6, "Makeup", "https://images.unsplash.com/photo-1556228724-4a381e7d7b42"),
//        SubCategory(54, 6, "Fragrances", "https://images.unsplash.com/photo-1574273974281-48c0c0d88a27"),
//        SubCategory(55, 6, "Personal Care", "https://images.unsplash.com/photo-1619947680853-a1df87dc13e4"),
        SubCategory(
            56,
            6,
            "Vitamins & Supplements",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTC46BfED5FmboeS-zmqMBMKWNfLUC6ST7BMw&s"
        ),
        SubCategory(
            57,
            6,
            "Fitness Nutrition",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTC46BfED5FmboeS-zmqMBMKWNfLUC6ST7BMw&s"
        ),
//        SubCategory(58, 6, "Oral Care", "https://images.unsplash.com/photo-1556228724-4a381e7d7b42"),
//        SubCategory(59, 6, "Men's Grooming", "https://images.unsplash.com/photo-1517840545241-cfe78c01d273"),
//        SubCategory(60, 6, "Wellness", "https://images.unsplash.com/photo-1566954579192-e313213ef026"),

        // Automotive
        SubCategory(
            61,
            7,
            "Car Accessories",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcROzewGHnqdVFhMMTC9pJVQeXGgYqf5vvmntQ&s"
        ),
        SubCategory(
            62,
            7,
            "Motorbike Gear",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcROzewGHnqdVFhMMTC9pJVQeXGgYqf5vvmntQ&s"
        ),
        SubCategory(
            63,
            7,
            "Car Care",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcROzewGHnqdVFhMMTC9pJVQeXGgYqf5vvmntQ&s"
        ),
        SubCategory(
            64,
            7,
            "Tools & Equipment",
            "https://images.unsplash.com/photo-1556761175-129418cb2dfe"
        ),
        SubCategory(
            65,
            7,
            "Car Electronics",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcROzewGHnqdVFhMMTC9pJVQeXGgYqf5vvmntQ&s"
        ),

        // Toys & Games
        SubCategory(
            66,
            8,
            "Educational Toys",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQtZ-6ZYTDpn-8EB7giTjrUgu59ClCzJWv4hQ&s"
        ),
        SubCategory(
            67,
            8,
            "Puzzles",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQtZ-6ZYTDpn-8EB7giTjrUgu59ClCzJWv4hQ&s"
        ),

        // Grocery
        SubCategory(
            71,
            9,
            "Fruits & Vegetables",
            "https://images.unsplash.com/photo-1561043433-aaf687c4cf04"
        ),

        // Pet Supplies
        SubCategory(
            76,
            10,
            "Pet Food",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            77,
            10,
            "Pet Care",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            78,
            10,
            "Pet Toys",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            79,
            10,
            "Cleanings Supplies",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            80,
            10,
            "Home Decor",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            81,
            10,
            "Storage Solutions",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),

        //personalized gift
        SubCategory(
            82,
            11,
            "Custom Jewelry",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            83,
            11,
            "Customized Apparel",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            84,
            11,
            "Personalized Home Decor",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            85,
            11,
            "Engraved Kitchenware",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            86,
            11,
            "Photo Gifts",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            87,
            11,
            "Personalized Stationery",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            88,
            11,
            "Customized Tech Gadgets",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            89,
            11,
            "Personalized Bags",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            90,
            11,
            "Custom Pet Items",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            91,
            11,
            "Personalized Gifts for Special Occasions",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),

        SubCategory(
            91,
            8,
            "Child Toys",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            92,
            8,
            "Board Games",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            93,
            8,
            "Action Figures",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),
        SubCategory(
            94,
            8,
            "Outdoor Playsets",
            "https://images.unsplash.com/photo-1560807707-8cc77767d783"
        ),

        // Groceries
        SubCategory(
            95,
            9,
            "Dairy Products",
            "https://images.unsplash.com/photo-1573664676428-2827b61d5179"
        ),
        SubCategory(
            96,
            9,
            "Snacks",
            "https://images.unsplash.com/photo-1600654280423-c0f89d9b9dbd"
        ),
        SubCategory(
            97,
            9,
            "Beverages",
            "https://images.unsplash.com/photo-1561794570-b15fe2db78c5"
        ),
        SubCategory(
            98,
            9,
            "Frozen Foods",
            "https://images.unsplash.com/photo-1598224573088-f4e48fbb31f8"
        ),
        SubCategory(
            99,
            9,
            "Meat & Seafood",
            "https://images.unsplash.com/photo-1606096989069-129c6c4b9787"
        ),

        // Fashion
        SubCategory(
            100,
            2,
            "Material",
            "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"
        ),
        SubCategory(
            101,
            2,
            "Kids Fashion",
            "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"
        ),
    )

}