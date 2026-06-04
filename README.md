# Pradera de Flores — Proyecto Integrador de Graficación (SCC-1010)

Una pradera tridimensional con 35 tipos de flores diferentes, terreno fractal con montañas, cielo procedural con sol y nubes, física, sombras dinámicas, viento, interacción al caminar entre las flores, y una espectacular lluvia de flores que cae del cielo al presionar la tecla `M`. Construido con **Java 17** y **jMonkeyEngine 3.6.1**.

Este documento te guía paso a paso para tener el proyecto funcionando desde cero, te explica exactamente qué tecla hace qué, te dice cómo solucionar los errores más comunes, y al final te muestra cómo cada archivo del proyecto cubre uno o varios subtemas del temario oficial de la materia.

---

## 1. ¿Qué necesitas instalar antes de tocar el proyecto?

Antes de abrir cualquier cosa, asegúrate de tener instalado lo siguiente en tu computadora. Si ya tienes todo, salta al paso 2.

### 1.1 JDK 17 (Java Development Kit)

El proyecto usa Java 17 porque jMonkeyEngine 3.6.1 requiere mínimo Java 11 y Java 17 es la versión LTS más estable hoy en día. Si tienes Java 11, 17, 21 o más reciente, también funciona. Si tienes Java 8 NO va a compilar.

Descarga desde el sitio oficial de **Eclipse Temurin** (es gratis y open source, recomendado para estudiantes):

> https://adoptium.net/temurin/releases/?version=17

En Windows ejecuta el instalador `.msi` y déjalo marcado para "Set JAVA_HOME" y "Add to PATH". En macOS usa el `.pkg`. En Linux baja el `.tar.gz`, descomprímelo y agrégalo al PATH manualmente.

Para verificar que quedó bien instalado, abre una **terminal nueva** (CMD en Windows, Terminal en Mac/Linux) y escribe:

```
java -version
javac -version
```

Ambos deben mostrar algo como `openjdk version "17.x.x"`. Si te dice "comando no encontrado", el PATH no quedó configurado: reinicia la computadora y vuelve a intentarlo.

### 1.2 Apache Maven

Maven es la herramienta que se encarga de descargar automáticamente las librerías de jMonkeyEngine. Sin Maven tendrías que bajar manualmente como 15 archivos `.jar` y eso es una pesadilla.

Descarga desde:

> https://maven.apache.org/download.cgi

Elige el "Binary zip archive" (`apache-maven-3.x.x-bin.zip`), descomprímelo en una carpeta sencilla como `C:\maven` en Windows o `/opt/maven` en Linux/Mac, y agrega `C:\maven\bin` al PATH del sistema. Reinicia la terminal y verifica:

```
mvn -version
```

Si te aparece la versión de Maven y la de Java que detecta, listo.

### 1.3 IntelliJ IDEA Community Edition

Es el IDE recomendado porque entiende Maven nativamente, tiene autocompletado excelente, y la edición Community es gratis para siempre. NetBeans también funciona pero el flujo es más enredado.

> https://www.jetbrains.com/idea/download/?section=windows

Baja la edición **Community** (la gratis), instálala con la configuración por defecto.

### 1.4 Drivers actualizados de tu tarjeta gráfica

El motor usa OpenGL 3.2 o superior. Si tu computadora tiene más de 5 años o nunca actualizaste los drivers, conviene hacerlo antes de ejecutar el proyecto. Si tienes una GPU NVIDIA o AMD dedicada, instala los drivers desde su sitio oficial; si es Intel integrada, ve a Configuración → Windows Update → drivers opcionales.

---

## 2. Abrir el proyecto por primera vez

Tienes la carpeta `PraderaFlores/` en tu máquina. Esa carpeta contiene un archivo `pom.xml`, una carpeta `src/`, y este `README.md`. No hay binarios incluidos: Maven los va a bajar solo.

### 2.1 Abrir en IntelliJ

Abre IntelliJ IDEA, en la pantalla de bienvenida elige **Open**, navega hasta la carpeta `PraderaFlores/` y selecciónala. Importante: elige la carpeta que contiene el `pom.xml`, no una subcarpeta.

IntelliJ va a detectar automáticamente que es un proyecto Maven y te mostrará un aviso "Maven project detected — Import as Maven project". Acepta. Si no aparece el aviso, busca el archivo `pom.xml` en el panel izquierdo, click derecho sobre él, y elige `Add as Maven Project`.

