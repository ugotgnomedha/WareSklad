package tech;

import UndoRedo.UndoManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;

public class InputHandler {

    private static boolean moveLeft, moveRight, moveUp, moveDown;
    private static boolean shiftLeft, shiftRight;
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

        inputManager.addMapping("MouseWheelPressed", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        inputManager.addListener(actionListener, "MouseWheelPressed");
        inputManager.addMapping("MouseWheelReleased", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        inputManager.addListener(actionListener, "MouseWheelReleased");

        inputManager.addMapping("Copy", new KeyTrigger(KeyInput.KEY_C), new KeyTrigger(KeyInput.KEY_LCONTROL));
        inputManager.addMapping("Paste", new KeyTrigger(KeyInput.KEY_V), new KeyTrigger(KeyInput.KEY_LCONTROL));
        inputManager.addListener(actionListener, "Copy", "Paste");

        inputManager.addMapping("ShiftLeft", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("ShiftRight", new KeyTrigger(KeyInput.KEY_RSHIFT));
        inputManager.addListener(analogListener, "ShiftLeft", "ShiftRight");
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("MouseWheelPressed") && isPressed) {
                jmeScene.onMouseWheelPressed();
            }
            if (name.equals("MouseWheelReleased") && !isPressed) {
                jmeScene.onMouseWheelReleased();
            }

            if (name.equals("Undo") && !isPressed) {
                jmeScene.deselectObject();
                undoManager.undo();
            }

            if (name.equals("Redo") && !isPressed) {
                jmeScene.deselectObject();
                undoManager.redo();
            }

            if (name.equals("Copy") && !isPressed) {
                jmeScene.copySelectedObjects();
            }

            if (name.equals("Paste") && !isPressed) {
                jmeScene.pasteObjects();
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

        if (name.equals("ShiftLeft")) shiftLeft = value > 0;
        if (name.equals("ShiftRight")) shiftRight = value > 0;
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

    public static boolean isShiftLeft() {
        return shiftLeft;
    }

    public static boolean isShiftRight() {
        return shiftRight;
    }

    public static void resetMovement() {
        moveLeft = moveRight = moveUp = moveDown = false;
    }
}
