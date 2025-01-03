package UndoRedo;

import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import java.util.Map;

public class PlainAreaPlacementAction implements UndoableAction {
    private final Geometry plainAreaGeometry;
    private final Node rootNode;
    private final Map<Geometry, Float> plainAreaCompleteAreas;

    public PlainAreaPlacementAction(
            Geometry plainAreaGeometry,
            Node rootNode,
            Map<Geometry, Float> plainAreaCompleteAreas) {
        this.plainAreaGeometry = plainAreaGeometry;
        this.rootNode = rootNode;
        this.plainAreaCompleteAreas = plainAreaCompleteAreas;
    }

    @Override
    public void undo() {
        rootNode.detachChild(plainAreaGeometry);
        plainAreaCompleteAreas.remove(plainAreaGeometry);
    }

    @Override
    public void redo() {
        rootNode.attachChild(plainAreaGeometry);
        plainAreaCompleteAreas.put(plainAreaGeometry, plainAreaCompleteAreas.get(plainAreaGeometry));
    }

    public Geometry getPlainAreaGeometry() {
        return plainAreaGeometry;
    }

    public Map<Geometry, Float> getPlainAreaCompleteAreas() {
        return plainAreaCompleteAreas;
    }
}
