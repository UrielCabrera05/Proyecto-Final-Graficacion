package com.halloweenhavoc;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;

/**

 * Controles:
 *   W / A / S / D : mover al personaje dentro del ring
 *   ESPACIO       : saltar
 *   Click derecho + arrastrar : rotar la cámara (yaw 360°, pitch 0-90°)
 *   Flechas       : rotar la cámara con teclado
 *   Rueda del mouse : acercar / alejar
 *   ESC           : salir
 */
public class Main extends SimpleApplication {

    private CrowdManager crowd;
    private RingCharacter player;
    private StageLightRig lightRig;
    private OrbitCameraController camController;

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Ring - Graficacion Genaro");
        settings.setResolution(1280, 720);
        settings.setVSync(true);
        settings.setSamples(4); // antialiasing
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Fondo casi negro como una arena oscura
        viewPort.setBackgroundColor(new ColorRGBA(0.02f, 0.02f, 0.04f, 1f));

        // === ILUMINACION GLOBAL ===
        // Luz ambiental tenue: simula la luz indirecta que rebota en toda la arena
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.18f));
        rootNode.addLight(ambient);

        // Luz direccional (como el sol): es la que proyecta sombras nitidas y coherentes
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.4f, -1f, -0.3f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(0.35f));
        rootNode.addLight(sun);

        // Renderer de sombras: 2048 = resolucion del shadow map, 3 = nivel de cascada
        DirectionalLightShadowRenderer dlsr =
            new DirectionalLightShadowRenderer(assetManager, 2048, 3);
        dlsr.setLight(sun);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON); // bordes suaves
        dlsr.setShadowIntensity(0.55f);
        viewPort.addProcessor(dlsr);

        // === ARMADO DE LA ESCENA ===
        // Arena, ring y backdrop tipo Halloween Havoc
        RingArena arena = new RingArena(assetManager, rootNode);
        arena.build();

        // Cielo de "estrellas" sobre la arena (puntitos brillantes del techo)
        StarryRoof.addTo(assetManager, rootNode);

        // Publico animado en anillos concentricos
        crowd = new CrowdManager(assetManager, rootNode);
        crowd.build();

        // Personaje en el ring
        player = new RingCharacter(assetManager, rootNode, inputManager);
        player.spawn();

        // Sistema de luces escenicas rotativas con conos visibles
        lightRig = new StageLightRig(assetManager, rootNode);
        lightRig.build();

        // === CAMARA ORBITAL ===
        flyCam.setEnabled(false); // desactivar la camara libre por defecto
        camController = new OrbitCameraController(cam, inputManager);
        camController.attach();

        // Por defecto, todo el rootNode puede arrojar y recibir sombras;
        // los Spatials individuales pueden sobreescribirlo.
        rootNode.setShadowMode(ShadowMode.CastAndReceive);

        addHud();
    }

    private void addHud() {
        BitmapText hud = new BitmapText(guiFont, false);
        hud.setSize(guiFont.getCharSet().getRenderedSize());
        hud.setColor(ColorRGBA.White);
        hud.setText("WASD: mover  |  ESPACIO: saltar  |  Click derecho: rotar camara  |  Rueda: zoom");
        hud.setLocalTranslation(15, settings.getHeight() - 15, 0);
        guiNode.attachChild(hud);
    }

    @Override
    public void simpleUpdate(float tpf) {
        crowd.update(tpf);
        lightRig.update(tpf);
        player.update(tpf);
        camController.update(tpf);
    }
}
