package com.pepdeal.infotech.util


import com.pepdeal.infotech.categories.ProductCategories
import com.pepdeal.infotech.categories.SubCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CategoriesUtil {

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
        SubCategory(30, 5, "Fiction Books", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fbooks%2Ffiction_books.jpeg?alt=media&token=4cd13d64-fb62-4ac2-9c78-8004d76a2847"),
        SubCategory(32, 5, "Non-fiction Books", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fbooks%2F2%20(1).jpeg?alt=media&token=d0f9cc4f-2592-45a1-add5-4585310745f6"),
        SubCategory(33, 5, "Academics Books", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fbooks%2Facademin_boo.jpeg?alt=media&token=56ab538b-4957-47db-a055-c8bed110cdac"),
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
        SubCategory(51, 8, "Educational & Learning Toys", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FToys%20%26%20Games%2FUntitleddesign25_enhance.jpeg?alt=media&token=ae8c435b-3493-4130-b8ff-b958c97519bd"),
        SubCategory(52, 8, "Action Figures & Collections", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FToys%20%26%20Games%2FUntitleddesign26_enhance.jpeg?alt=media&token=675cfb56-b620-4be9-8136-5a8cb429a2aa"),
        SubCategory(53, 8, "Puzzles", "https://images.unsplash.com/photo-1586810167152-25f5a1b4cd16"),
        SubCategory(54, 8,"Indoor & Board Games", "https://example.com/educational-toys.jpg"),
        SubCategory(55, 8,"Outdoor & Sports Toys", "https://example.com/outdoor-playsets.jpg"),
        SubCategory(56, 8,"Soft Toys & Role Play", "https://example.com/outdoor-playsets.jpg"),
        SubCategory(57, 8,"Remote Operated Toys", "https://example.com/outdoor-playsets.jpg"),

        // Grocery
        SubCategory(58, 9, "Fruits & Vegetables", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FGrocery%2FUntitleddesign.jpeg?alt=media&token=a238f1d5-9e11-4b5f-9968-07c327e9ce3e"),
        SubCategory(59, 9, "Dairy Products", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FGrocery%2F2%20(2).jpeg?alt=media&token=01c13e2c-76fe-4c1c-9788-d219137d54cd"),
        SubCategory(60, 9, "Snacks", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FGrocery%2FUntitleddesign%20(1).jpeg?alt=media&token=0118660a-8d34-4ec2-844f-813ae47c95d4"),
        SubCategory(61, 9, "Beverages", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FGrocery%2FUntitleddesign%20(2).jpeg?alt=media&token=c30af120-39f1-4e67-85d5-106e2cc54727"),
        SubCategory(62, 9, "Frozen Foods", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FGrocery%2F6099878347.jpeg?alt=media&token=e503260e-bc36-4ac8-a839-05c6a60388a0"),
        SubCategory(63, 9, "Meat & Seafood", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FGrocery%2FUntitleddesign5_enhance.jpeg?alt=media&token=5c34ae6a-f37e-4b92-b983-764c0c8aad0f"),

        // Pet Supplies
        SubCategory(64, 10, "Collars & Leashes", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fpet_supplies%2FUntitleddesign6_enhance.jpeg?alt=media&token=d835e5c0-0639-4190-b9a3-736eeaf6ad48"),
        SubCategory(65, 10, "Clothes And Grooming", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fpet_supplies%2FUntitleddesign7_enhance.jpeg?alt=media&token=ea43aa10-f602-48c5-af46-3cd999219b74"),
        SubCategory(66, 10, "Pet Toys", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fpet_supplies%2FUntitleddesign8_enhance.jpeg?alt=media&token=532462a0-086a-4172-bb3c-9628216674f0"),
        SubCategory(67, 10, "Pet Bowls", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fpet_supplies%2FUntitleddesign9_enhance.jpeg?alt=media&token=33945e59-51fb-4f61-85ae-5904f60eac27"),
        SubCategory(68, 10, "Food & Treats", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fpet_supplies%2FUntitleddesign10_enhance.jpeg?alt=media&token=f4dc9140-598f-45c8-89df-4cbecb515dac"),
        SubCategory(69, 10, "Aquarium & Accessories", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2Fpet_supplies%2FUntitleddesign11_enhance.jpeg?alt=media&token=1f64dc8c-6173-44b8-b0ad-f728dbc47ab8"),

        //personalized gift
        SubCategory(70, 11, "Custom Accessories", "https://example.com/custom-jewelry.jpg"),
        SubCategory(71, 11, "Photo & Memory Gifts", "https://example.com/photo-gifts.jpg"),
        SubCategory(72, 11, "Occasions & Festive Gifts", "https://example.com/photo-gifts.jpg"),
        SubCategory(73, 11, "Personalized Bags", "https://example.com/personalized-bags.jpg"),
        SubCategory(74, 11, "Custom Pet Items", "https://example.com/custom-pet-items.jpg"),

        //Stationary
        SubCategory(75, 12, "General Stationary", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FStationary%2FUntitleddesign13_enhance.jpeg?alt=media&token=06ee29fc-9f1a-4446-94f9-e6c1f6ecb76f"),
        SubCategory(76, 12, "Office Supplies", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FStationary%2FUntitleddesign14_enhance.jpeg?alt=media&token=1fc3be98-d779-4d25-958d-32573568f3e5"),
        SubCategory(77, 12, "Art & Craft Supplies", "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FStationary%2FUntitleddesign12_enhance.jpeg?alt=media&token=e4c4191b-a4b3-445d-9bd9-dbbacaa4d2a7"),

        //Hardware & Paint
        SubCategory(78,13,"Tools & Equipments",""),
        SubCategory(79,13,"Electrical & Plumbing",""),
        SubCategory(80,13,"Construction Material",""),
        SubCategory(81,13,"Paints & Adhesive",""),
        SubCategory(82,13,"Safety & Security",""),

        // Food & Beverages
        SubCategory(83,14,"Sabjis & Curries","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign15_enhance.jpeg?alt=media&token=acbf9a28-67e5-4b5e-a138-279b4847cf19"),
        SubCategory(84,14,"Thalis & Combos","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign15_enhance.jpeg?alt=media&token=acbf9a28-67e5-4b5e-a138-279b4847cf19"),
        SubCategory(85,14,"Chinese Dishes","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign17_enhance.jpeg?alt=media&token=0008339b-6bee-4b25-9ec4-db0649a72ab0"),
        SubCategory(86,14,"South Indian Dishes","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign18_enhance.jpeg?alt=media&token=bae5ec20-8059-4d49-9e49-4e9f04b4b03a"),
        SubCategory(87,14,"North Indian Dishes",""),
        SubCategory(88,14,"Mughlai & Biryani","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign19_enhance.jpeg?alt=media&token=cc657ac2-b642-4b7b-a988-7c47015a362d"),
        SubCategory(89,14,"Fast Food & Snacks","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign20_enhance.jpeg?alt=media&token=a2a43ce0-6374-459e-884d-136fc406c868"),
        SubCategory(90,14,"Pizzas & Pastas","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign21_enhance.jpeg?alt=media&token=5e3cb256-5ca4-49da-b8d6-fc3628d71651"),
        SubCategory(91,14,"Burgers & Sandwiches","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign20_enhance.jpeg?alt=media&token=a2a43ce0-6374-459e-884d-136fc406c868"),
        SubCategory(92,14,"Street Food & Chat","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign24_enhance.jpeg?alt=media&token=8761bfd1-1d4d-4399-b9e9-3b74c753d87b"),
        SubCategory(93,14,"Desserts & Sweets","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign23_enhance.jpeg?alt=media&token=1844f0e3-c746-4ffd-8421-a47328bc4090"),
        SubCategory(94,14,"Beverages","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FGrocery%2FUntitleddesign%20(2).jpeg?alt=media&token=c30af120-39f1-4e67-85d5-106e2cc54727"),
        SubCategory(95,14,"Alcoholic Beverages","https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/Categories_images%2FFood%20%26%20Beverages%2FUntitleddesign22_enhance.jpeg?alt=media&token=f8fad875-ae44-4cc5-a866-402dd9243870"),
    )

}