package com.halloweenhavoc;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * Personaje (wrestler) que el usuario controla en el ring.
 *
 * Estrategia para los "topes" del ring: en vez de usar fisica real con
 * colisionadores y rigid bodies (que seria innecesariamente pesado para
 * este caso), se hace clamping de coordenadas. Despues de calcular la
 * posicion candidata del personaje, se restringen X y Z al rango
 * [-RING_LIMIT, +RING_LIMIT]. Si el jugador intenta empujar contra una
 * cuerda, su movimiento simplemente se detiene en el borde.
 *
 * El salto se implementa con velocidad vertical + gravedad constante,
 * que es el modelo cinematico clasico: y(t+1) = y(t) + v_y * dt,
 * y v_y(t+1) = v_y(t) + g * dt.
 */
public class RingCharacter {

    private static final float SPEED = 3.5f;        // velocidad horizontal (unidades/seg)
    private static final float JUMP_VEL = 5.5f;     // velocidad inicial al saltar
    private static final float GRAVITY = -13f;      // gravedad en Y (negativa = hacia abajo)
    private static final float RING_LIMIT =
        RingArena.RING_HALF - 0.35f;                // dejar un margen del borde

    private final AssetManager am;
    private final Node parent;
    private final InputManager input;
    private final Node characterNode = new Node("Character");

    // Estado de movimiento
    private final Vector3f velocity = new Vector3f();
    private boolean fwd, back, left, right;
    private boolean grounded = true;

    public RingCharacter(AssetManager am, Node parent, InputManager input) {
        this.am = am;
        this.parent = parent;
        this.input = input;
    }

    public void spawn() {
        // === Cuerpo (rojo, como un singlet de luchador) ===
        Box bodyMesh = new Box(0.28f, 0.55f, 0.22f);
        Geometry body = new Geometry("Body", bodyMesh);
        body.setMaterial(litMaterial(new ColorRGBA(0.85f, 0.12f, 0.12f, 1f)));
        body.setLocalTranslation(0, 0.55f, 0);
        body.setShadowMode(ShadowMode.CastAndReceive);
        characterNode.attachChild(body);

        // === Cabeza ===
        Box headMesh = new Box(0.20f, 0.20f, 0.20f);
        Geometry head = new Geometry("Head", headMesh);
        head.setMaterial(litMaterial(new ColorRGBA(0.92f, 0.78f, 0.65f, 1f)));
        head.setLocalTranslation(0, 1.35f, 0);
        head.setShadowMode(ShadowMode.CastAndReceive);
        characterNode.attachChild(head);

        // === Brazos ===
        Box armMesh = new Box(0.10f, 0.40f, 0.10f);
        for (float side : new float[] { -1f, 1f }) {
            Geometry arm = new Geometry("Arm", armMesh);
            arm.setMaterial(litMaterial(new ColorRGBA(0.92f, 0.78f, 0.65f, 1f)));
            arm.setLocalTranslation(side * 0.42f, 0.65f, 0);
            arm.setShadowMode(ShadowMode.CastAndReceive);
            characterNode.attachChild(arm);
        }

        // === Piernas ===
        Box legMesh = new Box(0.12f, 0.35f, 0.12f);
        for (float side : new float[] { -1f, 1f }) {
            Geometry leg = new Geometry("Leg", legMesh);
            leg.setMaterial(litMaterial(new ColorRGBA(0.10f, 0.10f, 0.15f, 1f)));
            // Piernas justo debajo del cuerpo, una a cada lado del eje central
            leg.setLocalTranslation(side * 0.15f, -0.30f, 0f);
            leg.setShadowMode(ShadowMode.CastAndReceive);
            characterNode.attachChild(leg);
        }

        // Punto de spawn: al centro del ring, justo encima de la lona
        characterNode.setLocalTranslation(0,
            RingArena.RING_TOP_Y + 0.04f + 0.35f, 0);
        parent.attachChild(characterNode);

        setupInput();
    }

    private void setupInput() {
        input.addMapping("Fwd",   new KeyTrigger(KeyInput.KEY_W));
        input.addMapping("Back",  new KeyTrigger(KeyInput.KEY_S));
        input.addMapping("Left",  new KeyTrigger(KeyInput.KEY_A));
        input.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        input.addMapping("Jump",  new KeyTrigger(KeyInput.KEY_SPACE));

        ActionListener listener = new ActionListener() {
            @Override
            public void onAction(String name, boolean pressed, float tpf) {
                switch (name) {
                    case "Fwd":   fwd = pressed; break;
                    case "Back":  back = pressed; break;
                    case "Left":  left = pressed; break;
                    case "Right": right = pressed; break;
                    case "Jump":
                        if (pressed && grounded) {
                            velocity.y = JUMP_VEL;
                            grounded = false;
                        }
                        break;
                }
            }
        };
        input.addListener(listener, "Fwd", "Back", "Left", "Right", "Jump");
    }

    public void update(float tpf) {
        Vector3f pos = characterNode.getLocalTranslation().clone();

        // === Movimiento horizontal ===
        // Por simplicidad usamos ejes del mundo (no relativos a la camara).
        // Asi el jugador siempre va al mismo "norte" sin importar a donde
        // mira la camara.
        float dx = 0f, dz = 0f;
        if (fwd)   dz -= 1f;
        if (back)  dz += 1f;
        if (left)  dx -= 1f;
        if (right) dx += 1f;

        if (dx != 0f || dz != 0f) {
            // Normalizar para que el movimiento diagonal no sea mas rapido
            float len = (float) Math.sqrt(dx * dx + dz * dz);
            dx /= len;
            dz /= len;
            pos.x += dx * SPEED * tpf;
            pos.z += dz * SPEED * tpf;
        }

        // === Topes del ring (clamping) ===
        if (pos.x >  RING_LIMIT) pos.x =  RING_LIMIT;
        if (pos.x < -RING_LIMIT) pos.x = -RING_LIMIT;
        if (pos.z >  RING_LIMIT) pos.z =  RING_LIMIT;
        if (pos.z < -RING_LIMIT) pos.z = -RING_LIMIT;

        // === Gravedad + salto ===
        velocity.y += GRAVITY * tpf;
        pos.y += velocity.y * tpf;

        // Piso = altura de la lona del ring + el offset del centro del cuerpo
        float floorY = RingArena.RING_TOP_Y + 0.04f + 0.35f;
        if (pos.y <= floorY) {
            pos.y = floorY;
            velocity.y = 0f;
            grounded = true;
        }

        characterNode.setLocalTranslation(pos);
    }

    private Material litMaterial(ColorRGBA color) {
        Material m = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
        m.setColor("Diffuse", color);
        m.setColor("Ambient", ColorRGBA.White);
        m.setBoolean("UseMaterialColors", true);
        return m;
    }
}
