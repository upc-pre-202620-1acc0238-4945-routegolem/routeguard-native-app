package pe.edu.upc.routeguard.core.designsystem.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmerald
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavyDeep

/**
 * Navy header with the RouteGuard shield, the same identity as the landing page.
 * It draws behind the status bar, so the status bar icons are switched to light while it is on screen.
 */
@Composable
fun BrandHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    LightStatusBarIcons()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.verticalGradient(listOf(BrandNavy, BrandNavyDeep)))
            .statusBarsPadding()
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = if (compact) 20.dp else 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.brand_logo),
                contentDescription = "RouteGuard",
                modifier = Modifier.height(if (compact) 56.dp else 96.dp)
            )
            Text(
                text = title,
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Surface(color = BrandEmerald.copy(alpha = 0.18f), shape = CircleShape) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFA7F3D0),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/** Light (white) status bar icons while the calling screen is shown; the previous setting is restored on exit. */
@Composable
fun LightStatusBarIcons() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            if (controller != null && previous != null) controller.isAppearanceLightStatusBars = previous
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
