// ============================================================================
// FILE: app/src/main/java/com/example/MainActivity.kt
// ============================================================================
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.components.HeroHeader
import com.example.components.RedWater3DBackground
import com.example.components.TopNavigationBar
import com.example.screens.DragonSplashScreen
import com.example.screens.LogsScreen
import com.example.screens.MarketsScreen
import com.example.screens.SettingsScreen
import com.example.screens.TerminalScreen
import com.example.ui.theme.BgObsidian
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TradingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RedDragonApp()
            }
        }
    }
}

@Composable
fun RedDragonApp(
    viewModel: TradingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showSplash) {
        DragonSplashScreen(
            onDismiss = { viewModel.dismissSplash() }
        )
    } else {
        RedWater3DBackground(
            shakeTrigger = uiState.shakeTrigger
        ) {
            Scaffold(
                containerColor = BgObsidian.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 520.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Hero Header
                        HeroHeader(
                            isSoundEnabled = uiState.isSoundEnabled,
                            onToggleSound = { viewModel.toggleSound() },
                            onReplayDragon = { viewModel.replaySplash() }
                        )

                        // 2. Step Navigation Bar
                        TopNavigationBar(
                            currentStep = uiState.currentStep,
                            onStepSelected = { viewModel.setStep(it) },
                            onReset = { viewModel.resetAll() }
                        )

                        // 3. Screen Views (1: Markets, 2: Settings, 3: Terminal, 4: Logs)
                        AnimatedContent(
                            targetState = uiState.currentStep,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition",
                            modifier = Modifier.weight(1f)
                        ) { step ->
                            when (step) {
                                1 -> MarketsScreen(
                                    assets = uiState.assets,
                                    selectedCategory = uiState.selectedCategory,
                                    searchQuery = uiState.searchQuery,
                                    onCategorySelected = { viewModel.setCategory(it) },
                                    onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                                    onAssetSelected = { viewModel.selectAsset(it) }
                                )

                                2 -> SettingsScreen(
                                    asset = uiState.selectedAsset,
                                    accountType = uiState.accountType,
                                    executionMode = uiState.executionMode,
                                    timeframe = uiState.timeframe,
                                    onAccountTypeChange = { viewModel.setAccountType(it) },
                                    onExecutionModeChange = { viewModel.setExecutionMode(it) },
                                    onTimeframeChange = { viewModel.setTimeframe(it) },
                                    onLaunchTerminal = { viewModel.setStep(3) }
                                )

                                3 -> TerminalScreen(
                                    asset = uiState.selectedAsset,
                                    candleRemainingSeconds = uiState.candleRemainingSeconds,
                                    candles = uiState.candles,
                                    signal = uiState.currentSignal,
                                    isCalculating = uiState.isCalculatingSignal,
                                    activeTrade = uiState.activeTrade,
                                    lastTradeResult = uiState.lastTradeResult,
                                    onFireSignal = { viewModel.fireTradeOrSignal() },
                                    onNavigateToSettings = { viewModel.setStep(2) },
                                    onNavigateToLogs = { viewModel.setStep(4) },
                                    onCloseTradeResult = { viewModel.closeTradeResultModal() }
                                )

                                4 -> LogsScreen(
                                    tradeLogs = uiState.tradeLogs,
                                    onClearLogs = { viewModel.clearHistory() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ============================================================================
// FILE: app/src/main/java/com/example/screens/DragonSplashScreen.kt
// ============================================================================
package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundSynthesizer
import com.example.components.LiquidGlassButton
import com.example.components.RedWater3DBackground
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonDeepCrimson
import com.example.ui.theme.DragonRed
import com.example.ui.theme.DragonRedGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DragonSplashScreen(
    onDismiss: () -> Unit
) {
    var stage by remember { mutableStateOf(1) }
    var autoProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        SoundSynthesizer.playDragonRoar()
        delay(600L)
        stage = 2
        delay(600L)
        stage = 3
    }

    LaunchedEffect(stage) {
        if (stage == 3) {
            val total = 2800L
            val step = 50L
            var current = 0L
            while (current < total) {
                delay(step)
                current += step
                autoProgress = current.toFloat() / total
            }
            onDismiss()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_glow")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val auraRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rot"
    )

    val energyGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "energy_glow"
    )

    val flameWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flame_wave"
    )

    RedWater3DBackground(
        shakeTrigger = if (stage == 2) 1L else 0L
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Chinese Characters: 红 龙 (Red Dragon)
                AnimatedVisibility(
                    visible = stage >= 1,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(DragonCrimson, DragonDeepCrimson)
                                    )
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = AccentGold.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "赤龙量化",
                                color = AccentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "红 龙",
                            color = DragonRedGlow,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 8.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .scale(pulseScale)
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            listOf(
                                                DragonRed.copy(alpha = 0.45f * energyGlow),
                                                Color.Transparent
                                            )
                                        ),
                                        radius = size.width * 0.8f
                                    )
                                }
                        )

                        Text(
                            text = "东方赤龙 • 极速量化终端",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                    }
                }

                // 2. Powerful Red 3D Dragon Animation & Energy Vortex
                AnimatedVisibility(
                    visible = stage >= 2,
                    enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 60 }
                ) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(rotationZ = auraRotate)
                        ) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.width * 0.48f

                            drawCircle(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        DragonRed.copy(alpha = 0.85f * energyGlow),
                                        AccentGold.copy(alpha = 0.6f),
                                        DragonCrimson.copy(alpha = 0.3f),
                                        DragonRed.copy(alpha = 0.85f * energyGlow)
                                    )
                                ),
                                radius = radius,
                                center = center,
                                style = Stroke(width = 3.5.dp.toPx())
                            )

                            for (i in 0 until 12) {
                                val angle = (i * (360f / 12f)) * (PI / 180f)
                                val px = center.x + cos(angle).toFloat() * (radius - 12f)
                                val py = center.y + sin(angle).toFloat() * (radius - 12f)
                                drawCircle(
                                    color = if (i % 2 == 0) DragonRedGlow else AccentGold,
                                    radius = 4.5f,
                                    center = Offset(px, py)
                                )
                            }
                        }

                        // 3D Dragon Core Emblem Canvas
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = CircleShape,
                                    ambientColor = DragonRed,
                                    spotColor = DragonRedGlow
                                )
                                .clip(CircleShape)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            AccentGold,
                                            DragonRedGlow,
                                            DragonCrimson
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawProcedural3DDragon(
                                    energyGlow = energyGlow,
                                    flameWave = flameWave
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = stage >= 2,
                    enter = fadeIn(tween(600))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "RED DRAGON",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )

                        Text(
                            text = "ULTRA LIQUID QUANT TERMINAL",
                            color = DragonRedGlow,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = stage >= 3,
                    enter = fadeIn(tween(400))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LiquidGlassButton(
                            text = "LAUNCH QUANT MATRIX →",
                            onClick = onDismiss,
                            icon = Icons.Default.Bolt,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .testTag("launch_matrix_btn")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0x33FFFFFF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(autoProgress)
                                    .height(4.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(DragonCrimson, DragonRedGlow, AccentGold)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawProcedural3DDragon(
    energyGlow: Float,
    flameWave: Float
) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                DragonCrimson.copy(alpha = 0.95f),
                DragonDeepCrimson,
                Color(0xFF070204)
            ),
            center = center,
            radius = w * 0.7f
        )
    )

    for (r in 1..4) {
        val rad = (w * 0.15f) * r
        val alpha = (0.25f / r) * energyGlow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    DragonRedGlow.copy(alpha = alpha),
                    Color.Transparent
                ),
                center = center,
                radius = rad
            )
        )
    }

    val leftHorn = Path().apply {
        moveTo(center.x - 30f, center.y - 20f)
        cubicTo(center.x - 65f, center.y - 65f, center.x - 55f, center.y - 85f, center.x - 70f, center.y - 95f)
        cubicTo(center.x - 50f, center.y - 75f, center.x - 38f, center.y - 45f, center.x - 20f, center.y - 25f)
        close()
    }

    val rightHorn = Path().apply {
        moveTo(center.x + 30f, center.y - 20f)
        cubicTo(center.x + 65f, center.y - 65f, center.x + 55f, center.y - 85f, center.x + 70f, center.y - 95f)
        cubicTo(center.x + 50f, center.y - 75f, center.x + 38f, center.y - 45f, center.x + 20f, center.y - 25f)
        close()
    }

    drawPath(path = leftHorn, brush = Brush.linearGradient(listOf(AccentGold, DragonRedGlow, DragonCrimson)))
    drawPath(path = rightHorn, brush = Brush.linearGradient(listOf(AccentGold, DragonRedGlow, DragonCrimson)))

    val headPath = Path().apply {
        moveTo(center.x, center.y - 45f)
        cubicTo(center.x + 35f, center.y - 35f, center.x + 45f, center.y, center.x + 25f, center.y + 40f)
        cubicTo(center.x + 15f, center.y + 65f, center.x, center.y + 75f, center.x, center.y + 75f)
        cubicTo(center.x, center.y + 75f, center.x - 15f, center.y + 65f, center.x - 25f, center.y + 40f)
        cubicTo(center.x - 45f, center.y, center.x - 35f, center.y - 35f, center.x, center.y - 45f)
        close()
    }

    drawPath(
        path = headPath,
        brush = Brush.linearGradient(
            colors = listOf(DragonRedGlow, DragonRed, DragonCrimson, DragonDeepCrimson),
            start = Offset(center.x, center.y - 50f),
            end = Offset(center.x, center.y + 80f)
        )
    )

    drawPath(
        path = headPath,
        color = AccentGold.copy(alpha = 0.75f),
        style = Stroke(width = 2.2f)
    )

    for (side in listOf(-1, 1)) {
        val whisker = Path().apply {
            val startX = center.x + (side * 22f)
            val startY = center.y + 25f
            moveTo(startX, startY)
            cubicTo(
                startX + (side * 40f) + sin(flameWave) * 8f,
                startY + 15f + cos(flameWave) * 6f,
                startX + (side * 65f) + cos(flameWave) * 10f,
                startY + 45f,
                startX + (side * 85f),
                startY + 30f + sin(flameWave) * 12f
            )
        }
        drawPath(
            path = whisker,
            brush = Brush.linearGradient(listOf(AccentGold, DragonRedGlow, Color.Transparent)),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
    }

    val leftEye = Offset(center.x - 18f, center.y - 2f)
    val rightEye = Offset(center.x + 18f, center.y - 2f)

    for (eye in listOf(leftEye, rightEye)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AccentGold.copy(alpha = 0.9f * energyGlow), Color.Transparent),
                center = eye,
                radius = 16f
            )
        )
        drawOval(
            color = AccentAmber,
            topLeft = Offset(eye.x - 5f, eye.y - 7f),
            size = Size(10f, 14f)
        )
        drawOval(
            color = Color(0xFF0F0408),
            topLeft = Offset(eye.x - 1.8f, eye.y - 6f),
            size = Size(3.6f, 12f)
        )
        drawCircle(
            color = Color.White,
            radius = 1.8f,
            center = Offset(eye.x - 1.5f, eye.y - 2.5f)
        )
    }

    val pearl = Path().apply {
        moveTo(center.x, center.y - 28f)
        lineTo(center.x + 8f, center.y - 18f)
        lineTo(center.x, center.y - 8f)
        lineTo(center.x - 8f, center.y - 18f)
        close()
    }
    drawPath(
        path = pearl,
        brush = Brush.linearGradient(listOf(Color.White, AccentGold, DragonRedGlow))
    )
}


