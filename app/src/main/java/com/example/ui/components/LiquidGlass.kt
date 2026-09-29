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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.LiquidIndigo
import com.example.ui.theme.LiquidRose
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.LiquidWaterBlue
import com.example.ui.viewmodel.AppScreen
import kotlin.math.sin

/**
 * Ambient Liquid Fluid Background that renders smooth animated chromatic liquid orbs
 * creating realistic depth and refraction behind translucent glass panels
 * inspired by Apple Music's vibrant fluid mesh gradients on iOS.
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
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FluidRotation1"
    )

    val floatAnim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FluidRotation2"
    )

    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.18f,
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
                .background(
                    if (isAmoled) Color(0xFF000000)
                    else if (isDark) Color(0xFF070C16)
                    else Color(0xFFF1F5F9)
                )
        )

        // Animated Liquid Mesh Orbs (Apple Music / iOS Liquid Glow)
        if (!isAmoled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val rad1 = Math.toRadians(floatAnim1.toDouble())
                val rad2 = Math.toRadians(floatAnim2.toDouble())

                // 4 Smooth floating ambient liquid orbs
                val orb1X = width * 0.22f + (sin(rad1) * 90f).toFloat()
                val orb1Y = height * 0.20f + (sin(rad1 * 0.8) * 115f).toFloat()

                val orb2X = width * 0.80f + (sin(rad2) * 100f).toFloat()
                val orb2Y = height * 0.68f + (sin(rad2 * 0.7) * 130f).toFloat()

                val orb3X = width * 0.52f + (sin(rad1 * 1.3) * 110f).toFloat()
                val orb3Y = height * 0.42f + (sin(rad2 * 0.6) * 80f).toFloat()

                val orb4X = width * 0.18f + (sin(rad2 * 1.1) * 75f).toFloat()
                val orb4Y = height * 0.82f + (sin(rad1 * 0.9) * 100f).toFloat()

                val aqua = if (isDark) LiquidAqua.copy(alpha = 0.18f) else LiquidAqua.copy(alpha = 0.25f)
                val emerald = if (isDark) LiquidEmerald.copy(alpha = 0.16f) else LiquidEmerald.copy(alpha = 0.22f)
                val indigo = if (isDark) LiquidIndigo.copy(alpha = 0.14f) else LiquidIndigo.copy(alpha = 0.20f)
                val rose = if (isDark) LiquidRose.copy(alpha = 0.10f) else LiquidRose.copy(alpha = 0.15f)

                // Draw luminous ambient radial orbs
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(aqua, Color.Transparent),
                        center = Offset(orb1X, orb1Y),
                        radius = width * 0.68f * wavePulse
                    ),
                    radius = width * 0.68f * wavePulse,
                    center = Offset(orb1X, orb1Y)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(emerald, Color.Transparent),
                        center = Offset(orb2X, orb2Y),
                        radius = width * 0.72f * wavePulse
                    ),
                    radius = width * 0.72f * wavePulse,
                    center = Offset(orb2X, orb2Y)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(indigo, Color.Transparent),
                        center = Offset(orb3X, orb3Y),
                        radius = width * 0.62f * wavePulse
                    ),
                    radius = width * 0.62f * wavePulse,
                    center = Offset(orb3X, orb3Y)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(rose, Color.Transparent),
                        center = Offset(orb4X, orb4Y),
                        radius = width * 0.58f * wavePulse
                    ),
                    radius = width * 0.58f * wavePulse,
                    center = Offset(orb4X, orb4Y)
                )
            }
        }

        // Screen Content Layer
        content()
    }
}

/**
 * Reusable Liquid Glass Surface Container with Apple Music frosted acrylic styling,
 * 1.25dp specular border highlights, and ambient light refraction.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isAmoled: Boolean = false,
    glowColor: Color = LiquidAqua,
    borderAlpha: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme() || isAmoled

    val surfaceBrush = if (isAmoled) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF090E18).copy(alpha = 0.95f),
                Color(0xFF030509).copy(alpha = 0.90f)
            )
        )
    } else if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.06f),
                Color(0xFF0D1527).copy(alpha = 0.72f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.90f),
                Color.White.copy(alpha = 0.75f),
                Color(0xFFF0FDF4).copy(alpha = 0.55f)
            )
        )
    }

    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            if (isDark) Color.White.copy(alpha = 0.50f * borderAlpha) else Color.White.copy(alpha = 0.95f * borderAlpha),
            if (isDark) Color.White.copy(alpha = 0.12f * borderAlpha) else Color.White.copy(alpha = 0.40f * borderAlpha)
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isAmoled) 0.dp else 16.dp,
                shape = shape,
                spotColor = glowColor.copy(alpha = if (isDark) 0.28f else 0.18f),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.35f else 0.10f)
            )
            .clip(shape)
            .background(surfaceBrush)
            .border(
                width = 1.25.dp,
                brush = borderBrush,
                shape = shape
            )
    ) {
        // Specular highlight gleam at the top edge of the frosted glass
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.75f else 0.98f),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

/**
 * Clickable / non-clickable Liquid Glass Card for list items and section containers.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    isAmoled: Boolean = false,
    glowColor: Color = LiquidAqua,
    borderAlpha: Float = 1.0f,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else modifier

    val tagModifier = if (testTag.isNotBlank()) {
        clickableModifier.testTag(testTag)
    } else clickableModifier

    LiquidGlassSurface(
        shape = shape,
        isAmoled = isAmoled,
        glowColor = glowColor,
        borderAlpha = borderAlpha,
        modifier = tagModifier
    ) {
        content()
    }
}

/**
 * Animated Apple Music Style 4-Bar Equalizer / Waveform visualizer.
 */
