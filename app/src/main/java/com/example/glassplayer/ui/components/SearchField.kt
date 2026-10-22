package com.example.glassplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.ui.theme.BlackGlass

/** Campo de busqueda negro con borde translucido que se ilumina suavemente al enfocarlo. */
@Composable
fun GlassSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Canciones, artistas o álbumes",
) {
    val shape = RoundedCornerShape(50)
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val borderAlpha by animateFloatAsState(if (focused) 0.45f else 0.14f, tween(250), label = "searchBorder")

    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.85f))
            .border(1.dp, Color.White.copy(alpha = borderAlpha), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        Box(
            Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (query.isEmpty()) {
                Text(placeholder, color = BlackGlass.TextSecondary, fontSize = 15.sp, maxLines = 1)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = BlackGlass.TextPrimary, fontSize = 15.sp),
                cursorBrush = SolidColor(Color.White),
                interactionSource = source,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(tween(150)), exit = fadeOut(tween(120))) {
            Box(
                Modifier
                    .size(24.dp)
                    .pressClick({ onQueryChange("") }, pressedScale = 0.85f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, "Borrar búsqueda", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
