package com.pepdeal.infotech

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.categories.CategoriesScreen
import com.pepdeal.infotech.product.ProductScreen
import com.pepdeal.infotech.profile.ProfileScreen
import com.pepdeal.infotech.shop.ShopScreen
import com.pepdeal.infotech.shopVideo.FeedScreen
import network.chaintech.sdpcomposemultiplatform.sdp


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainBottomNavigationWithPager() {
    var selectedItem by remember { mutableStateOf(0) }

    // HorizontalPager for "ViewPager2" equivalent
    val pagerState = rememberPagerState(initialPage = 4, pageCount = { 5 })

    // Sync selectedItem with pagerState
    LaunchedEffect(pagerState.currentPage) {
        if (!pagerState.isScrollInProgress) {
            selectedItem = pagerState.currentPage
        }
    }

    // Define a scaffold to place the bottom navigation
    MaterialTheme {
        Scaffold(
            contentWindowInsets =  WindowInsets(0),
            containerColor = Color.White,
            bottomBar = {
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    NavigationBar(
                        modifier = Modifier.height(48.dp),
                        containerColor = Color.White,
                        contentColor = Color.White,
                        tonalElevation = NavigationBarDefaults.Elevation
                    ) {
                        val items = listOf(
                            "Feed" to Icons.Filled.Home,
                            "Shop" to Icons.Filled.ShoppingCart,
                            "Product" to Icons.AutoMirrored.Filled.List,
                            "Categories" to Icons.Filled.LocationOn,
                            "Profile" to Icons.Filled.Person
                        )

                        items.forEachIndexed { index, (label, icon) ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        icon,
                                        contentDescription = label,
                                        tint = if (selectedItem == index) Color.Black else Color.Gray,
                                        modifier = Modifier.padding(bottom = 0.sdp)
                                    )
                                },
                                label = {
                                    Text(
                                        label,
                                        fontSize = 10.sp,
                                        color = if (selectedItem == index) Color.Black else Color.Gray,
                                        modifier = Modifier.padding(top = 0.sdp)
                                    )
                                },
                                alwaysShowLabel = true,
                                selected = selectedItem == index,
                                onClick = {
                                    selectedItem = index
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    unselectedIconColor = Color.Gray,
                                    selectedTextColor = Color.Black,
                                    unselectedTextColor = Color.Gray,
                                    disabledIconColor = Color.Gray,
                                    disabledTextColor = Color.Gray,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
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
