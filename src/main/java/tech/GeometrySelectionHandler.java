package tech;

import UndoRedo.UndoManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import ui.PropertiesPanel;
import tech.Tag;

public class GeometrySelectionHandler implements SelectionHandler {
    private final UndoManager undoManager;
    private PropertiesPanel propertiesPanel;
    private ModelLoader modelLoader;

    public GeometrySelectionHandler(UndoManager undoManager, ModelLoader modelLoader) {
        this.undoManager = undoManager;
        this.modelLoader = modelLoader;
    }

    public void setPropertiesPanel(PropertiesPanel propertiesPanel) {
        this.propertiesPanel = propertiesPanel;
    }

    @Override
    public void handleSelection(Spatial object) {
        if (object instanceof Geometry) {
            Geometry selectedGeometry = (Geometry) object;

            Float distance = null;
            Float floorArea = null;
            Float plainArea = null;
            boolean rackShelf = false;

            Tag tag = undoManager.getTagMap().get(object);
            if (tag != null && "Rack".equals(tag.getName())) {
                rackShelf = true;
            }

            if (undoManager.getFloorSegmentDistances().containsKey(selectedGeometry)) {
                distance = undoManager.getFloorSegmentDistances().get(selectedGeometry);
            }

            if (undoManager.getFloorCompleteAreas().containsKey(selectedGeometry)) {
                floorArea = undoManager.getFloorCompleteAreas().get(selectedGeometry);
            }

            if (undoManager.getPlainAreaCompleteAreas().containsKey(selectedGeometry)) {
                plainArea = undoManager.getPlainAreaCompleteAreas().get(object);
            }

            if (propertiesPanel != null) {
                if (distance != null) {
                    propertiesPanel.updateDynamicSectionToFloorSegmentProperties(distance);
                } else if (floorArea != null) {
                    propertiesPanel.updateDynamicSectionToCompleteFloorProperties(floorArea);
                } else if (rackShelf) {
                    propertiesPanel.updateDynamicSectionToRackShelf();
                } else if (plainArea != null) {
                    propertiesPanel.updateDynamicSectionToPlainAreaProperties(plainArea);
                } else {
                    propertiesPanel.updateDynamicSectionToDefaultProperties(selectedGeometry);
                }
            }
        } else {
            if (propertiesPanel != null) {
                propertiesPanel.updateDynamicSectionToDefaultProperties(object);
            }
        }
    }
}
