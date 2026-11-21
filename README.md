# DOPO SOCCER · DOPOSoccer

Hola. Este es nuestro proyecto de DOPO 2026-2: una versión de **Head Soccer**, ese juego de fútbol de cabezones, hecha
en Java con Swing. Lo estamos construyendo entre los dos, por versiones. Empezó con la **Versión 1: Presentación +
Dominio** y ya trae la **Versión 2**: vida, daño y efectos de los poderes, mejoras de estadísticas, jugar contra la
máquina y guardar/abrir partidas.

**Autores:** César Daniel Morales Sandoval y Miguel de la Peña **Curso:** Desarrollo Orientado por Objetos (DOPO),
Escuela Colombiana de Ingeniería Julio Garavito, 2026-2

---

## ¿Qué se puede hacer?

Se puede jugar un partido de **jugador contra jugador** en un mismo teclado o de **jugador contra máquina**, con el estilo del juego original: estadio
con público, arcos con red, cabezones sencillos y planos, sin expresiones, y el balón girando.

- **Menús:** portada (con confirmación al salir), elección de modalidad, elección de personaje para cada jugador,
  mejoras de estadísticas, configuración del partido y configuración general (sonido, música e idioma).
- **Personajes:** Corea del Sur, Camerún, Nigeria y Estados Unidos. Cada uno tiene su tiro de poder: Aura Azul, Rayo,
  Tornado e Ilusión (que reparte siete balones falsos).
- **Partidos:** por tiempo (de 1 a 10 minutos) o por goles (de 1 a 20). Si el tiempo se acaba empatado, se juega muerte
  súbita: gana el siguiente gol.
- **Poder:** la barra se llena sola en 10 segundos y además sube un cuarto cada vez que te hacen un gol. Cuando está
  llena la activas, y el siguiente toque al balón lanza el tiro especial (para Nigeria, el balón se carga delante del
  pie y no de la frente).
- **Vida y daño:** cada jugador tiene vida (100 con Survival 1). Una patada que le llega al rival le quita
  10 x multiplicadorKick; un tiro de poder que lo golpea le quita el daño de su poder x multiplicadorKick. Si el tiro
  entra al arco sin tocar al rival, el gol cuenta pero no hay daño ni efecto.
- **Efectos de los poderes:** Aura Azul empuja al rival hacia su propio arco (1,5 s), Rayo lo electrocuta (3 s: no salta
  y va a la mitad de velocidad), Tornado lo lanza al aire inconsciente (3 s) e Ilusión lo confunde con ocho balones
  iguales (2,5 s). Los efectos no se acumulan: el nuevo reemplaza al anterior.
- **Aturdimiento:** cuando la vida llega a cero, el jugador no se mueve, no salta ni patea durante 3 segundos y después
  recupera toda su vida.
- **Puntos y mejoras:** el ganador se lleva 100 y el perdedor 25. Con esos puntos se sube Velocidad, Salto, Patada o
  Vida (de 1 a 10); la primera mejora de cada una cuesta 500 y cada siguiente cuesta el doble. Cada punto de mejora
  suma un 10 %. Los puntos y las mejoras se conservan de un partido a otro mientras el juego esté abierto.
- **Jugador contra máquina:** la máquina maneja al jugador 2 con las mismas acciones que el teclado (mover, saltar,
  patear y usar el poder). Se escoge su perfil: **Torpe** (todo al azar), **Ofensiva** (su prioridad es anotar),
  **Defensiva** (su prioridad es defender su arco) o **Experta** (decide según el estado del partido: defiende si el
  balón está en su zona, ataca si no, y usa el poder cuando conviene).
- **Guardar y abrir partidas:** en la pausa, GUARDAR PARTIDA guarda todo el partido (marcador, tiempo, posiciones,
  barras, vidas, efectos y la máquina) en un archivo `.dop`; en la portada, ABRIR PARTIDA lo recupera y queda en pausa.
- **Progreso:** los puntos, las mejoras y la configuración se guardan solos en `datos/progreso.properties` y se cargan
  al abrir el juego.
- **Extras:** cuenta regresiva, celebración con confeti, pausa, revancha y tres tamaños de ventana.

## ¿Qué todavía no está?

Lo dejamos para las próximas versiones, y preferimos decirlo claro:

- La música y el idioma se guardan en la configuración, pero todavía no cambian nada en el juego.

## Cómo correrlo (requisitos)

