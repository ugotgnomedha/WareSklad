package UndoRedo;

import com.jme3.scene.Spatial;
import com.jme3.scene.Node;

public class ModelLoadAction implements UndoableAction {
    private final Spatial model;
    private final Node rootNode;

    public ModelLoadAction(Spatial model, Node rootNode) {
        this.model = model;
        this.rootNode = rootNode;
    }

    @Override
    public void undo() {
        model.removeFromParent();
    }

    @Override
    public void redo() {
        rootNode.attachChild(model);
    }

    public Spatial getModel() {
        return model;
    }
}
