package com.pepdeal.infotech

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.util.NavigationProvider.navController
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.pepdeal_logo

@Composable
fun SplashScreen(){

    val yOffset = remember { Animatable(300f) }

    // Trigger animation on launch
    LaunchedEffect(Unit) {
        yOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )

        // Delay before navigating
        delay(500)
        print("Host root created, calling next  1")
        // Navigate after animation completes
        navController.navigate(Routes.MainPage) {
            print("Host root created, calling next screen 2")
            popUpTo(Routes.SplashScreenPage) { inclusive = true }
        }
    }


    MaterialTheme{
        Scaffold(
            containerColor = Color.White
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(it)){
                Image(
                    painter = painterResource(Res.drawable.pepdeal_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(top = 20.dp, start = 10.dp, end = 10.dp)
                        .offset(y = yOffset.value.dp)
                )
            }
        }
    }
}