// ============================================================================
// FILE: app/src/main/java/com/example/screens/TerminalScreen.kt
// ============================================================================
package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.LiquidGlass3DIcon
import com.example.components.LiquidGlassButton
import com.example.components.LiquidGlassCard
import com.example.model.ActiveTrade
import com.example.model.Candle
import com.example.model.MarketAsset
import com.example.model.SignalDirection
import com.example.model.TradeLog
import com.example.model.TradeSignal
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentSky
import com.example.ui.theme.BgObsidian
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonDeepCrimson
import com.example.ui.theme.DragonRed
import com.example.ui.theme.DragonRedGlow
import com.example.ui.theme.SignalCall
import com.example.ui.theme.SignalCallDark
import com.example.ui.theme.SignalCallGlow
import com.example.ui.theme.SignalPut
import com.example.ui.theme.SignalPutDark
import com.example.ui.theme.SignalPutGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.min

@Composable
fun TerminalScreen(
    asset: MarketAsset,
    candleRemainingSeconds: Int,
    candles: List<Candle>,
    signal: TradeSignal?,
    isCalculating: Boolean,
    activeTrade: ActiveTrade?,
    lastTradeResult: TradeLog?,
    onFireSignal: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onCloseTradeResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "terminal_glow")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ULTRA MATH EXECUTION TERMINAL",
                color = DragonRedGlow,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "${asset.symbol} • ${signal?.timeframe?.label ?: "S5"} • 5-SEC SCALP",
                color = TextSecondary,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 1. DEDICATED CONTINUOUS CANDLE CLOCK SYNC BAR
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            backgroundColor = DragonDeepCrimson.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(if (candleRemainingSeconds <= 2) AccentGold else DragonRedGlow)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Candle Clock Sync (Sniper Window):",
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (candleRemainingSeconds <= 2) "🔥 SNIPER ENTRY READY" else "SYNCHRONIZED WITH FEED",
                            color = if (candleRemainingSeconds <= 2) AccentGold else AccentCyan,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                val mins = candleRemainingSeconds / 60
                val secs = candleRemainingSeconds % 60
                Text(
                    text = String.format("%02d:%02d", mins, secs),
                    color = AccentGold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.testTag("candle_clock_val")
                )
            }
        }

        // 2. High-Frequency Candlestick Chart Canvas
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(12.dp),
            backgroundColor = BgObsidian.copy(alpha = 0.9f)
        ) {
            CandlestickChartCanvas(
                candles = candles,
                currentPrice = asset.price,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Telemetry Chips
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TelemetryChip(
                    label = "DELTA ORDER FLOW",
                    value = if (signal?.direction == SignalDirection.CALL) "+68.4% Buy Pressure" else "-74.2% Sell Pressure",
                    labelColor = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
                TelemetryChip(
                    label = "TICK VOLATILITY",
                    value = signal?.tickVolatility ?: "Ultra High (3.4x)",
                    labelColor = AccentSky,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TelemetryChip(
                    label = "VOL SPIKE FILTER",
                    value = signal?.volumeSpike ?: "Active (Confluence)",
                    labelColor = AccentGold,
                    modifier = Modifier.weight(1f)
                )
                TelemetryChip(
                    label = "MULTI-LAYER TREND",
                    value = signal?.multiTrend ?: "Bullish Dominance",
                    labelColor = SignalCallGlow,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TelemetryChip(
                    label = "LIQUIDITY VECTOR",
                    value = signal?.liquidityVector ?: "+3.42σ Flow Vector",
                    labelColor = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
                TelemetryChip(
                    label = "MATH MOMENTUM",
                    value = signal?.mathMomentum ?: "Fast Reversal 94.8%",
                    labelColor = DragonRedGlow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Quant Signal Card
        val isCall = signal?.direction == SignalDirection.CALL
        val signalCardBg = if (isCall) SignalCallDark.copy(alpha = 0.45f) else SignalPutDark.copy(alpha = 0.45f)
        val signalBorder = if (isCall) SignalCall else SignalPut
        val signalGlow = if (isCall) SignalCallGlow else SignalPutGlow

        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            backgroundColor = signalCardBg,
            borderColor = signalBorder,
            glowColor = signalGlow,
            elevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "QUANT ENGINE STATUS: ARMED",
                    color = TextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlass3DIcon(
                        icon = if (isCall) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        size = 32.dp,
                        iconSize = 18.dp,
                        badgeColor = if (isCall) SignalCall else SignalPut
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCall) "▲ CALL (HIGHER)" else "▼ PUT (LOWER)",
                        color = if (isCall) SignalCallGlow else SignalPutGlow,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22000000))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Strike", color = TextMuted, fontSize = 7.5.sp)
                        Text(
                            text = String.format(if (asset.price > 100) "%.2f" else "%.5f", signal?.strikePrice ?: asset.price),
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Win Probability", color = TextMuted, fontSize = 7.5.sp)
                        Text(
                            text = "${signal?.confidence ?: 92.4}%",
                            color = AccentGold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Expiry Window", color = TextMuted, fontSize = 7.5.sp)
                        Text(
                            text = "${signal?.timeframe?.label ?: "S5"} EXPIRY",
                            color = AccentCyan,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33000000))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val reasons = signal?.reasons ?: listOf(
                        "• RSI Hidden Divergence (34.2 Oversold Rebound)",
                        "• Stoch Fast %K crossed above %D line at 22.4",
                        "• +78.4% Aggressive Buying Liquidity Absorption",
                        "• Lower Band Mean Reversion snap-back confirmed"
                    )
                    reasons.forEach { r ->
                        Text(
                            text = r,
                            color = TextSecondary,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 5. PLAY / TRADE BUTTON
        LiquidGlassButton(
            text = if (isCalculating) "CALCULATING QUANT VECTORS..." else "⚡ FIRE ULTRA MATH SIGNAL (TRADE)",
            onClick = onFireSignal,
            icon = Icons.Default.Bolt,
            enabled = !isCalculating && activeTrade == null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fire_signal_btn")
        )

        // 6. Navigation Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp)),
                backgroundColor = Color(0x10FFFFFF)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidGlass3DIcon(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        size = 24.dp,
                        iconSize = 14.dp,
                        badgeColor = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚙ Settings",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("term_to_settings_btn")
                    )
                }
            }

            LiquidGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp)),
                backgroundColor = Color(0x10FFFFFF)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidGlass3DIcon(
                        icon = Icons.Default.History,
                        contentDescription = "Signal Log",
                        size = 24.dp,
                        iconSize = 14.dp,
                        badgeColor = AccentGold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📋 Signal Log",
                        color = AccentGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("term_to_logs_btn")
                    )
                }
            }
        }

        // 7. 1-Hour Signal Summary Dashboard
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1-HOUR SIGNAL SUMMARY",
                        color = DragonRedGlow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "83% DOMINANCE",
                        color = SignalCallGlow,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33000000))
                ) {
                    FrequencyChartCanvas(modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(text = "Strong Signals: 24", color = SignalCallGlow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Neutral: 5", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Alpha Ratio: 4.8x", color = AccentGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    AnimatedVisibility(
        visible = activeTrade != null,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        if (activeTrade != null) {
            ActiveTradeModal(trade = activeTrade)
        }
    }

    AnimatedVisibility(
        visible = lastTradeResult != null && activeTrade == null,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200))
    ) {
        if (lastTradeResult != null) {
            TradeResultModal(
                result = lastTradeResult,
                onDismiss = onCloseTradeResult
            )
        }
    }
}

