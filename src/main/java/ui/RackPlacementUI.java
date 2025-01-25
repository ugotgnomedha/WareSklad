package ui;

import UndoRedo.UndoManager;
import com.jme3.scene.Geometry;
import tech.CatalogueLoader;
import tech.RackPlacementManager;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.ResourceBundle;

public class RackPlacementUI {

    private ResourceBundle bundle;
    private final UndoManager undoManager;
    private final CatalogueLoader.Catalogue catalogue;
    private RackPlacementManager rackPlacementManager;

    public void setRackPlacementManager(RackPlacementManager rackPlacementManager) {
        this.rackPlacementManager = rackPlacementManager;
    }

    public RackPlacementUI(ResourceBundle bundle, UndoManager undoManager, CatalogueLoader.Catalogue catalogue) {
        this.bundle = bundle;
        this.undoManager = undoManager;
        this.catalogue = catalogue;
    }

    public void openRacksPlacementFrame() {
        JFrame racksFrame = new JFrame(bundle.getString("racksPlacementTitle"));
        racksFrame.setSize(600, 500);
        racksFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        racksFrame.setLayout(new BorderLayout());

        racksFrame.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) { }

            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                racksFrame.dispose();
            }
        });

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));

        JPanel floorPanel = new JPanel(new FlowLayout());
        JComboBox<Geometry> floorSelector = new JComboBox<>();
        floorSelector.addItem(null);
        Map<Geometry, Float> floorCompleteAreas = undoManager.getFloorCompleteAreas();
        floorCompleteAreas.keySet().forEach(floorSelector::addItem);
        floorPanel.add(new JLabel(bundle.getString("selectFloor")));
        floorPanel.add(floorSelector);
        controlsPanel.add(floorPanel);

        JPanel rackDimensionsPanel = new JPanel(new GridLayout(4, 2));
        rackDimensionsPanel.add(new JLabel(bundle.getString("rackWidth")));
        JTextField widthField = new JTextField("180.0");
        rackDimensionsPanel.add(widthField);

        rackDimensionsPanel.add(new JLabel(bundle.getString("rackDepth")));
        JTextField depthField = new JTextField("110.0");
        rackDimensionsPanel.add(depthField);

        rackDimensionsPanel.add(new JLabel(bundle.getString("rackHeight")));
        JTextField heightField = new JTextField("200.0");
        rackDimensionsPanel.add(heightField);
        controlsPanel.add(rackDimensionsPanel);

        rackDimensionsPanel.add(new JLabel(bundle.getString("ceilingHeight")));
        JTextField ceilingHeightField = new JTextField("300.0");
        rackDimensionsPanel.add(ceilingHeightField);
        controlsPanel.add(rackDimensionsPanel);

        JPanel distancePanel = new JPanel(new GridLayout(2, 2));
        distancePanel.add(new JLabel(bundle.getString("minDistanceToObstacles")));
        JTextField minObstacleDistanceField = new JTextField("45.0");
        distancePanel.add(minObstacleDistanceField);

        distancePanel.add(new JLabel(bundle.getString("minDistanceBetweenRows")));
        JTextField minRowDistanceField = new JTextField("120.0");
        distancePanel.add(minRowDistanceField);
        controlsPanel.add(distancePanel);

        JButton runButton = new JButton(bundle.getString("generatePlacement"));
        runButton.addActionListener(e -> {
            Geometry selectedFloor = (Geometry) floorSelector.getSelectedItem();
            if (selectedFloor != null) {
                System.out.println("Selected floor for rack placement: " + selectedFloor.getName());

                try {
                    // Convert centimeters to decimeters.
                    float rackWidth = Float.parseFloat(widthField.getText()) / 10.0f;
                    float rackDepth = Float.parseFloat(depthField.getText()) / 10.0f;
                    float rackHeight = Float.parseFloat(heightField.getText()) / 10.0f;
                    float ceilingHeight = Float.parseFloat(ceilingHeightField.getText()) / 10.0f;
                    float minObstacleDistance = Float.parseFloat(minObstacleDistanceField.getText()) / 10.0f;
                    float minRowDistance = Float.parseFloat(minRowDistanceField.getText()) / 10.0f;

                    if (rackWidth <= 0 || rackDepth <= 0 || rackHeight <= 0 || ceilingHeight <= 0 || minObstacleDistance < 0 || minRowDistance < 0) {
                        JOptionPane.showMessageDialog(racksFrame, bundle.getString("invalidInputMessage"), bundle.getString("errorTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    rackPlacementManager.placeRacks(selectedFloor, rackWidth, rackDepth, rackHeight, ceilingHeight, minObstacleDistance, minRowDistance);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(racksFrame, bundle.getString("invalidInputMessage"), bundle.getString("errorTitle"), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("No floor selected.");
            }
        });
        controlsPanel.add(runButton);

        racksFrame.add(controlsPanel, BorderLayout.SOUTH);
        racksFrame.setVisible(true);
    }
}
