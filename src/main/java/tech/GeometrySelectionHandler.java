package tech;

import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import ui.PropertiesPanel;

public class GeometrySelectionHandler implements SelectionHandler {
    private final FloorPlacer floorPlacer;
    private PropertiesPanel propertiesPanel;

    public GeometrySelectionHandler(FloorPlacer floorPlacer) {
        this.floorPlacer = floorPlacer;
    }

    public void setPropertiesPanel(PropertiesPanel propertiesPanel){
        this.propertiesPanel = propertiesPanel;
    }

    @Override
    public void handleSelection(Spatial object) {
        if (object instanceof Geometry) {
            Geometry selectedGeometry = (Geometry) object;

            Float distance = null;
            Float area = null;

            if (floorPlacer.getFloorSegmentDistances().containsKey(selectedGeometry)) {
                distance = floorPlacer.getFloorSegmentDistances().get(selectedGeometry);
            }

            if (floorPlacer.getFloorCompleteAreas().containsKey(selectedGeometry)) {
                area = floorPlacer.getFloorCompleteAreas().get(selectedGeometry);
            }

            if (propertiesPanel != null) {
                if (distance != null) {
                    propertiesPanel.updateDynamicSectionToFloorSegmentProperties(distance);
                } else if (area != null) {
                    propertiesPanel.updateDynamicSectionToCompleteFloorProperties(area);
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