@Composable
fun TelemetryChip(
    label: String,
    value: String,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0x18FFFFFF)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = label,
                color = labelColor,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CandlestickChartCanvas(
    candles: List<Candle>,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val gridLines = 4
        for (i in 1..gridLines) {
            val y = (h / (gridLines + 1)) * i
            drawLine(
                color = Color(0x10FFFFFF),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 0.8f
            )
        }

        if (candles.isEmpty()) return@Canvas

        var minPrice = candles.minOfOrNull { it.low } ?: currentPrice
        var maxPrice = candles.maxOfOrNull { it.high } ?: currentPrice
        if (maxPrice == minPrice) {
            maxPrice += 0.001
            minPrice -= 0.001
        }
        val priceRange = maxPrice - minPrice

        val step = w / candles.size
        val candleWidth = (step * 0.65f).coerceAtLeast(3f)

        candles.forEachIndexed { index, c ->
            val cx = index * step + step / 2f
            val isBull = c.close >= c.open
            val color = if (isBull) Color(0xFF10B981) else Color(0xFFF43F5E)

            val yHigh = h - ((c.high - minPrice) / priceRange).toFloat() * (h - 24f) - 12f
            val yLow = h - ((c.low - minPrice) / priceRange).toFloat() * (h - 24f) - 12f
            val yOpen = h - ((c.open - minPrice) / priceRange).toFloat() * (h - 24f) - 12f
            val yClose = h - ((c.close - minPrice) / priceRange).toFloat() * (h - 24f) - 12f

            drawLine(
                color = color,
                start = Offset(cx, yHigh),
                end = Offset(cx, yLow),
                strokeWidth = 1.2f
            )

            val top = min(yOpen, yClose)
            val bodyHeight = max(2.5f, kotlin.math.abs(yClose - yOpen))
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - candleWidth / 2f, top),
                size = Size(candleWidth, bodyHeight),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
        }

        val currentY = h - ((currentPrice - minPrice) / priceRange).toFloat() * (h - 24f) - 12f
        drawLine(
            color = AccentGold.copy(alpha = 0.8f),
            start = Offset(0f, currentY),
            end = Offset(w, currentY),
            strokeWidth = 1.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
        )
    }
}

