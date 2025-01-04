package tech;

import UndoRedo.PlainAreaPlacementAction;
import UndoRedo.UndoManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import ui.Grid;

import java.util.*;

public class PlainAreaPlacer {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final InputManager inputManager;
    private final Camera cam;
    private final UndoManager undoManager;
    private WareSkladInit jmeScene;

    private Vector3f startPoint = null;
    private Vector3f endPoint = null;
    private List<Geometry> plainAreaGeometries = new ArrayList<>();
    private Geometry previewPlainAreaGeometry = null;

    private boolean plainAreaMode = false;

    private Node measurementNode;

    public PlainAreaPlacer(Node rootNode, AssetManager assetManager, InputManager inputManager, Camera cam, UndoManager undoManager, WareSkladInit jmeScene) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.inputManager = inputManager;
        this.cam = cam;
        this.undoManager = undoManager;
        this.jmeScene = jmeScene;

        this.measurementNode = new Node("MeasurementNode");
        this.rootNode.attachChild(this.measurementNode);
        setupInput();
    }

    public boolean isPlainAreaMode() {
        return plainAreaMode;
    }

    public void setPlainAreaMode(boolean plainAreaMode) {
        this.plainAreaMode = plainAreaMode;
    }

    private void setupInput() {
        inputManager.addMapping("PlacePlainArea", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("CancelPlacement", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(plainAreaPlacementListener, "PlacePlainArea");
        inputManager.addListener(cancelPlacementListener, "CancelPlacement");
    }

    private final ActionListener plainAreaPlacementListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            if (!plainAreaMode) {
                return;
            }

            Vector3f gridPoint = getMouseGridIntersection();
            if (gridPoint == null) return;

            gridPoint = snapToGrid(gridPoint);

            if (startPoint == null) {
                startPoint = gridPoint;
            } else {
                endPoint = gridPoint;
                createPlainArea(startPoint, endPoint);
            }
        }
    };

    private final ActionListener cancelPlacementListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            resetPlainAreaPlacement();
            plainAreaMode = false;
        }
    };

    private Vector3f getMouseGridIntersection() {
        Vector2f cursorPosition = inputManager.getCursorPosition();
        Ray ray = new Ray(cam.getWorldCoordinates(cursorPosition, 0f),
                cam.getWorldCoordinates(cursorPosition, 1f).subtractLocal(
                        cam.getWorldCoordinates(cursorPosition, 0f)));

        Plane gridPlane = new Plane(Vector3f.UNIT_Y, Grid.GRID_Y_LEVEL);

        Vector3f intersection = new Vector3f();
        if (ray.intersectsWherePlane(gridPlane, intersection)) {
            return intersection;
        }

        return null;
    }

    private Vector3f snapToGrid(Vector3f point) {
        float snappedX = (float) Math.floor(point.x / Grid.GRID_SPACING) * Grid.GRID_SPACING;
        float snappedZ = (float) Math.floor(point.z / Grid.GRID_SPACING) * Grid.GRID_SPACING;
        return new Vector3f(snappedX, Grid.GRID_Y_LEVEL, snappedZ);
    }

    public void createPlainArea(Vector3f start, Vector3f end) {
        if (start != null && end != null) {

            float corner1X = Math.min(start.x, end.x);
            float corner1Z = Math.min(start.z, end.z);

            float corner2X = Math.max(start.x, end.x);
            float corner2Z = Math.max(start.z, end.z);

            Vector3f corner1 = new Vector3f(corner1X, Grid.GRID_Y_LEVEL, corner1Z);
            Vector3f corner2 = new Vector3f(corner2X, Grid.GRID_Y_LEVEL, corner2Z);

            float length = Math.abs(corner2.x - corner1.x) / Grid.GRID_SPACING;
            float width = Math.abs(corner2.z - corner1.z) / Grid.GRID_SPACING;
            float area = length * width;

            Box plainAreaBox = new Box(corner2.x/2 - corner1.x/2, 0.01f, corner2.z/2 - corner1.z/2);
            Geometry plainAreaGeometry = new Geometry("PlainArea", plainAreaBox);

            Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", ColorRGBA.Green);
            plainAreaGeometry.setMaterial(material);

            plainAreaGeometry.setLocalTranslation(corner1.add(corner2).multLocal(0.5f));

            rootNode.attachChild(plainAreaGeometry);
            plainAreaGeometries.add(plainAreaGeometry);

            Map<Geometry, Float> plainAreaCompleteAreas = new HashMap<>();
            plainAreaCompleteAreas.put(plainAreaGeometry, area);

            undoManager.addAction(new PlainAreaPlacementAction(plainAreaGeometry, rootNode, plainAreaCompleteAreas));

            resetPlainAreaPlacement();
        }
    }

    private void resetPlainAreaPlacement() {
        startPoint = null;
        endPoint = null;
        clearMeasurements();

        if (previewPlainAreaGeometry != null) {
            rootNode.detachChild(previewPlainAreaGeometry);
            previewPlainAreaGeometry = null;
        }
    }

    private void clearMeasurements() {
        measurementNode.detachAllChildren();
    }

    public void updatePreview() {
        if (plainAreaMode) {
            Vector3f gridPoint = getMouseGridIntersection();
            if (gridPoint != null && startPoint != null) {
                gridPoint = snapToGrid(gridPoint);
                updatePreviewPlainAreaGeometry(gridPoint);
            }
        }
    }

    private void updatePreviewPlainAreaGeometry(Vector3f gridPoint) {
        if (startPoint == null) return;

        if (previewPlainAreaGeometry != null) {
            rootNode.detachChild(previewPlainAreaGeometry);
        }

        float corner1X = Math.min(startPoint.x, gridPoint.x);
        float corner1Z = Math.min(startPoint.z, gridPoint.z);

        float corner2X = Math.max(startPoint.x, gridPoint.x);
        float corner2Z = Math.max(startPoint.z, gridPoint.z);

        Vector3f corner1 = new Vector3f(corner1X, Grid.GRID_Y_LEVEL, corner1Z);
        Vector3f corner2 = new Vector3f(corner2X, Grid.GRID_Y_LEVEL, corner2Z);

        Box previewBox = new Box(Math.abs(corner2.x - corner1.x) / 2, 0.01f, Math.abs(corner2.z - corner1.z) / 2);
        previewPlainAreaGeometry = new Geometry("PreviewPlainArea", previewBox);

        Material previewMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        previewMaterial.setColor("Color", ColorRGBA.Yellow);
        previewPlainAreaGeometry.setMaterial(previewMaterial);

        Vector3f center = corner1.add(corner2).multLocal(0.5f);
        previewPlainAreaGeometry.setLocalTranslation(center);

        rootNode.attachChild(previewPlainAreaGeometry);
    }


    public void placeTextureOnArea(Texture texture) {
        if (!plainAreaGeometries.isEmpty()) {
            for (Geometry geometry : plainAreaGeometries) {
                Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                material.setTexture("ColorMap", texture);
                geometry.setMaterial(material);
            }
        }
    }

    public void clearAllPlainAreas() {
        for (Geometry geometry : plainAreaGeometries) {
            rootNode.detachChild(geometry);
        }
        plainAreaGeometries.clear();
    }
}
