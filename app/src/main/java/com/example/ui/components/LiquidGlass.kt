package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorderAmoled
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.GlassSurfaceAmoled
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.LiquidAqua
import com.example.ui.theme.LiquidEmerald
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.LiquidWaterBlue
import com.example.ui.viewmodel.AppScreen
import kotlin.math.sin

/**
 * Ambient Liquid Fluid Background that renders smooth animated liquid orbs
 * creating realistic depth and refraction behind translucent glass panels.
 */
@Composable
fun LiquidGlassBackground(
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme() || isAmoled
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidBackgroundTransition")

    val floatAnim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FluidRotation1"
    )

    val floatAnim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FluidRotation2"
    )

    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WavePulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Base Ambient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        // Animated Liquid Orbs
        if (!isAmoled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val rad1 = Math.toRadians(floatAnim1.toDouble())
                val rad2 = Math.toRadians(floatAnim2.toDouble())

                val orb1X = width * 0.25f + (sin(rad1) * 70f).toFloat()
                val orb1Y = height * 0.2f + (sin(rad1 * 0.7) * 90f).toFloat()

                val orb2X = width * 0.8f + (sin(rad2) * 80f).toFloat()
                val orb2Y = height * 0.7f + (sin(rad2 * 0.8) * 110f).toFloat()

                val orb3X = width * 0.5f + (sin(rad1 * 1.2) * 90f).toFloat()
                val orb3Y = height * 0.45f + (sin(rad2 * 0.5) * 60f).toFloat()

                val primaryColor = if (isDark) LiquidAqua.copy(alpha = 0.12f) else LiquidAqua.copy(alpha = 0.18f)
                val secondaryColor = if (isDark) LiquidTeal.copy(alpha = 0.10f) else LiquidEmerald.copy(alpha = 0.16f)
                val waterColor = if (isDark) LiquidWaterBlue.copy(alpha = 0.08f) else LiquidWaterBlue.copy(alpha = 0.14f)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor, Color.Transparent),
                        center = Offset(orb1X, orb1Y),
                        radius = width * 0.55f * wavePulse
                    ),
                    radius = width * 0.55f * wavePulse,
                    center = Offset(orb1X, orb1Y)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondaryColor, Color.Transparent),
                        center = Offset(orb2X, orb2Y),
                        radius = width * 0.65f * wavePulse
                    ),
                    radius = width * 0.65f * wavePulse,
                    center = Offset(orb2X, orb2Y)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(waterColor, Color.Transparent),
                        center = Offset(orb3X, orb3Y),
                        radius = width * 0.5f * wavePulse
                    ),
                    radius = width * 0.5f * wavePulse,
                    center = Offset(orb3X, orb3Y)
                )
            }
        }

        // Screen Content Layer
        content()
    }
}

/**
 * Reusable Liquid Glass Surface Container with translucent glass styling,
 * frosted border highlights, and optional liquid wave glow.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    isAmoled: Boolean = false,
    glowColor: Color = LiquidAqua,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme() || isAmoled

    val surfaceColor = when {
        isAmoled -> GlassSurfaceAmoled
        isDark -> GlassSurfaceDark
        else -> GlassSurfaceLight
    }

    val borderColor = when {
        isAmoled -> GlassBorderAmoled
        isDark -> GlassBorderDark
        else -> GlassBorderLight
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isAmoled) 0.dp else 10.dp,
                shape = shape,
                spotColor = glowColor.copy(alpha = 0.25f),
                ambientColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        surfaceColor.copy(alpha = if (isDark) 0.85f else 0.90f),
                        surfaceColor.copy(alpha = if (isDark) 0.65f else 0.75f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassHighlight.copy(alpha = if (isDark) 0.35f else 0.65f),
                        borderColor
                    )
                ),
                shape = shape
            )
    ) {
        // Specular highlight gleam at the top edge of the glass
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GlassHighlight.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

data class NavTabItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
    val testTag: String
)

/**
 * Floating Liquid Glass Navigation Bar with realistic liquid water droplet
 * physics that glide smoothly and stretch/contract dynamically from tab to tab.
 */
@Composable
fun FloatingLiquidGlassNavBar(
    currentScreen: AppScreen,
    items: List<NavTabItem>,
    onTabSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier,
    isAmoled: Boolean = false
) {
    val selectedIndex = items.indexOfFirst { it.screen == currentScreen }.coerceAtLeast(0)
    val isDark = isSystemInDarkTheme() || isAmoled

    // Infinite gentle water shimmer on active droplet
    val infiniteTransition = rememberInfiniteTransition(label = "WaterDropletShimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerPhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("floating_liquid_glass_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(32.dp),
            isAmoled = isAmoled,
            glowColor = LiquidAqua,
            modifier = Modifier.fillMaxWidth()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val totalWidth = maxWidth
                val tabCount = items.size.coerceAtLeast(1)
                val tabWidth = totalWidth / tabCount

                // Animated position for the dynamic liquid droplet indicator
                val targetOffsetX = tabWidth * selectedIndex
                val animatedOffsetX by animateDpAsState(
                    targetValue = targetOffsetX,
                    animationSpec = spring(
                        dampingRatio = 0.62f, // Gentle fluid bounce
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "DropletSpringX"
                )

                // Velocity / stretch deformation effect
                var lastTargetIndex by remember { mutableStateOf(selectedIndex) }
                val isMoving = lastTargetIndex != selectedIndex
                lastTargetIndex = selectedIndex

                val dropletWidthScale by animateFloatAsState(
                    targetValue = if (isMoving) 1.25f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.55f,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "DropletStretch"
                )

                // 1. Dynamic Liquid Water Droplet Indicator (Slides behind active tab)
                Box(
                    modifier = Modifier
                        .offset(x = animatedOffsetX)
                        .width(tabWidth)
                        .height(52.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Fluid Glow Aura
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scaleX = dropletWidthScale, scaleY = 1.0f)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        LiquidAqua.copy(alpha = if (isDark) 0.35f else 0.45f),
                                        LiquidTeal.copy(alpha = if (isDark) 0.20f else 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Inner Liquid Capsule Droplet
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scaleX = dropletWidthScale, scaleY = 1.0f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.38f else 0.22f),
                                        LiquidTeal.copy(alpha = if (isDark) 0.22f else 0.14f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        LiquidAqua.copy(alpha = 0.7f),
                                        LiquidTeal.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                    )

                    // Subtle Water Wave Reflection Line inside droplet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(2.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.75f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }

                // 2. Navigation Tab Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = 0.5f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "IconScale"
                        )

                        val activeColor = if (isDark) LiquidAqua else MaterialTheme.colorScheme.primary
                        val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)

                        Box(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(52.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // Custom water droplet acts as the indication
                                ) {
                                    onTabSelected(item.screen)
                                }
                                .testTag(item.testTag),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.scale(iconScale)
                            ) {
                                if (item.badgeCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = item.badgeCount.toString(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = if (isSelected) activeColor else inactiveColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) activeColor else inactiveColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
