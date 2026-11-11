package com.example.glassplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
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
    val onClick: () -> Unit,
)

/**
 * Menu lateral (la barra lateral de la referencia de escritorio, adaptada al movil):
 * logo, MENU, BIBLIOTECA y CARPETAS (con papelera para quitarlas y «Agregar carpeta»).
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
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = Frost.Surface,
        drawerContentColor = Frost.TextPrimary,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 16.dp),
        ) {
            Row(
                Modifier.padding(start = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painterResource(R.drawable.logo_mark), null, Modifier.size(34.dp),
                    colorFilter = ColorFilter.tint(Frost.TextPrimary),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(R.string.app_name),
                    color = Frost.TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            DrawerSection("MENÚ")
            menu.forEach { DrawerItem(it.icon, it.label, it.selected, it.onClick) }

            DrawerSection("BIBLIOTECA")
            library.forEach { DrawerItem(it.icon, it.label, it.selected, it.onClick) }

            DrawerSection("CARPETAS")
            folders.forEach { folder ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .pressClick({ onOpenFolder(folder) }, pressedScale = 0.98f)
                        .padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Folder, null, tint = Frost.TextSecondary, modifier = Modifier.size(20.dp))
                    Text(
                        folder.name,
                        color = Frost.TextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                    PlainIconButton(
                        Icons.Rounded.DeleteOutline, "Quitar carpeta", { onRemoveFolder(folder) },
                        size = 38.dp, iconSize = 20.dp, tint = Frost.TextTertiary,
                    )
                }
            }
            DrawerItem(Icons.Rounded.CreateNewFolder, "Agregar carpeta", false, onAddFolder)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DrawerSection(title: String) {
    Text(
        title,
        color = Frost.TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 12.dp, top = 18.dp, bottom = 6.dp),
    )
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val accent = Frost.Accent
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .pressClick(onClick, pressedScale = 0.98f)
            .background(if (selected) accent.copy(alpha = 0.14f) else Color.Transparent)
            .drawBehind {
                if (selected) {
                    // Marca de seleccion a la izquierda (la barrita verde de la referencia)
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(0f, size.height * 0.26f),
                        size = Size(3.dp.toPx(), size.height * 0.48f),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                    )
                }
            }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (selected) accent else Frost.TextSecondary, modifier = Modifier.size(20.dp))
        Text(
            label,
            color = if (selected) Frost.TextPrimary else Frost.TextSecondary,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}
