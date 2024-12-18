package tech;

import UndoRedo.DeleteAction;
import UndoRedo.UndoManager;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;

public class DeleteObject {

    private final WareSkladInit jmeScene;
    private final UndoManager undoManager;
    private final Node rootNode;

    public DeleteObject(WareSkladInit jmeScene, Node rootNode) {
        this.jmeScene = jmeScene;
        this.undoManager = jmeScene.undoManager;
        this.rootNode = rootNode;
    }

    public void delete(Spatial object) {
        if (object != null) {
            jmeScene.enqueue(() -> {
                undoManager.addAction(new DeleteAction(object, rootNode));
                object.removeFromParent();
            });
        } else {
            System.out.println("No object selected for deletion.");
        }
    }
}
