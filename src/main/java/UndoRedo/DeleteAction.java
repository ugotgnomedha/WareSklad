package UndoRedo;

import com.jme3.scene.Spatial;
import com.jme3.scene.Node;

public class DeleteAction implements UndoableAction {
    private final Spatial deletedObject;
    private final Node rootNode;

    public DeleteAction(Spatial deletedObject, Node rootNode) {
        this.deletedObject = deletedObject;
        this.rootNode = rootNode;
    }

    public Spatial getDeletedObject() {
        return deletedObject;
    }

    @Override
    public void undo() {
        rootNode.attachChild(deletedObject);
    }

    @Override
    public void redo() {
        deletedObject.removeFromParent();
    }
}
