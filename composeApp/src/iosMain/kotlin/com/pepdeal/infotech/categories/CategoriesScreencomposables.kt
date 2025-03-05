package com.pepdeal.infotech.categories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.SearchView
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.place_holder

@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = ViewModals.categoriesViewModel) {

    // Observable
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subCategoriesMap by viewModel.subCategoriesMap.collectAsStateWithLifecycle()

    // variables
    var filteredCategories by remember { mutableStateOf(categories) }
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val displayedCategories = remember(searchQuery, categories) {
        if (searchQuery.isNotEmpty()) filteredCategories else categories
    }

    LaunchedEffect(searchQuery, categories, subCategoriesMap) {
        // Perform filtering on a background thread (if necessary)
        val filtered = withContext(Dispatchers.Default) {
            categories.mapNotNull { category ->
                // Check if category name or any subcategory name matches the search query
                if (category.name.contains(searchQuery, ignoreCase = true) ||
                    subCategoriesMap[category.id]?.any { subCategory ->
                        subCategory.name.contains(searchQuery, ignoreCase = true)
                    } == true
                ) {
                    category
                } else {
                    null
                }
            }
        }
        // If no categories matched, return the original categories list
        filteredCategories = filtered.ifEmpty { categories }
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()
            .background(color = Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
                detectHorizontalDragGestures { _, _ ->
                    keyboardController?.hide()
                }
            }) {

            SearchView("Search Product", searchQuery) {
                searchQuery = it
            }
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 5.dp)
                ) {
                    items(displayedCategories,
                        key = { it.id }) { category ->
                        CategoryCard(
                            category,
                            subCategoriesMap[category.id] ?: emptyList()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryCard(
    category: ProductCategories,
    subCategories: List<SubCategory>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(modifier = Modifier.padding(5.dp)) {
            Text(
                text = category.name.toNameFormat(),
                color = Color.Black,
                style = TextStyle(fontSize = 15.sp, lineHeight = 15.sp, color = Color.Black),
                modifier = Modifier.fillMaxWidth()
                    .padding(all = 3.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 4, // Ensure 4 items per row
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                subCategories.chunked(4).forEach { rowItems ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // For each chunk (group of 4 or less) distribute items
                        rowItems.forEach { subCategory ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                SubCategoryItem(subCategory, onSubCategoryClickListener = {
                                    NavigationProvider.navController.navigate(
                                        Routes.CategoryWiseProductPage(
                                            subCategory.name
                                        )
                                    )
                                })
                            }
                        }

                        // Fill the remaining space with empty space if needed
                        if (rowItems.size < 4) {
                            repeat(4 - rowItems.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubCategoryItem(
    subCategory: SubCategory,
    onSubCategoryClickListener: (SubCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(2.dp)
            .clickable { onSubCategoryClickListener(subCategory) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(70.dp),
            shape = RectangleShape,
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Box(contentAlignment = Alignment.Center) {
                CoilImage(
                    modifier = Modifier
                        .fillMaxSize(),
                    imageModel = { subCategory.imageUrl },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Inside,
                        alignment = Alignment.Center
                    ),
                    previewPlaceholder = painterResource(Res.drawable.compose_multiplatform),
                    loading = {
                        Image(
                            painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                            contentDescription = "Placeholder",
                            modifier = Modifier.fillMaxSize()
                                .background(color = Color.White),
                            contentScale = ContentScale.Crop,
                        )
                    },
                    failure = {
                        Image(
                            painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                            contentDescription = "Placeholder",
                            modifier = Modifier.fillMaxSize()
                                .background(color = Color.White),
                            contentScale = ContentScale.Crop
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        TruncatedText(
            text = subCategory.name,
            maxLines = 2,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
fun TruncatedText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    textStyle: TextStyle = TextStyle(
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
        color = Color.Black
    )
) {
    var finalText by remember { mutableStateOf(text) }

    Text(
        text = finalText,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = textStyle,
        modifier = modifier,
        onTextLayout = { layoutResult ->
            // Check if the text overflows and we have reached the maximum lines
            if (layoutResult.hasVisualOverflow) {
                val lastCharIndex =
                    layoutResult.getLineEnd(maxLines - 1).coerceAtMost(text.length - 1)

                // Ensure that the last word doesn't get split
                finalText = if (text[lastCharIndex].isWhitespace()) {
                    text.take(lastCharIndex)
                        .trimEnd() + "…" // Apply truncation without splitting words
                } else {
                    // If the last character is not a space, truncate and add ellipsis
                    val spaceBeforeLastWord = text.lastIndexOf(' ', lastCharIndex)
                    text.take(spaceBeforeLastWord).trimEnd() + "…"
                }
            }
        }
    )
}