@Composable
fun FrequencyChartCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val strong = listOf(4, 6, 3, 5, 4, 6)
        val neutral = listOf(1, 2, 1, 0, 1, 1)
        val colWidth = w / strong.size

        strong.forEachIndexed { i, sVal ->
            val cx = i * colWidth + colWidth / 2f
            val sH = (sVal / 7f) * (h - 18f)
            val nH = (neutral[i] / 7f) * (h - 18f)

            drawRoundRect(
                color = Color(0xFF10B981),
                topLeft = Offset(cx - 7f, h - 10f - sH),
                size = Size(6f, sH),
                cornerRadius = CornerRadius(2f, 2f)
            )

            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(cx + 2f, h - 10f - nH),
                size = Size(6f, nH),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

@Composable
fun ActiveTradeModal(trade: ActiveTrade) {
    val progress = (trade.remainingMillis.toFloat() / 5000f).coerceIn(0f, 1f)
    val secondsLeft = (trade.remainingMillis / 1000f)

    val isCall = trade.direction == SignalDirection.CALL
    val isCurrentlyWinning = if (isCall) trade.currentPrice >= trade.strikePrice else trade.currentPrice <= trade.strikePrice

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC05070C)),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = DragonDeepCrimson.copy(alpha = 0.85f),
            borderColor = if (isCurrentlyWinning) SignalCallGlow else SignalPutGlow,
            glowColor = if (isCurrentlyWinning) SignalCallGlow else SignalPutGlow,
            elevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACTIVE QUANT TRADE IN PROGRESS",
                    color = AccentGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isCurrentlyWinning) SignalCallGlow else SignalPutGlow,
                        strokeWidth = 8.dp,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1fs", secondsLeft),
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isCurrentlyWinning) "IN THE MONEY" else "OUT OF MONEY",
                            color = if (isCurrentlyWinning) SignalCallGlow else SignalPutGlow,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${trade.asset.symbol} • ${if (isCall) "▲ CALL" else "▼ PUT"}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = "Strike: " + String.format(if (trade.strikePrice > 100) "%.2f" else "%.5f", trade.strikePrice),
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Live: " + String.format(if (trade.currentPrice > 100) "%.2f" else "%.5f", trade.currentPrice),
                        color = if (isCurrentlyWinning) SignalCallGlow else SignalPutGlow,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TradeResultModal(
    result: TradeLog,
    onDismiss: () -> Unit
) {
    val isWin = result.isWin

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC05070C)),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = if (isWin) SignalCallDark.copy(alpha = 0.85f) else SignalPutDark.copy(alpha = 0.85f),
            borderColor = if (isWin) SignalCallGlow else SignalPutGlow,
            glowColor = if (isWin) SignalCallGlow else SignalPutGlow,
            elevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LiquidGlass3DIcon(
                    icon = if (isWin) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    size = 54.dp,
                    iconSize = 32.dp,
                    badgeColor = if (isWin) SignalCall else SignalPut
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isWin) "TRADE RESULT: WIN!" else "TRADE RESULT: LOSS",
                    color = if (isWin) SignalCallGlow else SignalPutGlow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${result.assetSymbol} • ${result.direction.name} • ${result.confidence}% Confidence",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isWin) {
                    Text(
                        text = "+$${String.format("%.2f", result.payoutAmount)} PAYOUT",
                        color = AccentGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LiquidGlassButton(
                    text = "CONTINUE EXECUTION",
                    onClick = onDismiss,
                    icon = Icons.Default.Bolt,
                    gradientColors = if (isWin) listOf(SignalCallDark, SignalCall) else listOf(DragonCrimson, DragonRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_result_btn")
                )
            }
        }
    }
}


// ============================================================================
// FILE: app/src/main/java/com/example/screens/MarketsScreen.kt
// ============================================================================
package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.LiquidGlass3DIcon
import com.example.components.LiquidGlassCard
import com.example.model.MarketAsset
import com.example.model.MarketCategory
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BgGlassBorder
import com.example.ui.theme.DragonRed
import com.example.ui.theme.DragonRedGlow
import com.example.ui.theme.SignalCallGlow
import com.example.ui.theme.SignalPutGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MarketsScreen(
    assets: List<MarketAsset>,
    selectedCategory: MarketCategory,
    searchQuery: String,
    onCategorySelected: (MarketCategory) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAssetSelected: (MarketAsset) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredAssets = assets.filter { asset ->
        val matchCat = selectedCategory == MarketCategory.ALL || asset.category == selectedCategory
        val matchQuery = asset.symbol.contains(searchQuery, ignoreCase = true) ||
                asset.name.contains(searchQuery, ignoreCase = true)
        matchCat && matchQuery
    }

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, BgGlassBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        singleLine = true,
                        cursorBrush = SolidColor(DragonRedGlow),
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("market_search_input"),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search Pairs (e.g. EUR, BTC, GOLD)...",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                MarketCategory.entries.forEach { cat ->
                    val isActive = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isActive) DragonRed.copy(alpha = 0.32f) else Color(0x10FFFFFF)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) DragonRed else BgGlassBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onCategorySelected(cat) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("cat_${cat.name}")
                    ) {
                        Text(
                            text = cat.label,
                            color = if (isActive) Color.White else TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredAssets, key = { it.id }) { asset ->
                    MarketRowItem(
                        asset = asset,
                        onClick = { onAssetSelected(asset) }
                    )
                }
            }
        }
    }
}

@Composable
fun MarketRowItem(
    asset: MarketAsset,
    onClick: () -> Unit
) {
    val isPositive = asset.change24h >= 0

    val iconVector = when (asset.category) {
        MarketCategory.CURRENCY -> Icons.Default.MonetizationOn
        MarketCategory.CRYPTO -> Icons.Default.CurrencyBitcoin
        MarketCategory.COMMODITY -> Icons.Default.AttachMoney
        MarketCategory.STOCK -> Icons.Default.Storefront
        MarketCategory.ALL -> Icons.Default.ShowChart
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x12FFFFFF))
            .border(1.dp, BgGlassBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("market_item_${asset.symbol}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiquidGlass3DIcon(
                    icon = iconVector,
                    contentDescription = asset.name,
                    size = 32.dp,
                    iconSize = 16.dp,
                    badgeColor = if (isPositive) DragonRed else Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = asset.symbol,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${asset.name} • ${if (isPositive) "+" else ""}${asset.change24h}%",
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${asset.payout}% Payout",
                    color = AccentGold,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = String.format(if (asset.price > 100) "%.2f" else "%.5f", asset.price),
                    color = if (isPositive) SignalCallGlow else SignalPutGlow,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ============================================================================
// FILE: app/src/main/java/com/example/screens/SettingsScreen.kt
// ============================================================================
package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.LiquidGlass3DIcon
import com.example.components.LiquidGlassButton
import com.example.components.LiquidGlassCard
import com.example.model.AccountType
import com.example.model.ExecutionMode
import com.example.model.MarketAsset
import com.example.model.Timeframe
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BgGlassBorder
import com.example.ui.theme.DragonRed
import com.example.ui.theme.DragonRedGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    asset: MarketAsset,
    accountType: AccountType,
    executionMode: ExecutionMode,
    timeframe: Timeframe,
    onAccountTypeChange: (AccountType) -> Unit,
    onExecutionModeChange: (ExecutionMode) -> Unit,
    onTimeframeChange: (Timeframe) -> Unit,
    onLaunchTerminal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "SELECTED ASSET",
                    color = DragonRedGlow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x18000000))
                        .border(1.dp, BgGlassBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlass3DIcon(
                                icon = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                size = 34.dp,
                                iconSize = 18.dp,
                                badgeColor = DragonRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = asset.symbol,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = asset.name,
                                    color = TextMuted,
                                    fontSize = 8.5.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${asset.payout}% Payout",
                                color = AccentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = String.format(if (asset.price > 100) "%.2f" else "%.5f", asset.price),
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "ACCOUNT CONFIGURATION",
                    color = DragonRedGlow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AccountType.entries.forEach { acc ->
                        val isSelected = accountType == acc
                        OptionTile(
                            text = acc.label,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            testTag = "acc_opt_${acc.name}",
                            onClick = { onAccountTypeChange(acc) }
                        )
                    }
                }
            }
        }

        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "EXECUTION MODE",
                    color = DragonRedGlow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OptionTile(
                            text = "5-SEC SCALP",
                            isSelected = executionMode == ExecutionMode.SCALP_5S,
                            modifier = Modifier.weight(1f),
                            testTag = "mode_scalp_5s",
                            onClick = { onExecutionModeChange(ExecutionMode.SCALP_5S) }
                        )
                        OptionTile(
                            text = "30-SEC SPRINT",
                            isSelected = executionMode == ExecutionMode.SPRINT_30S,
                            modifier = Modifier.weight(1f),
                            testTag = "mode_sprint_30s",
                            onClick = { onExecutionModeChange(ExecutionMode.SPRINT_30S) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OptionTile(
                            text = "1-MIN CLASSIC",
                            isSelected = executionMode == ExecutionMode.CLASSIC_1M,
                            modifier = Modifier.weight(1f),
                            testTag = "mode_classic_1m",
                            onClick = { onExecutionModeChange(ExecutionMode.CLASSIC_1M) }
                        )
                        OptionTile(
                            text = "5-MIN SWING",
                            isSelected = executionMode == ExecutionMode.SWING_5M,
                            modifier = Modifier.weight(1f),
                            testTag = "mode_swing_5m",
                            onClick = { onExecutionModeChange(ExecutionMode.SWING_5M) }
                        )
                    }
                }
            }
        }

        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "POCKET OPTION TIMEFRAME",
                    color = DragonRedGlow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Timeframe.entries.forEach { tf ->
                        val isSelected = timeframe == tf
                        OptionTile(
                            text = tf.label,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            testTag = "tf_${tf.name}",
                            onClick = { onTimeframeChange(tf) }
                        )
                    }
                }
            }
        }

        LiquidGlassButton(
            text = "LAUNCH QUANT TERMINAL →",
            onClick = onLaunchTerminal,
            icon = Icons.Default.Bolt,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("launch_term_btn")
        )
    }
}

