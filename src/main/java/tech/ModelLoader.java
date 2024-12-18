package tech;

import UndoRedo.ModelLoadAction;
import UndoRedo.UndoManager;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelLoader {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final UndoManager undoManager;
    private final List<String> defaultItemPaths;
    private final FloorPlacer floorPlacer;
    private final WareSkladInit wareSkladInit;

    private final Map<Spatial, String> modelPathsMap;

    public ModelLoader(Node rootNode, AssetManager assetManager, UndoManager undoManager, FloorPlacer floorPlacer, WareSkladInit wareSkladInit) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.undoManager = undoManager;
        this.floorPlacer = floorPlacer;
        this.wareSkladInit = wareSkladInit;
        this.modelPathsMap = new HashMap<>();
        this.defaultItemPaths = CatalogueLoader.getDefaultModelPaths();
    }

    public void loadAndPlaceModel(String modelPath, Vector3f position) {
        if (isDefaultItem(modelPath)) {
            if (modelPath.equals("Models/default_floor01.j3o")) {
                wareSkladInit.deselectObject();
                floorPlacer.setFloorMode(true);
                return;
            }

            System.out.println("Default item detected: " + modelPath + ". Skipping loading in scene.");
            return;
        }

        wareSkladInit.enqueue(() -> {
            Spatial model = assetManager.loadModel(modelPath);

            modelPathsMap.put(model, modelPath);

            undoManager.addAction(new ModelLoadAction(model, rootNode));

            model.setLocalTranslation(position);

            float targetSize = 20.0f;
            BoundingBox boundingBox = (BoundingBox) model.getWorldBound();
            if (boundingBox == null) {
                model.updateModelBound();
                model.updateGeometricState();
                boundingBox = (BoundingBox) model.getWorldBound();
            }

            float largestExtent = Math.max(boundingBox.getXExtent(),
                    Math.max(boundingBox.getYExtent(), boundingBox.getZExtent()));

            float scaleFactor = targetSize / largestExtent;

            model.setLocalScale(scaleFactor);

            model.updateModelBound();
            model.updateGeometricState();

            rootNode.attachChild(model);
        });
    }

    private boolean isDefaultItem(String modelPath) {
        return defaultItemPaths.contains(modelPath);
    }

    public String getModelPath(Spatial model) {
        return modelPathsMap.get(model);
    }
}