En la esquina inferior derecha verás una barra de progreso "Resolving Maven dependencies" o "Indexing". La **primera vez** esto puede tardar de 3 a 10 minutos porque Maven está bajando jMonkeyEngine y todas sus dependencias (unos 80 MB en total). Espera a que termine antes de hacer cualquier cosa. Tómate un café.

Cuando termine, en la barra de estado abajo no debe haber errores rojos. Si los hay, ve a la sección 5 (solución de problemas) más abajo.

### 2.2 Configurar el SDK del proyecto

Una vez importado, IntelliJ a veces no sabe qué versión de Java usar. Ve a:

`File → Project Structure → Project`

En "SDK" elige la versión 17 (o superior) que instalaste. En "Language level" elige `17 - Sealed types, always-strict floating-point semantics`. Click en Apply y OK.

---

## 3. Ejecutar el proyecto

Hay dos formas. La fácil y la de terminal. Te cuento ambas.

### 3.1 Forma fácil: desde IntelliJ

Navega en el panel izquierdo hasta `src/main/java/com/farid/pradera/Main.java` y ábrelo. A la izquierda del método `public static void main(String[] args)` vas a ver un triángulo verde pequeño. Click derecho sobre él y elige `Run 'Main.main()'`.

La primera vez IntelliJ va a compilar todo el código (medio minuto más o menos) y después debería abrirse una ventana de 1600x900 con la pradera. Si todo salió bien, vas a ver flores, terreno verde y cielo azul.

Si quieres más memoria asignada (el terreno fractal y las flores pueden consumir bastante RAM), haz click en la flechita junto al botón Play arriba a la derecha → `Edit Configurations...` → en el campo `VM options` agrega:

```
-Xmx2g -Xms512m
```

Esto le da a la JVM hasta 2 GB de memoria, que es más que suficiente.

### 3.2 Forma de terminal: con Maven directamente

Abre una terminal, navega a la carpeta del proyecto y ejecuta:

```
mvn clean compile
mvn exec:java -Dexec.mainClass="com.farid.pradera.Main"
```

Si quieres generar un JAR autoejecutable que puedas correr con doble click (útil para entregar el proyecto a tu profe):

```
mvn clean package
```

Esto va a crear el archivo `target/PraderaFlores-1.0.0.jar`. Lo ejecutas con:

```
java -Xmx2g -jar target/PraderaFlores-1.0.0.jar
```

---

## 4. Controles dentro de la aplicación

Una vez que la ventana esté abierta, el cursor del ratón desaparece y la cámara se controla con el movimiento del mouse. Estas son las teclas:

`W` adelante, `A` izquierda, `S` atrás, `D` derecha. Movimiento de primera persona clásico.

`Mouse` para mirar alrededor. Pitch limitado a 80 grados arriba y abajo para que no te voltees de cabeza.

`Shift` (izquierdo) para correr; cuádruplica casi la velocidad.

`Espacio` para saltar.

`M` para disparar la **lluvia de flores en cascada**. Cae una nube de unas 250 flores del cielo siguiendo curvas Bézier individuales con rotación y wobble. Puedes presionarla cuantas veces quieras: las cascadas se acumulan.

`R` para resetear la posición del jugador si se cae fuera del mapa (raro pero puede pasar).

`ESC` para salir de la aplicación.

Al pasar caminando entre las flores, las que estén a menos de 2.5 metros de ti se inclinan hacia donde tú vas, simulando que las empujas con las piernas. Es un oscilador armónico amortiguado por cada flor, así que después de pasar siguen oscilando un par de segundos antes de quedarse quietas. Lo verás más claro caminando despacio entre flores densas.

---

## 5. Solución de los errores más comunes

### "No goal specified" al ejecutar mvn

Te falta especificar la fase. Usa `mvn compile` o `mvn clean compile`, no solo `mvn`.

### "java: error: invalid source release: 17"

Tu Java instalado es menor a 17. Instala JDK 17 o cambia en el `pom.xml` la propiedad `maven.compiler.source` y `maven.compiler.target` a la versión que tengas (por ejemplo `11`). Java 11 también funciona con jME3 3.6.

### "Could not initialize class org.lwjgl..." o similar al ejecutar

Esto casi siempre es porque tu sistema no encuentra las librerías nativas de OpenGL. Soluciones en orden:

Primero, actualiza los drivers de tu GPU. Si tienes integrada Intel en una laptop con NVIDIA híbrida, asegúrate que la app esté usando la NVIDIA. En Windows: panel de control NVIDIA → Configuración 3D → Configuración del programa → agrega `java.exe` y elige "Procesador NVIDIA de alto rendimiento".

