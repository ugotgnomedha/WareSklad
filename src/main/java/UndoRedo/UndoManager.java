package UndoRedo;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import java.util.*;

public class UndoManager {
    private Stack<UndoableAction> undoStack = new Stack<>();
    private Stack<UndoableAction> redoStack = new Stack<>();
    private List<UndoRedoListener> listeners = new ArrayList<>();
    private List<Spatial> sceneObjects = new ArrayList<>();

    private final Map<Geometry, Float> floorCompleteAreas = new HashMap<>();
    private final Map<Geometry, Float> floorSegmentDistances = new HashMap<>();
    private final Map<Integer, Vector3f> completeFloorCenters = new HashMap<>();
    private final Map<Geometry, Integer> floorSegmentToFloorId = new HashMap<>();
    private final Map<Integer, List<Geometry>> floorIdToSegments = new HashMap<>();
    private final Map<Geometry, List<Vector3f>> floorSegmentVertices = new HashMap<>();
    private final Map<Geometry, List<Vector3f>> completeFloorVertices = new HashMap<>();

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

    public void addAction(UndoableAction action) {
        undoStack.push(action);
        redoStack.clear();

        if (action instanceof ModelLoadAction) {
            ModelLoadAction loadAction = (ModelLoadAction) action;
            sceneObjects.add(loadAction.getModel());
        } else if (action instanceof FloorPlacementAction) {
            FloorPlacementAction floorAction = (FloorPlacementAction) action;
            sceneObjects.addAll(floorAction.getFloorGeometries());
        } else if (action instanceof DeleteAction) {
            DeleteAction deleteAction = (DeleteAction) action;
            sceneObjects.remove(deleteAction.getDeletedObject());
        } else if (action instanceof FloorDeleteAction) {
            FloorDeleteAction floorDeleteAction = (FloorDeleteAction) action;
            removeFloorRelatedData(floorDeleteAction);
            sceneObjects.removeAll(floorDeleteAction.getFloorGeometries());
        }

        notifyListeners();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            UndoableAction action = undoStack.pop();
            action.undo();
            redoStack.push(action);

            if (action instanceof ModelLoadAction) {
                sceneObjects.remove(((ModelLoadAction) action).getModel());
            } else if (action instanceof FloorPlacementAction) {
                FloorPlacementAction floorAction = (FloorPlacementAction) action;
                sceneObjects.removeAll(floorAction.getFloorGeometries());

                floorAction.getFloorCompleteAreas().keySet().forEach(floorCompleteAreas::remove);
                floorAction.getFloorSegmentDistances().keySet().forEach(floorSegmentDistances::remove);
                floorAction.getFloorSegmentToFloorId().keySet().forEach(floorSegmentToFloorId::remove);
                floorAction.getFloorSegmentVertices().keySet().forEach(floorSegmentVertices::remove);
                floorAction.getCompleteFloorVertices().keySet().forEach(completeFloorVertices::remove);

                floorAction.getFloorIdToSegments().forEach((key, value) -> {
                    List<Geometry> managerSegments = floorIdToSegments.get(key);
                    if (managerSegments != null) {
                        managerSegments.removeAll(value);
                        if (managerSegments.isEmpty()) {
                            floorIdToSegments.remove(key);
                        }
                    }
                });

                floorAction.getCompleteFloorCenters().keySet().forEach(completeFloorCenters::remove);
            } else if (action instanceof DeleteAction) {
                sceneObjects.add(((DeleteAction) action).getDeletedObject());
            } else if (action instanceof FloorDeleteAction) {
                FloorDeleteAction floorDeleteAction = (FloorDeleteAction) action;

                sceneObjects.addAll(floorDeleteAction.getFloorGeometries());

                for (Geometry geometry : floorDeleteAction.getFloorGeometries()) {
                    floorCompleteAreas.put(geometry, floorDeleteAction.getFloorCompleteAreas().get(geometry));
                    floorSegmentDistances.put(geometry, floorDeleteAction.getFloorSegmentDistances().get(geometry));
                    floorSegmentToFloorId.put(geometry, floorDeleteAction.getFloorSegmentToFloorId().get(geometry));
                    floorSegmentVertices.put(geometry, floorDeleteAction.getFloorSegmentVertices().get(geometry));
                    completeFloorVertices.put(geometry, floorDeleteAction.getCompleteFloorVertices().get(geometry));

                    Integer floorId = floorDeleteAction.getFloorSegmentToFloorId().get(geometry);
                    if (floorId != null) {
                        List<Geometry> managerSegments = floorIdToSegments.computeIfAbsent(floorId, k -> new ArrayList<>());
                        managerSegments.add(geometry);
                    }
                }

                floorDeleteAction.getCompleteFloorCenters().forEach(completeFloorCenters::put);
            }

            notifyListeners();
        }
    }


    public void redo() {
        if (!redoStack.isEmpty()) {
            UndoableAction action = redoStack.pop();
            action.redo();
            undoStack.push(action);

            if (action instanceof ModelLoadAction) {
                sceneObjects.add(((ModelLoadAction) action).getModel());
            } else if (action instanceof FloorPlacementAction) {
                FloorPlacementAction floorAction = (FloorPlacementAction) action;
                sceneObjects.addAll(floorAction.getFloorGeometries());

                floorAction.getFloorCompleteAreas().forEach(floorCompleteAreas::put);
                floorAction.getFloorSegmentDistances().forEach(floorSegmentDistances::put);
                floorAction.getCompleteFloorCenters().forEach(completeFloorCenters::put);
                floorAction.getFloorSegmentToFloorId().forEach(floorSegmentToFloorId::put);

                floorAction.getFloorIdToSegments().forEach((key, value) ->
                        floorIdToSegments.merge(key, new ArrayList<>(value), (v1, v2) -> {
                            v1.addAll(v2);
                            return v1;
                        })
                );

                floorAction.getFloorSegmentVertices().forEach(floorSegmentVertices::put);
                floorAction.getCompleteFloorVertices().forEach(completeFloorVertices::put);
            } else if (action instanceof DeleteAction) {
                sceneObjects.remove(((DeleteAction) action).getDeletedObject());
            } else if (action instanceof FloorDeleteAction) {
                FloorDeleteAction floorDeleteAction = (FloorDeleteAction) action;

                sceneObjects.removeAll(floorDeleteAction.getFloorGeometries());

                for (Geometry geometry : floorDeleteAction.getFloorGeometries()) {
                    Integer floorId = floorSegmentToFloorId.get(geometry);
                    if (floorId != null) {
                        List<Geometry> segments = floorIdToSegments.get(floorId);
                        if (segments != null) {
                            segments.remove(geometry);
                            if (segments.isEmpty()) {
                                floorIdToSegments.remove(floorId);
                            }
                        }
                    }
                    floorCompleteAreas.remove(geometry);
                    floorSegmentDistances.remove(geometry);
                    floorSegmentToFloorId.remove(geometry);
                    floorSegmentVertices.remove(geometry);
                    completeFloorVertices.remove(geometry);
                }
            }

            notifyListeners();
        }
    }

    public void addUndoRedoListener(UndoRedoListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (UndoRedoListener listener : listeners) {
            listener.onUndoRedo();
        }
    }

    public List<Spatial> getCurrentSceneObjects() {
        return new ArrayList<>(sceneObjects);
    }

    public void setSceneObjects(List<Spatial> objects) {
        this.sceneObjects = objects;
        System.out.println();
        notifyListeners();
    }

    public boolean isFloorRelated(Spatial model) {
        if (model instanceof Geometry) {
            Geometry geometry = (Geometry) model;

            if (floorSegmentToFloorId.containsKey(geometry)) {
                return true;
            }

            for (List<Geometry> segmentList : floorIdToSegments.values()) {
                if (segmentList.contains(geometry)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void removeFloorRelatedData(FloorDeleteAction floorDeleteAction) {
        floorDeleteAction.getFloorGeometries().forEach(geometry -> {
            floorCompleteAreas.remove(geometry);
            floorSegmentDistances.remove(geometry);
            floorSegmentToFloorId.remove(geometry);
            floorSegmentVertices.remove(geometry);
            completeFloorVertices.remove(geometry);
        });

        for (Geometry geometry : floorDeleteAction.getFloorGeometries()) {
            Integer floorId = floorSegmentToFloorId.get(geometry);
            if (floorId != null) {
                List<Geometry> segments = floorIdToSegments.get(floorId);
                if (segments != null) {
                    segments.remove(geometry);
                    if (segments.isEmpty()) {
                        floorIdToSegments.remove(floorId);
                    }
                }
            }
        }
    }

    public Stack<UndoableAction> getUndoStack() {
        return undoStack;
    }
}
