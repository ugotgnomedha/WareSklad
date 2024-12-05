package UndoRedo;

import UndoRedo.UndoableAction;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class PropertyChangeAction implements UndoableAction {
    private final Spatial object;
    private final Vector3f prevPosition, prevRotation, prevScale;
    private Vector3f newPosition;
    private Vector3f newRotation;
    private Vector3f newScale;

    public PropertyChangeAction(Spatial object, Vector3f prevPosition, Vector3f prevRotation, Vector3f prevScale,
                                Vector3f newPosition, Vector3f newRotation, Vector3f newScale) {
        this.object = object;
        this.prevPosition = prevPosition;
        this.prevRotation = prevRotation;
        this.prevScale = prevScale;
        this.newPosition = newPosition;
        this.newRotation = newRotation;
        this.newScale = newScale;
    }

    public void undo() {
        object.setLocalTranslation(prevPosition);
        object.setLocalRotation(new Quaternion().fromAngles(prevRotation.x, prevRotation.y, prevRotation.z));
        object.setLocalScale(prevScale);
    }

    public void redo() {
        object.setLocalTranslation(newPosition);
        object.setLocalRotation(new Quaternion().fromAngles(newRotation.x, newRotation.y, newRotation.z));
        object.setLocalScale(newScale);
    }

    public void setFinalProperties(Vector3f newPosition, Vector3f newRotation, Vector3f newScale) {
        this.newPosition = newPosition.clone();
        this.newRotation = newRotation.clone();
        this.newScale = newScale.clone();
    }
}