Segundo, si estás en Linux, instala las librerías de OpenGL del sistema: `sudo apt install libgl1-mesa-glx libopenal1`.

Tercero, si estás en macOS con chip Apple Silicon (M1/M2/M3), jME3 3.6.1 ya soporta nativamente ARM64 con LWJGL3, pero asegúrate de que el JDK que instalaste sea ARM64 nativo (no x86_64 corriendo bajo Rosetta).

### La ventana abre pero todo se ve negro o todo blanco

Es problema de iluminación o de drivers. Intenta primero actualizar drivers. Si persiste, abre `GestorLuces.java` y baja la intensidad de la luz ambiente y direccional a la mitad — quizás esté saturando.

### Frame rate bajísimo (menos de 20 FPS)

Tu GPU está sufriendo con las 480 flores y los efectos de postprocesado. Soluciones, de más suave a más drástica: en `PraderaApp.java` baja `FLORES_TOTALES` de 480 a 200; en `GestorLuces.java` desactiva el SSAOFilter (es el más caro); reduce la resolución de las sombras de 2048 a 1024 en `GestorLuces.java`.

### "Cannot find symbol" en alguna clase de jME3

Maven no terminó de bajar las dependencias o se interrumpió. En IntelliJ click derecho en el `pom.xml` → `Maven` → `Reload project`. O en terminal: `mvn dependency:resolve`.

---

## 6. Estructura del proyecto y cómo cada pieza cubre el temario

El temario oficial de Graficación (SCC-1010) tiene cinco temas con sus subtemas. Aquí te explico qué archivo cubre cada uno, para que cuando tu profe te pregunte "¿dónde está el subtema 2.3.1?" tengas la respuesta lista.

### Tema 1: Introducción a la graficación

El **subtema 1.1 (historia y evolución)** y el **1.2 (áreas de aplicación)** son teóricos: los expones en clase o en un anexo escrito. Una pista para la historia: arranca con Ivan Sutherland y Sketchpad (1963), pasa por el sombreado Gouraud (1971) y Phong (1975), llega al rasterizado moderno, y termina con el ray tracing en tiempo real de la última década.

El **subtema 1.3 (aspectos matemáticos)** lo cubre la carpeta `matematicas/`. Ahí están `TransformacionesMatriz.java` con todas las matrices de transformación calculadas a mano (traslación, escala, rotación, sesgado), `CurvaBezier.java` con la fórmula cúbica y el algoritmo de De Casteljau, `CurvaBSpline.java` con las cuatro funciones base de la B-spline cúbica uniforme, `Fractal.java` con la curva de Koch, ruido fBm de Perlin, y el conjunto de Mandelbrot, y `NormalizadorModelos.java` que realiza el cálculo de normalización de escala y desfase de altura utilizando los límites geométricos de los modelos (`BoundingBox` y `BoundingSphere`).

El **subtema 1.4 (modelos de color RGB, CMY, HSV, HSL)** está completo en `matematicas/ConvertidorColor.java`. Esa clase tiene los seis pares de conversiones bidireccionales con la matemática explícita: por ejemplo, para RGB → HSV calculamos el max y min de los canales, derivamos la luminosidad, la saturación con la fórmula de la diferencia, y el hue por sextante.

El **subtema 1.5 (representación y trazo de líneas y polígonos)** lo cubre `flores/GeneradorPetalo.java`, donde cada contorno de pétalo se construye como un polígono cerrado a partir de una curva Bézier muestreada. También el terreno (TerrainQuad) es una malla triangulada que demuestra el trazo de polígonos planos a gran escala.

El **subtema 1.6 (formatos de imagen y procesamiento de mapas de bits)** lo cubre `cielo/GestorCielo.java`, que genera texturas procedurales (gradiente vertical para el skybox, mapa de nubes con alpha animado por fBm) escribiendo pixel por pixel en un buffer de bytes en formato RGBA estándar. Es exactamente el mismo procedimiento que usarías para escribir un archivo BMP manualmente, solo que en vez de guardarlo a disco se lo entregas directamente al motor como Texture2D.

### Tema 2: Graficación 2D

Los **subtemas 2.1 (transformaciones 2D) y 2.2 (representación matricial)** están en `matematicas/TransformacionesMatriz.java`. Esa clase implementa traslación, escala, rotación, y sesgado en 2D usando matrices 3×3 con coordenadas homogéneas (es decir, el tercer renglón es siempre `[0 0 1]`), y también combinaciones por multiplicación de matrices.

