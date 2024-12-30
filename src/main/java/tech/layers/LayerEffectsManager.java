package tech.layers;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

public class LayerEffectsManager {
    private final LayersManager layersManager;

    public LayerEffectsManager(LayersManager layersManager) {
        this.layersManager = layersManager;
    }

    public void applyEffects(String layerName) {
        Layer layer = layersManager.getLayer(layerName);
        if (layer != null) {
            float opacity = layer.getOpacity();
            for (Spatial spatial : layer.getSpatials()) {
                applyOpacity(spatial, opacity);
                System.out.println("Spatial: " + spatial.getName() +  " Setting opacity: "+  opacity);
            }
        } else {
            System.out.println("Layer not found: " + layerName);
        }
    }

    private void applyOpacity(Spatial spatial, float opacity) {
        if (spatial != null) {
            spatial.depthFirstTraversal(sp -> {
                if (sp instanceof Geometry) {
                    Geometry geometry = (Geometry) sp;
                    Material material = geometry.getMaterial();
                    if (material != null) {
                        if (material.getParam("Diffuse") != null) {
                            ColorRGBA diffuseColor = ((ColorRGBA) material.getParam("Diffuse").getValue()).clone();
                            diffuseColor.a = opacity;
                            material.setColor("Diffuse", diffuseColor);
                            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                            geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
                        } else {
                            System.err.println("Material does not have a 'Diffuse' parameter: " + geometry.getName());
                        }
                    }
                }
            });
        }
    }


    public void resetMaterialEffects(Spatial spatial) {
        if (spatial != null) {
            spatial.depthFirstTraversal(sp -> {
                if (sp instanceof Geometry) {
                    Geometry geometry = (Geometry) sp;
                    Material material = geometry.getMaterial();
                    if (material != null) {
                        if (material.getParam("Diffuse") != null) {
                            ColorRGBA diffuseColor = ((ColorRGBA) material.getParam("Diffuse").getValue()).clone();
                            diffuseColor.a = 1.0f;
                            material.setColor("Diffuse", diffuseColor);
                            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Off);
                            geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
                        } else {
                            System.err.println("Material does not have a 'Diffuse' parameter: " + geometry.getName());
                        }
                    }
                }
            });
        }
    }

}
