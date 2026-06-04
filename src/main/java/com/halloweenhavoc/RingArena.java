package com.halloweenhavoc;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;

/**
 * Construye la arena: piso, falda del ring, lona, postes, cuerdas
 * y el backdrop tipo pantalla LED con motivo de Halloween Havoc.
 *
 * Las constantes publicas se reutilizan en otras clases (personaje,
 * camara) para mantener coherencia geometrica.
 */
public class RingArena {

    /** Medio lado del cuadrado interior del ring (lona). */
    public static final float RING_HALF = 3.0f;
    /** Altura de la superficie de la lona sobre el piso. */
    public static final float RING_TOP_Y = 1.0f;
    /** Altura de los postes medida desde la lona. */
    public static final float POST_HEIGHT = 2.2f;

    private static final float ROPE_RADIUS = 0.04f;

    private final AssetManager am;
    private final Node parent;
    private final Node ringNode = new Node("Ring");

    public RingArena(AssetManager am, Node parent) {
        this.am = am;
        this.parent = parent;
    }

    public void build() {
        buildFloor();
        buildRingApron();
        buildCanvas();
        buildPostsAndRopes();
        buildBackdrop();
        buildBarricades();
        parent.attachChild(ringNode);
    }

    // ------- Piso de la arena (recibe sombras) -------
    private void buildFloor() {
        Box floorMesh = new Box(40, 0.05f, 40);
        Geometry floor = new Geometry("Floor", floorMesh);
        Material m = litMaterial(new ColorRGBA(0.11f, 0.11f, 0.12f, 1f));
        floor.setMaterial(m);
        floor.setLocalTranslation(0, -0.05f, 0);
        floor.setShadowMode(ShadowMode.Receive);
        parent.attachChild(floor);
    }

    // ------- Falda del ring (la parte vertical visible debajo de la lona) -------
    private void buildRingApron() {
        Box apronMesh = new Box(RING_HALF + 0.3f, RING_TOP_Y / 2f, RING_HALF + 0.3f);
        Geometry apron = new Geometry("Apron", apronMesh);
        apron.setMaterial(litMaterial(new ColorRGBA(0.07f, 0.07f, 0.08f, 1f)));
        apron.setLocalTranslation(0, RING_TOP_Y / 2f, 0);
        apron.setShadowMode(ShadowMode.CastAndReceive);
        ringNode.attachChild(apron);

        // Franja decorativa naranja en los cuatro lados (estilo Halloween Havoc)
        float[][] sides = {
            { 0, RING_HALF + 0.32f,   0 },
            { 0, -(RING_HALF + 0.32f), 180 },
            { RING_HALF + 0.32f, 0,  90 },
            { -(RING_HALF + 0.32f), 0, 270 }
        };
        for (float[] s : sides) {
            Box stripeMesh = new Box(RING_HALF + 0.31f, 0.18f, 0.02f);
            Geometry stripe = new Geometry("ApronStripe", stripeMesh);
            stripe.setMaterial(unshadedMaterial(new ColorRGBA(1f, 0.42f, 0.05f, 1f)));
            // Rotacion alrededor de Y para colocar la franja en el lado correcto
            stripe.rotate(0, (float) Math.toRadians(s[2]), 0);
            stripe.setLocalTranslation(s[0], 0.55f, s[1]);
            ringNode.attachChild(stripe);
        }
    }

    // ------- Lona del ring (superficie azul clara como en la imagen) -------
    private void buildCanvas() {
        Box canvasMesh = new Box(RING_HALF, 0.02f, RING_HALF);
        Geometry canvas = new Geometry("Canvas", canvasMesh);
        canvas.setMaterial(litMaterial(new ColorRGBA(0.65f, 0.85f, 0.95f, 1f)));
        canvas.setLocalTranslation(0, RING_TOP_Y + 0.02f, 0);
        canvas.setShadowMode(ShadowMode.Receive);
        ringNode.attachChild(canvas);
    }

    // ------- Postes y cuerdas -------
    private void buildPostsAndRopes() {
        Vector3f[] corners = new Vector3f[] {
            new Vector3f( RING_HALF, 0,  RING_HALF),
            new Vector3f(-RING_HALF, 0,  RING_HALF),
            new Vector3f(-RING_HALF, 0, -RING_HALF),
            new Vector3f( RING_HALF, 0, -RING_HALF)
        };

        // Postes negros
        for (Vector3f c : corners) {
            Box postMesh = new Box(0.12f, POST_HEIGHT / 2f, 0.12f);
            Geometry post = new Geometry("Post", postMesh);
            post.setMaterial(litMaterial(new ColorRGBA(0.05f, 0.05f, 0.05f, 1f)));
            post.setLocalTranslation(c.x, RING_TOP_Y + POST_HEIGHT / 2f, c.z);
            post.setShadowMode(ShadowMode.CastAndReceive);
            ringNode.attachChild(post);

            // Almohadilla naranja arriba del poste
            Box padMesh = new Box(0.20f, 0.25f, 0.20f);
            Geometry pad = new Geometry("Pad", padMesh);
            pad.setMaterial(unshadedMaterial(new ColorRGBA(1f, 0.45f, 0.05f, 1f)));
            pad.setLocalTranslation(c.x, RING_TOP_Y + POST_HEIGHT + 0.1f, c.z);
            pad.setShadowMode(ShadowMode.Cast);
            ringNode.attachChild(pad);
        }

        // Tres cuerdas a alturas distintas, en los cuatro lados
        float[] ropeHeights = { 0.55f, 1.05f, 1.55f };
        for (float h : ropeHeights) {
            for (int side = 0; side < 4; side++) {
                Vector3f a = corners[side];
                Vector3f b = corners[(side + 1) % 4];
                createRope(a, b, h);
            }
        }
    }

