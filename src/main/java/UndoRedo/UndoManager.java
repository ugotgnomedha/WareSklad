package UndoRedo;

import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UndoManager {
    private Stack<UndoableAction> undoStack = new Stack<>();
    private Stack<UndoableAction> redoStack = new Stack<>();
    private List<UndoRedoListener> listeners = new ArrayList<>();
    private List<Spatial> sceneObjects = new ArrayList<>();

    public void addAction(UndoableAction action) {
        undoStack.push(action);
        redoStack.clear();

        if (action instanceof ModelLoadAction) {
            ModelLoadAction loadAction = (ModelLoadAction) action;
            sceneObjects.add(loadAction.getModel());
        } else if (action instanceof FloorPlacementAction) {
            FloorPlacementAction floorAction = (FloorPlacementAction) action;
            sceneObjects.addAll(floorAction.getFloorGeometries());
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
                sceneObjects.removeAll(((FloorPlacementAction) action).getFloorGeometries());
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
                sceneObjects.addAll(((FloorPlacementAction) action).getFloorGeometries());
            }

            notifyListeners();
        }
    }

    public void addUndoRedoListener(UndoRedoListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (UndoRedoListener listener : listeners) {
            listener.onUndoRedo();
        }
    }

    public List<Spatial> getCurrentSceneObjects() {
        return new ArrayList<>(sceneObjects);
    }

    public Stack<UndoableAction> getUndoStack() {
        return undoStack;
    }

}
