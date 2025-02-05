import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.categories.CategoriesScreen
import com.pepdeal.infotech.login.LoginScreen
import com.pepdeal.infotech.product.ProductScreen
import com.pepdeal.infotech.profile.ProfileScreen
import com.pepdeal.infotech.registration.RegisterScreen
import com.pepdeal.infotech.shop.ShopScreen

@Composable
fun FeedScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Feed Screen", style = MaterialTheme.typography.headlineSmall)
    }
}

// Bottom Navigation and Pager
@Composable
fun BottomNavigationWithPager() {
    var selectedItem by remember { mutableStateOf(4) }

    // HorizontalPager for "ViewPager2" equivalent
    val pagerState = rememberPagerState(initialPage = 4, pageCount = { 5 })

    // Define a scaffold to place the bottom navigation
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "Feed",
                            tint = if (selectedItem == 0) Color.Black else Color.Gray // Change icon color based on selection
                        )
                    },
                    label = {
                        Text(
                            "Feed",
                            style = TextStyle(
                                lineHeight = 12.sp,
                                fontSize = 12.sp, // Decrease text size
                                color = if (selectedItem == 0) Color.Black else Color.Gray // Text color based on selection
                            )
                        )
                    },
                    alwaysShowLabel = false,
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                    },
                    colors = NavigationBarItemColors(
                        selectedIndicatorColor = Color.Transparent,
                        selectedIconColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        selectedTextColor = Color.Black,
                        disabledIconColor = Color.Gray,
                        disabledTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = "Shop",
                            tint = if (selectedItem == 1) Color.Black else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Shop",
                            style = TextStyle(
                                lineHeight = 12.sp,
                                fontSize = 12.sp,
                                color = if (selectedItem == 1) Color.Black else Color.Gray
                            )
                        )
                    },
                    alwaysShowLabel = false,
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                    },
                    colors = NavigationBarItemColors(
                        selectedIndicatorColor = Color.Transparent,
                        selectedIconColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        selectedTextColor = Color.Black,
                        disabledIconColor = Color.Gray,
                        disabledTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Product",
                            tint = if (selectedItem == 2) Color.Black else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Product",
                            style = TextStyle(
                                lineHeight = 12.sp,
                                fontSize = 12.sp,
                                color = if (selectedItem == 2) Color.Black else Color.Gray
                            )
                        )
                    },
                    alwaysShowLabel = false,
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                    },
                    colors = NavigationBarItemColors(
                        selectedIndicatorColor = Color.Transparent,
                        selectedIconColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        selectedTextColor = Color.Black,
                        disabledIconColor = Color.Gray,
                        disabledTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = "Categories",
                            tint = if (selectedItem == 3) Color.Black else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Categories",
                            style = TextStyle(
                                lineHeight = 12.sp,
                                fontSize = 12.sp,
                                color = if (selectedItem == 3) Color.Black else Color.Gray
                            )
                        )
                    },
                    alwaysShowLabel = false,
                    selected = selectedItem == 3,
                    onClick = {
                        selectedItem = 3
                    },
                    colors = NavigationBarItemColors(
                        selectedIndicatorColor = Color.Transparent,
                        selectedIconColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        selectedTextColor = Color.Black,
                        disabledIconColor = Color.Gray,
                        disabledTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = if (selectedItem == 4) Color.Black else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Profile",
                            style = TextStyle(
                                lineHeight = 12.sp,
                                fontSize = 12.sp,
                                color = if (selectedItem == 4) Color.Black else Color.Gray
                            )
                        )
                    },
                    alwaysShowLabel = false,
                    selected = selectedItem == 4,
                    onClick = {
                        selectedItem = 4
                    },
                    colors = NavigationBarItemColors(
                        selectedIndicatorColor = Color.Transparent,
                        selectedIconColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        selectedTextColor = Color.Black,
                        disabledIconColor = Color.Gray,
                        disabledTextColor = Color.Gray
                    )
                )
            }
        }
    ) { padding ->
        // Using HorizontalPager for ViewPager2
        HorizontalPager(state = pagerState, modifier = Modifier.padding(padding)) { page ->
            when (page) {
                0 -> FeedScreen()
                1 -> ShopScreen()
                2 -> ProductScreen()
                3 -> CategoriesScreen()
                4 -> RegisterScreen()
            }
        }
    }

    // Launch the scroll animation after the BottomNavigation item change
    LaunchedEffect(selectedItem) {
        pagerState.scrollToPage(selectedItem)
    }
}

@Composable
fun PreviewBottomNavigationWithPager() {
    MaterialTheme {
        BottomNavigationWithPager()
    }
}
