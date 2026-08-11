package com.example.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeSplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animatable states for precise choreography
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.90f) }
    
    // Ring pulse micro-animation (animates outward once)
    val ringScale = remember { Animatable(1.0f) }
    val ringAlpha = remember { Animatable(0f) }
    
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    // Final "approach user" transition
    val approachScale = remember { Animatable(1.0f) }
    val approachAlpha = remember { Animatable(1.0f) }

    LaunchedEffect(Unit) {
        // Step 1: Dark background appears & Logo fades in (Opacity 0->100%, Scale 90%->100%, 350ms)
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1.00f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }

        delay(300)

        // Step 2 & Micro Animation: Subtle pulse (100% -> 105% -> 100%) + Gentle outward chime ring pulse
        launch {
            logoScale.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
            logoScale.animateTo(
                targetValue = 1.00f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
        }

        // Single outward ringing soundwave pulse
        launch {
            ringAlpha.snapTo(0.45f)
            ringScale.animateTo(
                targetValue = 1.35f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
            ringAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )
        }

        delay(180)

        // Step 3: Fade in "TimeChime"
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        }

        delay(140)

        // Fade in "Every Moment Matters"
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        }

        // Step 4: Hold briefly
        delay(300)

        // Step 5: Logo gently moves toward the user (Scale 100% -> 125%, Opacity 100% -> 0%)
        launch {
            approachScale.animateTo(
                targetValue = 1.25f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }
        launch {
            approachAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }

        delay(320)

        // Step 6: Crossfade into Home screen
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer(alpha = approachAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(approachScale.value)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(logoScale.value)
                    .graphicsLayer(alpha = logoAlpha.value)
            ) {
                // Outward Ring Chime Soundwave Pulse
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(ringScale.value)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha.value),
                            shape = CircleShape
                        )
                )

                // Soft Background Glow Aura
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                )

                // Vector Logo Asset (Crisp, zero white corners, high contrast)
                Image(
                    painter = painterResource(id = R.drawable.ic_timechime_logo),
                    contentDescription = "TimeChime Logo",
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title
            Text(
                text = "TimeChime",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer(alpha = titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // App Tagline
            Text(
                text = "Every Moment Matters",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.25.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.graphicsLayer(alpha = taglineAlpha.value)
            )
        }
    }
}
