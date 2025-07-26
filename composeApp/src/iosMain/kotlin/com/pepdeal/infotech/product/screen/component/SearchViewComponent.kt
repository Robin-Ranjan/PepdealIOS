package com.pepdeal.infotech.product.screen.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchView(label: String, searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(15.dp), // Rounded corners
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(0.5.dp, Color.Black) // Stroke color and width
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp) // Padding for the content inside the card
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Black,
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(start = 3.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            TextField(
                value = searchQuery,
                onValueChange = { newQuery -> onSearchQueryChanged(newQuery) },
                label = {
                    if (!isFocused && searchQuery.isEmpty()) {
                        Text(label, fontSize = 14.sp, color = Color.Gray, lineHeight = 14.sp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .focusRequester(focusRequester) // Attach focusRequester to the TextField
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused // Track focus state
                    },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    // Customize TextField colors
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Gray,
                    disabledTextColor = Color.LightGray,
                    errorTextColor = Color.Red,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    errorContainerColor = Color.Red.copy(alpha = 0.1f),
                    cursorColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent, // No underline
                    unfocusedIndicatorColor = Color.Transparent, // No underline
                    disabledIndicatorColor = Color.Transparent, // No underline
                    errorIndicatorColor = Color.Red, // Red underline for error state
                    focusedLabelColor = Color.Gray, // Label color when focused
                    unfocusedLabelColor = Color.Gray, // Label color when unfocused
                    errorLabelColor = Color.Red, // Label color for error state
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
            )
        }
    }
}