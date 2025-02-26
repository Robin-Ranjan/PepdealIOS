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
        ProductCategories(4, "Sports & Fitness"),
        ProductCategories(5, "Books"),
        ProductCategories(6, "Health & Beauty"),
        ProductCategories(7, "Automotive"),
        ProductCategories(8, "Toys & Games"),
        ProductCategories(9, "Grocery"),
        ProductCategories(10, "Pet Supplies"),
        ProductCategories(11, "Personalized Gifts"),
        ProductCategories(12,"Stationary"),
        ProductCategories(13,"Hardware & Paint"),
        ProductCategories(14,"Food & Beverages")
    )

    val subCategories = listOf(
        // Electronics
        SubCategory(1, 1, "Mobiles & Accessories", "https://fdn2.gsmarena.com/vv/bigpic/apple-iphone-14-pro.jpg"),
        SubCategory(2, 1, "Computer & Laptop", "https://pngimg.com/uploads/laptop/laptop_PNG101763.png"),
//        SubCategory(3, 1, "Home & Kitchen Appliances", "https://pngimg.com/uploads/photo_camera/photo_camera_PNG7828.png"),
        SubCategory(3, 1, "Home & Kitchen Appliances", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FElectronics%2Fcompressed_png-clipart-home-appliance-refrigerator-graphy-cooking-ranges-small-appliance-home-appliances-kitchen-electronics-removebg-preview.png?alt=media&token=048ce61b-91c0-47b9-ac5f-a9526c8d70a6"),
        SubCategory(4, 1, "Tv,Audio & Entertainment", "https://www.freepngimg.com/thumb/tv/22378-2-tv.png"),
        SubCategory(5, 1, "Gaming & Accessories", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FElectronics%2Fcompressed_pngtree-holding-a-controller-png-image_2664577-removebg-preview.png?alt=media&token=7396dd03-b3d4-4910-b03f-6e524fd1645c"),
        SubCategory(6, 1, "Smart Gadgets & Wearables", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FElectronics%2Fcompressed_pngtree-ai-powered-health-gadgets-improving-diagnostics-accuracy-png-image_14888216.png?alt=media&token=a906b46d-61fe-47af-8b27-ada14f43041d"),


        // Fashion
        SubCategory(7, 2, "Men's Clothing", "https://images.unsplash.com/photo-1512436991641-6745cdb1723f"),
        SubCategory(8, 2, "Women's Clothing", "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c"),
        SubCategory(9, 2, "Kid's Clothing", "https://images.freeimages.com/images/large-previews/fb0/shoes-1420565.jpg"),
        SubCategory(10, 2, "Accessories", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b"),
        SubCategory(11, 2, "Footwear", "https://images.unsplash.com/photo-1512436991641-6745cdb1723f"),
        SubCategory(12, 2, "Sportswear", "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"),
        SubCategory(13, 2, "Swimwear", "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"),
        SubCategory(14, 2, "Materials", "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"),


        // Home & Kitchen
        SubCategory(15, 3, "Kitchen ware", "https://images.unsplash.com/photo-1582495617764-67583f1d7215"),
        SubCategory(16, 3, "Home Decor & Furnishings", "https://images.unsplash.com/photo-1505692794403-eab5e3adf1b2"),
        SubCategory(17, 3, "Lighting", "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2"),
        SubCategory(18, 3, "Gardening", "https://images.unsplash.com/photo-1506748686214-e9df14d4d9d0"),
        SubCategory(19, 3, "Furniture & Storage", "https://images.unsplash.com/photo-1592194996308-7d757625fe36"),
        SubCategory(20, 3, "Cleaning & Household Supplies", "https://images.unsplash.com/photo-1616627787931-3c29c86578ff"),
        SubCategory(21, 3, "Kitchen & Dining Essentials", "https://images.unsplash.com/photo-1570561571408-11db3dfba1d7"),
        SubCategory(22, 3, "Bath Accessories", "https://images.unsplash.com/photo-1597767780401-3c84e4e3b8f4"),

        // Sports & Fitness
        SubCategory(23, 4, "Sports Equipment", "https://images.unsplash.com/photo-1599058917217-fbe4d4da0837"),
        SubCategory(24, 4, "Fitness & Gym Gear", "https://images.unsplash.com/photo-1542377284-2b28c136c4c6"),
        SubCategory(25, 4, "Outdoor & Adventure Gear", "https://images.unsplash.com/photo-1528353819071-64a51a3d7b87"),
        SubCategory(26, 4, "Water Sports", "https://images.unsplash.com/photo-1518655061811-f0c8f5403e7b"),
        SubCategory(27, 4, "Kids Sports", "https://images.unsplash.com/photo-1518655061811-f0c8f5403e7b"),
        SubCategory(28, 4, "Sportswear & Accessories", "https://images.unsplash.com/photo-1518655061811-f0c8f5403e7b"),
        SubCategory(29, 4, "Yoga & Pilates", "https://images.unsplash.com/photo-1606238882904-0e0d43b75c0d"),

        // Books
        SubCategory(30, 5, "Fiction Books", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fbooks%2F2.jpeg?alt=media&token=3037177e-31d5-4e41-99bd-ef45fdc7bead"),
        SubCategory(32, 5, "Non-fiction Books", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fbooks%2F2%20(1).jpeg?alt=media&token=d0f9cc4f-2592-45a1-add5-4585310745f6"),
        SubCategory(33, 5, "Academics Books", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fbooks%2Facademic_books?alt=media&token=a45f9c80-562b-48eb-be6d-df744e7b751e"),
        SubCategory(34, 5, "Magazines & Comics", "https://images.unsplash.com/photo-1561234468-2d3605e0b41a"),
        SubCategory(35, 5, "Hobby & Special Interest", "https://images.unsplash.com/photo-1511974035430-5de47d3b95da"),
        SubCategory(36, 5, "Note Books", "https://images.unsplash.com/photo-1553798395-2ea636b7f374"),

        // Health & Beauty
        SubCategory(37, 6, "Makeup", "https://images.unsplash.com/photo-1556228724-4a381e7d7b42"),
        SubCategory(38, 6, "Skincare", "https://images.unsplash.com/photo-1586816879429-35c0841dd8df"),
        SubCategory(39, 6, "Haircare", "https://images.unsplash.com/photo-1617118617150-62d7eec13f83"),
        SubCategory(40, 6, "Personal Care", "https://images.unsplash.com/photo-1619947680853-a1df87dc13e4"),
        SubCategory(41, 6, "Perfumes", "https://images.unsplash.com/photo-1574273974281-48c0c0d88a27"),
        SubCategory(42, 6, "Vitamins & Supplements", "https://images.unsplash.com/photo-1598970434795-0c54fe7c0642"),
        SubCategory(43, 6, "Surgical", "https://images.unsplash.com/photo-1519010764707-63697fcf92e3"),
        SubCategory(44, 6, "Medicines & Others", "https://images.unsplash.com/photo-1556228724-4a381e7d7b42"),
        SubCategory(45, 6, "Ayurvedic & Homeopathic Medicines", "https://images.unsplash.com/photo-1517840545241-cfe78c01d273"),
        SubCategory(46, 6, "Hygiene Products", "https://images.unsplash.com/photo-1566954579192-e313213ef026"),

        // Automotive
        SubCategory(47, 7, "Car Accessories", "https://images.unsplash.com/photo-1601025792213-c2d08d3d8b5c"),
        SubCategory(48, 7, "Motorbike Accessories", "https://images.unsplash.com/photo-1600986603720-e7b7f21e19cf"),
        SubCategory(49, 7, "Tools & Equipment", "https://images.unsplash.com/photo-1556761175-129418cb2dfe"),
        SubCategory(50, 7, "Vehicle Repair & Care", "https://images.unsplash.com/photo-1611175694221-7d7d71e392d2"),

        // Toys & Games
        SubCategory(51, 8, "Educational & Learning Toys", "https://images.unsplash.com/photo-1542482374-d33d6a2a6e81"),
        SubCategory(52, 8, "Action Figures & Collections", "https://images.unsplash.com/photo-1542482374-d33d6a2a6e81"),
        SubCategory(53, 8, "Puzzles", "https://images.unsplash.com/photo-1586810167152-25f5a1b4cd16"),
        SubCategory(54, 8,"Indoor & Board Games", "https://example.com/educational-toys.jpg"),
        SubCategory(55, 8,"Outdoor & Sports Toys", "https://example.com/outdoor-playsets.jpg"),
        SubCategory(56, 8,"Soft Toys & Role Play", "https://example.com/outdoor-playsets.jpg"),
        SubCategory(57, 8,"Remote Operated Toys", "https://example.com/outdoor-playsets.jpg"),

        // Grocery
        SubCategory(58, 9, "Fruits & Vegetables", "https://images.unsplash.com/photo-1561043433-aaf687c4cf04"),
        SubCategory(59, 9, "Dairy Products", "https://images.unsplash.com/photo-1573664676428-2827b61d5179"),
        SubCategory(60, 9, "Snacks", "https://images.unsplash.com/photo-1600654280423-c0f89d9b9dbd"),
        SubCategory(61, 9, "Beverages", "https://images.unsplash.com/photo-1561794570-b15fe2db78c5"),
        SubCategory(62, 9, "Frozen Foods", "https://images.unsplash.com/photo-1598224573088-f4e48fbb31f8"),
        SubCategory(63, 9, "Meat & Seafood", "https://images.unsplash.com/photo-1606096989069-129c6c4b9787"),

        // Pet Supplies
        SubCategory(64, 10, "Collars & Leashes", "https://images.unsplash.com/photo-1560807707-8cc77767d783"),
        SubCategory(65, 10, "Clothes And Grooming", "https://images.unsplash.com/photo-1560807707-8cc77767d783"),
        SubCategory(66, 10, "Pet Toys", "https://images.unsplash.com/photo-1560807707-8cc77767d783"),
        SubCategory(67, 10, "Pet Bowls", "https://images.unsplash.com/photo-1560807707-8cc77767d783"),
        SubCategory(68, 10, "Food & Treats", "https://images.unsplash.com/photo-1560807707-8cc77767d783"),
        SubCategory(69, 10, "Aquarium & Accessories", "https://images.unsplash.com/photo-1560807707-8cc77767d783"),


        //personalized gift
        SubCategory(70, 11, "Custom Accessories", "https://example.com/custom-jewelry.jpg"),
        SubCategory(71, 11, "Photo & Memory Gifts", "https://example.com/photo-gifts.jpg"),
        SubCategory(72, 11, "Occasions & Festive Gifts", "https://example.com/photo-gifts.jpg"),
        SubCategory(73, 11, "Personalized Bags", "https://example.com/personalized-bags.jpg"),
        SubCategory(74, 11, "Custom Pet Items", "https://example.com/custom-pet-items.jpg"),


        //Stationary
        SubCategory(75, 12, "General Stationary", "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"),
        SubCategory(76, 12, "Office Supplies", "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"),
        SubCategory(77, 12, "Art & Craft Supplies", "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"),

        //Hardware & Paint
        SubCategory(78,13,"Tools & Equipments",""),
        SubCategory(79,13,"Electrical & Plumbing",""),
        SubCategory(80,13,"Construction Material",""),
        SubCategory(81,13,"Paints & Adhesive",""),
        SubCategory(82,13,"Safety & Security",""),

        // Food & Beverages
        SubCategory(83,14,"Sabjis & Curries",""),
        SubCategory(84,14,"Thalis & Combos",""),
        SubCategory(85,14,"Chinese Dishes",""),
        SubCategory(86,14,"South Indian Dishes",""),
        SubCategory(87,14,"North Indian Dishes",""),
        SubCategory(88,14,"Mughlai & Biryani",""),
        SubCategory(89,14,"Fast Food & Snacks",""),
        SubCategory(90,14,"Pizzas & Pastas",""),
        SubCategory(91,14,"Burgers & Sandwiches",""),
        SubCategory(92,14,"Street Food & Chat",""),
        SubCategory(93,14,"Desserts & Sweets",""),
        SubCategory(94,14,"Beverages",""),
        SubCategory(95,14,"Alcoholic Beverages",""),
    )

}