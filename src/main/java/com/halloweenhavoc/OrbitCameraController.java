package com.halloweenhavoc;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * Camara orbital alrededor del ring, con limites explicitos:
 *   - YAW (rotacion horizontal, eje vertical Y del mundo):
 *     libre 360 grados, da la vuelta completa.
 *   - PITCH (inclinacion vertical, eje horizontal):
 *     limitada al rango (0, 90 grados], asi nunca se mete debajo del
 *     ring ni rebasa la vertical absoluta.
 *
 * La camara se posiciona en coordenadas esfericas (yaw, pitch, distance)
 * alrededor de un punto objetivo fijo (el centro del ring).
 *
 * Controles:
 *   - Click derecho + arrastrar mouse : rotacion libre
 *   - Flechas del teclado             : rotacion paso a paso
 *   - Rueda del mouse                 : zoom in/out
 */
public class OrbitCameraController {

    private static final float MIN_PITCH = 0.05f;             // casi horizontal
    private static final float MAX_PITCH = FastMath.HALF_PI;  // 90 grados (PI/2)
    private static final float MIN_DIST = 4f;
    private static final float MAX_DIST = 28f;

    private final Camera cam;
    private final InputManager input;

    // Punto al que mira la camara: ligeramente sobre la lona del ring
    private final Vector3f target = new Vector3f(0, RingArena.RING_TOP_Y + 0.5f, 0);

    // Coordenadas esfericas
    private float yaw = FastMath.PI * 0.25f;     // angulo horizontal
    private float pitch = FastMath.PI * 0.30f;   // angulo vertical (0..PI/2)
    private float distance = 14f;

    private boolean dragging = false;

    public OrbitCameraController(Camera cam, InputManager input) {
        this.cam = cam;
        this.input = input;
    }

    public void attach() {
        input.setCursorVisible(true);

        input.addMapping("OrbitDrag",  new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        input.addMapping("MouseX+",    new MouseAxisTrigger(MouseInput.AXIS_X, false));
        input.addMapping("MouseX-",    new MouseAxisTrigger(MouseInput.AXIS_X, true));
        input.addMapping("MouseY+",    new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        input.addMapping("MouseY-",    new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        input.addMapping("ZoomIn",     new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        input.addMapping("ZoomOut",    new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        input.addMapping("ArrowLeft",  new KeyTrigger(KeyInput.KEY_LEFT));
        input.addMapping("ArrowRight", new KeyTrigger(KeyInput.KEY_RIGHT));
        input.addMapping("ArrowUp",    new KeyTrigger(KeyInput.KEY_UP));
        input.addMapping("ArrowDown",  new KeyTrigger(KeyInput.KEY_DOWN));

        // Click derecho activa/desactiva el modo de arrastre
        ActionListener actListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean pressed, float tpf) {
                if (name.equals("OrbitDrag")) dragging = pressed;
            }
        };
        input.addListener(actListener, "OrbitDrag");

        // Movimientos analogicos
        AnalogListener analog = new AnalogListener() {
            @Override
            public void onAnalog(String name, float value, float tpf) {
                final float MOUSE_SENS = 3.0f;
                final float KEY_SPEED = 1.5f;
                final float ZOOM_STEP = 20f;

                if (dragging) {
                    switch (name) {
                        case "MouseX+": yaw   -= value * MOUSE_SENS; break;
                        case "MouseX-": yaw   += value * MOUSE_SENS; break;
                        case "MouseY+": pitch += value * MOUSE_SENS; break;
                        case "MouseY-": pitch -= value * MOUSE_SENS; break;
                    }
                }
                switch (name) {
                    case "ArrowLeft":  yaw   += KEY_SPEED * tpf; break;
                    case "ArrowRight": yaw   -= KEY_SPEED * tpf; break;
                    case "ArrowUp":    pitch += KEY_SPEED * tpf; break;
                    case "ArrowDown":  pitch -= KEY_SPEED * tpf; break;
                    case "ZoomIn":     distance -= value * ZOOM_STEP; break;
                    case "ZoomOut":    distance += value * ZOOM_STEP; break;
                }
                clampValues();
            }
        };
        input.addListener(analog,
            "MouseX+", "MouseX-", "MouseY+", "MouseY-",
            "ZoomIn", "ZoomOut",
            "ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown");
    }

    /**
     * Aplica los limites:
     *   - yaw se envuelve en [0, 2*PI) -> da la vuelta completa.
     *   - pitch se clampea a (MIN_PITCH, MAX_PITCH) -> nunca rebasa 90 grados
     *     ni se mete por debajo del horizonte.
     *   - distance se clampea para no atravesar el ring ni alejarse demasiado.
     */
    private void clampValues() {
        if (yaw > FastMath.TWO_PI) yaw -= FastMath.TWO_PI;
        if (yaw < 0)               yaw += FastMath.TWO_PI;

        if (pitch < MIN_PITCH) pitch = MIN_PITCH;
        if (pitch > MAX_PITCH) pitch = MAX_PITCH;

        if (distance < MIN_DIST) distance = MIN_DIST;
        if (distance > MAX_DIST) distance = MAX_DIST;
    }

    public void update(float tpf) {
        // Conversion de coordenadas esfericas a cartesianas:
        //   x = d * cos(pitch) * sin(yaw)
        //   y = d * sin(pitch)
        //   z = d * cos(pitch) * cos(yaw)
        float cosP = FastMath.cos(pitch);
        float sinP = FastMath.sin(pitch);
        float cosY = FastMath.cos(yaw);
        float sinY = FastMath.sin(yaw);

        Vector3f offset = new Vector3f(
            cosP * sinY * distance,
            sinP * distance,
            cosP * cosY * distance
        );

        Vector3f camPos = target.add(offset);
        cam.setLocation(camPos);
        cam.lookAt(target, Vector3f.UNIT_Y);
    }
}
