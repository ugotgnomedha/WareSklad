package UndoRedo;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class PropertyChangeAction implements UndoableAction {
    private Spatial object;
    private String prevName, newName;
    private Vector3f prevPosition, newPosition;
    private Vector3f prevRotation, newRotation;
    private Vector3f prevScale, newScale;

    public PropertyChangeAction(Spatial object, String prevName, Vector3f prevPosition, Vector3f prevRotation, Vector3f prevScale,
                                String newName, Vector3f newPosition, Vector3f newRotation, Vector3f newScale) {
        this.object = object;
        this.prevName = prevName;
        this.prevPosition = prevPosition;
        this.prevRotation = prevRotation;
        this.prevScale = prevScale;
        this.newName = newName;
        this.newPosition = newPosition;
        this.newRotation = newRotation;
        this.newScale = newScale;
    }

    @Override
    public void undo() {
        // Revert to previous state
        object.setName(prevName);
        object.setLocalTranslation(prevPosition);
        object.setLocalRotation(new Quaternion().fromAngles(prevRotation.x, prevRotation.y, prevRotation.z));
        object.setLocalScale(prevScale);
    }

    @Override
    public void redo() {
        // Apply the new state
        object.setName(newName);
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
