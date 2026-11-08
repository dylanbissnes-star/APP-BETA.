# GlassPlayer

Reproductor de música para Android con una UI oscura y sólida (tarjetas con borde fino, sin cristal),
inspirada en apps de música modernas. Kotlin + Jetpack Compose + Media3 (ExoPlayer).

## Qué incluye

- **Inicio**: banner «Tendencias» con degradado de la portada (Reproducir / Ver lista), tus carpetas
  como tarjetas con 3 portadas, tabla «Top» de canciones (#, título, artista, tiempo, corazón) y
  lista numerada de artistas.
- **Barra inferior** con 5 pestañas: Inicio, Explorar, Favoritos, Biblioteca y Buscar.
- **Menú lateral** (botón ☰): MENÚ, BIBLIOTECA (Álbumes, Canciones, Artistas), CARPETAS (con papelera
  y «Agregar carpeta») y Cola.
- **Modo oscuro/claro**: interruptor siempre visible arriba (oscuro por defecto, se recuerda).
- **Explorar**: rejilla de artistas o álbumes; al tocar uno se abre su página con Reproducir / Aleatorio.
- **Favoritos**: corazón en cualquier canción; se guardan entre sesiones.
- **Reproductor**: portada grande sobre su propia imagen desenfocada, título con corazón, progreso,
  transporte, volumen y acciones (aleatorio, repetir, salida de audio, cola).
- **Mini reproductor** sobre la barra inferior, con progreso.
- **Biblioteca**: filtro por carpeta; **desliza a la izquierda** para añadir a la cola.
- **Cola «Siguiente»**: «Reproduciendo ahora», «En cola» y «A continuación».
- **Carpetas**: selector del sistema; se recorren subcarpetas, se leen título/artista/portada y se
  recuerdan al reiniciar.

## Estructura

```
app/src/main/java/com/example/glassplayer/
├── MainActivity.kt, PlaybackService.kt, PlayerViewModel.kt
├── data/                 Track, FolderStore, FolderScanner, FavoriteStore
└── ui/
    ├── PlayerScreen.kt   Navegación, menú lateral, barra inferior y reproductor
    ├── HomeScreen.kt, ExploreScreen.kt, FavoritesScreen.kt, LibraryScreen.kt,
    │   SearchScreen.kt, QueueScreen.kt, DetailScreen.kt, Collections.kt
    ├── theme/            Theme.kt y Frost.kt (paleta oscura/clara)
    └── components/       AppDrawer, BottomTabBar, MiniPlayer, TrackRow, PlayerControls,
                          CoverCarousel, PageBackground, Buttons, Surfaces (card), etc.
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

## Próximos pasos sugeridos

- Playlists propias (crear y añadir canciones) con Room.
- Pantalla "Reproduciendo" a pantalla completa con letra.
- Ecualizador (`android.media.audiofx.Equalizer`) y control de volumen.
- Cola persistente al cerrar la app.
- Tema claro tipo "Story Music" (segunda referencia) como alternativa.
