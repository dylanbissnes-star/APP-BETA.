package com.example.glassplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
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
    /** Punto de aviso a la derecha (p. ej. hay canciones en cola). */
    val badge: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * Menu lateral: un panel de cristal ahumado flotante (como el menu de la referencia) con el logo,
 * secciones separadas por lineas finas (MENU, BIBLIOTECA, CARPETAS), la entrada activa en negrita
 * con una barra de acento en el borde y una flecha, y abajo la tarjeta de la cuenta.
 */
@Composable
fun AppDrawerContent(
    menu: List<DrawerEntry>,
    library: List<DrawerEntry>,
    folders: List<Folder>,
    onOpenFolder: (Folder) -> Unit,
    onRemoveFolder: (Folder) -> Unit,
    onAddFolder: () -> Unit,
    settings: DrawerEntry,
    songCount: Int,
    onOpenAccount: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.width(318.dp),
        drawerShape = RoundedCornerShape(0.dp),
        drawerContainerColor = Color.Transparent,
        drawerContentColor = Frost.TextPrimary,
        drawerTonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        val shape = RoundedCornerShape(22.dp)
        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(12.dp)
                .glassPanel(shape, blur = 30.dp),
        ) {
            Column(Modifier.fillMaxHeight()) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 18.dp),
                ) {
                    Row(
                        Modifier.padding(horizontal = 22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painterResource(R.drawable.logo_mark), null, Modifier.size(30.dp),
                            colorFilter = ColorFilter.tint(Frost.TextPrimary),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.app_name),
                            color = Frost.TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Separator()

                    DrawerSection("MENU")
                    menu.forEach { DrawerItem(it) }
                    Separator()

                    DrawerSection("BIBLIOTECA")
                    library.forEach { DrawerItem(it) }
                    Separator()

                    DrawerSection("CARPETAS")
                    folders.forEach { folder ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .pressClick({ onOpenFolder(folder) }, pressedScale = 0.99f)
                                .padding(start = 22.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.FolderOpen, null, tint = Frost.TextSecondary, modifier = Modifier.size(20.dp))
                            Text(
                                folder.name,
                                color = Frost.TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            )
                            PlainIconButton(
                                Icons.Outlined.DeleteOutline, "Quitar carpeta", { onRemoveFolder(folder) },
                                size = 36.dp, iconSize = 19.dp, tint = Frost.TextTertiary,
                            )
                        }
                    }
                    DrawerItem(DrawerEntry(Icons.Outlined.CreateNewFolder, "Agregar carpeta", false, onClick = onAddFolder))
                    Separator()
                    DrawerItem(settings)
                }

                Separator()
                // Tarjeta de la cuenta (abajo): logo en un circulo, nombre y resumen
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressClick(onOpenAccount, pressedScale = 0.99f)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Frost.SurfaceHigh)
                            .border(1.dp, Frost.EdgeLight, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painterResource(R.drawable.logo_mark), null, Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Frost.TextPrimary),
                        )
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Text(
                            stringResource(R.string.app_name),
                            color = Frost.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                        )
                        Text(
                            "Mi biblioteca · $songCount",
                            color = Frost.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Light, maxLines = 1,
                        )
                    }
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = Frost.TextSecondary, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

/** Linea fina que se desvanece en los bordes. */
@Composable
private fun Separator() {
    Box(
        Modifier
            .padding(vertical = 10.dp, horizontal = 18.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(hairlineBrush())
    )
}

@Composable
private fun DrawerSection(title: String) {
    Text(
        title,
        color = Frost.TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 22.dp, top = 4.dp, bottom = 6.dp),
    )
}

@Composable
private fun DrawerItem(entry: DrawerEntry) {
    val accent = Frost.Accent
    val selected = entry.selected
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .pressClick(entry.onClick, pressedScale = 0.99f),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Barra de acento en el borde izquierdo de la entrada activa
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .size(width = 3.dp, height = 24.dp)
                    .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                    .background(accent)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 22.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                entry.icon, null,
                tint = if (selected) Frost.TextPrimary else Frost.TextSecondary,
                modifier = Modifier.size(21.dp),
            )
            Text(
                entry.label,
                color = if (selected) Frost.TextPrimary else Frost.TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Light,
                modifier = Modifier.weight(1f).padding(start = 16.dp),
            )
            if (entry.badge) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
            } else if (selected) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = Frost.TextPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
