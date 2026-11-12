# ProjectPolaris

Reproductor de música para Android. Diseño «Polaris Neon»: negro con cuadrícula tenue, tarjetas gris oscuro
con contorno fino y un color de acento neón que eliges en Ajustes (verde por defecto), con titulares en
letra cursiva. También hay un modo claro suave. Kotlin + Jetpack Compose + Media3 (ExoPlayer).

## Qué incluye

- **Inicio**: titular cursivo «¿Qué vas a escuchar hoy?», buscador y botón de aleatorio, fila de artistas,
  tus carpetas (tarjetas con 3 portadas), favoritas y tu colección en tabla (la que suena va rellena con el acento).
- **Mini reproductor**: repetir, anterior, play, siguiente y aleatorio; la flecha abre el reproductor.
- **Barra inferior** con 5 pestañas: Inicio, Explorar, Favoritos, Biblioteca y Buscar.
- **Menú lateral** (botón ☰): MENÚ, BIBLIOTECA (Álbumes, Canciones, Artistas), CARPETAS (con papelera
  y «Agregar carpeta») y Cola.
- **Ajustes** (⚙ arriba o en el menú lateral): **color de acento a tu gusto** (paleta + mezclador de
  tono/intensidad/brillo), modo oscuro/claro, y si el reproductor se pinta con tu color o muestra la
  portada desenfocada. Todo se recuerda.
- **Letras**: botón de letra en el reproductor. Se buscan en [LRCLIB](https://lrclib.net) (gratis, necesita
  internet), se guardan en el teléfono y, si vienen sincronizadas, la línea actual se resalta y
  puedes tocar una línea para saltar a ella.
- **Explorar**: rejilla de artistas o álbumes; su página muestra la portada centrada, «Aleatorio» (con tu acento) y «Me gusta» (marca todas como favoritas).
- **Favoritos**: corazón en cualquier canción; se guardan entre sesiones.
- **Reproductor** (inspirado en la referencia amarilla): fondo del color de acento, portada grande,
  título centrado, progreso, transporte, volumen y acciones (aleatorio, repetir, favorito, letra, salida de audio); la cola está arriba a la derecha.
- **Biblioteca**: filtro por carpeta; **desliza a la izquierda** para añadir a la cola.
- **Cola «Siguiente»**: «Reproduciendo ahora», «En cola» y «A continuación».
- **Carpetas**: selector del sistema; se recorren subcarpetas, se leen título/artista/portada y se
  recuerdan al reiniciar.

## Estructura

```
app/src/main/java/com/example/glassplayer/
├── MainActivity.kt, PlaybackService.kt, PlayerViewModel.kt
├── data/                 Track, FolderStore, FolderScanner, FavoriteStore, SettingsStore,
│                         Lyrics (+ parser LRC), LyricsRepository (LRCLIB + caché)
└── ui/
    ├── PlayerScreen.kt   Navegación, menú lateral, barra inferior y reproductor
    ├── HomeScreen.kt, ExploreScreen.kt, FavoritesScreen.kt, LibraryScreen.kt,
    │   SearchScreen.kt, QueueScreen.kt, DetailScreen.kt, SettingsScreen.kt, Collections.kt
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

## Tipografías

Poppins (texto) y Mr De Haviland (titulares cursivos), ambas con licencia SIL OFL (Google Fonts), en `app/src/main/res/font`.
