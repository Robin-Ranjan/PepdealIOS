package com.pepdeal.infotech.util


import com.pepdeal.infotech.categories.ProductCategories
import com.pepdeal.infotech.categories.SubCategory

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
        SubCategory(3, 1, "Home & Kitchen Appliances", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FElectronics%2FUntitleddesign44_enhance.jpeg?alt=media&token=be60a147-6017-4387-813d-e8ed5a6dfd1e"),
        SubCategory(4, 1, "Tv,Audio & Entertainment", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FElectronics%2FUntitleddesign60_enhance.jpeg?alt=media&token=b5ec2da9-ba23-46dd-b394-e899b6fe3fc6"),
        SubCategory(5, 1, "Gaming & Accessories", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FElectronics%2FUntitleddesign42_enhance.jpeg?alt=media&token=ca70a0be-ac55-48c4-be37-473c39ba2b20"),
        SubCategory(6, 1, "Smart Gadgets & Wearables", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FElectronics%2FUntitleddesign43_enhance.jpeg?alt=media&token=5f7e544c-66df-4390-b0fd-28cee9671fa4"),

        // Fashion
        SubCategory(7, 2, "Men's Clothing", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFashion%2FUntitleddesign45_enhance.jpeg?alt=media&token=d3235cf3-e745-4bd1-a087-78cccdf14776"),
        SubCategory(8, 2, "Women's Clothing", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFashion%2FUntitleddesign46_enhance.jpeg?alt=media&token=4d3a986c-bc2a-4a91-a9d7-9f3efdcf604b"),
        SubCategory(9, 2, "Kid's Clothing", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFashion%2FUntitleddesign47_enhance.jpeg?alt=media&token=2116166d-ff3e-4d75-91db-eb90006c13da"),
        SubCategory(10, 2, "Accessories", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFashion%2FUntitleddesign48_enhance.jpeg?alt=media&token=5f7c0264-e02e-4822-9de2-3594a63b45b3"),
        SubCategory(11, 2, "Footwear", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFashion%2FUntitleddesign49_enhance.jpeg?alt=media&token=13942e4e-22cb-4be3-a117-35f4a5bece3d"),
        SubCategory(12, 2, "Sportswear", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFashion%2FUntitleddesign50_enhance.jpeg?alt=media&token=6e2cc556-a68a-427a-aad8-d95ffb2aec4c"),
        SubCategory(13, 2, "Swimwear", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFashion%2FUntitleddesign51_enhance.jpeg?alt=media&token=ef74c047-5669-4c7a-a97f-4dbe9fb18cf6"),
        SubCategory(14, 2, "Materials", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFashion%2FUntitleddesign83_enhance.jpeg?alt=media&token=e6024186-090a-4474-829b-5947289b8f17"),

        // Home & Kitchen
        SubCategory(15, 3, "Kitchen ware", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHome%20%26%20Kitchen%2FUntitleddesign52_enhance.jpeg?alt=media&token=8d507eb8-10ab-446c-aac8-dc5df62c81f6"),
        SubCategory(16, 3, "Home Decor & Furnishings", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHome%20%26%20Kitchen%2FUntitleddesign53_enhance.jpeg?alt=media&token=a85ee352-f1c9-427a-a960-68ca2823de57"),
        SubCategory(17, 3, "Lighting", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHome%20%26%20Kitchen%2FUntitleddesign54_enhance.jpeg?alt=media&token=511d2c82-1d56-445e-b9e3-5d953fe1113e"),
        SubCategory(18, 3, "Gardening", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHome%20%26%20Kitchen%2FUntitleddesign55_enhance.jpeg?alt=media&token=cdd9b4e6-646c-440c-96e9-1b6814740b8a"),
        SubCategory(19, 3, "Furniture & Storage", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHome%20%26%20Kitchen%2FUntitleddesign56_enhance.jpeg?alt=media&token=a8a7a9f3-8f63-48f5-b543-34438c310704"),
        SubCategory(20, 3, "Cleaning & Household Supplies", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHome%20%26%20Kitchen%2FUntitleddesign57_enhance.jpeg?alt=media&token=7b61b00d-83a5-45c2-a0f6-db63a0bdc201"),
        SubCategory(21, 3, "Kitchen & Dining Essentials", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHome%20%26%20Kitchen%2FUntitleddesign58_enhance.jpeg?alt=media&token=f19677d4-0c4c-4c1b-94cd-53a7f4f0e2f1"),
        SubCategory(22, 3, "Bath Accessories", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHome%20%26%20Kitchen%2FUntitleddesign59_enhance.jpeg?alt=media&token=22131b5c-6916-442f-b70b-9649fa82ef1e"),

        // Sports & Fitness
        SubCategory(23, 4, "Sports Equipment", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FSports%20%26%20Fitness%2FUntitleddesign61_enhance.jpeg?alt=media&token=15ec83e1-e2e4-44d6-a66b-86fa7e225820"),
        SubCategory(24, 4, "Fitness & Gym Gear", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FSports%20%26%20Fitness%2FUntitleddesign62_enhance.jpeg?alt=media&token=943298ae-de67-45d5-9479-b034db4a455f"),
        SubCategory(25, 4, "Outdoor & Adventure Gear", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FSports%20%26%20Fitness%2FUntitleddesign63_enhance.jpeg?alt=media&token=e51c279a-93af-4d38-b414-da58a396c4f7"),
        SubCategory(26, 4, "Water Sports", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FSports%20%26%20Fitness%2FUntitleddesign64_enhance.jpeg?alt=media&token=bb0c1c36-e688-4848-aebc-c92bd923e4f2"),
        SubCategory(27, 4, "Kids Sports", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FSports%20%26%20Fitness%2FUntitleddesign65_enhance.jpeg?alt=media&token=4ec31576-1399-4f90-95a8-ec6574c2b9b2"),
        SubCategory(28, 4, "Sportswear & Accessories", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FSports%20%26%20Fitness%2FUntitleddesign66_enhance.jpeg?alt=media&token=ac4ab5ae-051c-4044-a472-a4d643cfa85c"),
        SubCategory(29, 4, "Yoga & Pilates", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FSports%20%26%20Fitness%2FUntitleddesign67_enhance.jpeg?alt=media&token=9f102383-eedf-4502-b316-057f34650fab"),

        // Books
        SubCategory(30, 5, "Fiction Books", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fbooks%2Ffiction_books.jpeg?alt=media&token=4cd13d64-fb62-4ac2-9c78-8004d76a2847"),
        SubCategory(32, 5, "Non-fiction Books", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fbooks%2F2%20(1).jpeg?alt=media&token=d0f9cc4f-2592-45a1-add5-4585310745f6"),
        SubCategory(33, 5, "Academics Books", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fbooks%2Facademin_boo.jpeg?alt=media&token=56ab538b-4957-47db-a055-c8bed110cdac"),
        SubCategory(34, 5, "Magazines & Comics", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fbooks%2FUntitleddesign39_enhance.jpeg?alt=media&token=4ed1a85f-d30e-4bb8-b544-52a0f93be367"),
        SubCategory(35, 5, "Hobby & Special Interest", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fbooks%2FUntitleddesign40_enhance.jpeg?alt=media&token=25de97d4-8c68-4b46-a2f1-75c3183c624f"),
        SubCategory(36, 5, "Note Books", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fbooks%2FUntitleddesign41_enhance.jpeg?alt=media&token=2ba3197e-1b36-4318-895d-231f68c3cfa2"),

        // Health & Beauty
        SubCategory(37, 6, "Makeup", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2FUntitleddesign33_enhance.jpeg?alt=media&token=51716cf6-d924-4103-b05e-f6eeb5abc587"),
        SubCategory(38, 6, "Skincare", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2FUntitleddesign34_enhance.jpeg?alt=media&token=2fdcfbf6-d96d-43af-8091-262f00ceadf6"),
        SubCategory(39, 6, "Haircare", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2FUntitleddesign35_enhance.jpeg?alt=media&token=e9af972a-768d-462d-9d3d-54817a973cdc"),
        SubCategory(40, 6, "Personal Care", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2FUntitleddesign38_enhance.jpeg?alt=media&token=5018e695-c04b-4ef8-8a71-c3d543d61efb"),
        SubCategory(41, 6, "Perfumes", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2FUntitleddesign36_enhance.jpeg?alt=media&token=725b9d61-874d-41b8-9889-b90095e74f68"),
        SubCategory(42, 6, "Vitamins & Supplements", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2FUntitleddesign37_enhance.jpeg?alt=media&token=6c890547-4e71-4418-853f-af39a7b62ef8"),
        SubCategory(43, 6, "Surgical", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2F20250307_131254_000%20(7).jpeg?alt=media&token=27dc1da6-d282-4f6a-8107-46663074554b"),
        SubCategory(44, 6, "Medicines & Others", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2F20250307_131254_000%20(6).jpeg?alt=media&token=e50d9de0-f045-42a2-b25e-1e430ffa2282"),
        SubCategory(45, 6, "Ayurvedic & Homeopathic Medicines", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2F20250307_131254_000%20(5).jpeg?alt=media&token=166fbf44-e504-4a29-a090-751debde0a1e"),
        SubCategory(46, 6, "Hygiene Products", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHealth%20%26%20Beauty%2F20250307_131254_000%20(4).jpeg?alt=media&token=27c3916b-989f-41ff-9eed-8dd8696c1450"),

        // Automotive
        SubCategory(47, 7, "Car Accessories", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FAutomotive%2F20250307_131254_000%20(9).jpeg?alt=media&token=16b0f19c-e266-4c7f-9117-1698f197355f"),
        SubCategory(48, 7, "Motorbike Accessories", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FAutomotive%2FUntitleddesign81_enhance.jpeg?alt=media&token=333b435d-262e-472f-b3a3-c97d0b972ef9"),
        SubCategory(49, 7, "Tools & Equipment", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FAutomotive%2FUntitleddesign32_enhance.jpeg?alt=media&token=beeaa201-c7c8-4581-aa99-6aa2b5819823"),
        SubCategory(50, 7, "Vehicle Repair & Care", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FAutomotive%2F20250307_131254_000%20(8).jpeg?alt=media&token=4454b511-2ce3-4d31-99f4-1266a6168160"),

        // Toys & Games
        SubCategory(51, 8, "Educational & Learning Toys", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FToys%20%26%20Games%2FUntitleddesign25_enhance.jpeg?alt=media&token=ae8c435b-3493-4130-b8ff-b958c97519bd"),
        SubCategory(52, 8, "Action Figures & Collections", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FToys%20%26%20Games%2FUntitleddesign26_enhance.jpeg?alt=media&token=675cfb56-b620-4be9-8136-5a8cb429a2aa"),
        SubCategory(53, 8, "Puzzles", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FToys%20%26%20Games%2FUntitleddesign27_enhance.jpeg?alt=media&token=d0fe2790-ef95-4f63-bcef-65603c2ebe07"),
        SubCategory(54, 8,"Indoor & Board Games", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FToys%20%26%20Games%2FUntitleddesign28_enhance.jpeg?alt=media&token=823e5ca4-d7a5-4a0c-9e27-032f902af135"),
        SubCategory(55, 8,"Outdoor & Sports Toys", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FToys%20%26%20Games%2FUntitleddesign29_enhance.jpeg?alt=media&token=b0b899d1-fa73-495a-9a8e-3a56998d20c1"),
        SubCategory(56, 8,"Soft Toys & Role Play", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FToys%20%26%20Games%2FUntitleddesign30_enhance.jpeg?alt=media&token=d6fcc3d0-6ed7-4dc3-a6b4-8155121b330a"),
        SubCategory(57, 8,"Remote Operated Toys", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FToys%20%26%20Games%2FUntitleddesign31_enhance.jpeg?alt=media&token=29e68f9d-d2da-48e4-803c-b5032c11fbe7"),

        // Grocery
        SubCategory(58, 9, "Fruits & Vegetables", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FGrocery%2FUntitleddesign.jpeg?alt=media&token=a238f1d5-9e11-4b5f-9968-07c327e9ce3e"),
        SubCategory(59, 9, "Dairy Products", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FGrocery%2F2%20(2).jpeg?alt=media&token=01c13e2c-76fe-4c1c-9788-d219137d54cd"),
        SubCategory(60, 9, "Snacks", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FGrocery%2FUntitleddesign%20(1).jpeg?alt=media&token=0118660a-8d34-4ec2-844f-813ae47c95d4"),
        SubCategory(61, 9, "Beverages", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FGrocery%2FUntitleddesign%20(2).jpeg?alt=media&token=c30af120-39f1-4e67-85d5-106e2cc54727"),
        SubCategory(62, 9, "Frozen Foods", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FGrocery%2F6099878347.jpeg?alt=media&token=e503260e-bc36-4ac8-a839-05c6a60388a0"),
        SubCategory(63, 9, "Meat & Seafood", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FGrocery%2FUntitleddesign5_enhance.jpeg?alt=media&token=5c34ae6a-f37e-4b92-b983-764c0c8aad0f"),

        // Pet Supplies
        SubCategory(64, 10, "Collars & Leashes", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpet_supplies%2FUntitleddesign6_enhance.jpeg?alt=media&token=d835e5c0-0639-4190-b9a3-736eeaf6ad48"),
        SubCategory(65, 10, "Clothes And Grooming", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpet_supplies%2FUntitleddesign7_enhance.jpeg?alt=media&token=ea43aa10-f602-48c5-af46-3cd999219b74"),
        SubCategory(66, 10, "Pet Toys", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpet_supplies%2FUntitleddesign8_enhance.jpeg?alt=media&token=532462a0-086a-4172-bb3c-9628216674f0"),
        SubCategory(67, 10, "Pet Bowls", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpet_supplies%2FUntitleddesign9_enhance.jpeg?alt=media&token=33945e59-51fb-4f61-85ae-5904f60eac27"),
        SubCategory(68, 10, "Food & Treats", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpet_supplies%2FUntitleddesign10_enhance.jpeg?alt=media&token=f4dc9140-598f-45c8-89df-4cbecb515dac"),
        SubCategory(69, 10, "Aquarium & Accessories", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpet_supplies%2FUntitleddesign11_enhance.jpeg?alt=media&token=1f64dc8c-6173-44b8-b0ad-f728dbc47ab8"),

        //personalized gift
        SubCategory(70, 11, "Custom Accessories", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpersonalized%20gift%2F20250307_131254_000%20(3).jpeg?alt=media&token=5b91e45d-eff4-418c-93b9-0a55acdcd9f6"),
        SubCategory(71, 11, "Photo & Memory Gifts", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpersonalized%20gift%2FUntitleddesign69_enhance.jpeg?alt=media&token=5b9db67d-6ccd-44cb-a895-910ce1e567f0"),
        SubCategory(72, 11, "Occasions & Festive Gifts", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpersonalized%20gift%2FUntitleddesign68_enhance.jpeg?alt=media&token=06c1edc3-d7ca-4959-b199-f96b3cf8c67c"),
        SubCategory(73, 11, "Personalized Bags", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpersonalized%20gift%2F20250307_131254_000%20(2).jpeg?alt=media&token=2761d838-5f8e-4ce9-a6b1-3ccd997d9d85"),
        SubCategory(74, 11, "Custom Pet Items", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpersonalized%20gift%2F20250307_131254_000%20(1).jpeg?alt=media&token=485779d6-0c3b-4a70-a3c4-e6e3294a6756"),

        //Stationary
        SubCategory(75, 12, "General Stationary", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FStationary%2FUntitleddesign13_enhance.jpeg?alt=media&token=06ee29fc-9f1a-4446-94f9-e6c1f6ecb76f"),
        SubCategory(76, 12, "Office Supplies", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FStationary%2FUntitleddesign14_enhance.jpeg?alt=media&token=1fc3be98-d779-4d25-958d-32573568f3e5"),
        SubCategory(77, 12, "Art & Craft Supplies", "${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FStationary%2FUntitleddesign12_enhance.jpeg?alt=media&token=e4c4191b-a4b3-445d-9bd9-dbbacaa4d2a7"),

        //Hardware & Paint
        SubCategory(78,13,"Tools & Equipments","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHardware%20%26%20Paint%2F20250307_131254_000.jpeg?alt=media&token=b30131ee-9d0b-4700-8ece-b3e9e07df1f0"),
        SubCategory(79,13,"Electrical & Plumbing","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpersonalized%20gift%2FUntitleddesign72_enhance.jpeg?alt=media&token=abf88e48-21c5-44f0-b3c8-206b94121f31"),
        SubCategory(80,13,"Construction Material","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpersonalized%20gift%2FUntitleddesign71_enhance.jpeg?alt=media&token=6efdc4e7-e1f4-413b-9a67-73784ef07232"),
        SubCategory(81,13,"Paints & Adhesive","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FHardware%20%26%20Paint%2F20250307_131254_001%20(1).jpeg?alt=media&token=156fd856-eb9f-4dab-b08d-2b3791b3cdd2"),
        SubCategory(82,13,"Safety & Security","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2Fpersonalized%20gift%2FUntitleddesign70_enhance.jpeg?alt=media&token=93fc2051-f517-4516-bf88-13772088f48b"),

        // Food & Beverages
        SubCategory(83,14,"Sabjis & Curries","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign15_enhance.jpeg?alt=media&token=acbf9a28-67e5-4b5e-a138-279b4847cf19"),
        SubCategory(84,14,"Thalis & Combos","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign15_enhance.jpeg?alt=media&token=acbf9a28-67e5-4b5e-a138-279b4847cf19"),
        SubCategory(85,14,"Chinese Dishes","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign17_enhance.jpeg?alt=media&token=0008339b-6bee-4b25-9ec4-db0649a72ab0"),
        SubCategory(86,14,"South Indian Dishes","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign18_enhance.jpeg?alt=media&token=bae5ec20-8059-4d49-9e49-4e9f04b4b03a"),
        SubCategory(87,14,"North Indian Dishes","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2F20250307_131254_001.jpeg?alt=media&token=98003725-903c-4f24-bfa6-bfdfa1b9af6e"),
        SubCategory(88,14,"Mughlai & Biryani","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign19_enhance.jpeg?alt=media&token=cc657ac2-b642-4b7b-a988-7c47015a362d"),
        SubCategory(89,14,"Fast Food & Snacks","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign20_enhance.jpeg?alt=media&token=a2a43ce0-6374-459e-884d-136fc406c868"),
        SubCategory(90,14,"Pizzas & Pastas","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign21_enhance.jpeg?alt=media&token=5e3cb256-5ca4-49da-b8d6-fc3628d71651"),
        SubCategory(91,14,"Burgers & Sandwiches","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign20_enhance.jpeg?alt=media&token=a2a43ce0-6374-459e-884d-136fc406c868"),
        SubCategory(92,14,"Street Food & Chat","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign24_enhance.jpeg?alt=media&token=8761bfd1-1d4d-4399-b9e9-3b74c753d87b"),
        SubCategory(93,14,"Desserts & Sweets","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign23_enhance.jpeg?alt=media&token=1844f0e3-c746-4ffd-8421-a47328bc4090"),
        SubCategory(94,14,"Beverages","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FGrocery%2FUntitleddesign%20(2).jpeg?alt=media&token=c30af120-39f1-4e67-85d5-106e2cc54727"),
        SubCategory(95,14,"Alcoholic Beverages","${FirebaseUtil.CATEGORIES_IMAGES_URL}%2FFood%20%26%20Beverages%2FUntitleddesign22_enhance.jpeg?alt=media&token=f8fad875-ae44-4cc5-a866-402dd9243870"),
    )

}