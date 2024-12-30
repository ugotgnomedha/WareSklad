package UndoRedo;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloorDeleteAction implements UndoableAction {

    private final List<Geometry> floorGeometries;
    private final Node rootNode;

    private final Map<Geometry, Float> floorCompleteAreas = new HashMap<>();
    private final Map<Geometry, Float> floorSegmentDistances = new HashMap<>();
    private final Map<Integer, Vector3f> completeFloorCenters = new HashMap<>();
    private final Map<Geometry, Integer> floorSegmentToFloorId = new HashMap<>();
    private final Map<Integer, List<Geometry>> floorIdToSegments = new HashMap<>();
    private final Map<Geometry, List<Vector3f>> floorSegmentVertices = new HashMap<>();
    private final Map<Geometry, List<Vector3f>> completeFloorVertices = new HashMap<>();

    public FloorDeleteAction(
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

        for (Geometry geometry : floorGeometries) {
            if (floorCompleteAreas.containsKey(geometry)) {
                this.floorCompleteAreas.put(geometry, floorCompleteAreas.get(geometry));
            }
            if (floorSegmentDistances.containsKey(geometry)) {
                this.floorSegmentDistances.put(geometry, floorSegmentDistances.get(geometry));
            }
            if (floorSegmentToFloorId.containsKey(geometry)) {
                this.floorSegmentToFloorId.put(geometry, floorSegmentToFloorId.get(geometry));
            }
            if (floorSegmentVertices.containsKey(geometry)) {
                this.floorSegmentVertices.put(geometry, new ArrayList<>(floorSegmentVertices.get(geometry)));
            }
            if (completeFloorVertices.containsKey(geometry)) {
                this.completeFloorVertices.put(geometry, new ArrayList<>(completeFloorVertices.get(geometry)));
            }
        }
        this.completeFloorCenters.putAll(completeFloorCenters);
        floorIdToSegments.forEach((key, value) ->
                this.floorIdToSegments.put(key, new ArrayList<>(value))
        );
    }


    @Override
    public void undo() {
        for (Geometry geometry : floorGeometries) {
            rootNode.attachChild(geometry);
        }
    }

    @Override
    public void redo() {
        for (Geometry geometry : floorGeometries) {
            rootNode.detachChild(geometry);
        }
    }

    public Map<Geometry, Float> getFloorCompleteAreas() {
        return floorCompleteAreas;
    }

    public Map<Geometry, Float> getFloorSegmentDistances() {
        return floorSegmentDistances;
    }

    public Map<Geometry, Integer> getFloorSegmentToFloorId() {
        return floorSegmentToFloorId;
    }

    public Map<Geometry, List<Vector3f>> getFloorSegmentVertices() {
        return floorSegmentVertices;
    }

    public Map<Geometry, List<Vector3f>> getCompleteFloorVertices() {
        return completeFloorVertices;
    }

    public Map<Integer, Vector3f> getCompleteFloorCenters() {
        return completeFloorCenters;
    }

    public List<Geometry> getFloorGeometries() {
        return floorGeometries;
    }
}
