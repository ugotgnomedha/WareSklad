package tech;

import UndoRedo.PropertyChangeAction;
import UndoRedo.UndoManager;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import ui.Grid;
import ui.PropertiesPanel;
import ui.UILinesDrawer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class WareSkladInit extends SimpleApplication {
    private final Map<Class<? extends Spatial>, SelectionHandler> selectionHandlers = new HashMap<>();
    private final CountDownLatch initLatch = new CountDownLatch(1);

    private CameraController cameraController;
    private Grid grid;
    private InputHandler inputHandler;
    public ModelLoader modelLoader;

    private Spatial selectedObject;
    private PropertiesPanel propertiesPanel;
    public ObjectControls objectControls;
    public UndoManager undoManager;

    private BitmapText cursorInfoText;
    private BitmapText zoomInfoText;

    private FloorPlacer floorPlacer;
    private UILinesDrawer uiLinesDrawer;
    private GeometrySelectionHandler geometrySelectionHandler;

    public void setPropertiesPanel(PropertiesPanel propertiesPanel) {
        this.propertiesPanel = propertiesPanel;
        this.objectControls.setPropertiesPanel(propertiesPanel);
        this.geometrySelectionHandler.setPropertiesPanel(propertiesPanel);
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        cameraController = new CameraController(cam);
        grid = new Grid(assetManager, rootNode);

        undoManager = new UndoManager();
        inputHandler = new InputHandler(inputManager, cameraController, undoManager, this);

        cam.setLocation(new Vector3f(0, cameraController.getCurrentZoom(), 0));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);

        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        grid.addGrid();

        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(ambientLight);

        AppSettings settings = new AppSettings(true);
        settings.setResizable(true);
        settings.setWidth(1000);
        settings.setHeight(800);
        settings.setFullscreen(false);
        this.setSettings(settings);
        this.setDisplayStatView(false);
        this.setDisplayFps(false);

        objectControls = new ObjectControls(inputManager, assetManager, rootNode, cam, undoManager);

        floorPlacer = new FloorPlacer(rootNode, assetManager, inputManager, cam, undoManager);

        modelLoader = new ModelLoader(rootNode, assetManager, undoManager, floorPlacer, this);

        setupMouseClickListener();

        setupInfoText();

        uiLinesDrawer = new UILinesDrawer();
        uiLinesDrawer.addLines(this, rootNode);

        geometrySelectionHandler = new GeometrySelectionHandler(floorPlacer);
        selectionHandlers.put(Geometry.class, geometrySelectionHandler);

        initLatch.countDown();
    }

    public void waitForInitialization() throws InterruptedException {
        initLatch.await();
    }

    private void setupInfoText() {
        cursorInfoText = new BitmapText(guiFont, false);
        cursorInfoText.setSize(20);
        cursorInfoText.setColor(ColorRGBA.White);
        cursorInfoText.setLocalTranslation(10, 30, 0);
        guiNode.attachChild(cursorInfoText);

        zoomInfoText = new BitmapText(guiFont, false);
        zoomInfoText.setSize(20);
        zoomInfoText.setColor(ColorRGBA.White);
        zoomInfoText.setLocalTranslation(10, 50, 0);
        guiNode.attachChild(zoomInfoText);
    }

    private void setupMouseClickListener() {
        inputManager.addMapping("MouseClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        ActionListener mouseClickListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (!isPressed) {
                    handleObjectSelection();
                }
            }
        };

        inputManager.addListener(mouseClickListener, "MouseClick");
    }

    public void selectObject(Spatial object) {
        deselectObject();

        selectedObject = object;

        if (object != null) {
            updateProperties(object);
            if (objectControls != null) {
                objectControls.setSelectedObject(object);
            }

            SelectionHandler handler = selectionHandlers.get(object.getClass());
            if (handler != null) {
                handler.handleSelection(object);
            } else {
                System.out.println("No handler registered for object of type: " + object.getClass().getSimpleName());
            }
        }
    }

    private void updateProperties(Spatial object) {
        if (object == null || propertiesPanel == null) {
            return;
        }

        Vector3f originalPosition = object.getLocalTranslation().clone();
        float[] originalAngles = object.getLocalRotation().toAngles(null);
        Vector3f originalRotation = new Vector3f(originalAngles[0], originalAngles[1], originalAngles[2]);
        Vector3f originalScale = object.getLocalScale().clone();
        String originalName = object.getName() != null ? object.getName() : "Unnamed Object";

        Vector3f position = object.getLocalTranslation();
        float[] angles = object.getLocalRotation().toAngles(null);
        Vector3f rotation = new Vector3f(angles[0], angles[1], angles[2]);
        Vector3f scale = object.getLocalScale();
        String objectName = object.getName() != null ? object.getName() : "Unnamed Object";

        propertiesPanel.updateProperties(objectName, position, rotation, scale);

        propertiesPanel.setOnNameChange(name -> {
            if (selectedObject != null) {
                String previousName = selectedObject.getName();
                selectedObject.setName(name);

                undoManager.addAction(new PropertyChangeAction(
                        selectedObject,
                        previousName, originalPosition, originalRotation, originalScale,
                        name, originalPosition, originalRotation, originalScale
                ));
            }
        });

        propertiesPanel.setOnPositionChange(pos -> {
            if (selectedObject != null) {
                Vector3f prevPos = selectedObject.getLocalTranslation().clone();
                selectedObject.setLocalTranslation(pos);

                undoManager.addAction(new PropertyChangeAction(
                        selectedObject,
                        originalName, prevPos, originalRotation, originalScale,
                        originalName, pos, originalRotation, originalScale
                ));
            }
        });

        propertiesPanel.setOnRotationChange(rot -> {
            if (selectedObject != null) {
                Vector3f prevRot = new Vector3f(originalAngles[0], originalAngles[1], originalAngles[2]);
                Quaternion newRotation = new Quaternion().fromAngles(rot.x, rot.y, rot.z);
                selectedObject.setLocalRotation(newRotation);

                undoManager.addAction(new PropertyChangeAction(
                        selectedObject,
                        originalName, originalPosition, prevRot, originalScale,
                        originalName, originalPosition, rot, originalScale
                ));
            }
        });

        propertiesPanel.setOnScaleChange(scl -> {
            if (selectedObject != null) {
                Vector3f prevScale = selectedObject.getLocalScale().clone();
                selectedObject.setLocalScale(scl);

                undoManager.addAction(new PropertyChangeAction(
                        selectedObject,
                        originalName, originalPosition, originalRotation, prevScale,
                        originalName, originalPosition, originalRotation, scl
                ));
            }
        });
    }

    public void deselectObject() {
        selectedObject = null;

        if (propertiesPanel != null) {
            propertiesPanel.clearPropertiesPanel();

            propertiesPanel.setOnNameChange(null);
            propertiesPanel.setOnPositionChange(null);
            propertiesPanel.setOnRotationChange(null);
            propertiesPanel.setOnScaleChange(null);
        }

        objectControls.setSelectedObject(null);
    }

    private void handleObjectSelection() {
        Vector2f mousePos = inputManager.getCursorPosition();
        Vector3f near = cameraController.getCam().getWorldCoordinates(mousePos, 0);
        Vector3f far = cameraController.getCam().getWorldCoordinates(mousePos, 1);
        Ray ray = new Ray(near, far.subtract(near).normalizeLocal());

        CollisionResults results = new CollisionResults();
        rootNode.collideWith(ray, results);

        if (results.size() > 0) {
            Spatial clickedObject = results.getClosestCollision().getGeometry();

            if (!isExcluded(clickedObject)) {
                if (selectedObject != clickedObject || !isObjectInBounds(selectedObject)) {
                    deselectObject();
                    selectObject(clickedObject);
                }
            } else {
                deselectObject();
            }
        } else {
            deselectObject();
        }
    }

    private boolean isObjectInBounds(Spatial object) {
        if (object.getWorldBound() != null) {
            BoundingVolume bounds = object.getWorldBound();

            return bounds.contains(cam.getWorldCoordinates(inputManager.getCursorPosition(), 0));
        }
        return false;
    }

    private boolean isExcluded(Spatial object) {
        for (RayExcludedObjects excluded : RayExcludedObjects.values()) {
            if (object.getName().equals(excluded.getObjectName())) {
                return true;
            }
        }
        return false;
    }

    public Canvas getCanvas() {
        JmeCanvasContext context = (JmeCanvasContext) getContext();
        return context.getCanvas();
    }

    @Override
    public void simpleUpdate(float tpf) {
        cameraController.updateCameraPosition(tpf);

        Vector2f mousePos = inputManager.getCursorPosition();

        Vector3f near = cam.getWorldCoordinates(mousePos, 0);
        Vector3f far = cam.getWorldCoordinates(mousePos, 1);
        Ray ray = new Ray(near, far.subtract(near).normalizeLocal());

        Plane gridPlane = new Plane(Vector3f.UNIT_Y, 0);

        Vector3f intersection = new Vector3f();
        if (ray.intersectsWherePlane(gridPlane, intersection)) {
            float gridX = (float) Math.floor(intersection.x);
            float gridY = (float) Math.floor(intersection.z);

            cursorInfoText.setText("Cursor: X=" + gridX + " Y=" + gridY);
        }

        zoomInfoText.setText("Zoom: " + cameraController.getCurrentZoom());

        floorPlacer.updatePreview();
    }
}
