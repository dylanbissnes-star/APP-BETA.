package com.example.glassplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.R
import com.example.glassplayer.data.Folder
import com.example.glassplayer.ui.theme.Frost

data class DrawerEntry(
    val icon: ImageVector,
    val label: String,
    val selected: Boolean,
    /** Puntito rojo de aviso (p. ej. canciones en la cola). */
    val dot: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * Menu lateral como la tarjeta "MENU" de la referencia: vidrio oscuro redondeado, iconos de linea
 * fina, marca de seleccion en el borde izquierdo con el color de acento, puntito de aviso,
 * separadores finos y la fila de perfil abajo. Incluye BIBLIOTECA y CARPETAS (con papelera para
 * quitarlas y «Agregar carpeta»).
 */
@Composable
fun AppDrawerContent(
    menu: List<DrawerEntry>,
    library: List<DrawerEntry>,
    folders: List<Folder>,
    onOpenFolder: (Folder) -> Unit,
    onRemoveFolder: (Folder) -> Unit,
    onAddFolder: () -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)
    ModalDrawerSheet(
        drawerShape = RectangleShape,
        drawerContainerColor = Color.Transparent,
        drawerContentColor = Frost.TextPrimary,
        drawerTonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
                .clip(shape)
                .background(Frost.Floating)
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        if (Frost.OnDark) listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.06f))
                        else listOf(Color.White, Color.White.copy(alpha = 0.4f))
                    ),
                    shape,
                ),
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 20.dp, bottom = 8.dp),
            ) {
                DrawerSection("MENU")
                menu.forEachIndexed { i, e ->
                    if (i > 0) Hairline()
                    DrawerItem(e.icon, e.label, e.selected, e.dot, e.onClick)
                }

                DrawerSection("BIBLIOTECA")
                library.forEachIndexed { i, e ->
                    if (i > 0) Hairline()
                    DrawerItem(e.icon, e.label, e.selected, e.dot, e.onClick)
                }

                DrawerSection("CARPETAS")
                folders.forEach { folder ->
                    Hairline()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .pressClick({ onOpenFolder(folder) }, pressedScale = 0.99f)
                            .padding(start = 22.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.FolderOpen, null, tint = Frost.TextSecondary, modifier = Modifier.size(20.dp))
                        Text(
                            folder.name,
                            color = Frost.TextPrimary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                        )
                        PlainIconButton(
                            Icons.Outlined.DeleteOutline, "Quitar carpeta", { onRemoveFolder(folder) },
                            size = 38.dp, iconSize = 19.dp, tint = Frost.TextTertiary,
                        )
                    }
                }
                Hairline()
                DrawerItem(Icons.Outlined.CreateNewFolder, "Agregar carpeta", false, false, onAddFolder)
            }

            // Fila de perfil, abajo
            Hairline()
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (Frost.OnDark) 0.12f else 0.9f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painterResource(R.drawable.logo_mark), null, Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(if (Frost.OnDark) Color.White else Color(0xFF16161A)),
                    )
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        stringResource(R.string.app_name),
                        color = Frost.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    )
                    Text("Reproductor local", color = Frost.TextTertiary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun Hairline() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(1.dp)
            .background(Frost.Outline.copy(alpha = 0.10f)),
    )
}

@Composable
private fun DrawerSection(title: String) {
    Text(
        title,
        color = Frost.TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.6.sp,
        modifier = Modifier.padding(start = 22.dp, top = 14.dp, bottom = 10.dp),
    )
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, selected: Boolean, dot: Boolean, onClick: () -> Unit) {
    val accent = Frost.Accent
    Row(
        Modifier
            .fillMaxWidth()
            .pressClick(onClick, pressedScale = 0.99f)
            .background(if (selected) Frost.TextPrimary.copy(alpha = 0.05f) else Color.Transparent)
            .drawBehind {
                if (selected) {
                    // Marca de seleccion pegada al borde izquierdo (la barra roja de la referencia)
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(0f, size.height * 0.18f),
                        size = Size(3.dp.toPx(), size.height * 0.64f),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                    )
                }
            }
            .padding(start = 22.dp, end = 20.dp, top = 15.dp, bottom = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (selected) Frost.TextPrimary else Frost.TextSecondary, modifier = Modifier.size(20.dp))
        Text(
            label,
            color = if (selected) Frost.TextPrimary else Frost.TextSecondary,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
        if (dot) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
        }
    }
}
