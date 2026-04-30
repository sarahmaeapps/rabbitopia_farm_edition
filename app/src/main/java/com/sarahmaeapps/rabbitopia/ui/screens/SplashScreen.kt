package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarahmaeapps.rabbitopia.ui.theme.SunshineDream
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 1f,
        animationSpec = repeatable(
            iterations = 1,
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000) // Stay on splash for 2 seconds
        onAnimationFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim).scale(scaleAnim)
        ) {
            Text(
                text = "Rabbitopia!",
                fontSize = 56.sp,
                fontFamily = SunshineDream,
                color = Color(0xFF880015) // Deep Red for Logo
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Purebred Excellence",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
