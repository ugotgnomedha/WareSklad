package tech;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Map;

public class LayersManager {
    private final Map<String, Layer> layers;
    private final Map<String, Boolean> layerEditingStates;
    private final LayerEffectsManager layerEffectsManager;

    public LayersManager() {
        this.layers = new HashMap<>();
        this.layerEditingStates = new HashMap<>();
        this.layerEffectsManager = new LayerEffectsManager(this);
    }

    public void addLayer(String name, float opacity, boolean lockEdit) {
        float normalizedOpacity = Math.max(0, Math.min(opacity, 100)) / 100;

        if (!layers.containsKey(name)) {
            layers.put(name, new Layer(name, normalizedOpacity, lockEdit));
            layerEditingStates.put(name, lockEdit);
        } else {
            System.out.println("Layer with name " + name + " already exists.");
        }
    }

    public void removeLayer(String name) {
        layers.remove(name);
        layerEditingStates.remove(name);
    }

    public boolean isLayerEditLocked(String name) {
        if (name == null || !layerEditingStates.containsKey(name)) {
            return false;
        }
        return layerEditingStates.get(name);
    }

    public Layer getLayer(String name) {
        return layers.get(name);
    }

    public void editLayer(String name, String newName, float opacity, boolean lockEdit) {
        float normalizedOpacity = Math.max(0, Math.min(opacity, 100)) / 100;

        Layer layer = layers.get(name);
        if (layer != null) {
            layer.setName(newName);
            layer.setOpacity(normalizedOpacity);
            layer.setLockEdit(lockEdit);
            layerEditingStates.put(newName, lockEdit);

            if (!name.equals(newName)) {
                layers.remove(name);
                layerEditingStates.remove(name);
                layers.put(newName, layer);
            }
        } else {
            System.out.println("Layer not found: " + name);
        }
    }

    public void addSpatialToLayer(String layerName, Spatial spatial) {
        Layer layer = layers.get(layerName);
        if (layer != null) {
            layer.addSpatial(spatial);
            // Apply effects of the layer to all layer's spatials.
            layerEffectsManager.applyEffects(layerName);
        } else {
            System.out.println("Layer not found: " + layerName);
        }
    }

    public void removeSpatialFromLayer(String layerName, Spatial spatial) {
        Layer layer = layers.get(layerName);
        if (layer != null) {
            layer.removeSpatial(spatial);
            layerEffectsManager.resetMaterialEffects(spatial);
        } else {
            System.out.println("Layer not found: " + layerName);
        }
    }

    public Map<String, Layer> getAllLayers() {
        return layers;
    }

    public String getLayerForSpatial(Spatial spatial) {
        for (Map.Entry<String, Layer> entry : layers.entrySet()) {
            Layer layer = entry.getValue();
            if (layer.containsSpatial(spatial)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
