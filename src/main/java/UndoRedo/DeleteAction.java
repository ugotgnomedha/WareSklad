package UndoRedo;

import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import tech.WareSkladInit;

public class DeleteAction implements UndoableAction {
    private final WareSkladInit jmeScene;
    private final Spatial deletedObject;
    private final Node rootNode;

    public DeleteAction(WareSkladInit jmeScene, Spatial deletedObject, Node rootNode) {
        this.jmeScene = jmeScene;
        this.deletedObject = deletedObject;
        this.rootNode = rootNode;
    }

    public Spatial getDeletedObject() {
        return deletedObject;
    }

    @Override
    public void undo() {
        jmeScene.enqueue(() -> {
            rootNode.attachChild(deletedObject);
        });
    }

    @Override
    public void redo() {
        jmeScene.enqueue(() -> {
            deletedObject.removeFromParent();
        });
    }
}