@Composable
fun AppleMusicWaveVisualizer(
    color: Color = LiquidAqua,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AppleMusicWave")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Bar1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(620, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Bar2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(530, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Bar3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(590, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Bar4"
    )

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(h1, h2, h3, h4).forEach { heightFraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp * heightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
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
 * Floating Liquid Glass Navigation Bar with authentic Apple Music / Instagram frosted glass
 * floating pill dock, real-time backdrop blur refraction, 1.25dp specular prism bevel,
 * and dynamic liquid water droplet physics that glide smoothly and stretch/contract dynamically.
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

    val dockShape = RoundedCornerShape(36.dp)

    // Glass backdrop brush with high-end iOS acrylic frosted translucency
    val glassDockBrush = if (isAmoled) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A).copy(alpha = 0.92f),
                Color(0xFF050B14).copy(alpha = 0.96f)
            )
        )
    } else if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color(0xFF1E293B).copy(alpha = 0.78f),
                Color(0xFF0F172A).copy(alpha = 0.88f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.92f),
                Color.White.copy(alpha = 0.78f),
                Color(0xFFF1F5F9).copy(alpha = 0.65f)
            )
        )
    }

    // Specular border gradient for 3D beveled glass prism edge
    val glassBorderBrush = Brush.verticalGradient(
        colors = listOf(
            if (isDark) Color.White.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.98f),
            if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.45f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("floating_liquid_glass_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        // Outer Frosted Glass Pill Dock Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isAmoled) 0.dp else 22.dp,
                    shape = dockShape,
                    spotColor = LiquidAqua.copy(alpha = if (isDark) 0.35f else 0.22f),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.15f)
                )
                .clip(dockShape)
                .background(glassDockBrush)
                .border(
                    width = 1.25.dp,
                    brush = glassBorderBrush,
                    shape = dockShape
                )
        ) {
            // Top specular gloss highlight gleam
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (isDark) 0.85f else 0.98f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Dynamic Tab Content & Liquid Droplet Slider
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                val totalWidth = maxWidth
                val tabCount = items.size.coerceAtLeast(1)
                val tabWidth = totalWidth / tabCount

                // Animated position for dynamic liquid droplet indicator
                val targetOffsetX = tabWidth * selectedIndex
                val animatedOffsetX by animateDpAsState(
                    targetValue = targetOffsetX,
                    animationSpec = spring(
                        dampingRatio = 0.58f, // Fluid elastic spring
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "DropletSpringX"
                )

                // Velocity & stretch deformation physics
                var lastTargetIndex by remember { mutableStateOf(selectedIndex) }
                val isMoving = lastTargetIndex != selectedIndex
                lastTargetIndex = selectedIndex

                val dropletWidthScale by animateFloatAsState(
                    targetValue = if (isMoving) 1.22f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.52f,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "DropletStretch"
                )

                // 1. Dynamic Liquid Water Droplet Capsule (Slides behind active tab)
                Box(
                    modifier = Modifier
                        .offset(x = animatedOffsetX)
                        .width(tabWidth)
                        .height(54.dp)
                        .padding(horizontal = 3.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Fluid Glow Aura
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scaleX = dropletWidthScale, scaleY = 1.0f)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        LiquidAqua.copy(alpha = if (isDark) 0.42f else 0.50f),
                                        LiquidTeal.copy(alpha = if (isDark) 0.25f else 0.30f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Inner Liquid Capsule Droplet (Apple Music Neon Translucency)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scaleX = dropletWidthScale, scaleY = 1.0f)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.45f else 0.28f),
                                        LiquidTeal.copy(alpha = if (isDark) 0.28f else 0.18f)
                                    )
                                )
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        LiquidAqua.copy(alpha = 0.85f),
                                        LiquidTeal.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(26.dp)
                            )
                    )

                    // Top Bevel Glint inside droplet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(2.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = 3.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.88f),
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
                            targetValue = if (isSelected) 1.18f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = 0.5f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "IconScale"
                        )

                        val activeColor = if (isDark) LiquidAqua else MaterialTheme.colorScheme.primary
                        val inactiveColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.58f)

                        Box(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(54.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // Custom water droplet acts as the tactile feedback
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
                                            modifier = Modifier.size(23.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
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

