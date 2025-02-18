package com.pepdeal.infotech

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.categories.CategoriesScreen
import com.pepdeal.infotech.product.ProductScreen
import com.pepdeal.infotech.profile.ProfileScreen
import com.pepdeal.infotech.shop.ShopScreen
import com.pepdeal.infotech.shopVideo.FeedScreen


@Composable
fun MainBottomNavigationWithPager() {
    var selectedItem by remember { mutableStateOf(0) }

    // HorizontalPager for "ViewPager2" equivalent
    val pagerState = rememberPagerState(initialPage = 3, pageCount = { 5 })

    LaunchedEffect(pagerState.currentPage) {
        selectedItem = pagerState.currentPage // Update selectedItem when swiping
    }

    // Define a scaffold to place the bottom navigation
    MaterialTheme{
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
                    4 -> ProfileScreen()
                }
            }
        }

        // Launch the scroll animation after the BottomNavigation item change
        LaunchedEffect(selectedItem) {
            pagerState.scrollToPage(selectedItem)
        }
    }
}
