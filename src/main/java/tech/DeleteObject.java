package tech;

import UndoRedo.FloorDeleteAction;
import UndoRedo.DeleteAction;
import UndoRedo.UndoManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;

import java.util.List;

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
                if (undoManager.isFloorRelated(object)) {
                    undoManager.addAction(new FloorDeleteAction(
                            List.of((Geometry) object),
                            rootNode,
                            undoManager.getFloorCompleteAreas(),
                            undoManager.getFloorSegmentDistances(),
                            undoManager.getCompleteFloorCenters(),
                            undoManager.getFloorSegmentToFloorId(),
                            undoManager.getFloorIdToSegments(),
                            undoManager.getFloorSegmentVertices(),
                            undoManager.getCompleteFloorVertices()
                    ));
                } else {
                    undoManager.addAction(new DeleteAction(jmeScene, object, rootNode));
                }
                object.removeFromParent();
            });
        } else {
            System.out.println("No object selected for deletion.");
        }
    }
}
