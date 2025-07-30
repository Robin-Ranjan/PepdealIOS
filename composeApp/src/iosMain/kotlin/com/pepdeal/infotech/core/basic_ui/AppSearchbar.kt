package com.pepdeal.infotech.core.basic_ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.pepdeal.infotech.shop.BackGroundColor
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSearchBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    isSearchLoading: Boolean = false,
    placeholderText: String = "Search...",
    onSearchTriggered: (String) -> Unit = {},
    onClearClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    SearchBar(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        colors = SearchBarColors(
            containerColor = BackGroundColor,
            dividerColor = Color.Gray
        ),
        shape = RectangleShape,
        shadowElevation = SearchBarDefaults.TonalElevation,
        inputField = {
            SearchBarDefaults.InputField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { onSearchTriggered(searchQuery) },
                expanded = isSearchActive,
                onExpandedChange = onSearchActiveChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                placeholder = {
                    if (searchQuery.isEmpty()) {
                        AnimatedSearchHintText(query = searchQuery)
                    }
                },
                leadingIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            onSearchActiveChange(false)
                            onClearClick()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search"
                            )
                        }
                    }
                }
            )
        },
        expanded = isSearchActive,
        onExpandedChange = onSearchActiveChange,
    ) {
        when {
            isSearchLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Blue)
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent()
                                    keyboardController?.hide()
                                }
                            }
                        }
                ) {
                    content()
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedSearchHintText(query: String) {
    val suggestions = listOf("Food 🍔", "Fashion 👗", "Hotels 🏨", "Electronics 📱")
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = query.isEmpty()) {
        if (query.isEmpty()) {
            while (true) {
                delay(2500)
                index = (index + 1) % suggestions.size
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Static "Search" part
        Text(
            text = "Search ",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        // Animated suggestion part
        AnimatedContent(
            targetState = suggestions[index],
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            },
            label = "Animated Suggestion"
        ) { word ->
            Text(
                text = word,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}
