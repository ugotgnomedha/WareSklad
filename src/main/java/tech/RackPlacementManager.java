package tech;

import UndoRedo.RackPlacementAction;
import UndoRedo.UndoManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import java.util.*;
import java.util.concurrent.Future;

public class RackPlacementManager {

    private final UndoManager undoManager;
    private final Map<Geometry, List<Vector3f>> completeFloorVertices;
    private final Map<Integer, Vector3f> completeFloorCenters;
    private List<Spatial> sceneObjects;
    private final WareSkladInit jmeScene;
    private final Map<Geometry, Float> floorCompleteAreas;
    private final Map<Geometry, Float> floorSegmentDistances;

    private float RACK_HEIGHT = 20.0f;
    private float RACK_WIDTH = 18.0f;
    private float RACK_DEPTH = 11.0f;
    private float CEILING_HEIGHT = 300.f;
    private float MIN_DISTANCE_TO_OBSTACLES = 4.5f;
    private float MIN_DISTANCE_BETWEEN_ROWS = 12.0f;

    public RackPlacementManager(UndoManager undoManager, WareSkladInit jmeScene) {
        this.undoManager = undoManager;
        this.jmeScene = jmeScene;
        this.completeFloorVertices = undoManager.getCompleteFloorVertices();
        this.completeFloorCenters = undoManager.getCompleteFloorCenters();
        this.sceneObjects = undoManager.getCurrentSceneObjects();
        this.floorCompleteAreas = undoManager.getFloorCompleteAreas();
        this.floorSegmentDistances = undoManager.getFloorSegmentDistances();
    }