@Composable
fun OptionTile(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) DragonRed.copy(alpha = 0.32f) else Color(0x10FFFFFF)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) DragonRed else BgGlassBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
        )
    }
}


// ============================================================================
// FILE: app/src/main/java/com/example/screens/LogsScreen.kt
// ============================================================================
package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.LiquidGlass3DIcon
import com.example.components.LiquidGlassBadge
import com.example.components.LiquidGlassButton
import com.example.components.LiquidGlassCard
import com.example.model.SignalDirection
import com.example.model.TradeLog
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BgGlassBorder
import com.example.ui.theme.DragonRedGlow
import com.example.ui.theme.SignalCall
import com.example.ui.theme.SignalCallGlow
import com.example.ui.theme.SignalPut
import com.example.ui.theme.SignalPutGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LogsScreen(
    tradeLogs: List<TradeLog>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = tradeLogs.size
    val wins = tradeLogs.count { it.isWin }
    val winRate = if (total > 0) (wins.toDouble() / total * 100) else 0.0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "PERFORMANCE METRICS",
                    color = DragonRedGlow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    MetricBox(
                        title = "TOTAL SIGNALS",
                        value = "$total",
                        valueColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "TOTAL WINS",
                        value = "$wins",
                        valueColor = SignalCallGlow,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "WIN RATE %",
                        value = String.format("%.1f%%", winRate),
                        valueColor = AccentGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUANT SIGNAL AUDIT LOG",
                        color = DragonRedGlow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    if (tradeLogs.isNotEmpty()) {
                        LiquidGlassBadge(
                            text = "${tradeLogs.size} EXECUTIONS",
                            color = AccentGold,
                            bgColor = AccentGold.copy(alpha = 0.15f),
                            borderColor = AccentGold.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (tradeLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LiquidGlass3DIcon(
                                icon = Icons.Default.History,
                                contentDescription = null,
                                size = 42.dp,
                                iconSize = 22.dp,
                                badgeColor = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No quant signals executed yet.",
                                color = TextMuted,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(tradeLogs, key = { it.id }) { log ->
                            TradeLogRowItem(log = log)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LiquidGlassButton(
                        text = "CLEAR AUDIT HISTORY",
                        onClick = onClearLogs,
                        icon = Icons.Default.DeleteSweep,
                        gradientColors = listOf(Color(0xFF334155), Color(0xFF1E293B)),
                        glowColor = Color(0xFF475569),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_logs_btn")
                    )
                }
            }
        }
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x15FFFFFF))
            .border(1.dp, BgGlassBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = valueColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun TradeLogRowItem(log: TradeLog) {
    val isCall = log.direction == SignalDirection.CALL

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x12FFFFFF))
            .border(1.dp, BgGlassBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCall) "CALL" else "PUT",
                    color = if (isCall) SignalCallGlow else SignalPutGlow,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = log.assetSymbol,
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = log.timeFormatted,
                    color = TextMuted,
                    fontSize = 7.5.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${log.confidence}%",
                    color = AccentGold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                LiquidGlassBadge(
                    text = if (log.isWin) "WIN" else "LOSS",
                    color = if (log.isWin) SignalCallGlow else SignalPutGlow,
                    bgColor = if (log.isWin) SignalCall.copy(alpha = 0.2f) else SignalPut.copy(alpha = 0.2f),
                    borderColor = if (log.isWin) SignalCall else SignalPut
                )
            }
        }
    }
}


// ============================================================================
// FILE: app/src/main/java/com/example/viewmodel/TradingViewModel.kt
// ============================================================================
package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundSynthesizer
import com.example.model.AccountType
import com.example.model.ActiveTrade
import com.example.model.Candle
import com.example.model.ExecutionMode
import com.example.model.MarketAsset
import com.example.model.MarketCategory
import com.example.model.SignalDirection
import com.example.model.Timeframe
import com.example.model.TradeLog
import com.example.model.TradeSignal
import com.example.model.TradeStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class TradingUiState(
    val showSplash: Boolean = true,
    val currentStep: Int = 1,
    val selectedCategory: MarketCategory = MarketCategory.ALL,
    val searchQuery: String = "",
    val assets: List<MarketAsset> = defaultAssets,
    val selectedAsset: MarketAsset = defaultAssets[0],
    val accountType: AccountType = AccountType.DEMO,
    val executionMode: ExecutionMode = ExecutionMode.SCALP_5S,
    val timeframe: Timeframe = Timeframe.S5,
    val candleRemainingSeconds: Int = 5,
    val candles: List<Candle> = emptyList(),
    val currentSignal: TradeSignal? = null,
    val isCalculatingSignal: Boolean = false,
    val activeTrade: ActiveTrade? = null,
    val tradeLogs: List<TradeLog> = emptyList(),
    val isSoundEnabled: Boolean = true,
    val shakeTrigger: Long = 0L,
    val lastTradeResult: TradeLog? = null
)

