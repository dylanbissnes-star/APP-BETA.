# GlassPlayer

Base de reproductor de música para Android con estilo **glassmorphism**.
Kotlin + Jetpack Compose + Media3 (ExoPlayer).

## Qué incluye

- **Fondo aurora** animado que toma los colores de la portada actual (Palette).
- **Carrusel 3D** de portadas en tarjetas de vidrio (Pager + `graphicsLayer`).
- **Barra cápsula de vidrio**: anterior / play-pausa / siguiente, mini tarjeta con
  progreso (toca para saltar), aleatorio y repetir.
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
├── PlayerViewModel.kt       Estado y acciones del reproductor
├── data/Track.kt            Modelo + lectura de MediaStore + pistas demo
└── ui/
    ├── PlayerScreen.kt      Pantalla principal
    ├── theme/Theme.kt
    └── components/
        ├── Glass.kt             Modifier.glass(): el efecto vidrio
        ├── AuroraBackground.kt  Fondo + extracción de colores
        ├── CoverCarousel.kt     Carrusel 3D
        └── GlassControlBar.kt   Cápsula de controles
```

## Personalizar el vidrio

Todo el efecto está en `Modifier.glass(shape, tint, strength)` (`Glass.kt`):
sube/baja `strength` para más o menos opacidad, o cambia `tint` para teñirlo.

Compose no trae *backdrop blur* (difuminar lo que hay detrás del panel).
Aquí se simula con translucidez + borde brillante sobre un fondo ya difuso.
Para blur real puedes añadir la librería **Haze** (`dev.chrisbanes.haze`) y aplicar
`hazeChild` a los paneles; revisa su documentación para la versión actual.

## Próximos pasos sugeridos

- Pantalla de lista/búsqueda de canciones y playlists (Room).
- Pantalla "Reproduciendo" a pantalla completa con letra.
- Ecualizador (`android.media.audiofx.Equalizer`) y control de volumen.
- Cola persistente al cerrar la app.
- Tema claro tipo "Story Music" (segunda referencia) como alternativa.