El **subtema 2.3.1 (curvas Bézier)** está en `matematicas/CurvaBezier.java`. La uso en dos lugares visibles: para curvar los tallos de las flores (`flores/FabricaFlores.java` construye cada segmento del tallo a lo largo de una Bézier cúbica), y para definir la trayectoria individual de cada flor que cae durante la cascada (`animacion/AnimacionCascada.java`).

El **subtema 2.3.2 (B-spline)** está en `matematicas/CurvaBSpline.java`. La uso en los contornos de los pétalos: en lugar de polígonos rectos, cada contorno se suaviza con B-spline cúbica uniforme para que el borde no se vea pixeleado.

El **subtema 2.4 (fractales)** lo cubre `matematicas/Fractal.java` con tres implementaciones: la curva de Koch (un fractal lineal clásico), el ruido fBm (Brownian Motion fraccional, suma de octavas de ruido Perlin) usado para generar tanto las alturas del terreno como las nubes, y el conjunto de Mandelbrot por si quieres mostrar una visualización fractal pura.

El **subtema 2.5 (uso y creación de fuentes de texto)** lo cubre el HUD construido en `PraderaApp.construirHUD()`, que usa `BitmapText` con la fuente por defecto del motor. Si tu profe quiere ver una fuente propia diseñada por ti, jME3 acepta fuentes BMFont generadas con la herramienta gratuita BMFont (de AngelCode); puedes dibujar tu propia tipografía en GIMP/Photoshop y exportarla, después colocar el `.fnt` y el `.png` en `src/main/resources/Interface/Fonts/` y cambiar `guiFont` por tu fuente custom.

### Tema 3: Graficación 3D

El **subtema 3.1 (representación y visualización 3D)** y el **3.2 (formas geométricas)** se demuestran a través de toda la escena: cada flor está compuesta de pétalos (superficies curvas Bézier-parametrizadas), centro (esfera), tallo (cilindros encadenados a lo largo de una curva), y hoja opcional (superficie plana deformada). El terreno es una superficie plana subdividida en cientos de miles de triángulos y luego deformada con el mapa de alturas fractal. Las rocas son esferas con escalas no uniformes (eso prueba que entendiste la diferencia entre superficie generadora paramétrica y superficie deformada). Además, la normalización e integración de modelos externos (.glb de casas y animales) se maneja dinámicamente mediante `NormalizadorModelos.java`, demostrando el uso práctico de volúmenes envolventes (`BoundingBox`) para ajustar el tamaño y la traslación vertical relativa al terreno de forma precisa.

El **subtema 3.3 (transformaciones 3D)** lo cubre `matematicas/TransformacionesMatriz.java` con sus variantes 3D usando matrices 4×4 con coordenadas homogéneas. La rotación alrededor de un eje arbitrario está implementada con la fórmula de Rodrigues (es la versión limpia, sin trigonometría inversa).

El **subtema 3.3.5 (perspectiva)** está en el mismo archivo con `proyeccionPerspectiva(fov, aspect, near, far)` que genera la matriz canónica de proyección perspectiva. En la práctica el motor ya la aplica internamente cuando configuras `cam.setFrustumPerspective` en `PraderaApp.simpleInitApp()`, pero tener tu propia implementación demuestra que la entiendes.

### Tema 4: Relleno, iluminación y sombreado

El **subtema 4.1 (relleno de polígonos)** se demuestra en tres modos: las rocas usan **color homogéneo**; los pétalos usan **color degradado** porque cada capa interpola en espacio HSL entre el colorPrincipal y el colorSecundario del TipoFlor; el terreno usa **textura** (un material tipo Lighting con textura difusa). Los tres casos están integrados en el render con `Lighting.j3md`.

El **subtema 4.2 (modelos de iluminación)** está en `iluminacion/GestorLuces.java`, que configura: luz ambiental (AmbientLight, baña toda la escena con poca intensidad para que las sombras no queden negras), luz direccional (DirectionalLight, simula el sol con todos los rayos paralelos, es la única que proyecta sombras grandes), y luz puntual (PointLight para añadir un brillo cálido focal). En el código vienen comentados el SpotLight y otros modos por si los necesitas activar.

El **subtema 4.3 (sombreado)** se cubre así: el modelo Phong (subtema 4.3.3) lo implementa el shader del material Lighting.j3md de jME3, que es Blinn-Phong por píxel (la versión optimizada que se usa en gráficos en tiempo real desde hace 20 años). El sombreado Gouraud (subtema 4.3.2) está disponible activando el flag `VertexLighting=true` en el mismo material. El interpolado (subtema 4.3.1) es lo que hace automáticamente la rasterización de cualquier triángulo en GPU: interpolar atributos entre vértices.