val defaultAssets = listOf(
    MarketAsset("1", "EUR/USD OTC", "Euro / US Dollar OTC", MarketCategory.CURRENCY, 92, 1.08452, 0.34),
    MarketAsset("2", "GBP/USD OTC", "Great Britain Pound OTC", MarketCategory.CURRENCY, 90, 1.26384, 0.18),
    MarketAsset("3", "USD/JPY OTC", "US Dollar / Yen OTC", MarketCategory.CURRENCY, 89, 154.210, -0.22),
    MarketAsset("4", "AUD/CAD OTC", "Aussie / Canadian OTC", MarketCategory.CURRENCY, 88, 0.89215, 0.45),
    MarketAsset("5", "BTC/USD", "Bitcoin / US Dollar", MarketCategory.CRYPTO, 91, 67420.50, 2.14),
    MarketAsset("6", "ETH/USD", "Ethereum / US Dollar", MarketCategory.CRYPTO, 89, 3512.40, 1.85),
    MarketAsset("7", "SOL/USD", "Solana / US Dollar", MarketCategory.CRYPTO, 87, 148.90, -0.95),
    MarketAsset("8", "XAU/USD OTC", "Gold OTC", MarketCategory.COMMODITY, 92, 2384.20, 0.78),
    MarketAsset("9", "XAG/USD OTC", "Silver OTC", MarketCategory.COMMODITY, 86, 28.65, 1.12),
    MarketAsset("10", "US Oil", "Crude Oil Brent", MarketCategory.COMMODITY, 85, 82.40, -0.40),
    MarketAsset("11", "AAPL", "Apple Inc.", MarketCategory.STOCK, 88, 224.30, 1.40),
    MarketAsset("12", "TSLA", "Tesla Motors", MarketCategory.STOCK, 87, 218.60, -1.80)
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    private var candleTimerJob: Job? = null
    private var tradeTimerJob: Job? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        initCandles(defaultAssets[0].price)
        startContinuousCandleTimer()
        generateInitialSignal(defaultAssets[0])
    }

    fun dismissSplash() {
        _uiState.update { it.copy(showSplash = false) }
        SoundSynthesizer.playClick()
    }

    fun replaySplash() {
        _uiState.update { it.copy(showSplash = true) }
        SoundSynthesizer.playDragonRoar()
    }

    fun setStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
        SoundSynthesizer.playNav()
    }

    fun setCategory(category: MarketCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        SoundSynthesizer.playClick()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectAsset(asset: MarketAsset) {
        _uiState.update {
            it.copy(
                selectedAsset = asset,
                currentStep = 2
            )
        }
        initCandles(asset.price)
        generateInitialSignal(asset)
        SoundSynthesizer.playClick()
    }

    fun setAccountType(accountType: AccountType) {
        _uiState.update { it.copy(accountType = accountType) }
        SoundSynthesizer.playClick()
    }

    fun setExecutionMode(mode: ExecutionMode) {
        _uiState.update { it.copy(executionMode = mode) }
        SoundSynthesizer.playClick()
    }

    fun setTimeframe(timeframe: Timeframe) {
        _uiState.update {
            it.copy(
                timeframe = timeframe,
                candleRemainingSeconds = timeframe.seconds
            )
        }
        SoundSynthesizer.playClick()
    }

    fun toggleSound() {
        val newState = !_uiState.value.isSoundEnabled
        _uiState.update { it.copy(isSoundEnabled = newState) }
        SoundSynthesizer.isSoundEnabled = newState
        if (newState) {
            SoundSynthesizer.playClick()
        }
    }

    fun resetAll() {
        val initialAsset = defaultAssets[0]
        _uiState.update {
            TradingUiState(
                showSplash = false,
                currentStep = 1,
                selectedAsset = initialAsset,
                isSoundEnabled = it.isSoundEnabled
            )
        }
        initCandles(initialAsset.price)
        generateInitialSignal(initialAsset)
        SoundSynthesizer.playClick()
    }

    fun clearHistory() {
        _uiState.update { it.copy(tradeLogs = emptyList()) }
        SoundSynthesizer.playClick()
    }

    fun closeTradeResultModal() {
        _uiState.update { it.copy(lastTradeResult = null) }
    }

    private fun startContinuousCandleTimer() {
        candleTimerJob?.cancel()
        candleTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _uiState.value
                val remaining = current.candleRemainingSeconds - 1

                if (remaining <= 0) {
                    val tfSeconds = current.timeframe.seconds
                    val updatedCandles = current.candles.toMutableList()
                    val lastClose = updatedCandles.lastOrNull()?.close ?: current.selectedAsset.price
                    val volatility = lastClose * 0.00035

                    val open = lastClose
                    val change = (Random.nextDouble() - 0.485) * volatility
                    val close = open + change
                    val high = max(open, close) + Random.nextDouble() * (volatility * 0.6)
                    val low = min(open, close) - Random.nextDouble() * (volatility * 0.6)

                    if (updatedCandles.size >= 30) {
                        updatedCandles.removeAt(0)
                    }
                    updatedCandles.add(Candle(open, high, low, close))

                    val updatedAsset = current.selectedAsset.copy(price = close)

                    _uiState.update {
                        it.copy(
                            candleRemainingSeconds = tfSeconds,
                            candles = updatedCandles,
                            selectedAsset = updatedAsset
                        )
                    }
                } else {
                    val updatedCandles = current.candles.toMutableList()
                    if (updatedCandles.isNotEmpty()) {
                        val last = updatedCandles.last()
                        val delta = (Random.nextDouble() - 0.49) * (last.close * 0.00008)
                        val newClose = last.close + delta
                        val newHigh = max(last.high, newClose)
                        val newLow = min(last.low, newClose)
                        updatedCandles[updatedCandles.size - 1] = last.copy(close = newClose, high = newHigh, low = newLow)

                        _uiState.update {
                            it.copy(
                                candleRemainingSeconds = remaining,
                                candles = updatedCandles,
                                selectedAsset = it.selectedAsset.copy(price = newClose)
                            )
                        }
                    } else {
                        _uiState.update { it.copy(candleRemainingSeconds = remaining) }
                    }
                }
            }
        }
    }

    private fun initCandles(basePrice: Double) {
        val list = mutableListOf<Candle>()
        var price = basePrice
        for (i in 0 until 30) {
            val delta = (Random.nextDouble() - 0.49) * (price * 0.0004)
            val open = price
            val close = open + delta
            val high = max(open, close) + Random.nextDouble() * (price * 0.0002)
            val low = min(open, close) - Random.nextDouble() * (price * 0.0002)
            list.add(Candle(open, high, low, close))
            price = close
        }
        _uiState.update { it.copy(candles = list) }
    }

    private fun generateInitialSignal(asset: MarketAsset) {
        val isCall = Random.nextBoolean()
        val conf = calculateEstimatedConfidence(isCall)
        val reasons = generateReasons(isCall)

        val signal = TradeSignal(
            id = "SIG_${System.currentTimeMillis()}",
            asset = asset,
            direction = if (isCall) SignalDirection.CALL else SignalDirection.PUT,
            strikePrice = asset.price,
            confidence = conf,
            reasons = reasons,
            deltaOrderFlow = if (isCall) 68.4 else -74.2,
            tickVolatility = "Ultra High (3.4x)",
            volumeSpike = "Active (Confluence)",
            multiTrend = if (isCall) "Bullish Dominance" else "Bearish Dominance",
            liquidityVector = if (isCall) "+3.42σ Flow Vector" else "-3.85σ Flow Vector",
            mathMomentum = "Fast Reversal ${conf}%",
            timeframe = _uiState.value.timeframe
        )
        _uiState.update { it.copy(currentSignal = signal) }
    }

    fun fireTradeOrSignal() {
        if (_uiState.value.isCalculatingSignal || _uiState.value.activeTrade != null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCalculatingSignal = true,
                    shakeTrigger = System.currentTimeMillis()
                )
            }
            vibrate(50)
            SoundSynthesizer.playTradeTrigger()

            delay(800L)

            val isCall = Random.nextDouble() > 0.47
            val conf = calculateEstimatedConfidence(isCall)
            val reasons = generateReasons(isCall)
            val strike = _uiState.value.selectedAsset.price

            val newSignal = TradeSignal(
                id = "SIG_${System.currentTimeMillis()}",
                asset = _uiState.value.selectedAsset,
                direction = if (isCall) SignalDirection.CALL else SignalDirection.PUT,
                strikePrice = strike,
                confidence = conf,
                reasons = reasons,
                deltaOrderFlow = if (isCall) (62.0 + Random.nextDouble() * 18.0) else -(64.0 + Random.nextDouble() * 16.0),
                tickVolatility = if (Random.nextBoolean()) "Ultra High (3.4x)" else "High Volatility (2.8x)",
                volumeSpike = "Confluence Absorption",
                multiTrend = if (isCall) "Bullish Dominance" else "Bearish Dominance",
                liquidityVector = if (isCall) "+3.42σ Flow Vector" else "-3.85σ Flow Vector",
                mathMomentum = "Fast Reversal ${conf}%",
                timeframe = _uiState.value.timeframe
            )

            if (isCall) {
                SoundSynthesizer.playCallSignal()
            } else {
                SoundSynthesizer.playPutSignal()
            }

            val totalTradeMillis = 5000L
            val activeTrade = ActiveTrade(
                id = "TRD_${System.currentTimeMillis()}",
                asset = _uiState.value.selectedAsset,
                direction = newSignal.direction,
                strikePrice = strike,
                confidence = conf,
                durationSeconds = 5,
                remainingMillis = totalTradeMillis,
                currentPrice = strike,
                status = TradeStatus.IN_PROGRESS
            )

            _uiState.update {
                it.copy(
                    isCalculatingSignal = false,
                    currentSignal = newSignal,
                    activeTrade = activeTrade
                )
            }

            start5SecondTradeExecution(activeTrade, totalTradeMillis)
        }
    }

    private fun start5SecondTradeExecution(trade: ActiveTrade, totalMillis: Long) {
        tradeTimerJob?.cancel()
        tradeTimerJob = viewModelScope.launch {
            val interval = 50L
            var remaining = totalMillis
            var lastSecBeep = 5

            while (remaining > 0) {
                delay(interval)
                remaining -= interval
                val currentSec = (remaining / 1000L).toInt() + 1
                if (currentSec != lastSecBeep && remaining > 0) {
                    lastSecBeep = currentSec
                    SoundSynthesizer.playCountdownTick(currentSec)
                    vibrate(20)
                }

                val currentPrice = _uiState.value.selectedAsset.price + (Random.nextDouble() - 0.485) * (_uiState.value.selectedAsset.price * 0.00004)

                _uiState.update {
                    it.copy(
                        activeTrade = it.activeTrade?.copy(
                            remainingMillis = remaining,
                            currentPrice = currentPrice
                        )
                    )
                }
            }

            val isCall = trade.direction == SignalDirection.CALL
            val winChance = trade.confidence / 100.0
            val isWin = Random.nextDouble() < winChance

            val exitPrice = if (isWin) {
                if (isCall) trade.strikePrice + trade.strikePrice * 0.0001 else trade.strikePrice - trade.strikePrice * 0.0001
            } else {
                if (isCall) trade.strikePrice - trade.strikePrice * 0.00008 else trade.strikePrice + trade.strikePrice * 0.00008
            }

            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeStr = formatter.format(Date())
            val payout = if (isWin) (100.0 * (trade.asset.payout / 100.0)) else 0.0

            val log = TradeLog(
                id = trade.id,
                assetSymbol = trade.asset.symbol,
                direction = trade.direction,
                strikePrice = trade.strikePrice,
                exitPrice = exitPrice,
                confidence = trade.confidence,
                isWin = isWin,
                timeFormatted = timeStr,
                payoutAmount = payout
            )

            if (isWin) {
                SoundSynthesizer.playWin()
                vibrate(100)
            } else {
                SoundSynthesizer.playLoss()
                vibrate(60)
            }

            _uiState.update {
                it.copy(
                    activeTrade = null,
                    tradeLogs = listOf(log) + it.tradeLogs,
                    lastTradeResult = log,
                    shakeTrigger = System.currentTimeMillis()
                )
            }
        }
    }

    private fun calculateEstimatedConfidence(isCall: Boolean): Double {
        val base = 84.0 + Random.nextDouble() * 10.4
        return (base * 10.0).toInt() / 10.0
    }

    private fun generateReasons(isCall: Boolean): List<String> {
        return if (isCall) {
            listOf(
                "• RSI Hidden Divergence (${(31.0 + Random.nextDouble() * 6.0).toInt() / 10.0} Oversold Rebound)",
                "• Stoch Fast %K crossed above %D line at ${(21.0 + Random.nextDouble() * 4.0).toInt() / 10.0}",
                "• +${(72.0 + Random.nextDouble() * 14.0).toInt() / 10.0}% Aggressive Buying Liquidity Absorption",
                "• Lower Band Mean Reversion snap-back confirmed"
            )
        } else {
            listOf(
                "• RSI Bearish Exhaustion at ${(76.0 + Random.nextDouble() * 6.0).toInt() / 10.0} Overbought Peak",
                "• Stoch %K crossed below %D line at ${(79.0 + Random.nextDouble() * 5.0).toInt() / 10.0}",
                "• -${(75.0 + Random.nextDouble() * 14.0).toInt() / 10.0}% Institutional Sell Wall Resistance",
                "• Upper Band Rejection with High Volume Reversal"
            )
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        candleTimerJob?.cancel()
        tradeTimerJob?.cancel()
    }
}


