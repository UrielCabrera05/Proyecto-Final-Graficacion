package com.halloweenhavoc;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Crea cientos de "personitas" en anillos concentricos alrededor del ring
 * y las hace oscilar verticalmente con funciones seno desfasadas.
 *
 * El truco para que el publico no se vea como una "ola sincronizada" es
 * darle a cada miembro una fase (offset inicial del seno) y una frecuencia
 * distintas. Asi el conjunto se ve como cientos de personas brincando
 * independientemente, no como un coro coordinado.
 */
public class CrowdManager {

    private final AssetManager am;
    private final Node parent;
    private final Node crowdNode = new Node("Crowd");
    private final List<CrowdMember> members = new ArrayList<>();
    private final Random rng = new Random(42L);

    private float time = 0f;

    public CrowdManager(AssetManager am, Node parent) {
        this.am = am;
        this.parent = parent;
    }

    public void build() {
        // 8 anillos concentricos, cada uno mas alto que el anterior (gradas).
        // El radio mas pequeno empieza despues de las barricadas.
        for (int ring = 0; ring < 8; ring++) {
            float radius = 6.5f + ring * 1.3f;
            int count = (int) (radius * 6); // mas gente en anillos exteriores
            float baseY = ring * 0.4f;       // las filas exteriores estan mas elevadas

            for (int i = 0; i < count; i++) {
                float angle = (FastMath.TWO_PI * i) / count
                              + (rng.nextFloat() - 0.5f) * 0.05f;
                float x = FastMath.cos(angle) * radius;
                float z = FastMath.sin(angle) * radius;
                addPerson(x, baseY, z);
            }
        }
        parent.attachChild(crowdNode);
    }

    private void addPerson(float x, float baseY, float z) {
        // Cada persona = una caja delgada (cuerpo) con color aleatorio de "camiseta"
        Box body = new Box(0.22f, 0.5f, 0.18f);
        Geometry geo = new Geometry("Person", body);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", randomShirtColor());
        geo.setMaterial(mat);
        geo.setLocalTranslation(x, baseY + 0.5f, z);

        CrowdMember cm = new CrowdMember();
        cm.geo = geo;
        cm.baseY = baseY + 0.5f;
        cm.phase = rng.nextFloat() * FastMath.TWO_PI;          // 0..2pi
        cm.amplitude = 0.10f + rng.nextFloat() * 0.22f;        // 0.10..0.32 (movimiento vertical)
        cm.frequency = 1.5f + rng.nextFloat() * 2.0f;          // 1.5..3.5 Hz aprox
        members.add(cm);

        crowdNode.attachChild(geo);
    }

    private ColorRGBA randomShirtColor() {
        // Paleta de tonos oscuros y medios para imitar a un publico real en arena
        ColorRGBA[] palette = {
            new ColorRGBA(0.15f, 0.15f, 0.18f, 1f), // negro
            new ColorRGBA(0.25f, 0.30f, 0.20f, 1f), // verde militar
            new ColorRGBA(0.45f, 0.20f, 0.15f, 1f), // rojo oscuro
            new ColorRGBA(0.20f, 0.25f, 0.40f, 1f), // azul oscuro
            new ColorRGBA(0.35f, 0.30f, 0.25f, 1f), // beige
            new ColorRGBA(0.10f, 0.30f, 0.10f, 1f), // verde
            new ColorRGBA(0.45f, 0.45f, 0.50f, 1f), // gris
            new ColorRGBA(0.55f, 0.20f, 0.55f, 1f)  // morado
        };
        return palette[rng.nextInt(palette.length)];
    }

    /**
     * Llamado cada frame desde Main.simpleUpdate.
     * Recorre todos los miembros y aplica la oscilacion vertical.
     */
    public void update(float tpf) {
        time += tpf;
        for (CrowdMember cm : members) {
            float y = cm.baseY
                    + FastMath.sin(time * cm.frequency + cm.phase) * cm.amplitude;
            // Conservamos X y Z, solo modificamos Y
            float x = cm.geo.getLocalTranslation().x;
            float z = cm.geo.getLocalTranslation().z;
            cm.geo.setLocalTranslation(x, y, z);
        }
    }

    /** Estructura interna que guarda los parametros de oscilacion. */
    private static class CrowdMember {
        Geometry geo;
        float baseY;
        float phase;
        float amplitude;
        float frequency;
    }
}
