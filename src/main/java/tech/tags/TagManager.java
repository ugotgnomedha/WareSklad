package tech.tags;


import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import java.awt.Color;
import java.util.HashMap;

public class TagManager {
    private HashMap<Spatial, ColorRGBA> originalColors = new HashMap<>();

    public void addSpatialWithTag(Spatial spatial, Tag tag) {
        if (spatial != null && tag != null) {
            if (spatial instanceof Geometry) {
                Geometry geometry = (Geometry) spatial;
                Material material = geometry.getMaterial();

                if (material != null) {
                    if (!originalColors.containsKey(spatial)) {
                        saveOriginalColor(geometry);
                    }

                    if (tag.isCustomColor()) {
                        applyCustomColor(material, tag);
                    }
                }
            }
        }
    }

    private void applyCustomColor(Material material, Tag tag) {
        if (material.getParam("Diffuse") != null) {
            material.setColor("Diffuse", convertToColorRGBA(tag.getColor()));
        } else if (material.getParam("Albedo") != null) {
            material.setColor("Albedo", convertToColorRGBA(tag.getColor()));
        } else if (material.getParam("Color") != null) {
            material.setColor("Color", convertToColorRGBA(tag.getColor()));
        }
    }

    public void removeSpatialTag(Spatial spatial) {
        if (spatial != null) {
            if (originalColors.containsKey(spatial)) {
                restoreOriginalColor(spatial);
            }
        }
    }

    private void restoreOriginalColor(Spatial spatial) {
        if (spatial instanceof Geometry) {
            Geometry geometry = (Geometry) spatial;
            if (geometry.getMaterial() != null && originalColors.containsKey(spatial)) {
                ColorRGBA originalColor = originalColors.get(spatial);

                if (geometry.getMaterial().getParam("Diffuse") != null) {
                    geometry.getMaterial().setColor("Diffuse", originalColor);
                    System.out.println("Restored original color to Diffuse: " + originalColor);
                } else if (geometry.getMaterial().getParam("Albedo") != null) {
                    geometry.getMaterial().setColor("Albedo", originalColor);
                    System.out.println("Restored original color to Albedo: " + originalColor);
                } else if (geometry.getMaterial().getParam("Color") != null) {
                    geometry.getMaterial().setColor("Color", originalColor);
                    System.out.println("Restored original color to Color: " + originalColor);
                }
            }
        }
    }

    private void saveOriginalColor(Geometry geometry) {
        Material material = geometry.getMaterial();
        if (material != null) {
            if (material.getParam("Diffuse") != null) {
                originalColors.put(geometry, (ColorRGBA) material.getParam("Diffuse").getValue());
            } else if (material.getParam("Albedo") != null) {
                originalColors.put(geometry, (ColorRGBA) material.getParam("Albedo").getValue());
            } else if (material.getParam("Color") != null) {
                originalColors.put(geometry, (ColorRGBA) material.getParam("Color").getValue());
            }
        }
    }

    private ColorRGBA convertToColorRGBA(Color color) {
        return new ColorRGBA(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        );
    }

    public HashMap<Spatial, ColorRGBA> getOriginalColors() {
        return originalColors;
    }
}