### Tema 5: Animación

El **subtema 5.1 (historia y evolución de la animación)** es teórico para el reporte. Una pista: arranca con Pixar y el corto Luxo Jr. (1986), pasa por Toy Story (1995, primer largometraje totalmente CGI), y llega a las técnicas modernas de motion capture y simulación física.

El **subtema 5.2.1 (Tweening)** lo cubre `animacion/TweeningFlor.java` con su catálogo de cinco funciones de easing diferentes (lineal, ease-in cuadrático, ease-out cuadrático, ease-in-out cúbico, y rebote final con spring-back). Se aplica al inicio de la app: las 480 flores nacen con escala cero y crecen hasta su tamaño normal en oleadas durante el primer par de segundos.

El **subtema 5.2.2 (Morphing)** lo cubre `animacion/MorphingFlor.java`, que aplica a las 80 flores más cercanas al jugador la animación de "apertura de capullo": inician con los pétalos cerrados (rotados hacia arriba y agrupados al centro) y se abren con interpolación esférica (slerp) de rotaciones y lineal (lerp) de posiciones a lo largo de unos 3 a 5 segundos. Es una pradera que despierta al amanecer.

El **subtema 5.3.3 (Skeletal)** lo cubre conceptualmente `animacion/AnimacionViento.java`, que aplica una rotación bone-like a cada flor desde su base, simulando un "hueso" único que dobla todo el cuerpo de la planta. El campo de viento se calcula con ruido fBm con componente temporal, así que las ráfagas se mueven coherentemente por la pradera en vez de que cada flor oscile independiente.

El **subtema 5.4 (animación controlada por usuario)** lo cubren dos cosas: `fisica/ControladorJugador.java` con todo el WASD + ratón en primera persona, y `interaccion/MovimientoFlores.java` que hace que las flores cercanas reaccionen físicamente al jugador (oscilador armónico amortiguado por flor con constante de rigidez K=35 y damping D=6, integración por Euler). La cascada disparada con la tecla M también es animación controlada por usuario.

---

## 7. Cómo cubrir las 9 prácticas del temario con este mismo código

El temario menciona 9 prácticas. Aquí te apunto rápido en qué archivo demostrar cada una si tu profe las pide individualmente:

**Práctica 1 (líneas y polígonos):** muestra `GeneradorPetalo.java` y la generación de contornos.

**Práctica 2 (fuentes):** muestra el HUD y opcionalmente añade una fuente custom en `src/main/resources/Interface/Fonts/`.

**Práctica 3 (transformaciones 2D):** muestra `TransformacionesMatriz.java` y haz un programa pequeño aparte (o un test en `src/test/`) que aplique traslación, rotación, escala y reflexión a un cuadrado dibujado.

**Práctica 4 (curvas Bézier):** muestra `CurvaBezier.java` y enseña visualmente que los tallos y las trayectorias de la cascada son Béziers.

**Práctica 5 (objetos 3D):** muestra que las flores, rocas y terreno son objetos 3D con vértices, aristas y caras estructuradas.

**Práctica 6 (transformaciones 3D):** muestra `TransformacionesMatriz.java` parte 3D y demuéstralas en escena (las flores rotan alrededor de su eje base por la animación de viento).

**Práctica 7 (eliminación de caras ocultas):** explica que el motor hace **backface culling** automático y **z-buffer** para resolver la visibilidad, que son los algoritmos modernos equivalentes al "vector normal" del temario.

**Práctica 8 (color y fuente de luz):** muestra `GestorLuces.java` y los materiales Lighting de las flores.

**Práctica 9 (animación 2D y 3D):** muestra `TweeningFlor.java`, `MorphingFlor.java`, `AnimacionViento.java`, `AnimacionCascada.java` y `MovimientoFlores.java`. Cinco tipos diferentes de animación en una sola escena.

---

## 8. Notas finales

Este proyecto se hizo con cariño para una persona especial. Si lo ejecutas en la oscuridad con audio ambiente de pajaritos da extra puntos de impresión.

Si tu profe te pregunta por qué elegiste jMonkeyEngine en vez de OpenGL puro o LibGDX, la respuesta correcta es: porque jME3 es exactamente el motor de alto nivel que menciona el temario oficial del TecNM (página 2, párrafo del API), así que cumple textualmente con lo que la materia pide, mientras te deja concentrarte en demostrar los conceptos en vez de pelearte con boilerplate de OpenGL.

Buena suerte, Farid. Que la pradera florezca.
