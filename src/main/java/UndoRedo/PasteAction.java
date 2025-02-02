package UndoRedo;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.List;

public class PasteAction implements UndoableAction {

    private final List<Spatial> pastedObjects;
    private final Node rootNode;

    public PasteAction(List<Spatial> pastedObjects, Node rootNode) {
        this.pastedObjects = pastedObjects;
        this.rootNode = rootNode;
    }

    @Override
    public void undo() {
        for (Spatial object : pastedObjects) {
            if (object.getParent() != null) {
                object.removeFromParent();
            }
        }
    }

    @Override
    public void redo() {
        for (Spatial object : pastedObjects) {
            if (object.getParent() == null) {
                rootNode.attachChild(object);
            }
        }
    }

    public List<Spatial> getPastedObjects() {
        return pastedObjects;
    }
}