// ============================================================================
// FILE: app/src/main/java/com/example/model/DataModels.kt
// ============================================================================
package com.example.model

enum class MarketCategory(val label: String) {
    ALL("All Markets"),
    CURRENCY("Forex OTC"),
    CRYPTO("Crypto"),
    COMMODITY("Commodities"),
    STOCK("Stocks")
}

enum class SignalDirection {
    CALL,
    PUT
}

enum class AccountType(val label: String) {
    DEMO("DEMO PRACTICE"),
    REAL("REAL ACCOUNT")
}

enum class ExecutionMode(val label: String, val seconds: Int) {
    SCALP_5S("5-SEC SCALP", 5),
    SPRINT_30S("30-SEC SPRINT", 30),
    CLASSIC_1M("1-MIN CLASSIC", 60),
    SWING_5M("5-MIN SWING", 300)
}

enum class Timeframe(val label: String, val seconds: Int) {
    S5("S5", 5),
    S15("S15", 15),
    S30("S30", 30),
    M1("M1", 60),
    M5("M5", 300)
}

data class MarketAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val category: MarketCategory,
    val payout: Int,
    var price: Double,
    val change24h: Double,
    val decimals: Int = if (price > 100) 2 else 5
)

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 1.0
)

data class TradeSignal(
    val id: String,
    val asset: MarketAsset,
    val direction: SignalDirection,
    val strikePrice: Double,
    val confidence: Double,
    val reasons: List<String>,
    val deltaOrderFlow: Double,
    val tickVolatility: String,
    val volumeSpike: String,
    val multiTrend: String,
    val liquidityVector: String,
    val mathMomentum: String,
    val timeframe: Timeframe,
    val timestamp: Long = System.currentTimeMillis()
)

data class ActiveTrade(
    val id: String,
    val asset: MarketAsset,
    val direction: SignalDirection,
    val strikePrice: Double,
    val confidence: Double,
    val durationSeconds: Int = 5,
    val remainingMillis: Long,
    val currentPrice: Double,
    val status: TradeStatus = TradeStatus.IN_PROGRESS
)

enum class TradeStatus {
    IN_PROGRESS,
    WIN,
    LOSS
}

data class TradeLog(
    val id: String,
    val assetSymbol: String,
    val direction: SignalDirection,
    val strikePrice: Double,
    val exitPrice: Double,
    val confidence: Double,
    val isWin: Boolean,
    val timeFormatted: String,
    val payoutAmount: Double = 0.0
)


// ============================================================================
// FILE: app/src/main/java/com/example/components/RedWater3DBackground.kt
// ============================================================================
package com.example.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.example.ui.theme.BgObsidian
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonDeepCrimson
import com.example.ui.theme.DragonRed
import com.example.ui.theme.DragonRedGlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

data class TouchRipple(
    val center: Offset,
    val birthTime: Long,
    val maxRadius: Float = 350f,
    val durationMs: Long = 1000L
)

data class GlowingMote(
    val xRatio: Float,
    var yRatio: Float,
    val radius: Float,
    val speed: Float,
    val color: Color,
    val alpha: Float
)

