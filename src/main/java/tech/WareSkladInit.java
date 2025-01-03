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
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import tech.layers.LayersManager;
import ui.Grid;
import ui.PropertiesPanel;
import ui.UILinesDrawer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
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
    private LayersManager layersManager;
    public DeleteObject deleteObject;

    private BitmapText cursorInfoText;
    private BitmapText zoomInfoText;

    public FloorPlacer floorPlacer;
    public PlainAreaPlacer plainAreaPlacer;
    public MeasureTool measureTool;
    private UILinesDrawer uiLinesDrawer;
    private GeometrySelectionHandler geometrySelectionHandler;

    private boolean is3DMode = false;
    private boolean isMouseWheelPressed = false;

    private float mouseX, mouseY;

    public void setPropertiesPanel(PropertiesPanel propertiesPanel, LayersManager layersManager, ResourceBundle bundle) {
        this.layersManager = layersManager;
        this.propertiesPanel = propertiesPanel;
        this.propertiesPanel.setLayersManager(layersManager);
        this.propertiesPanel.setUndoManager(undoManager);
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

        // Scene light.
        DirectionalLight light1 = new DirectionalLight();
        light1.setDirection(new Vector3f(1, 1, 1).normalizeLocal());
        light1.setColor(ColorRGBA.White);
        rootNode.addLight(light1);

        DirectionalLight light2 = new DirectionalLight();
        light2.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        light2.setColor(ColorRGBA.White);
        rootNode.addLight(light2);

        AppSettings settings = new AppSettings(true);
        settings.setResizable(true);
        this.setSettings(settings);
        this.setDisplayStatView(false);
        this.setDisplayFps(false);

        objectControls = new ObjectControls(inputManager, assetManager, rootNode, cam, undoManager);

        deleteObject = new DeleteObject(this, rootNode);

        floorPlacer = new FloorPlacer(rootNode, assetManager, inputManager, cam, undoManager, this);

        plainAreaPlacer = new PlainAreaPlacer(rootNode, assetManager, inputManager, cam, undoManager, this);

        measureTool = new MeasureTool(rootNode, assetManager, inputManager, cam);

        modelLoader = new ModelLoader(rootNode, assetManager, undoManager, floorPlacer, plainAreaPlacer, measureTool, this);

        setupMouseClickListener();

        setupInfoText();

        uiLinesDrawer = new UILinesDrawer();
        uiLinesDrawer.addLines(this, rootNode);

        geometrySelectionHandler = new GeometrySelectionHandler(undoManager, modelLoader);
        selectionHandlers.put(Geometry.class, geometrySelectionHandler);

        initLatch.countDown();
    }

    public void setTwoDView() {
        if (is3DMode) {
            is3DMode = false;
            isMouseWheelPressed = false;

            cam.setLocation(new Vector3f(0, 100f, 0));
            cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);

            cameraController.setIs3DMode(false);
        }
    }

    public void setThreeDView() {
        if (!is3DMode) {
            is3DMode = true;
            isMouseWheelPressed = false;

            cam.setLocation(new Vector3f(100f, cameraController.getCurrentZoom(), 100f));
            cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);

            cameraController.setIs3DMode(true);

            Vector2f currentCursorPos = inputManager.getCursorPosition();
            mouseX = currentCursorPos.x;
            mouseY = currentCursorPos.y;
        }
    }

    public void onMouseWheelPressed() {
        isMouseWheelPressed = true;
        inputManager.setCursorVisible(false);
        mouseX = inputManager.getCursorPosition().x;
        mouseY = inputManager.getCursorPosition().y;
    }

    public void onMouseWheelReleased() {
        isMouseWheelPressed = false;
        inputManager.setCursorVisible(true);
    }

    public void updateCameraRotation(float tpf) {
        if (isMouseWheelPressed && is3DMode) {
            Vector2f currentCursorPos = inputManager.getCursorPosition();
            float deltaX = currentCursorPos.x - mouseX;
            float deltaY = currentCursorPos.y - mouseY;

            cameraController.rotateCamera(deltaX, deltaY);

            mouseX = currentCursorPos.x;
            mouseY = currentCursorPos.y;
        }

    }

    public void recreateGridAndLines() {
        enqueue(() -> {
            grid.addGrid();
            uiLinesDrawer.addLines(WareSkladInit.this, rootNode);
        });
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

        String layerName = layersManager.getLayerForSpatial(object);

        if (!layersManager.isLayerEditLocked(layerName)) {
            selectedObject = object;

            if (object != null) {
                updateProperties(object);
                if (objectControls != null) {
                    objectControls.setSelectedObject(object);
                    propertiesPanel.setSelectedObject(selectedObject);
                }

                SelectionHandler handler = selectionHandlers.get(object.getClass());
                if (handler != null) {
                    handler.handleSelection(object);
                } else {
                    System.out.println("No handler registered for object of type: " + object.getClass().getSimpleName());
                }
            }
        } else {
            System.out.println("Cannot select object: Layer " + layerName + " is not editable.");
            propertiesPanel.setSelectedObject(object);
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

        propertiesPanel.updateProperties(objectName, position, rotation, scale, getColorFromObject(object));

        propertiesPanel.setOnNameChange(name -> {
            if (selectedObject != null) {
                enqueue(() -> {
                    String previousName = selectedObject.getName();
                    ColorRGBA previousColor = getColorFromObject(selectedObject);
                    selectedObject.setName(name);

                    undoManager.addAction(new PropertyChangeAction(
                            selectedObject,
                            previousName, originalPosition, originalRotation, originalScale, previousColor,
                            name, originalPosition, originalRotation, originalScale, previousColor
                    ));
                });
            }
        });

        propertiesPanel.setOnPositionChange(pos -> {
            if (selectedObject != null) {
                enqueue(() -> {
                    Vector3f prevPos = selectedObject.getLocalTranslation().clone();
                    ColorRGBA prevColor = getColorFromObject(selectedObject);
                    selectedObject.setLocalTranslation(pos);

                    undoManager.addAction(new PropertyChangeAction(
                            selectedObject,
                            originalName, prevPos, originalRotation, originalScale, prevColor,
                            originalName, pos, originalRotation, originalScale, prevColor
                    ));
                });
            }
        });

        propertiesPanel.setOnRotationChange(rot -> {
            if (selectedObject != null) {
                enqueue(() -> {
                    Vector3f prevRot = new Vector3f(originalAngles[0], originalAngles[1], originalAngles[2]);
                    ColorRGBA prevColor = getColorFromObject(selectedObject);
                    Quaternion newRotation = new Quaternion().fromAngles(rot.x, rot.y, rot.z);
                    selectedObject.setLocalRotation(newRotation);

                    undoManager.addAction(new PropertyChangeAction(
                            selectedObject,
                            originalName, originalPosition, prevRot, originalScale, prevColor,
                            originalName, originalPosition, rot, originalScale, prevColor
                    ));
                });
            }
        });

        propertiesPanel.setOnScaleChange(scl -> {
            if (selectedObject != null) {
                enqueue(() -> {
                    Vector3f prevScale = selectedObject.getLocalScale().clone();
                    ColorRGBA prevColor = getColorFromObject(selectedObject);
                    selectedObject.setLocalScale(scl);

                    undoManager.addAction(new PropertyChangeAction(
                            selectedObject,
                            originalName, originalPosition, originalRotation, prevScale, prevColor,
                            originalName, originalPosition, originalRotation, scl, prevColor
                    ));
                });
            }
        });

        propertiesPanel.setOnColorChange(color -> {
            if (selectedObject != null) {
                enqueue(() -> {
                    ColorRGBA previousColor = getColorFromObject(selectedObject);
                    setObjectColor(selectedObject, color);

                    undoManager.addAction(new PropertyChangeAction(
                            selectedObject,
                            originalName, originalPosition, originalRotation, originalScale,
                            previousColor, originalName, originalPosition, originalRotation, originalScale,
                            color
                    ));
                });
            }
        });
    }

    private ColorRGBA getColorFromObject(Spatial object) {
        if (object instanceof Geometry) {
            Geometry geometry = (Geometry) object;
            Material material = geometry.getMaterial();
            if (material != null) {
                if (material.getParam("Diffuse") != null) {
                    return (ColorRGBA) material.getParam("Diffuse").getValue();
                } else if (material.getParam("Albedo") != null) {
                    return (ColorRGBA) material.getParam("Albedo").getValue();
                } else if (material.getParam("Color") != null) {
                    return (ColorRGBA) material.getParam("Color").getValue();
                }
            }
        }
        return ColorRGBA.White;
    }

    private void setObjectColor(Spatial object, ColorRGBA color) {
        if (object instanceof Geometry) {
            Geometry geometry = (Geometry) object;
            Material material = geometry.getMaterial();
            if (material != null) {
                if (material.getParam("Diffuse") != null) {
                    material.setColor("Diffuse", color);
                } else if (material.getParam("Albedo") != null) {
                    material.setColor("Albedo", color);
                } else if (material.getParam("Color") != null) {
                    material.setColor("Color", color);
                } else {
                    material.setColor("Diffuse", color);
                }
            }
        }
    }

    public void deselectObject() {
        selectedObject = null;
        propertiesPanel.setSelectedObject(null);

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
        updateCameraRotation(tpf);
        cameraController.updateCameraPosition(tpf, is3DMode);

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
        plainAreaPlacer.updatePreview();

        measureTool.updateMeasureToolPreview();
    }

    public void cleanup() {
        if (getContext() != null) {
            this.getCanvas().setVisible(false);
            getContext().setSystemListener(null);
        }
    }

    public void stopCanvas() {
        if (getContext() != null) {
            this.getCanvas().setVisible(false);
            this.stop();
        }
    }

    public float getTotalFloorArea(){
        return this.floorPlacer.calculateTotalFloorArea();
    }

    public float getModelUsedSpace(){
        return this.modelLoader.calculateModelUsedSpace();
    }
}
