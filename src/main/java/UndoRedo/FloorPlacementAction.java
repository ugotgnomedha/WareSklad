package UndoRedo;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import java.util.List;
import java.util.Map;

public class FloorPlacementAction implements UndoableAction {
    private final List<Geometry> floorGeometries;
    private final Node rootNode;

    private final Map<Geometry, Float> floorCompleteAreas;
    private final Map<Geometry, Float> floorSegmentDistances;
    private final Map<Integer, Vector3f> completeFloorCenters;
    private final Map<Geometry, Integer> floorSegmentToFloorId;
    private final Map<Integer, List<Geometry>> floorIdToSegments;
    private final Map<Geometry, List<Vector3f>> floorSegmentVertices;
    private final Map<Geometry, List<Vector3f>> completeFloorVertices;

    public FloorPlacementAction(
            List<Geometry> floorGeometries,
            Node rootNode,
            Map<Geometry, Float> floorCompleteAreas,
            Map<Geometry, Float> floorSegmentDistances,
            Map<Integer, Vector3f> completeFloorCenters,
            Map<Geometry, Integer> floorSegmentToFloorId,
            Map<Integer, List<Geometry>> floorIdToSegments,
            Map<Geometry, List<Vector3f>> floorSegmentVertices,
            Map<Geometry, List<Vector3f>> completeFloorVertices) {
        this.floorGeometries = floorGeometries;
        this.rootNode = rootNode;

        this.floorCompleteAreas = floorCompleteAreas;
        this.floorSegmentDistances = floorSegmentDistances;
        this.completeFloorCenters = completeFloorCenters;
        this.floorSegmentToFloorId = floorSegmentToFloorId;
        this.floorIdToSegments = floorIdToSegments;
        this.floorSegmentVertices = floorSegmentVertices;
        this.completeFloorVertices = completeFloorVertices;
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

    public Map<Geometry, Float> getFloorCompleteAreas() {
        return floorCompleteAreas;
    }

    public Map<Geometry, Float> getFloorSegmentDistances() {
        return floorSegmentDistances;
    }

    public Map<Integer, Vector3f> getCompleteFloorCenters() {
        return completeFloorCenters;
    }

    public Map<Geometry, Integer> getFloorSegmentToFloorId() {
        return floorSegmentToFloorId;
    }

    public Map<Integer, List<Geometry>> getFloorIdToSegments() {
        return floorIdToSegments;
    }

    public Map<Geometry, List<Vector3f>> getFloorSegmentVertices() {
        return floorSegmentVertices;
    }

    public Map<Geometry, List<Vector3f>> getCompleteFloorVertices() {
        return completeFloorVertices;
    }
}
