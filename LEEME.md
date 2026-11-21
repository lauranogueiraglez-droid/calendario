# Adviento: calendario de bloques para Izan

24 bloques que se pican uno cada mañana de diciembre. Cada bloque tiene su propio minijuego. Al abrir la app sale una pantalla de carga con el avatar de Izan y luego otra que le da la bienvenida.

En esta carpeta hay dos versiones del mismo calendario:

- **Android**: la app (APK), con avisos cada mañana.
- **iPhone**: una página web (carpeta `docs`) que se añade a la pantalla de inicio y funciona como una app.

## 1. Subirlo a GitHub (sirve para las dos)

1. Crea un repositorio en github.com (puede ser privado para el APK, pero **público si quieres la versión de iPhone gratis**).
2. Pulsa «uploading an existing file» y sube **todo el contenido** de esta carpeta, incluidas las carpetas `.github` y `docs` (desde un ordenador es más fácil).

## 2. Android: conseguir el APK

1. En la pestaña **Actions** se lanza sola «Compilar APK» (tarda unos 3–5 minutos).
2. Cuando termine en verde, entra y descarga **Adviento-apk**, que es un .zip con `app-debug.apk` dentro.
3. Pasa el APK al móvil, ábrelo y acepta «instalar apps de origen desconocido».
4. La primera vez que se abre, la app pide permiso para enviar notificaciones: hay que **permitirlo**.

Avisos: cada mañana a las 10:30 (hora de España), del 1 al 24 de diciembre, llega «¡Bloque X desbloqueado!». En Ajustes se pueden apagar, y el botón «Probar aviso» manda uno al momento para comprobarlo.

También puedes abrir la carpeta con Android Studio (*File › Open*) y hacer *Build › Build APK(s)*.

## 3. iPhone: la app web

1. En el repositorio, ve a **Settings › Pages**.
2. En «Branch» elige `main` y la carpeta `/docs`, y pulsa **Save**.
3. Al cabo de un minuto te dará una dirección del tipo `https://tu-usuario.github.io/nombre-del-repo/`.
4. Izan la abre en **Safari** y pulsa **Compartir › Añadir a pantalla de inicio**. Le queda un icono con su avatar y se abre a pantalla completa.
5. **Avisos en iPhone:** abajo del todo de la página está «Activar los avisos de cada mañana en mi calendario». Al tocarlo se añaden 24 recordatorios a su app Calendario, a las 10:30 de cada día.

Importante: la versión de iPhone muestra el contenido que lleve `docs/index.html`. Cuando termines de rellenar el calendario, pásaselo a Claude para que prepare la versión final con todo dentro, o usa «Enviar» y que Izan lo importe con «Importar un calendario».

## Uso

- **Minijuegos**: cada bloque tiene el suyo (24 distintos: picar, memoria, gemas, secuencia, tres en raya, laberinto, serpiente, sopa de letras, pozo de lava, pesca, parkour…). En el editor puedes cambiar el de cada bloque, poner una palabra secreta o el número del cofre. En Ajustes puedes quitarlos todos. En el día de la pesca, lo último que se pesca es la foto de ese día.
- **Inventario**: cada bloque abierto da un objeto pixelado. El bloque 24 solo se abre con los otros 23 en el inventario.
- **Música**: por defecto una música tranquila de piano (original), y en Ajustes se puede cambiar a villancicos en 8 bits. Tiene un botón para quitarla.
- **Cielo**: cambia con la hora (día, atardecer y noche con luna y estrellas). En Ajustes puedes fijar uno.
- **Editar**: toca un bloque y escribe título, mensaje, foto, «Vale por…» y un enlace (vídeo, canción, audio en Drive…).
- **Ajustes**: para quién es, quién lo firma, texto de bienvenida, año, cielo, pantalla de inicio, avisos, sonido y **PIN de edición**.
- **Enviar**: comparte el calendario como archivo `.json` por WhatsApp, Telegram, Drive…
- Quien lo recibe abre el archivo con «Adviento» o usa «Importar un calendario».
- Si pusiste PIN, esa persona solo puede abrir bloques: no puede editar ni ver los que aún no tocan.

Las puertas se abren a las 00:00 de cada día (hora de España). Las letras pixeladas se descargan de Google Fonts con internet; sin conexión se usan las del sistema.
