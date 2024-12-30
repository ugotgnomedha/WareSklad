package UndoRedo;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import java.awt.*;

public class PropertyChangeAction implements UndoableAction {
    private Spatial object;
    private String prevName, newName;
    private Vector3f prevPosition, newPosition;
    private Vector3f prevRotation, newRotation;
    private Vector3f prevScale, newScale;
    private ColorRGBA prevColor, newColor;

    public PropertyChangeAction(Spatial object, String prevName, Vector3f prevPosition, Vector3f prevRotation, Vector3f prevScale, ColorRGBA prevColor,
                                String newName, Vector3f newPosition, Vector3f newRotation, Vector3f newScale, ColorRGBA newColor) {
        this.object = object;
        this.prevName = prevName;
        this.prevPosition = prevPosition;
        this.prevRotation = prevRotation;
        this.prevScale = prevScale;
        this.prevColor = prevColor;
        this.newColor = newColor;
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
        setObjectColor(object, prevColor);
    }

    @Override
    public void redo() {
        // Apply the new state
        object.setName(newName);
        object.setLocalTranslation(newPosition);
        object.setLocalRotation(new Quaternion().fromAngles(newRotation.x, newRotation.y, newRotation.z));
        object.setLocalScale(newScale);
        setObjectColor(object, newColor);
    }

    public void setFinalProperties(Vector3f newPosition, Vector3f newRotation, Vector3f newScale, ColorRGBA newColor) {
        this.newPosition = newPosition.clone();
        this.newRotation = newRotation.clone();
        this.newScale = newScale.clone();
        this.newColor = newColor.clone();
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
                }
            }
        }
    }
}
