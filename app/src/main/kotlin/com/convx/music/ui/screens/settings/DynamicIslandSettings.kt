package com.convx.music.ui.screens.settings

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.convx.music.R
import com.convx.music.constants.MusicIslandEnabledKey
import com.convx.music.constants.MusicIslandHeightKey
import com.convx.music.constants.MusicIslandOffsetKey
import com.convx.music.constants.MusicIslandOffsetXKey
import com.convx.music.constants.MusicIslandOpacityKey
import com.convx.music.constants.MusicIslandTextSizeKey
import com.convx.music.constants.MusicIslandWidthKey
import com.convx.music.island.IslandStyle
import com.convx.music.island.MusicIslandAccessibilityService
import com.convx.music.island.MusicIslandBridge
import com.convx.music.ui.component.DefaultDialog
import com.convx.music.ui.component.IosSettingsGroup
import com.convx.music.ui.component.IosSettingsItem
import com.convx.music.ui.ios.compat.AppTheme
import com.convx.music.ui.ios.compat.Switch
import com.convx.music.ui.ios.compat.Text
import com.convx.music.ui.ios.compat.TextButton
import com.convx.music.utils.rememberPreference
import kotlin.math.roundToInt

private class IslandSliderSpec(
    val title: String,
    val range: ClosedFloatingPointRange<Float>,
    val current: Float,
    val format: (Float) -> String,
    val save: (Float) -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicIslandSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val (enabled, onEnabledChange) = rememberPreference(key = MusicIslandEnabledKey, defaultValue = false)
    val (width, onWidthChange) = rememberPreference(key = MusicIslandWidthKey, defaultValue = 220)
    val (height, onHeightChange) = rememberPreference(key = MusicIslandHeightKey, defaultValue = 40)
    val (offset, onOffsetChange) = rememberPreference(key = MusicIslandOffsetKey, defaultValue = 36)
    val (offsetX, onOffsetXChange) = rememberPreference(key = MusicIslandOffsetXKey, defaultValue = 0)
    val (textSize, onTextSizeChange) = rememberPreference(key = MusicIslandTextSizeKey, defaultValue = 13)
    val (opacity, onOpacityChange) = rememberPreference(key = MusicIslandOpacityKey, defaultValue = 1f)

    val serviceOn = isMusicIslandServiceEnabled(context)

    // Terapkan ke overlay langsung, dan tampilkan pratinjau selama layar ini dibuka
    LaunchedEffect(enabled, width, height, offset, offsetX, textSize, opacity) {
        MusicIslandBridge.enabled = enabled
        MusicIslandBridge.style = IslandStyle(
            widthDp = width,
            heightDp = height,
            offsetDp = offset,
            offsetXDp = offsetX,
            textSp = textSize,
            opacity = opacity,
        )
    }
    DisposableEffect(Unit) {
        MusicIslandBridge.preview = true
        onDispose { MusicIslandBridge.preview = false }
    }
    var editing by remember { mutableStateOf<IslandSliderSpec?>(null) }

    SettingsPage(
        navController = navController,
        title = "Dynamic Island",
        pageColor = AppTheme.colorScheme.background,
        horizontalPadding = 16.dp,
    ) {
        IosSettingsGroup(
            title = "Musik",
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text("Dynamic Island musik") },
                    description = { Text("Pill pemutar di atas layar saat musik diputar, termasuk saat app di background.") },
                    trailingContent = { Switch(checked = enabled, onCheckedChange = onEnabledChange) },
                    onClick = { onEnabledChange(!enabled) },
                ),
            ),
        )

        IosSettingsGroup(
            title = "Tampilan",
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text("Lebar") },
                    description = { Text("$width dp") },
                    onClick = {
                        editing = IslandSliderSpec("Lebar", 120f..360f, width.toFloat(),
                            { "${it.roundToInt()} dp" }, { onWidthChange(it.roundToInt()) })
                    },
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text("Tinggi") },
                    description = { Text("$height dp") },
                    onClick = {
                        editing = IslandSliderSpec("Tinggi", 28f..64f, height.toFloat(),
                            { "${it.roundToInt()} dp" }, { onHeightChange(it.roundToInt()) })
                    },
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text("Jarak dari atas layar") },
                    description = { Text("$offset dp") },
                    onClick = {
                        editing = IslandSliderSpec("Jarak dari atas layar", 0f..120f, offset.toFloat(),
                            { "${it.roundToInt()} dp" }, { onOffsetChange(it.roundToInt()) })
                    },
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text("Geser kiri / kanan") },
                    description = { Text("$offsetX dp (negatif ke kiri, positif ke kanan)") },
                    onClick = {
                        editing = IslandSliderSpec("Geser kiri / kanan", -180f..180f, offsetX.toFloat(),
                            { "${it.roundToInt()} dp" }, { onOffsetXChange(it.roundToInt()) })
                    },
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text("Ukuran teks") },
                    description = { Text("$textSize sp") },
                    onClick = {
                        editing = IslandSliderSpec("Ukuran teks", 10f..18f, textSize.toFloat(),
                            { "${it.roundToInt()} sp" }, { onTextSizeChange(it.roundToInt()) })
                    },
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text("Transparansi latar") },
                    description = { Text("${(opacity * 100).roundToInt()}%") },
                    onClick = {
                        editing = IslandSliderSpec("Transparansi latar", 0.3f..1f, opacity,
                            { "${(it * 100).roundToInt()}%" }, { onOpacityChange(it) })
                    },
                ),
            ),
        )

        IosSettingsGroup(
            title = "Izin",
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text("Layanan aksesibilitas") },
                    description = {
                        Text(
                            if (serviceOn) "Aktif"
                            else "Belum aktif. Ketuk untuk membuka pengaturan aksesibilitas, lalu aktifkan Aurora."
                        )
                    },
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                ),
            ),
        )
    }

    editing?.let { spec -> IslandSliderDialog(spec) { editing = null } }
}

@Composable
private fun IslandSliderDialog(spec: IslandSliderSpec, onDismiss: () -> Unit) {
    var temp by remember(spec.title) { mutableFloatStateOf(spec.current) }
    DefaultDialog(
        onDismiss = onDismiss,
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Text(text = spec.title, style = AppTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))
                Text(text = spec.format(temp), style = AppTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))
                Slider(
                    value = temp,
                    onValueChange = { temp = it },
                    valueRange = spec.range,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        buttons = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
            TextButton(onClick = { spec.save(temp); onDismiss() }) { Text(stringResource(android.R.string.ok)) }
        },
    )
}

private fun isMusicIslandServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
    val target = ComponentName(context, MusicIslandAccessibilityService::class.java)
    return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
        val si = it.resolveInfo.serviceInfo
        si.packageName == target.packageName && si.name == target.className
    }
}
