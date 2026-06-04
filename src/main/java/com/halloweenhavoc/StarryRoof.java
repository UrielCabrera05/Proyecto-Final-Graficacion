package com.halloweenhavoc;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.Random;

/**
 * Decora el techo de la arena con muchos puntos blancos pequenos
 * que imitan las miles de luces LED que se ven en la parte de arriba
 * de la imagen de referencia (esos puntos que parecen estrellas).
 *
 * Comparte un solo Material entre todas las geometrias para ser
 * eficiente: no tiene sentido crear 250 materiales identicos.
 */
public class StarryRoof {

    public static void addTo(AssetManager am, Node parent) {
        Random rng = new Random(7L);
        Node roof = new Node("StarryRoof");

        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);

        // 300 puntitos esparcidos en un area sobre la arena
        for (int i = 0; i < 300; i++) {
            float x = (rng.nextFloat() - 0.5f) * 40f;
            float z = (rng.nextFloat() - 0.5f) * 30f - 5f;
            float y = 11f + rng.nextFloat() * 1.5f;

            Box b = new Box(0.06f, 0.06f, 0.06f);
            Geometry g = new Geometry("Star", b);
            g.setMaterial(mat);
            g.setLocalTranslation(x, y, z);
            roof.attachChild(g);
        }
        parent.attachChild(roof);
    }
}
