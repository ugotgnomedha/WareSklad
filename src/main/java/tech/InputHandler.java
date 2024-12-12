package tech;

import UndoRedo.UndoManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;

public class InputHandler {

    private static boolean moveLeft, moveRight, moveUp, moveDown;
    private CameraController cameraController;
    private UndoManager undoManager;
    private WareSkladInit jmeScene;

    public InputHandler(InputManager inputManager, CameraController cameraController, UndoManager undoManager, WareSkladInit jmeScene) {
        this.cameraController = cameraController;
        this.undoManager = undoManager;
        this.jmeScene = jmeScene;

        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("MoveUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("MoveDown", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(analogListener, "MoveLeft", "MoveRight", "MoveUp", "MoveDown");

        inputManager.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addListener(analogListener, "ZoomIn", "ZoomOut");

        inputManager.addMapping("Undo", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping("Redo", new KeyTrigger(KeyInput.KEY_Y));
        inputManager.addListener(actionListener, "Undo", "Redo");
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (!isPressed) {

                if (name.equals("Undo")) {
                    jmeScene.deselectObject();
                    undoManager.undo();
                }

                if (name.equals("Redo")) {
                    jmeScene.deselectObject();
                    undoManager.redo();
                }
            }
        }
    };

    private final AnalogListener analogListener = (name, value, tpf) -> {
        if (name.equals("MoveLeft")) moveLeft = true;
        if (name.equals("MoveRight")) moveRight = true;
        if (name.equals("MoveUp")) moveUp = true;
        if (name.equals("MoveDown")) moveDown = true;

        if (name.equals("ZoomIn")) cameraController.zoomIn();
        if (name.equals("ZoomOut")) cameraController.zoomOut();
    };

    public static boolean isMoveLeft() {
        return moveLeft;
    }

    public static boolean isMoveRight() {
        return moveRight;
    }

    public static boolean isMoveUp() {
        return moveUp;
    }

    public static boolean isMoveDown() {
        return moveDown;
    }

    public static void resetMovement() {
        moveLeft = moveRight = moveUp = moveDown = false;
    }
}