@Composable
fun RedWater3DBackground(
    shakeTrigger: Long = 0L,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "water_waves")

    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val causticShimmer by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "caustic"
    )

    val shakeOffsetX = remember { Animatable(0f) }
    val shakeOffsetY = remember { Animatable(0f) }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0L) {
            coroutineScope.launch {
                val duration = 400
                val decay = 8
                for (i in 0..decay) {
                    val strength = (1f - (i.toFloat() / decay)) * 14f
                    val dx = (Random.nextFloat() * 2f - 1f) * strength
                    val dy = (Random.nextFloat() * 2f - 1f) * strength
                    shakeOffsetX.snapTo(dx)
                    shakeOffsetY.snapTo(dy)
                    kotlinx.coroutines.delay((duration / decay).toLong())
                }
                shakeOffsetX.animateTo(0f, tween(100))
                shakeOffsetY.animateTo(0f, tween(100))
            }
        }
    }

    val ripples = remember { mutableStateListOf<TouchRipple>() }
    val motes = remember {
        val list = mutableStateListOf<GlowingMote>()
        val rnd = Random(42)
        for (i in 0 until 28) {
            val isGold = rnd.nextFloat() > 0.75f
            list.add(
                GlowingMote(
                    xRatio = rnd.nextFloat(),
                    yRatio = rnd.nextFloat(),
                    radius = rnd.nextFloat() * 3f + 1.5f,
                    speed = rnd.nextFloat() * 0.00035f + 0.00015f,
                    color = if (isGold) Color(0xFFFBBF24) else DragonRedGlow,
                    alpha = rnd.nextFloat() * 0.6f + 0.3f
                )
            )
        }
        list
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset {
                IntOffset(
                    x = shakeOffsetX.value.roundToInt(),
                    y = shakeOffsetY.value.roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    ripples.add(
                        TouchRipple(
                            center = offset,
                            birthTime = System.currentTimeMillis()
                        )
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val now = System.currentTimeMillis()

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BgObsidian,
                        DragonDeepCrimson.copy(alpha = 0.55f),
                        BgObsidian
                    )
                ),
                size = size
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DragonRed.copy(alpha = 0.28f * causticShimmer),
                        DragonCrimson.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.2f),
                    radius = width * 0.85f
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DragonRedGlow.copy(alpha = 0.18f * (1.1f - causticShimmer)),
                        DragonCrimson.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.8f, height * 0.75f),
                    radius = width * 0.7f
                )
            )

            drawLiquidWaveLayer(
                width = width,
                height = height,
                baseYRatio = 0.82f,
                amplitude = 22f,
                frequency = 0.008f,
                phase = wavePhase1,
                color = DragonDeepCrimson.copy(alpha = 0.45f)
            )

            drawLiquidWaveLayer(
                width = width,
                height = height,
                baseYRatio = 0.88f,
                amplitude = 16f,
                frequency = 0.012f,
                phase = wavePhase2 + 1.2f,
                color = DragonCrimson.copy(alpha = 0.35f)
            )

            drawLiquidWaveLayer(
                width = width,
                height = height,
                baseYRatio = 0.93f,
                amplitude = 12f,
                frequency = 0.016f,
                phase = wavePhase1 * 1.5f + 0.6f,
                color = DragonRed.copy(alpha = 0.25f)
            )

            motes.forEach { mote ->
                mote.yRatio -= mote.speed
                if (mote.yRatio < -0.05f) {
                    mote.yRatio = 1.05f
                }
                val px = mote.xRatio * width + sin(mote.yRatio * 10f + wavePhase1) * 8f
                val py = mote.yRatio * height
                drawCircle(
                    color = mote.color.copy(alpha = mote.alpha * causticShimmer),
                    radius = mote.radius,
                    center = Offset(px, py)
                )
            }

            val iterator = ripples.iterator()
            while (iterator.hasNext()) {
                val r = iterator.next()
                val elapsed = now - r.birthTime
                if (elapsed > r.durationMs) {
                    iterator.remove()
                } else {
                    val progress = elapsed.toFloat() / r.durationMs
                    val radius = r.maxRadius * progress
                    val alpha = (1f - progress) * 0.4f
                    drawCircle(
                        color = DragonRedGlow.copy(alpha = alpha),
                        radius = radius,
                        center = r.center,
                        style = Stroke(width = (4f * (1f - progress)).coerceAtLeast(1f))
                    )
                }
            }
        }

        content()
    }
}

private fun DrawScope.drawLiquidWaveLayer(
    width: Float,
    height: Float,
    baseYRatio: Float,
    amplitude: Float,
    frequency: Float,
    phase: Float,
    color: Color
) {
    val path = Path()
    val baseY = height * baseYRatio
    path.moveTo(0f, height)
    path.lineTo(0f, baseY)

    var x = 0f
    val step = 14f
    while (x <= width) {
        val y = baseY + sin(x * frequency + phase) * amplitude +
                cos(x * frequency * 0.5f + phase * 1.3f) * (amplitude * 0.4f)
        path.lineTo(x, y)
        x += step
    }
    path.lineTo(width, height)
    path.close()

    drawPath(path = path, color = color)
}


// ============================================================================
// FILE: app/src/main/java/com/example/components/LiquidGlassComponents.kt
// ============================================================================
package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BgGlassBorder
import com.example.ui.theme.BgGlassBorderBright
import com.example.ui.theme.BgGlassCard
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonRed
import com.example.ui.theme.DragonRedGlow
import com.example.ui.theme.TextPrimary

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = BgGlassCard,
    borderColor: Color = BgGlassBorder,
    glowColor: Color = Color.Transparent,
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = DragonRed.copy(alpha = 0.25f),
                spotColor = DragonRedGlow.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.75f),
                        backgroundColor.copy(alpha = 0.45f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 800f)
                )
            )
            .border(
                border = BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            if (glowColor != Color.Transparent) glowColor else BgGlassBorderBright,
                            borderColor,
                            Color(0x05FFFFFF)
                        )
                    )
                ),
                shape = shape
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x22FFFFFF),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, 0f),
                        radius = size.width * 0.7f
                    )
                )
            },
        content = content
    )
}

@Composable
fun LiquidGlass3DIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    badgeColor: Color = DragonRed,
    iconTint: Color = Color.White,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = badgeColor.copy(alpha = 0.4f),
                spotColor = badgeColor.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        badgeColor.copy(alpha = 0.45f),
                        badgeColor.copy(alpha = 0.15f),
                        Color(0x220A0F1D)
                    ),
                    center = Offset.Unspecified,
                    radius = 80f
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x88FFFFFF),
                        badgeColor.copy(alpha = 0.7f),
                        Color(0x11FFFFFF)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(DragonCrimson, DragonRed),
    textColor: Color = TextPrimary,
    glowColor: Color = DragonRedGlow,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 48.dp)
            .shadow(
                elevation = if (enabled) 10.dp else 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = glowColor.copy(alpha = 0.35f),
                spotColor = glowColor.copy(alpha = 0.55f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(gradientColors)
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF2B3245), Color(0xFF1E2433)))
                }
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x99FFFFFF),
                        glowColor.copy(alpha = 0.8f),
                        Color(0x22FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
fun LiquidGlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = DragonRedGlow,
    bgColor: Color = DragonRed.copy(alpha = 0.2f),
    borderColor: Color = DragonRed.copy(alpha = 0.45f)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                width = 0.9.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}


// ============================================================================
// FILE: app/src/main/java/com/example/components/HeroTopNav.kt
// ============================================================================
package com.example.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentSky
import com.example.ui.theme.BgGlassBorder