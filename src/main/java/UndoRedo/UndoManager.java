package UndoRedo;

import java.util.Stack;

public class UndoManager {
    private Stack<UndoableAction> undoStack = new Stack<>();
    private Stack<UndoableAction> redoStack = new Stack<>();

    public void addAction(UndoableAction action) {
        undoStack.push(action);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            UndoableAction action = undoStack.pop();
            action.undo();
            redoStack.push(action);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            UndoableAction action = redoStack.pop();
            action.redo();
            undoStack.push(action);
        }
    }
}
