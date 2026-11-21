# Planificación de las versiones · DOPOSoccer

**Autores:** César Daniel Morales Sandoval y Miguel de la Peña · DOPO 2026-2

Cada versión se trabaja en miniciclos cortos. En cada miniciclo se diseña, se programa y se prueba una parte pequeña,
de modo que el juego siempre se pueda ejecutar.

## Versión 1: Presentación + Dominio

**Objetivo:** tener el juego básico jugable de jugador contra jugador, con una interfaz que se ve como el Head Soccer
original y un dominio separado de la interfaz.

| Miniciclo | Qué se hace |
|---|---|
| 1 | Cancha, balón con física, arcos y marcador en el dominio, con sus pruebas. |
| 2 | Jugador (mover, saltar, patear) y los cuatro personajes con sus poderes. |
| 3 | Partido: cuenta regresiva, tiempo o goles, muerte súbita, pausa y puntos. |
| 4 | Pantallas: portada, modalidad, personajes, configuración del partido y configuración general. |
| 5 | Pantalla del partido (dibujo, teclado, sonidos) y manejo de excepciones. |

## Versión 2: Persistencia + Jugador contra máquina

**Objetivo:** que el progreso no se pierda y que se pueda jugar solo. Incluye vida, daño y efectos de los poderes,
mejoras de estadísticas con puntos, guardar y abrir partidas, y la máquina con cuatro perfiles.

| Miniciclo | Qué se hace |
|---|---|
| 1 | Vida, daño de patadas y poderes, aturdimiento y efectos (Aura Azul, Rayo, Tornado, Ilusión). |
| 2 | Puntos y mejoras de estadísticas (el costo se duplica en cada mejora). |
| 3 | Guardar y abrir partidas (archivo `.dop`) y guardar el progreso (puntos, mejoras, configuración). |
| 4 | Máquina: clase base con las mismas acciones del teclado y los perfiles Torpe (azar), Ofensiva (anotar), Defensiva (defender el arco) y Experta (según el estado del partido). |
| 5 | Pantallas nuevas (perfil de la máquina, abrir partida, confirmaciones), pruebas y documentación. |

## Versión 3: Más contenido

**Objetivo:** ampliar el juego con nuevos personajes y poderes, y hacer que el idioma y la música realmente funcionen.

| Miniciclo | Qué se hace |
|---|---|
| 1 | Nuevos personajes y poderes (la lista de personajes ya está pensada para crecer). |
| 2 | Idioma: textos del juego en español e inglés. |
| 3 | Música de fondo y más efectos de sonido. |
| 4 | Pruebas y balance de poderes. |

## Versión 4: Pulido y entrega final

**Objetivo:** dejar el juego estable, probado y bien documentado.

| Miniciclo | Qué se hace |
|---|---|
| 1 | Revisión de calidad: cobertura de pruebas y análisis estático. |
| 2 | Corrección de errores y ajuste de la dificultad de la máquina. |
| 3 | Documentación final y diagramas UML actualizados. |

## Requisitos que no se van a incluir (por ahora)

- **Idioma y música funcionales:** la configuración los guarda, pero todavía no cambian el juego (queda para la versión 3).

