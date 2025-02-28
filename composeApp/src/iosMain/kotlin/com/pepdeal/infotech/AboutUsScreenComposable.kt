package com.pepdeal.infotech

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.util.NavigationProvider.navController
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.Font
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen() {
    var aboutText by remember { mutableStateOf("") }

    // Extract text from HTML
    LaunchedEffect(Unit) {
        val rawHtml = """
            <div xss="removed"><span lang="EN-US" xss="removed"><font face="Arial">Welcome to Pepdeal, 
            we're dedicated to giving you the very best of experience with focus on dependability, 
            customer service and uniqueness.</font></span></div>
            <div xss="removed"><span lang="EN-US" xss="removed"><font face="Arial">Pepdeal began with a simple vision 
            where innovative technology can be combined with the potential retail owners to start their 
            business online & connect with customers looking for any kind of products.</font></span></div>
            <div xss="removed"><font face="Arial">Pepdeal's mission is to make it easy to do businesses 
            from anywhere. We offer to enable the retailers to transform the way they market, sell and 
            operate with the vision to improve their efficiency & scope.</font></div>
            <div xss="removed"><font face="Arial">We believe in the power of technology & strongly recommend 
            that retailers should get the chance to engage with their customers in a more focused manner and 
            operate in a much more efficient way.</font></div>
        """.trimIndent()

        aboutText = parseHtml(rawHtml)
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "About Us",
                            fontSize = 20.ssp,
                            lineHeight = 20.ssp,
                            fontFamily = FontFamily(Font(Res.font.manrope_bold)),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,  // Background color
                        titleContentColor = Color.Black,  // Title color
                        navigationIconContentColor = Color.Black,  // Back button color
                        actionIconContentColor = Color.Unspecified
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                // Scrollable Text Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = aboutText,
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Start,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

fun parseHtml(html: String): String {
    return html.replace(Regex("<[^>]*>"), "").trim() // Removes HTML tags using regex
}