    public void placeRacks(Geometry selectedFloor, float rackWidth, float rackDepth, float rackHeight, float ceilingHeight, float minObstacleDistance, float minRowDistance) {
        this.RACK_HEIGHT = rackHeight;
        this.RACK_WIDTH = rackWidth;
        this.RACK_DEPTH = rackDepth;
        this.CEILING_HEIGHT = ceilingHeight;
        this.MIN_DISTANCE_TO_OBSTACLES = minObstacleDistance;
        this.MIN_DISTANCE_BETWEEN_ROWS = minRowDistance;

        if (selectedFloor == null) {
            throw new IllegalArgumentException("Selected floor geometry cannot be null.");
        }

        this.sceneObjects = undoManager.getCurrentSceneObjects();
        List<Spatial> filteredSceneObjects = new ArrayList<>(sceneObjects);
        for (Spatial sceneObject : sceneObjects) {
            if (sceneObject instanceof Geometry) {
                Geometry geometry = (Geometry) sceneObject;
                if (floorCompleteAreas.containsKey(geometry) || floorSegmentDistances.containsKey(geometry)) {
                    filteredSceneObjects.remove(sceneObject);
                }
            }
        }

        List<Vector3f> floorVertices = completeFloorVertices.get(selectedFloor);
        if (floorVertices == null) {
            throw new IllegalArgumentException("The selected floor geometry is not found in the completeFloorVertices map.");
        }

        Integer floorId = undoManager.getFloorSegmentToFloorId().get(selectedFloor);
        Vector3f floorCenter = completeFloorCenters.get(floorId);

        if (floorCenter == null) {
            throw new IllegalArgumentException("The selected floor geometry does not have a floor center in the completeFloorCenters map.");
        }

        List<Vector3f> absoluteVertices = new ArrayList<>(floorVertices.size());
        for (Vector3f vertex : floorVertices) absoluteVertices.add(floorCenter.add(vertex));

        Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

        for (Vector3f vertex : absoluteVertices) {
            min.x = Math.min(min.x, vertex.x);
            min.y = Math.min(min.y, vertex.y);
            min.z = Math.min(min.z, vertex.z);
            max.x = Math.max(max.x, vertex.x);
            max.y = Math.max(max.y, vertex.y);
            max.z = Math.max(max.z, vertex.z);
        }

        List<Spatial> placedRacks = new ArrayList<>();

        float xSpace = max.x - min.x, zSpace = max.z - min.z;
        boolean placeRowsAlongX = (xSpace / (RACK_WIDTH + MIN_DISTANCE_BETWEEN_ROWS)) >= (zSpace / (RACK_WIDTH + MIN_DISTANCE_BETWEEN_ROWS));

        float floorY = min.y;

        int racksPerStack = (int) Math.floor(CEILING_HEIGHT / RACK_HEIGHT);
        if (racksPerStack <= 0) {
            throw new IllegalArgumentException("Ceiling height is too low to place even one rack.");
        }

        if (placeRowsAlongX) {
            for (float x = min.x + RACK_DEPTH / 2 + MIN_DISTANCE_TO_OBSTACLES; x <= max.x - RACK_DEPTH / 2 - MIN_DISTANCE_TO_OBSTACLES; x += RACK_DEPTH + MIN_DISTANCE_BETWEEN_ROWS) {
                for (float z = min.z + RACK_WIDTH / 2 + MIN_DISTANCE_TO_OBSTACLES; z <= max.z - RACK_WIDTH / 2 - MIN_DISTANCE_TO_OBSTACLES; z += RACK_WIDTH) {
                    Vector3f rackBasePosition = new Vector3f(x, floorY, z);
                    if (isPointInPolygon(rackBasePosition, absoluteVertices) && !isCollidingWithSceneObjects(rackBasePosition, RACK_WIDTH, RACK_DEPTH, filteredSceneObjects)) {
                        if (rackBasePosition.z + RACK_WIDTH / 2 > max.z - MIN_DISTANCE_TO_OBSTACLES) {
                            continue;
                        }
                        System.out.println("Rack at position: " + rackBasePosition + ", max.z: " + max.z);
                        for (int stackLevel = 0; stackLevel < racksPerStack; stackLevel++) {
                            float stackedY = floorY + stackLevel * RACK_HEIGHT;
                            Vector3f rackPosition = new Vector3f(x, stackedY, z);
                            Spatial rack = visualizeRackPlacement(rackPosition, RACK_WIDTH, RACK_DEPTH, RACK_HEIGHT, false);
                            placedRacks.add(rack);
                        }
                    }
                }
            }
        } else {
            for (float z = min.z + RACK_DEPTH / 2 + MIN_DISTANCE_TO_OBSTACLES; z <= max.z - RACK_DEPTH / 2 - MIN_DISTANCE_TO_OBSTACLES; z += RACK_DEPTH + MIN_DISTANCE_BETWEEN_ROWS) {
                for (float x = min.x + RACK_WIDTH / 2 + MIN_DISTANCE_TO_OBSTACLES; x <= max.x - RACK_WIDTH / 2 - MIN_DISTANCE_TO_OBSTACLES; x += RACK_WIDTH) {
                    Vector3f rackBasePosition = new Vector3f(x, floorY, z);
                    if (isPointInPolygon(rackBasePosition, absoluteVertices) && !isCollidingWithSceneObjects(rackBasePosition, RACK_DEPTH, RACK_WIDTH, filteredSceneObjects)) {
                        if (rackBasePosition.z + RACK_WIDTH / 2 > max.z - MIN_DISTANCE_TO_OBSTACLES) {
                            continue;
                        }
                        System.out.println("Rack at position: " + rackBasePosition + ", max.z: " + max.z);
                        for (int stackLevel = 0; stackLevel < racksPerStack; stackLevel++) {
                            float stackedY = floorY + stackLevel * RACK_HEIGHT;
                            Vector3f rackPosition = new Vector3f(x, stackedY, z);
                            Spatial rack = visualizeRackPlacement(rackPosition, RACK_DEPTH, RACK_WIDTH, RACK_HEIGHT, true);
                            placedRacks.add(rack);
                        }
                    }
                }
            }
        }

        RackPlacementAction rackPlacementAction = new RackPlacementAction(placedRacks);
        undoManager.addAction(rackPlacementAction);
    }

