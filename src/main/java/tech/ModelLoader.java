package tech;

import UndoRedo.ModelLoadAction;
import UndoRedo.UndoManager;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.List;

public class ModelLoader {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final UndoManager undoManager;
    private final List<String> defaultItemPaths;
    private final FloorPlacer floorPlacer;
    private WareSkladInit wareSkladInit;

    public ModelLoader(Node rootNode, AssetManager assetManager, UndoManager undoManager, FloorPlacer floorPlacer, WareSkladInit wareSkladInit) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.undoManager = undoManager;
        this.floorPlacer = floorPlacer;
        this.wareSkladInit = wareSkladInit;

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

        Spatial model = assetManager.loadModel(modelPath);

        undoManager.addAction(new ModelLoadAction(model, rootNode));

        model.setLocalTranslation(position);
        model.setLocalScale(10, 10, 10);

        rootNode.attachChild(model);
    }

    private boolean isDefaultItem(String modelPath) {
        return defaultItemPaths.contains(modelPath);
    }
}
