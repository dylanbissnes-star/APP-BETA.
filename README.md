# GlassPlayer

Base de reproductor de música para Android con estilo **glassmorphism** (cristal esmerilado claro).
Kotlin + Jetpack Compose + Media3 (ExoPlayer).

## Qué incluye

- **Cristal esmerilado (Frosted Glass)**: un panel grande de cristal claro y translúcido sobre un
  fondo de color con **esferas brillantes** que flotan despacio. Los colores del fondo salen de la
  portada que suena (`SphereBackground.kt`); paleta en `ui/theme/Frost.kt`.
- **Desenfoque real** (Android 12+): el panel, el dock y el mini reproductor desenfocan lo que hay
  detrás (`Backdrop.kt`, capas gráficas de Compose, sin librerías). En versiones anteriores quedan
  translúcidos.
- **Reproductor** (Inicio): portada grande con marco de cristal (carrusel con profundidad), título y
  artista centrados, anterior / play-pausa / siguiente como símbolos blancos sin fondo, barra de
  progreso fina con tiempos, y una pastilla de cristal con aleatorio, repetir, salida de audio
  (selector del sistema, con volumen) y cola.
- **Mini reproductor** flotante (portada, título, pausa y siguiente) en Buscar, Biblioteca y Cola;
  al tocarlo vuelves al reproductor.
- **Buscar**: campo de cristal; busca por título, artista o álbum. Filas con triángulo de reproducir.
- **Dock flotante** (Inicio / Buscar / Biblioteca / Cola): la pestaña activa se expande en una
  píldora; la cola muestra un contador.
- **Biblioteca**: rejilla de tarjetas pequeñas con las fuentes (todas, dispositivo, demo y cada
  carpeta) y debajo la lista de canciones. Toca para reproducir; **desliza a la izquierda** para
  añadir a la cola.
- **Carpetas**: botón «agregar carpeta» (selector del sistema). Se recorren las subcarpetas, se leen
  título/artista/portada de cada archivo y se recuerdan al reiniciar. Selecciona una tarjeta de
  carpeta para ver solo sus canciones; el ✕ la quita.
- **Cola de reproducción**: «Reproduciendo ahora», «En cola» (lo que añadiste, suena primero) y
  «A continuación». Toca para saltar, ✕ para quitar, «Vaciar cola» para limpiar lo añadido.
- **Animaciones**: botones con rebote al pulsar, transiciones suaves entre pantallas, esferas en
  movimiento lento y barras de ecualizador en la canción que suena.
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
        ├── Glass.kt             Modifier.glass() y Modifier.glassStrong()
        ├── GlassButton.kt       Botones de cristal, iconos simples y pastillas
        ├── GlassDock.kt         Dock de navegación
        ├── MiniPlayer.kt        Mini reproductor flotante
        ├── ScreenHeader.kt      Cabecera común
        ├── Interactions.kt      pressClick(): pulsación con resorte
        ├── EqBars.kt            Barras de ecualizador
        ├── TrackRow.kt          Fila de canción y miniatura
        ├── SphereBackground.kt  Fondo de esferas + extracción de colores
        ├── CoverCarousel.kt     Carrusel de portadas
        └── PlayerControls.kt    Transporte, progreso y pastilla de acciones
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
