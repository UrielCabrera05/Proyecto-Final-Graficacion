package com.halloweenhavoc;

import com.jme3.asset.AssetManager;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de luces escenicas de la arena.
 *
 * Cada luz tiene DOS componentes que se mueven en sincronia:
 *
 *   1) Un SpotLight real de jME3: ilumina con fisica correcta de cono
 *      las superficies del ring y del personaje (materiales Lighting.j3md).
 *      Esta es la luz "real" que se nota cuando el haz toca al wrestler.
 *
 *   2) Una geometria de CONO semitransparente con additive blending:
 *      es lo que TE PERMITE VER el haz cortando el aire oscuro de la
 *      arena (como cuando ves rayos de sol entrando por una ventana).
 *      Sin esto, solo verias un circulo de luz en el suelo pero no el
 *      haz mismo.
 *
 * Las dos componentes se reorientan cada frame hacia un punto objetivo
 * que oscila siguiendo funciones seno/coseno con velocidades distintas
 * en X y Z. Eso da el efecto de luces "barriendo" el ring en patrones
 * complejos, no lineas rectas.
 */
public class StageLightRig {

    private final AssetManager am;
    private final Node parent;
    private final List<StageLight> lights = new ArrayList<>();
    private float time = 0f;

    public StageLightRig(AssetManager am, Node parent) {
        this.am = am;
        this.parent = parent;
    }

    public void build() {
        // Posiciones, colores y patrones de las luces escenicas.
        // Distribuidas en una linea sobre el escenario y otro par desde atras.
        addStageLight(new Vector3f(-8, 9.5f, -3),
                      new ColorRGBA(0.20f, 0.45f, 1.0f, 1f), 0.0f, 0.7f);  // azul izq
        addStageLight(new Vector3f(-4, 9.5f, -3),
                      new ColorRGBA(1.0f, 0.85f, 0.20f, 1f), 1.2f, 0.9f);  // amarilla
        addStageLight(new Vector3f( 0, 9.5f, -3),
                      new ColorRGBA(1.0f, 0.30f, 0.05f, 1f), 2.0f, 1.1f);  // naranja
        addStageLight(new Vector3f( 4, 9.5f, -3),
                      new ColorRGBA(0.60f, 0.30f, 1.0f, 1f), 0.7f, 0.8f);  // violeta
        addStageLight(new Vector3f( 8, 9.5f, -3),
                      new ColorRGBA(0.20f, 0.50f, 1.0f, 1f), 1.7f, 1.0f);  // azul der

        // Dos luces traseras (sobre el publico, apuntando al ring)
        addStageLight(new Vector3f(-7, 9.5f,  7),
                      new ColorRGBA(1.0f, 0.85f, 0.20f, 1f), 2.6f, 0.6f);
        addStageLight(new Vector3f( 7, 9.5f,  7),
                      new ColorRGBA(0.20f, 0.45f, 1.0f, 1f), 3.1f, 0.85f);
    }

    private void addStageLight(Vector3f pos, ColorRGBA color, float phase, float speed) {
        // === Componente 1: SpotLight real ===
        SpotLight sl = new SpotLight();
        sl.setPosition(pos);
        sl.setDirection(new Vector3f(0, -1, 0));
        sl.setColor(color.mult(2.0f));               // mas brillante que el color visible del haz
        sl.setSpotRange(35f);
        sl.setSpotInnerAngle(7f * FastMath.DEG_TO_RAD);   // angulo del cono interior (intensidad maxima)
        sl.setSpotOuterAngle(13f * FastMath.DEG_TO_RAD);  // angulo del cono exterior (caida a cero)
        parent.addLight(sl);

        // === Componente 2: cono visible (haz semitransparente) ===
        // Cylinder con dos radios crea un cono truncado: radio pequeno arriba
        // (cerca del fixture) y radio grande abajo (cerca del piso).
        // axisSamples=16, radialSamples=16, r1=0.12, r2=2.2, height=11
        Cylinder beamMesh = new Cylinder(16, 16, 0.12f, 2.2f, 11f, true, false);
        Geometry beam = new Geometry("LightBeam", beamMesh);

        Material bm = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        // Color del haz con alpha bajo: se acumula visualmente con additive
        bm.setColor("Color", new ColorRGBA(color.r, color.g, color.b, 0.20f));
        // Additive blending: el haz suma luz sobre lo que hay detras,
        // simulando como el polvo y la niebla en el aire dispersan la luz.
        bm.getAdditionalRenderState().setBlendMode(BlendMode.Additive);
        bm.getAdditionalRenderState().setDepthWrite(false);
        beam.setMaterial(bm);
        beam.setQueueBucket(Bucket.Transparent);

        // El cono se construye centrado en el origen y alineado con su altura
        // en el eje Z. Lo desplazamos en +Z para que el extremo angosto quede
        // en el pivote (la posicion de la luz).
        beam.setLocalTranslation(0, 0, 5.5f);

        // Pivote que rotaremos para apuntar el cono y el SpotLight juntos
        Node beamPivot = new Node("BeamPivot");
        beamPivot.setLocalTranslation(pos);
        beamPivot.attachChild(beam);
        parent.attachChild(beamPivot);

        // === Carcasa visible del fixture (la "lampara" colgando) ===
        Cylinder housingMesh = new Cylinder(8, 12, 0.25f, 0.4f, true);
        Geometry housing = new Geometry("LightHousing", housingMesh);
        Material hm = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        hm.setColor("Color", new ColorRGBA(0.05f, 0.05f, 0.05f, 1f));
        housing.setMaterial(hm);
        housing.setLocalTranslation(pos.x, pos.y + 0.2f, pos.z);
        housing.rotate(FastMath.HALF_PI, 0, 0); // orientarlo vertical
        parent.attachChild(housing);

        // Guardar para animar
        StageLight s = new StageLight();
        s.light = sl;
        s.beamPivot = beamPivot;
        s.basePos = pos;
        s.phase = phase;
        s.speed = speed;
        lights.add(s);
    }

    /**
     * Actualiza la direccion de cada luz para que barra el ring.
     * El punto objetivo oscila en X y Z con frecuencias distintas:
     * esto produce trayectorias tipo Lissajous (curvas tipo "ocho")
     * en lugar de movimientos pendulares aburridos.
     */
    public void update(float tpf) {
        time += tpf;
        for (StageLight s : lights) {
            float targetX = FastMath.sin(time * s.speed + s.phase) * 4.5f;
            float targetZ = FastMath.cos(time * s.speed * 0.65f + s.phase * 1.3f) * 3.5f;
            Vector3f target = new Vector3f(targetX, RingArena.RING_TOP_Y, targetZ);

            // 1) Direccion del SpotLight real
            Vector3f dir = target.subtract(s.basePos).normalizeLocal();
            s.light.setDirection(dir);

            // 2) Orientar el pivote del cono visible hacia el mismo target.
            // lookAt en jME orienta el +Z del Spatial hacia el target,
            // y como el cono esta posicionado en +Z del pivote, se proyecta
            // exactamente en la direccion correcta.
            s.beamPivot.lookAt(target, Vector3f.UNIT_Y);
        }
    }

    private static class StageLight {
        SpotLight light;
        Node beamPivot;
        Vector3f basePos;
        float phase;
        float speed;
    }
}
