package tech;

import com.jme3.scene.Spatial;
import java.util.HashSet;
import java.util.Set;

public class Layer {
    private String name;
    private float opacity;
    private boolean lockEdit;
    private Set<Spatial> spatials;

    public Layer(String name, float opacity, boolean lockEdit) {
        this.name = name;
        this.opacity = opacity;
        this.lockEdit = lockEdit;
        this.spatials = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public boolean isLockEdit() {
        return lockEdit;
    }

    public void setLockEdit(boolean lockEdit) {
        this.lockEdit = lockEdit;
    }

    public Set<Spatial> getSpatials() {
        return spatials;
    }

    public void addSpatial(Spatial spatial) {
        this.spatials.add(spatial);
    }

    public void removeSpatial(Spatial spatial) {
        this.spatials.remove(spatial);
    }

    public boolean containsSpatial(Spatial spatial) {
        return spatials.contains(spatial);
    }
}
