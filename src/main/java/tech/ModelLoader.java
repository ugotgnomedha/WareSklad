package tech;

import UndoRedo.ModelLoadAction;
import UndoRedo.UndoManager;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ModelLoader {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final UndoManager undoManager;

    public ModelLoader(Node rootNode, AssetManager assetManager, UndoManager undoManager) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.undoManager = undoManager;
    }

    public void loadAndPlaceModel(String modelPath, Vector3f position) {
        Spatial model = assetManager.loadModel(modelPath);

        undoManager.addAction(new ModelLoadAction(model, rootNode));

        model.setLocalTranslation(position);
        model.setLocalScale(10, 10, 10);

        rootNode.attachChild(model);
    }
}
