# Halloween Havoc — Proyecto de Graficación (jMonkeyEngine 3)

Recreación 3D interactiva de la escena de un evento WWE Halloween Havoc, con
público animado, personaje controlable en el ring, iluminación dinámica con
sombras y cámara orbital con límites configurables.

## Cómo importarlo a NetBeans

1. Abre NetBeans 25.
2. Crea un nuevo proyecto: **File > New Project > JME3 > BasicGame**
   (o reutiliza tu proyecto existente, como PraderaFlores).
3. Asegúrate de que las librerías de jMonkeyEngine 3 estén agregadas al
   classpath del proyecto (jme3-core, jme3-desktop, jme3-lwjgl3, etc.).
4. Dentro de `src/`, crea el paquete `com.halloweenhavoc`.
5. Copia los 7 archivos `.java` adjuntos al paquete recién creado.
6. En las propiedades del proyecto (Run), establece la clase principal como
   `com.halloweenhavoc.Main`.
7. Compila y ejecuta (F6).

## Controles

| Acción                         | Tecla / Input                       |
|--------------------------------|-------------------------------------|
| Mover al personaje             | W A S D                             |
| Saltar                         | ESPACIO                             |
| Rotar la cámara (libre)        | Click derecho + arrastrar el mouse  |
| Rotar la cámara (paso a paso)  | Flechas del teclado                 |
| Zoom in / zoom out             | Rueda del mouse                     |
| Salir                          | ESC                                 |

## Arquitectura

El proyecto sigue el principio de **responsabilidad única**: cada clase
atiende un aspecto específico de la escena, y `Main` actúa como director
que las coordena.

| Archivo                       | Responsabilidad                                                                 |
|-------------------------------|---------------------------------------------------------------------------------|
| `Main.java`                   | Punto de entrada; configura iluminación global, sombras, HUD y arranca todo.    |
| `RingArena.java`              | Construye piso, falda del ring, lona, postes, cuerdas, barricadas y backdrop.   |
| `CrowdManager.java`           | Crea cientos de personitas en anillos concéntricos y las anima con senos desfasados. |
| `RingCharacter.java`          | Cuerpo del wrestler, control WASD, salto con gravedad, clamping al ring.        |
| `StageLightRig.java`          | SpotLights reales + conos visibles que rotan siguiendo curvas tipo Lissajous.   |
| `OrbitCameraController.java`  | Cámara orbital con yaw libre 360° y pitch limitado a 90°.                       |
| `StarryRoof.java`             | Decoración: cientos de puntitos blancos en el techo (luces tipo cielo).         |

## Puntos clave de la implementación

**Animación del público.** Cada miembro del público tiene tres parámetros
aleatorios: una fase inicial, una amplitud y una frecuencia. La posición Y
se calcula con `baseY + sin(time * frecuencia + fase) * amplitud`. Como
cada miembro tiene parámetros distintos, el conjunto se ve como cientos de
personas brincando independientemente, no como una ola coreografiada.

**Topes del ring.** En lugar de usar física rígida con colisionadores
(innecesariamente pesado para este caso), se aplica *clamping* sobre las
coordenadas X y Z del personaje después de calcular su posición candidata.
Si el jugador intenta empujar contra una cuerda, su movimiento simplemente
se detiene en el borde, pero el personaje sigue intentando moverse hacia
allí en caso de que tú gires la cámara y cambies de intención.

**Salto y gravedad.** Modelo cinemático clásico: al presionar espacio, se
asigna `velocity.y = JUMP_VEL`. Cada frame, `velocity.y += GRAVITY * tpf`,
y luego `pos.y += velocity.y * tpf`. Cuando el personaje alcanza la altura
del piso, se anulan la velocidad y la bandera `grounded` se restaura.

**Luces escénicas: dos componentes en sincronía.** Cada luz tiene:
- Un `SpotLight` real de jME3 que ilumina con física correcta de cono las
  superficies con material `Lighting.j3md` (ring, personaje).
- Una geometría de cono semitransparente con *additive blending* que viaja
  pegada al SpotLight. Sin esto sólo verías un círculo iluminado en el
  suelo; con esto, **ves el haz cortando el aire**, como cuando hay polvo
  o niebla iluminada en un concierto.

**Trayectorias de las luces.** El punto objetivo de cada luz oscila en X y
Z con velocidades distintas (`speed * 0.65` para Z). Esto produce
trayectorias tipo **Lissajous** (curvas tipo "ocho" o "rosa"), mucho más
interesantes que un simple barrido lineal.

**Sombras.** Se usa un único `DirectionalLightShadowRenderer` con shadow
map de 2048 px y filtrado PCFPOISSON para bordes suaves. Los SpotLights
sólo aportan iluminación dinámica, sin generar sombras propias (eso
multiplicaría el costo por la cantidad de luces). Es el balance correcto:
sombras coherentes y rendimiento aceptable.

**Cámara orbital.** En vez de mover la cámara con vectores libres, se usan
**coordenadas esféricas** (yaw, pitch, distance) alrededor de un punto
objetivo fijo. La conversión a cartesianas es:

```
x = distance * cos(pitch) * sin(yaw)
y = distance * sin(pitch)
z = distance * cos(pitch) * cos(yaw)
```

Los límites se aplican directamente sobre yaw y pitch: yaw se envuelve en
`[0, 2π)` para permitir vueltas completas, y pitch se clampea a `(0, π/2]`
para que la cámara nunca se meta debajo del piso ni rebase la vertical.

## Ideas para extender el proyecto

- Cargar texturas reales para el logo de Halloween Havoc, la lona del ring
  y la pantalla LED del backdrop.
- Agregar partículas de chispas o niebla en el escenario con
  `ParticleEmitter`.
- Sustituir las cajas del público por un modelo 3D simple
  (`assetManager.loadModel(...)`) instanciado muchas veces.
- Implementar movimiento del personaje relativo a la dirección de la
  cámara (en vez de ejes del mundo) para que WASD se sienta más natural.
- Agregar audio de la multitud con `AudioNode`.
- Añadir un segundo personaje y detección de colisión entre los dos.
