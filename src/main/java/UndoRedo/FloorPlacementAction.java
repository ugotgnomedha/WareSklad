package UndoRedo;

import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import java.util.List;

public class FloorPlacementAction implements UndoableAction {
    private final List<Geometry> floorGeometries;
    private final Node rootNode;

    public FloorPlacementAction(List<Geometry> floorGeometries, Node rootNode) {
        this.floorGeometries = floorGeometries;
        this.rootNode = rootNode;
    }

    @Override
    public void undo() {
        for (Geometry geometry : floorGeometries) {
            rootNode.detachChild(geometry);
        }
    }

    @Override
    public void redo() {
        for (Geometry geometry : floorGeometries) {
            rootNode.attachChild(geometry);
        }
    }

    public List<Geometry> getFloorGeometries() {
        return floorGeometries;
    }
}