    private Spatial visualizeRackPlacement(Vector3f position, float rackWidth, float rackDepth, float rackHeight, boolean rotate) {
        Future<Spatial> rackFuture = jmeScene.enqueue(() -> {
            Spatial rackModel = jmeScene.getAssetManager().loadModel("Models/Racks/Rack200cm180cm110cm.j3o");

            float modelWidth = 1.8f;
            float modelHeight = 2.0f;
            float modelDepth = 1.1f;

            final float finalRackWidth = rotate ? rackDepth : rackWidth;
            final float finalRackDepth = rotate ? rackWidth : rackDepth;

            float scaleX = finalRackWidth / modelWidth;
            float scaleY = rackHeight / modelHeight;
            float scaleZ = finalRackDepth / modelDepth;

            rackModel.setLocalScale(scaleX, scaleY, scaleZ);
            rackModel.setLocalTranslation(position);

            if (rotate) {
                rackModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI / 2, Vector3f.UNIT_Y));
            } else {
                rackModel.setLocalRotation(Quaternion.IDENTITY);
            }

            rackModel.updateModelBound();
            rackModel.updateGeometricState();

            jmeScene.getRootNode().attachChild(rackModel);
            return rackModel;
        });
        try {
            return rackFuture.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create rack model", e);
        }
    }

    private boolean isPointInPolygon(Vector3f point, List<Vector3f> polygon) {
        int intersectCount = 0;
        for (int i = 0; i < polygon.size(); i++) {
            Vector3f v1 = polygon.get(i), v2 = polygon.get((i + 1) % polygon.size());
            if (((v1.z > point.z) != (v2.z > point.z)) &&
                    (point.x < (v2.x - v1.x) * (point.z - v1.z) / (v2.z - v1.z) + v1.x)) {
                intersectCount++;
            }
        }
        return (intersectCount % 2) == 1;
    }

    private boolean checkCollision(Vector3f rackPosition, float rackWidth, float rackDepth, Vector3f objectPosition, float objectWidth, float objectDepth) {
        return (Math.abs(rackPosition.x - objectPosition.x) < (rackWidth + objectWidth) / 2 + MIN_DISTANCE_TO_OBSTACLES) &&
                (Math.abs(rackPosition.z - objectPosition.z) < (rackDepth + objectDepth) / 2 + MIN_DISTANCE_TO_OBSTACLES);
    }

    private boolean isCollidingWithSceneObjects(Vector3f rackPosition, float rackWidth, float rackDepth, List<Spatial> filteredSceneObjects) {
        for (Spatial sceneObject : filteredSceneObjects) {
            if (sceneObject instanceof Geometry) {
                Geometry geometry = (Geometry) sceneObject;
                Vector3f objectPosition = geometry.getLocalTranslation();

                BoundingVolume boundingVolume = geometry.getWorldBound();
                if (boundingVolume instanceof BoundingBox || boundingVolume instanceof BoundingSphere) {
                    Vector3f objectDimensions = getObjectDimensions(boundingVolume);

                    if (checkCollision(rackPosition, rackWidth, rackDepth, objectPosition, objectDimensions.x, objectDimensions.z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Vector3f getObjectDimensions(BoundingVolume volume) {
        if (volume instanceof BoundingBox) {
            BoundingBox box = (BoundingBox) volume;
            return new Vector3f(box.getXExtent() * 2, box.getYExtent() * 2, box.getZExtent() * 2);
        } else if (volume instanceof BoundingSphere) {
            BoundingSphere sphere = (BoundingSphere) volume;
            float diameter = sphere.getRadius() * 2;
            return new Vector3f(diameter, diameter, diameter);
        }
        return Vector3f.ZERO;
    }
}
