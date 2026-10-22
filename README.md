# GlassPlayer

Base de reproductor de música para Android con estilo **glassmorphism**.
Kotlin + Jetpack Compose + Media3 (ExoPlayer).

## Qué incluye

- **Fondo negro con luz ambiental**: la portada actual, muy difuminada y apagada, más manchas
  suaves de sus colores y un haz de luz tenue arriba a la derecha. Predomina el negro.
- **Dock flotante** de navegación (Reproductor / Biblioteca / Cola): la pestaña activa se expande
  en una píldora con icono y texto, borde degradado y halo de luz; la cola muestra un contador.
- **Cabecera común** (título, subtítulo, acción y separador fino) en las tres pantallas.
- **Animaciones**: los botones se encogen y rebotan al pulsar, los iconos cambian con fundido,
  el botón de play «respira» mientras suena y las filas muestran barras de ecualizador.
- **Carrusel 3D** de portadas en tarjetas de vidrio (Pager + `graphicsLayer`).
- **Botones flotantes de vidrio negro** con borde degradado (`Modifier.blackGlass`) y tarjetas de
  **cristal translúcido** con borde suave y reflejo diagonal (`Modifier.glass`).
- **Controles**: cápsula de vidrio con la canción actual y barra de progreso (toca o arrastra),
  y debajo aleatorio / anterior / play-pausa / siguiente / repetir.
- **Biblioteca** (botón arriba a la izquierda): listado completo con filtros por origen.
  Toca una canción para reproducirla; **desliza a la izquierda** para añadirla a la cola.
- **Carpetas**: en la biblioteca, botón «agregar carpeta» (selector del sistema). Se recorren las
  subcarpetas, se leen título/artista/portada de cada archivo y se recuerdan al reiniciar.
  Selecciona una carpeta (chip) para ver solo sus canciones; el ✕ del chip la quita.
- **Cola de reproducción** (botón arriba a la derecha, con contador): «Reproduciendo ahora»,
  «En cola» (lo que añadiste, suena primero) y «A continuación». Toca para saltar, ✕ para quitar,
  «Vaciar cola» para limpiar lo añadido.
- **Reproducción en segundo plano** con notificación y controles en pantalla de bloqueo
  (`MediaSessionService`).
- Lee la **música local** del teléfono (MediaStore). Si no hay, carga 4 pistas de prueba
  por streaming para que veas la app funcionando.

## Cómo abrirlo

1. Android Studio (Koala o más reciente) → **Open** → carpeta `GlassPlayer`.
2. Deja que sincronice Gradle (generará el wrapper si falta).
3. Ejecuta en un dispositivo/emulador con Android 8.0+ (API 26).

> Las versiones de dependencias están en `app/build.gradle.kts`. Si Android Studio
> sugiere actualizarlas, puedes aceptarlo.

## Estructura

```
app/src/main/java/com/example/glassplayer/
├── MainActivity.kt          Permisos + arranque
├── PlaybackService.kt       ExoPlayer + MediaSession (segundo plano)
├── PlayerViewModel.kt       Estado, biblioteca, carpetas y cola
├── data/Track.kt            Modelo + lectura de MediaStore + pistas demo
├── data/FolderStore.kt      Carpetas elegidas (permiso persistente)
├── data/FolderScanner.kt    Recorre una carpeta y lee metadatos
└── ui/
    ├── PlayerScreen.kt      Pantalla principal y navegación
    ├── LibraryScreen.kt     Biblioteca + carpetas + deslizar a la cola
    ├── QueueScreen.kt       Cola de reproducción
    ├── theme/Theme.kt
    └── components/
        ├── Glass.kt             Modifier.glass() y Modifier.blackGlass()
        ├── BlackGlassButton.kt  Botones flotantes negros (animados)
        ├── GlassDock.kt         Dock de navegación
        ├── ScreenHeader.kt      Cabecera común + separador
        ├── Interactions.kt      pressClick(): pulsación con resorte
        ├── EqBars.kt            Barras de ecualizador
        ├── TrackRow.kt          Fila de canción y miniatura
        ├── AuroraBackground.kt  Fondo + extracción de colores
        ├── CoverCarousel.kt     Carrusel 3D
        └── GlassControlBar.kt   Cápsula de controles
```

## Icono y nombre

- Logo en `res/drawable-nodpi/`: `ic_launcher_foreground.png` (icono adaptable sobre fondo negro),
  `ic_launcher_monochrome.png` (icono temático de Android 13+) y `logo_mark.png` (el que se ve
  dentro de la app: cabecera y marca de agua tenue).
- El nombre que aparece bajo el icono es `app_name` en `res/values/strings.xml`.

## Notas sobre la cola

- La cola real es la lista del propio reproductor: «añadir a la cola» inserta la canción justo
  después de la actual. Con **aleatorio** activado el orden de reproducción puede variar.
- Tocar una canción en la biblioteca reemplaza la lista por la del filtro actual, pero conserva
  lo que ya tenías en cola.

## Personalizar el vidrio

Todo el efecto está en `Modifier.glass(shape, tint, strength)` (`Glass.kt`):
sube/baja `strength` para más o menos opacidad, o cambia `tint` para teñirlo.

Compose no trae *backdrop blur* (difuminar lo que hay detrás del panel).
Aquí se simula con translucidez + borde brillante sobre un fondo ya difuso.
Para blur real puedes añadir la librería **Haze** (`dev.chrisbanes.haze`) y aplicar
`hazeChild` a los paneles; revisa su documentación para la versión actual.

## Próximos pasos sugeridos

- Búsqueda de canciones y playlists guardadas (Room).
- Pantalla "Reproduciendo" a pantalla completa con letra.
- Ecualizador (`android.media.audiofx.Equalizer`) y control de volumen.
- Cola persistente al cerrar la app.
- Tema claro tipo "Story Music" (segunda referencia) como alternativa.
