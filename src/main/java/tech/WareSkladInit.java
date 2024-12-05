package tech;

import UndoRedo.UndoManager;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.math.*;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import ui.Grid;
import ui.PropertiesPanel;

import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class WareSkladInit extends SimpleApplication {
    private final CountDownLatch initLatch = new CountDownLatch(1);

    private CameraController cameraController;
    private Grid grid;
    private InputHandler inputHandler;
    public ModelLoader modelLoader;

    private Spatial selectedObject;
    private PropertiesPanel propertiesPanel;
    private ObjectControls objectControls;
    public UndoManager undoManager;

    public void setPropertiesPanel(PropertiesPanel propertiesPanel) {
        this.propertiesPanel = propertiesPanel;
        this.objectControls.setPropertiesPanel(propertiesPanel);
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        cameraController = new CameraController(cam);
        grid = new Grid(assetManager, rootNode);

        undoManager = new UndoManager();
        inputHandler = new InputHandler(inputManager, cameraController, undoManager);

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

        modelLoader = new ModelLoader(rootNode, assetManager, undoManager);

        objectControls = new ObjectControls(inputManager, assetManager, rootNode, cam, undoManager);

        setupMouseClickListener();

        initLatch.countDown();
    }

    public void waitForInitialization() throws InterruptedException {
        initLatch.await();
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

    private void selectObject(Spatial object) {
        selectedObject = object;

        Vector3f position = object.getLocalTranslation();
        float[] angles = object.getLocalRotation().toAngles(null);
        Vector3f rotation = new Vector3f(angles[0], angles[1], angles[2]);
        Vector3f scale = object.getLocalScale();

        String objectName = object.getName() != null ? object.getName() : "Unnamed Object";

        if (propertiesPanel != null) {
            propertiesPanel.updateProperties(objectName, position, rotation, scale);

            propertiesPanel.setOnNameChange(name -> selectedObject.setName(name));
            propertiesPanel.setOnPositionChange(pos -> selectedObject.setLocalTranslation(pos));
            propertiesPanel.setOnRotationChange(rot -> {
                selectedObject.setLocalRotation(new Quaternion().fromAngles(rot.x, rot.y, rot.z));
            });
            propertiesPanel.setOnScaleChange(scl -> selectedObject.setLocalScale(scl));
        }

        if (selectedObject != null) {
            objectControls.setSelectedObject(object);
        }
    }

    private void deselectObject() {
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
    }
}