Necesitas **JDK 24** y **Maven 3.9+**. Desde la carpeta del proyecto:

mvn test mvn package java -jar target/doposoccer-1.0.jar

El primero corre las pruebas, el segundo arma el `.jar` y el tercero abre el juego.

## Controles

|         | Jugador 1 | Jugador 2 |
|---------|-----------|-----------|
| Moverse | A / D     | ← / →     |
| Saltar  | W         | ↑         |
| Patear  | F         | . (punto) |
| Poder   | G         | , (coma)  |

Con `ESC` o `P` pausas el partido.

## Cómo está organizado

Separamos el proyecto en dos paquetes, para que cada parte tenga una sola responsabilidad:

```
src/main/java
├── domain         las reglas del juego (no sabe nada de ventanas)
└── presentation   lo que se ve y se toca (solo le pregunta al dominio)
```

La idea es sencilla: la interfaz le pasa al dominio las teclas que aprietan los jugadores, le dice "avanza un 1/60 de
segundo" y después dibuja lo que el dominio le cuenta. Así las reglas se pueden probar sin abrir una sola ventana.

Los personajes y los poderes son clases abstractas (`Personaje` y `Poder`) con una subclase por país. Por eso agregar un
personaje nuevo es crear su clase y sumarla a la lista de `Juego`; no hay que tocar nada más.

Si algo sale mal en el dominio, lanzamos una `DOPOSoccerExcepcion` y la guardamos en `logs/errores.log`, mientras el
jugador solo ve un mensaje amable.

## Pruebas

Tenemos 130 pruebas de unidad en `src/test/java/domain`. Cubren la cancha, el balón, los arcos, el marcador, el jugador,
el partido, los poderes, las máquinas y la clase `Juego`, incluyendo la vida, el daño, los efectos, el aturdimiento y las mejoras, y los
casos en los que algo debe fallar (minutos fuera de rango, un idioma que no existe, empezar contra la máquina sin perfil, abrir un archivo dañado,
mejorar sin puntos suficientes, etc.). También se prueba cada perfil de máquina, guardar y abrir una partida y
conservar el progreso.

## Uso de inteligencia artificial

Usamos Claude como apoyo en este proyecto: para entender el enunciado, plantear la estructura de clases, generar una
primera base del código y del dibujo del estadio, y armar las pruebas y los documentos. Las partes más complejas del
código (como la física de los choques, la carga del tiro de poder o el poder Ilusión) llevan un comentario que empieza
con "Recomendación de la IA generativa para...", para que se vea de dónde vino cada idea.

## Tecnologías

- Java 24
- Swing
- Maven
- JUnit 5
- JaCoCo
- PMD
- Git
- GitHub
- Astah UML

---

## Requisitos

- Java 24
- Maven 3.9 o superior
- IntelliJ IDEA (recomendado)

---

## Estructura del Proyecto

```text
src
├── main
│   ├── java
│   │   ├── domain
│   │   ├── presentation
│   │   ├── persistence
│   │   ├── exceptions
│   │   └── util
│   │
│   └── resources
│       ├── images
│       ├── sounds
│       ├── config
│       ├── languages
│       └── saves
│
└── test
    └── java

docs
├── entregas
├── reportes
└── capturas

uml
├── astah
├── png
└── pdf

logs
```

---

## Diagramas UML

Los diagramas utilizados durante el diseño del proyecto se encuentran en:

```text
uml/
```

Incluyen:

- Diagrama de Dominio
- Diagrama de Presentación
- Diagrama de Paquetes
- Diagramas de Secuencia

---

## Calidad del Software

### Pruebas Unitarias

Las pruebas unitarias se ejecutan mediante:

```bash
mvn test
```

Actualmente el proyecto cuenta con pruebas para las clases principales del dominio, incluyendo jugadores, balón, cancha,
porterías, marcador, partido, poderes y la clase principal del juego.

### Cobertura de Código (JaCoCo)

El proyecto utiliza JaCoCo para medir la cobertura de las pruebas unitarias y generar reportes de cobertura del código.

### Análisis Estático (PMD)

El proyecto utiliza PMD para detectar:

- Código duplicado
- Variables innecesarias
- Métodos excesivamente largos
- Problemas de mantenibilidad
- Malas prácticas de programación

### Construcción del Proyecto

Compilar y empaquetar:

```bash
mvn package
```

Ejecutar pruebas:

```bash
mvn test
```

Ejecutar verificación completa:

```bash
mvn verify
```