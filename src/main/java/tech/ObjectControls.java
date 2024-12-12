package tech;

import UndoRedo.PropertyChangeAction;
import UndoRedo.UndoManager;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import ui.Grid;
import ui.PropertiesPanel;

public class ObjectControls {
    private Spatial selectedObject;
    private Geometry outlineGeometry;
    private InputManager inputManager;
    private boolean isDragging = false;
    private AssetManager assetManager;
    private Node rootNode;
    private Camera camera;
    private PropertiesPanel propertiesPanel;
    private UndoManager undoManager;
    private PropertyChangeAction ongoingAction = null;
    private boolean snapToGrid = false;
    private boolean heightAdjustment = true;

    public void setPropertiesPanel(PropertiesPanel propertiesPanel) {
        this.propertiesPanel = propertiesPanel;
    }

    public void setSnapToGrid(boolean snapToGrid) {
        this.snapToGrid = snapToGrid;
    }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    public void setHeightAdjustment(boolean heightAdjustment) {
        this.heightAdjustment = heightAdjustment;
    }

    public boolean isHeightAdjustment() {
        return heightAdjustment;
    }

    public ObjectControls(InputManager inputManager, AssetManager assetManager, Node rootNode, Camera cam, UndoManager undoManager) {
        this.inputManager = inputManager;
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.camera = cam;
        this.undoManager = undoManager;
        setupMouseListeners();
    }

    public void setSelectedObject(Spatial selectedObject) {
        if (this.selectedObject != selectedObject) {
            this.selectedObject = selectedObject;

            if (selectedObject != null) {
                highlightObject(selectedObject);
            } else {
                removeHighlight();
            }
        }
    }

    private void highlightObject(Spatial object) {
        if (object instanceof Geometry) {
            Geometry geom = (Geometry) object;

            Box box = new Box(geom.getLocalScale().x * 1.05f, geom.getLocalScale().y * 1.05f, geom.getLocalScale().z * 1.05f);
            outlineGeometry = new Geometry("Outline", box);

            Material outlineMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            outlineMaterial.setColor("Color", ColorRGBA.Yellow);
            outlineMaterial.getAdditionalRenderState().setWireframe(true);

            outlineGeometry.setMaterial(outlineMaterial);
            rootNode.attachChild(outlineGeometry);
            outlineGeometry.setLocalTranslation(object.getLocalTranslation());
        }
    }

    private void removeHighlight() {
        if (outlineGeometry != null) {
            outlineGeometry.removeFromParent();
            outlineGeometry = null;
        }
    }

    private void setupMouseListeners() {
        inputManager.addMapping("MouseDrag", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("MouseRelease", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        AnalogListener dragListener = (name, value, tpf) -> {
            if ("MouseDrag".equals(name) && selectedObject != null) {
                Vector2f mousePos = inputManager.getCursorPosition();
                Vector3f newPosition = getWorldPositionFromMouse(mousePos);

                if (snapToGrid) {
                    newPosition.x = Math.round(newPosition.x / Grid.GRID_SPACING) * Grid.GRID_SPACING;
                    newPosition.z = Math.round(newPosition.z / Grid.GRID_SPACING) * Grid.GRID_SPACING;
                }

                if (heightAdjustment) {
                    float objectHeight = 0;
                    if (selectedObject instanceof Geometry) {
                        Geometry geom = (Geometry) selectedObject;
                        if (geom.getModelBound() instanceof BoundingBox) {
                            BoundingBox box = (BoundingBox) geom.getModelBound();
                            objectHeight = box.getExtent(new Vector3f(0, 1, 0)).y * geom.getLocalScale().y;
                        }
                    }

                    newPosition.y = Grid.GRID_Y_LEVEL + objectHeight;
                }

                if (ongoingAction == null) {
                    String initialName = selectedObject.getName() != null ? selectedObject.getName() : "Unnamed Object";
                    Vector3f initialPosition = selectedObject.getLocalTranslation().clone();
                    float[] initialAngles = selectedObject.getLocalRotation().toAngles(null);
                    Vector3f initialRotation = new Vector3f(initialAngles[0], initialAngles[1], initialAngles[2]);
                    Vector3f initialScale = selectedObject.getLocalScale().clone();

                    ongoingAction = new PropertyChangeAction(
                            selectedObject,
                            initialName, initialPosition, initialRotation, initialScale,
                            initialName, initialPosition.clone(), initialRotation.clone(), initialScale.clone());
                }


                float[] currentAngles = selectedObject.getLocalRotation().toAngles(null);
                Vector3f currentRotation = new Vector3f(currentAngles[0], currentAngles[1], currentAngles[2]);

                ongoingAction.setFinalProperties(newPosition, currentRotation, selectedObject.getLocalScale());

                selectedObject.setLocalTranslation(newPosition);

                if (outlineGeometry != null) {
                    outlineGeometry.setLocalTranslation(newPosition);
                }
                if (propertiesPanel != null) {
                    Vector3f rotation = new Vector3f(currentAngles[0], currentAngles[1], currentAngles[2]);
                    propertiesPanel.updateProperties(selectedObject.getName(), newPosition, rotation, selectedObject.getLocalScale());
                }
            }
        };

        ActionListener stopDragListener = (name, isPressed, tpf) -> {
            if ("MouseRelease".equals(name) && !isPressed) {
                if (ongoingAction != null) {
                    undoManager.addAction(ongoingAction);
                    ongoingAction = null;
                }
            }
        };

        inputManager.addListener(dragListener, "MouseDrag");
        inputManager.addListener(stopDragListener, "MouseRelease");
    }

    private Vector3f getWorldPositionFromMouse(Vector2f mousePos) {
        Vector3f near = camera.getWorldCoordinates(mousePos, 0);
        Vector3f far = camera.getWorldCoordinates(mousePos, 1);
        Vector3f dir = far.subtract(near).normalizeLocal();

        float planeY = 0;
        float t = (planeY - near.y) / dir.y;
        return near.add(dir.mult(t));
    }
}
