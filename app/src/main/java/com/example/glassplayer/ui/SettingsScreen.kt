package com.example.glassplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import com.example.glassplayer.ui.components.ActionPill
import com.example.glassplayer.ui.components.ScreenHeader
import com.example.glassplayer.ui.components.card
import com.example.glassplayer.ui.components.pressClick
import com.example.glassplayer.ui.theme.Frost
import com.example.glassplayer.ui.theme.onAccentFor
import kotlin.math.roundToInt

/** Colores sugeridos; ademas se puede mezclar cualquiera con los deslizadores. */
private val Presets = listOf(
    Color(0xFFFFC21A), // ambar (por defecto)
    Color(0xFFFF8A3D), // naranja
    Color(0xFFFF5A5F), // coral
    Color(0xFFFF4D8D), // rosa
    Color(0xFFB66DFF), // violeta
    Color(0xFF6C7BFF), // azul violeta
    Color(0xFF3DA5FF), // azul
    Color(0xFF22D3C5), // turquesa
    Color(0xFF4ADE80), // verde
    Color(0xFFC6F135), // lima
    Color(0xFFF2F2F2), // blanco
)

private fun hsvOf(c: Color): FloatArray =
    FloatArray(3).also { android.graphics.Color.colorToHSV(c.toArgb(), it) }

private const val MinBrightness = 0.35f

/**
 * Ajustes: color de acento (paleta + mezclador libre), modo oscuro, fondo del reproductor y
 * borrado de letras guardadas.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    accent: Color,
    onAccent: (Color) -> Unit,
    dark: Boolean,
    onDark: (Boolean) -> Unit,
    playerAccentBg: Boolean,
    onPlayerAccentBg: (Boolean) -> Unit,
    onClearLyrics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tono, saturacion y brillo se guardan aparte para que mover un deslizador no
    // pierda el tono cuando el color queda gris/negro
    var hsv by remember { mutableStateOf(hsvOf(accent)) }
    fun pick(h: Float, s: Float, v: Float) {
        val vv = v.coerceIn(MinBrightness, 1f)
        hsv = floatArrayOf(h, s, vv)
        onAccent(Color.hsv(h, s, vv))
    }

    Column(modifier.fillMaxSize()) {
        ScreenHeader(title = "Ajustes", subtitle = "Personaliza la app")

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 168.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // --- Color de acento ---
            Section("Color de acento") {
                AccentPreview(accent)
                Spacer(Modifier.height(16.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Presets.forEach { c ->
                        Swatch(c, selected = c.toArgb() == accent.toArgb()) {
                            val h = hsvOf(c)
                            hsv = h
                            onAccent(c)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("Mezclar tu propio color", color = Frost.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                val h = hsv[0]; val s = hsv[1]; val v = hsv[2]
                SliderRow("Tono") {
                    GradientSlider(
                        value = h / 360f,
                        brush = Brush.horizontalGradient(
                            listOf(0f, 60f, 120f, 180f, 240f, 300f, 359f).map { Color.hsv(it, 1f, 1f) }
                        ),
                        thumb = Color.hsv(h, 1f, 1f),
                        onChange = { pick(it * 360f, s.coerceAtLeast(0.25f), v) },
                    )
                }
                SliderRow("Intensidad") {
                    GradientSlider(
                        value = s,
                        brush = Brush.horizontalGradient(listOf(Color.hsv(h, 0f, v), Color.hsv(h, 1f, v))),
                        thumb = accent,
                        onChange = { pick(h, it, v) },
                    )
                }
                SliderRow("Brillo") {
                    GradientSlider(
                        value = (v - MinBrightness) / (1f - MinBrightness),
                        brush = Brush.horizontalGradient(
                            listOf(Color.hsv(h, s, MinBrightness), Color.hsv(h, s, 1f))
                        ),
                        thumb = accent,
                        onChange = { pick(h, s, MinBrightness + it * (1f - MinBrightness)) },
                    )
                }
            }

            // --- Apariencia ---
            Section("Apariencia") {
                SwitchRow("Modo oscuro", "Fondo oscuro o claro en toda la app", dark, onDark)
                Spacer(Modifier.height(6.dp))
                SwitchRow(
                    "Reproductor con tu color",
                    "Pinta el reproductor con el acento. Si lo apagas, se ve la portada desenfocada.",
                    playerAccentBg, onPlayerAccentBg,
                )
            }

            // --- Letras ---
            Section("Letras") {
                Text(
                    "Las letras se buscan en LRCLIB (necesita internet) y se guardan en el teléfono, " +
                        "así la próxima vez se ven sin conexión.",
                    color = Frost.TextSecondary, fontSize = 13.sp, lineHeight = 18.sp,
                )
                Spacer(Modifier.height(12.dp))
                ActionPill("Borrar letras guardadas", onClearLyrics)
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(Modifier.fillMaxWidth().card(shape).padding(18.dp)) {
        Text(title, color = Frost.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

/** Vista previa con los elementos que usan el acento (boton, play, corazon). */
@Composable
private fun AccentPreview(accent: Color) {
    val on = onAccentFor(accent)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Frost.SurfaceHigh)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.clip(RoundedCornerShape(50)).background(accent).padding(horizontal = 16.dp, vertical = 9.dp),
        ) { Text("Aleatorio", color = on, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
        Box(Modifier.size(40.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.PlayArrow, null, tint = on, modifier = Modifier.size(24.dp))
        }
        Icon(Icons.Rounded.Favorite, null, tint = accent, modifier = Modifier.size(26.dp))
        Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(Frost.Outline)) {
            Box(Modifier.fillMaxWidth(0.6f).height(4.dp).background(accent))
        }
    }
}

@Composable
private fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .pressClick(onClick, pressedScale = 0.9f)
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 3.dp else 1.dp, if (selected) Frost.TextPrimary else Frost.Outline, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Rounded.Check, "Seleccionado", tint = onAccentFor(color), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SliderRow(label: String, slider: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Frost.TextSecondary, fontSize = 13.sp, modifier = Modifier.width(84.dp))
        Box(Modifier.weight(1f)) { slider() }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .pressClick({ onChange(!checked) }, pressedScale = 0.99f)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = Frost.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Frost.TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Frost.AccentFill,
                checkedThumbColor = Frost.OnAccent,
                uncheckedTrackColor = Frost.SurfaceHigh,
                uncheckedThumbColor = Frost.TextSecondary,
                uncheckedBorderColor = Frost.Outline,
            ),
        )
    }
}

/** Barra con degradado y un circulo que se arrastra (valor de 0 a 1). */
@Composable
private fun GradientSlider(value: Float, brush: Brush, thumb: Color, onChange: (Float) -> Unit) {
    val latest by rememberUpdatedState(onChange)
    val shape = RoundedCornerShape(50)
    val thumbSize = 26.dp
    var widthPx by remember { mutableStateOf(1) }
    val thumbPx = with(LocalDensity.current) { thumbSize.roundToPx() }

    Box(
        Modifier
            .fillMaxWidth()
            .height(30.dp)
            .onSizeChanged { widthPx = it.width }
            .clip(shape)
            .background(brush)
            .border(1.dp, Frost.Outline, shape)
            .pointerInput(Unit) {
                detectTapGestures { latest((it.x / size.width).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    latest((change.position.x / size.width).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset { IntOffset(((widthPx - thumbPx - 4) * value.coerceIn(0f, 1f)).roundToInt() + 2, 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumb)
                .border(3.dp, Color.White, CircleShape)
        )
    }
}
