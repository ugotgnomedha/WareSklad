package tech;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;
import ui.Grid;

import java.util.ArrayList;
import java.util.List;

public class MeasureTool {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final InputManager inputManager;
    private final Camera cam;
    private boolean measureMode = false;
    private boolean snapping = false;
    private Vector3f startPoint = null;
    private Geometry previewLineGeometry = null;
    private final Node measurementNode;
    private List<Geometry> measurementLines = new ArrayList<>();
    private List<BitmapText> measurementTexts = new ArrayList<>();

    public MeasureTool(Node rootNode, AssetManager assetManager, InputManager inputManager, Camera cam) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.inputManager = inputManager;
        this.cam = cam;
        this.measurementNode = new Node("MeasurementNode");
        this.rootNode.attachChild(this.measurementNode);
        setupInput();
    }

    public void setMeasureMode(boolean measureMode) {
        this.measureMode = measureMode;
        if (!measureMode) {
            resetMeasurement();
        }
    }

    public void setSnapping(boolean snapping) {
        this.snapping = snapping;
    }

    private void setupInput() {
        inputManager.addMapping("PlaceMeasureLine", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("CancelMeasurement", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(measurementListener, "PlaceMeasureLine");
        inputManager.addListener(cancelMeasurementListener, "CancelMeasurement");
    }

    private final ActionListener measurementListener = (name, isPressed, tpf) -> {
        if (isPressed && measureMode) {
            Vector3f gridPoint = getMouseGridIntersection();
            if (gridPoint == null) return;

            if (snapping) {
                gridPoint = snapToGrid(gridPoint);
            }

            if (startPoint == null) {
                startPoint = gridPoint;
            } else {
                Vector3f endPoint = gridPoint;
                placeMeasurementLine(startPoint, endPoint);
                startPoint = null;
            }
        }
    };

    private final ActionListener cancelMeasurementListener = (name, isPressed, tpf) -> {
        if (isPressed && measureMode) {
            setMeasureMode(false);
        }
    };

    private void resetMeasurement() {
        startPoint = null;

        if (previewLineGeometry != null) {
            rootNode.detachChild(previewLineGeometry);
            previewLineGeometry = null;
        }

        for (Geometry line : measurementLines) {
            rootNode.detachChild(line);
        }
        measurementLines.clear();

        for (BitmapText text : measurementTexts) {
            measurementNode.detachChild(text);
        }
        measurementTexts.clear();
    }

    private void placeMeasurementLine(Vector3f start, Vector3f end) {
        Line line = new Line(start, end);
        Geometry lineGeometry = new Geometry("MeasurementLine", line);

        Material lineMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", ColorRGBA.Yellow);
        lineGeometry.setMaterial(lineMaterial);

        rootNode.attachChild(lineGeometry);
        measurementLines.add(lineGeometry);

        updateMeasurementText(start, end);
    }

    private void updateMeasurementText(Vector3f start, Vector3f end) {
        float distance = start.distance(end) / Grid.GRID_SPACING;
        String distanceText = String.format("%.2f m", distance);

        BitmapText text = createMeasurementText(distanceText);
        Vector3f textPosition = start.add(end).mult(0.5f);
        text.setLocalTranslation(textPosition.x, textPosition.y + 1, textPosition.z);
        text.rotate(FastMath.DEG_TO_RAD * 90, 0, FastMath.DEG_TO_RAD * 180);
        measurementNode.attachChild(text);
        measurementTexts.add(text);
    }

    private BitmapText createMeasurementText(String text) {
        BitmapText bitmapText = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"), false);
        bitmapText.setText(text);
        bitmapText.setColor(ColorRGBA.White);
        bitmapText.setLocalScale(0.5f);
        return bitmapText;
    }

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
        float snappedX = Math.round(point.x / Grid.GRID_SPACING) * Grid.GRID_SPACING;
        float snappedZ = Math.round(point.z / Grid.GRID_SPACING) * Grid.GRID_SPACING;
        return new Vector3f(snappedX, Grid.GRID_Y_LEVEL, snappedZ);
    }

    public void updateMeasureToolPreview() {
        if (measureMode) {
            Vector3f gridPoint = getMouseGridIntersection();
            if (gridPoint != null && startPoint != null) {
                if (snapping) {
                    gridPoint = snapToGrid(gridPoint);
                }
                updatePreviewLineGeometry(gridPoint);
            }
        }
    }

    private void updatePreviewLineGeometry(Vector3f gridPoint) {
        if (startPoint == null) return;

        if (previewLineGeometry != null) {
            rootNode.detachChild(previewLineGeometry);
        }

        Line previewLine = new Line(startPoint, gridPoint);
        previewLineGeometry = new Geometry("PreviewMeasurementLine", previewLine);

        Material previewMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        previewMaterial.setColor("Color", ColorRGBA.Yellow);
        previewLineGeometry.setMaterial(previewMaterial);

        rootNode.attachChild(previewLineGeometry);
    }
}
