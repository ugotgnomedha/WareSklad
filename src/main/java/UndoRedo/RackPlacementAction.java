package UndoRedo;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RackPlacementAction implements UndoableAction {

    private final List<Spatial> placedRacks;
    private final Map<Spatial, Node> originalParents;

    public RackPlacementAction(List<Spatial> placedRacks) {
        this.placedRacks = placedRacks;
        this.originalParents = new HashMap<>();

        for (Spatial rack : placedRacks) {
            if (rack.getParent() instanceof Node) {
                originalParents.put(rack, rack.getParent());
            }
        }
    }

    @Override
    public void undo() {
        for (Spatial rack : placedRacks) {
            if (rack.getParent() != null) {
                rack.removeFromParent();
            }
        }
    }

    @Override
    public void redo() {
        for (Spatial rack : placedRacks) {
            Node originalParent = originalParents.get(rack);
            if (originalParent != null) {
                originalParent.attachChild(rack);
            } else {
                throw new IllegalStateException("Original parent for rack is not found.");
            }
        }
    }

    public List<Spatial> getPlacedRacks() {
        return placedRacks;
    }
}