    private void createRope(Vector3f a, Vector3f b, float relativeHeight) {
        Vector3f mid = a.add(b).multLocal(0.5f);
        float length = a.distance(b);
        Cylinder ropeMesh = new Cylinder(6, 6, ROPE_RADIUS, length, true);
        Geometry rope = new Geometry("Rope", ropeMesh);
        rope.setMaterial(unshadedMaterial(ColorRGBA.White));

        // El cilindro de jME se construye con su altura sobre el eje Z.
        // Usando lookAt orientamos su +Z hacia uno de los extremos, lo que
        // alinea el cilindro entero entre los dos puntos.
        Vector3f targetWorld = new Vector3f(b.x, RING_TOP_Y + relativeHeight, b.z);
        rope.setLocalTranslation(mid.x, RING_TOP_Y + relativeHeight, mid.z);
        rope.lookAt(targetWorld, Vector3f.UNIT_Y);
        ringNode.attachChild(rope);
    }

    // ------- Backdrop: pantalla LED + estructura del escenario -------
    private void buildBackdrop() {
        // Panel principal de la pantalla
        Box screenMesh = new Box(10, 4, 0.1f);
        Geometry screen = new Geometry("LedScreen", screenMesh);
        // Fondo naranja calido del logo (no responde a las luces, autoiluminado)
        screen.setMaterial(unshadedMaterial(new ColorRGBA(0.78f, 0.22f, 0.05f, 1f)));
        screen.setLocalTranslation(0, 5, -16);
        parent.attachChild(screen);

        // Calabaza simulada (caja naranja brillante)
        Box pumpkin = new Box(1.4f, 1.4f, 0.05f);
        Geometry pumpkinGeo = new Geometry("Pumpkin", pumpkin);
        pumpkinGeo.setMaterial(unshadedMaterial(new ColorRGBA(1f, 0.55f, 0.08f, 1f)));
        pumpkinGeo.setLocalTranslation(0, 6.5f, -15.9f);
        parent.attachChild(pumpkinGeo);

        // "Texto" del logo: una franja oscura imitando las letras estilizadas
        Box logoText = new Box(2.8f, 0.7f, 0.05f);
        Geometry logoGeo = new Geometry("LogoText", logoText);
        logoGeo.setMaterial(unshadedMaterial(new ColorRGBA(0.15f, 0.05f, 0.1f, 1f)));
        logoGeo.setLocalTranslation(0, 4.5f, -15.9f);
        parent.attachChild(logoGeo);

        // Marco metalico oscuro del escenario
        addStageFrame();
    }

    private void addStageFrame() {
        // Viga horizontal arriba
        Box top = new Box(11, 0.3f, 0.3f);
        Geometry topG = new Geometry("StageTop", top);
        topG.setMaterial(unshadedMaterial(new ColorRGBA(0.1f, 0.1f, 0.1f, 1f)));
        topG.setLocalTranslation(0, 9.2f, -16f);
        parent.attachChild(topG);

        // Columnas laterales
        for (float side : new float[] { -1f, 1f }) {
            Box col = new Box(0.3f, 4.5f, 0.3f);
            Geometry colG = new Geometry("StageCol", col);
            colG.setMaterial(unshadedMaterial(new ColorRGBA(0.1f, 0.1f, 0.1f, 1f)));
            colG.setLocalTranslation(side * 10.5f, 4.5f, -16f);
            parent.attachChild(colG);
        }

        // Pequenos paneles colgantes con luces (a los lados del backdrop)
        for (float side : new float[] { -1f, 1f }) {
            for (int row = 0; row < 4; row++) {
                Box panel = new Box(0.7f, 0.2f, 0.15f);
                Geometry pg = new Geometry("LightPanel", panel);
                pg.setMaterial(unshadedMaterial(new ColorRGBA(0.95f, 0.85f, 0.3f, 1f)));
                pg.setLocalTranslation(side * 11.2f, 8.5f - row * 1.2f, -15.5f);
                parent.attachChild(pg);
            }
        }
    }

    // ------- Barricadas que separan al publico del ring -------
    private void buildBarricades() {
        float dist = 5.2f; // distancia desde el centro del ring
        ColorRGBA railColor = new ColorRGBA(0.45f, 0.45f, 0.48f, 1f);

        // Cuatro barricadas en cuadrado alrededor del ring
        float[][] sides = {
            { 0,  dist, 0 },
            { 0, -dist, 0 },
            { dist,  0, 90 },
            { -dist, 0, 90 }
        };
        for (float[] s : sides) {
            Box railMesh = new Box(dist + 0.5f, 0.5f, 0.08f);
            Geometry rail = new Geometry("Barricade", railMesh);
            rail.setMaterial(litMaterial(railColor));
            rail.rotate(0, (float) Math.toRadians(s[2]), 0);
            rail.setLocalTranslation(s[0], 0.5f, s[1]);
            rail.setShadowMode(ShadowMode.CastAndReceive);
            ringNode.attachChild(rail);
        }
    }

    // === Helpers de materiales ===
    private Material litMaterial(ColorRGBA color) {
        Material m = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
        m.setColor("Diffuse", color);
        m.setColor("Ambient", ColorRGBA.White);
        m.setBoolean("UseMaterialColors", true);
        return m;
    }

    private Material unshadedMaterial(ColorRGBA color) {
        Material m = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", color);
        return m;
    }
}
