package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import tech.*;

public class PropertiesPanel extends JPanel {

    private JTextField nameField;
    private JTextField positionXField, positionYField, positionZField;
    private JTextField rotationXField, rotationYField, rotationZField;
    private JTextField scaleXField, scaleYField, scaleZField;

    private JButton deleteButton;

    private Consumer<String> onNameChange;
    private Consumer<Vector3f> onPositionChange;
    private Consumer<Vector3f> onRotationChange;
    private Consumer<Vector3f> onScaleChange;

    private boolean isTransformFolded = true;
    private ObjectControls objectControls;
    private MeasureTool measureTool;
    private DeleteObject deleteObject;

    private JComboBox<String> layerDropdown;
    private JButton addToLayerButton, removeFromLayerButton;
    private LayersManager layersManager;
    private Spatial selectedObject;
    private JLabel currentLayerLabel;

    private JPanel dynamicSection;

    public void setObjectControls(ObjectControls objectControls) {
        this.objectControls = objectControls;
    }

    public void setMeasureTool(MeasureTool measureTool){
        this.measureTool = measureTool;
    }

    public void setDeleteObject(DeleteObject deleteObject) {
        this.deleteObject = deleteObject;
    }

    public void setLayersManager(LayersManager layersManager) {
        this.layersManager = layersManager;
    }

    public void setSelectedObject(Spatial object) {
        this.selectedObject = object;

        String currentLayer = getCurrentLayer();
        updateCurrentLayerLabel(currentLayerLabel, currentLayer);

        String selectedLayer = (String) layerDropdown.getSelectedItem();
        updateLayerButtons(selectedLayer);

        deleteButton.setEnabled(selectedObject != null);
    }

    public PropertiesPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        nameField = createCompactTextField(15);
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(new JLabel("Name:"), BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);
        namePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        add(namePanel);

        JPanel transformPanel = createFoldablePanel("Transform", createTransformPanel());
        add(transformPanel);

        dynamicSection = new JPanel();
        dynamicSection.setLayout(new BoxLayout(dynamicSection, BoxLayout.Y_AXIS));
        add(dynamicSection, BorderLayout.CENTER);

        JPanel layersPanel = createFoldablePanel("Layers", createLayersPanel());
        add(layersPanel);

        JCheckBox snapToGridCheckbox = new JCheckBox("Snap to Grid");
        add(snapToGridCheckbox);
        snapToGridCheckbox.addActionListener(e -> {
            measureTool.setSnapping(snapToGridCheckbox.isSelected());
            objectControls.setSnapToGrid(snapToGridCheckbox.isSelected());
        });

        JCheckBox stackingCheckbox = new JCheckBox("Stacking");
        add(stackingCheckbox);
        stackingCheckbox.addActionListener(e -> objectControls.setStackingObjects(stackingCheckbox.isSelected()));

        JCheckBox heightAdjustmentCheckbox = new JCheckBox("Height Adjustment");
        heightAdjustmentCheckbox.setSelected(true);
        add(heightAdjustmentCheckbox);
        heightAdjustmentCheckbox.addActionListener(e -> objectControls.setHeightAdjustment(heightAdjustmentCheckbox.isSelected()));

        deleteButton = new JButton("Delete");
        deleteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> {
            if (selectedObject != null) {
                deleteObject.delete(selectedObject);
                objectControls.setSelectedObject(null);
                clearPropertiesPanel();
                System.out.println("Object deleted.");
            } else {
                System.out.println("No object selected.");
            }
        });
        add(deleteButton);

        add(Box.createVerticalGlue());

        setupRealTimeListeners();
    }

    private JPanel createTransformPanel() {
        JPanel transformContent = new JPanel();
        transformContent.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        transformContent.add(new JLabel("Position:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        transformContent.add(positionXField = createCompactTextField(5), gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        transformContent.add(positionYField = createCompactTextField(5), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        transformContent.add(positionZField = createCompactTextField(5), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        transformContent.add(new JLabel("Rotation:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        transformContent.add(rotationXField = createCompactTextField(5), gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        transformContent.add(rotationYField = createCompactTextField(5), gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        transformContent.add(rotationZField = createCompactTextField(5), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        transformContent.add(new JLabel("Scale:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        transformContent.add(scaleXField = createCompactTextField(5), gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        transformContent.add(scaleYField = createCompactTextField(5), gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        transformContent.add(scaleZField = createCompactTextField(5), gbc);

        JPanel floorInfoPanel = new JPanel();
        floorInfoPanel.setLayout(new BoxLayout(floorInfoPanel, BoxLayout.Y_AXIS));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        transformContent.add(floorInfoPanel, gbc);

        return transformContent;
    }

    private JPanel createLayersPanel() {
        JPanel layersContent = new JPanel();
        layersContent.setLayout(new BoxLayout(layersContent, BoxLayout.Y_AXIS));

        currentLayerLabel = new JLabel("Current Layer: None");
        layersContent.add(currentLayerLabel);

        layerDropdown = new JComboBox<>();
        layerDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        layerDropdown.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                refreshLayerDropdown();
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            }
        });

        layerDropdown.addActionListener(e -> {
            String selectedLayer = (String) layerDropdown.getSelectedItem();
            updateLayerButtons(selectedLayer);
        });

        layersContent.add(layerDropdown);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addToLayerButton = new JButton("Add Object to Layer");
        removeFromLayerButton = new JButton("Remove Object from Layer");

        buttonsPanel.add(addToLayerButton);
        buttonsPanel.add(removeFromLayerButton);
        layersContent.add(buttonsPanel);

        addToLayerButton.addActionListener(e -> {
            if (selectedObject == null) {
                System.out.println("No object selected.");
                return;
            }

            String selectedLayer = (String) layerDropdown.getSelectedItem();
            if (selectedLayer != null && layersManager != null) {
                String currentLayer = getCurrentLayer();
                if (currentLayer != null) {
                    layersManager.removeSpatialFromLayer(currentLayer, selectedObject);
                    System.out.println("Removed object from current layer: " + currentLayer);
                }

                layersManager.addSpatialToLayer(selectedLayer, selectedObject);
                System.out.println("Added object to layer: " + selectedLayer);

                updateCurrentLayerLabel(currentLayerLabel, selectedLayer);
                updateLayerButtons(selectedLayer);
            }
        });

        removeFromLayerButton.addActionListener(e -> {
            if (selectedObject == null) {
                System.out.println("No object selected.");
                return;
            }

            String selectedLayer = (String) layerDropdown.getSelectedItem();
            if (selectedLayer != null && layersManager != null) {
                layersManager.removeSpatialFromLayer(selectedLayer, selectedObject);
                System.out.println("Removed object from layer: " + selectedLayer);

                updateCurrentLayerLabel(currentLayerLabel, null);
                updateLayerButtons(null);
            }
        });

        updateLayerButtons(null);

        return layersContent;
    }

    private void refreshLayerDropdown() {
        if (layersManager != null) {
            layerDropdown.removeAllItems();
            layersManager.getAllLayers().keySet().forEach(layerDropdown::addItem);
        }
    }

    private void updateLayerButtons(String selectedLayer) {
        String currentLayer = getCurrentLayer();

        if (currentLayer != null && currentLayer.equals(selectedLayer)) {
            addToLayerButton.setEnabled(false);
            removeFromLayerButton.setEnabled(true);
        } else {
            addToLayerButton.setEnabled(selectedLayer != null);
            removeFromLayerButton.setEnabled(false);
        }
    }

    private String getCurrentLayer() {
        if (layersManager != null && selectedObject != null) {
            for (Map.Entry<String, Layer> entry : layersManager.getAllLayers().entrySet()) {
                if (entry.getValue().getSpatials().contains(selectedObject)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private void updateCurrentLayerLabel(JLabel label, String layerName) {
        if (layerName == null) {
            label.setText("Current Layer: None");
        } else {
            label.setText("Current Layer: " + layerName);
        }
    }

    private JPanel createFoldablePanel(String title, JPanel contentPanel) {
        JPanel foldablePanel = new JPanel();
        foldablePanel.setLayout(new BoxLayout(foldablePanel, BoxLayout.Y_AXIS));
        foldablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JToggleButton toggleButton = new JToggleButton("▼");
        toggleButton.setHorizontalAlignment(SwingConstants.LEFT);
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(e -> toggleFoldableView(toggleButton, contentPanel));

        toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        foldablePanel.add(toggleButton);

        contentPanel.setVisible(!isTransformFolded);
        foldablePanel.add(contentPanel);

        foldablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        foldablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, contentPanel.getPreferredSize().height + 30));

        return foldablePanel;
    }

    private void toggleFoldableView(JToggleButton toggleButton, JPanel contentPanel) {
        isTransformFolded = !isTransformFolded;
        contentPanel.setVisible(!isTransformFolded);
        toggleButton.setText(isTransformFolded ? "▼" : "▲");
        revalidate();
        repaint();
    }

    private JTextField createCompactTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setMaximumSize(new Dimension(100, 20));
        textField.setPreferredSize(new Dimension(100, 20));
        return textField;
    }

    private void setupRealTimeListeners() {
        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (onNameChange != null) {
                    onNameChange.accept(nameField.getText());
                }
            }
        });

        addVectorFieldListener(positionXField, positionYField, positionZField, v -> {
            if (onPositionChange != null) onPositionChange.accept(v);
        });

        addVectorFieldListener(rotationXField, rotationYField, rotationZField, v -> {
            if (onRotationChange != null) {
                Vector3f radiansVector = new Vector3f(
                        (float) Math.toRadians(v.x),
                        (float) Math.toRadians(v.y),
                        (float) Math.toRadians(v.z)
                );
                onRotationChange.accept(radiansVector);
            }
        });

        addVectorFieldListener(scaleXField, scaleYField, scaleZField, v -> {
            if (onScaleChange != null) onScaleChange.accept(v);
        });
    }

    private void addVectorFieldListener(JTextField xField, JTextField yField, JTextField zField, Consumer<Vector3f> callback) {
        KeyAdapter adapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    float x = Float.parseFloat(xField.getText());
                    float y = Float.parseFloat(yField.getText());
                    float z = Float.parseFloat(zField.getText());
                    callback.accept(new Vector3f(x, y, z));
                } catch (NumberFormatException ex) {
                }
            }
        };
        xField.addKeyListener(adapter);
        yField.addKeyListener(adapter);
        zField.addKeyListener(adapter);
    }

    public void updateDynamicSectionToFloorSegmentProperties(float distance) {
        dynamicSection.removeAll();
        dynamicSection.add(new JLabel("Segment Distance (m): " + String.format("%.2f", distance)));
        dynamicSection.revalidate();
        dynamicSection.repaint();
    }

    public void updateDynamicSectionToCompleteFloorProperties(float area) {
        dynamicSection.removeAll();
        dynamicSection.add(new JLabel("Complete Area (m²): " + String.format("%.2f", area)));
        dynamicSection.revalidate();
        dynamicSection.repaint();
    }

    public void updateDynamicSectionToDefaultProperties(Spatial object) {
        dynamicSection.removeAll();
        if (object != null) {
            //dynamicSection.add(new JLabel("Object Name: " + object.getName()));
        } else {
            //dynamicSection.add(new JLabel("No object selected."));
        }
        dynamicSection.revalidate();
        dynamicSection.repaint();
    }

    public void updateProperties(String name, Vector3f position, Vector3f rotation, Vector3f scale) {
        nameField.setText(name);
        positionXField.setText(String.format(Locale.US, "%.2f", position.x));
        positionYField.setText(String.format(Locale.US, "%.2f", position.y));
        positionZField.setText(String.format(Locale.US, "%.2f", position.z));

        rotationXField.setText(String.format(Locale.US, "%.2f", Math.toDegrees(rotation.x)));
        rotationYField.setText(String.format(Locale.US, "%.2f", Math.toDegrees(rotation.y)));
        rotationZField.setText(String.format(Locale.US, "%.2f", Math.toDegrees(rotation.z)));

        scaleXField.setText(String.format(Locale.US, "%.2f", scale.x));
        scaleYField.setText(String.format(Locale.US, "%.2f", scale.y));
        scaleZField.setText(String.format(Locale.US, "%.2f", scale.z));
    }

    public void clearPropertiesPanel() {
        nameField.setText("");
        positionXField.setText("");
        positionYField.setText("");
        positionZField.setText("");
        rotationXField.setText("");
        rotationYField.setText("");
        rotationZField.setText("");
        scaleXField.setText("");
        scaleYField.setText("");
        scaleZField.setText("");
    }

    public void setOnNameChange(Consumer<String> callback) {
        this.onNameChange = callback;
    }

    public void setOnPositionChange(Consumer<Vector3f> callback) {
        this.onPositionChange = callback;
    }

    public void setOnRotationChange(Consumer<Vector3f> callback) {
        this.onRotationChange = callback;
    }

    public void setOnScaleChange(Consumer<Vector3f> callback) {
        this.onScaleChange = callback;
    }
}
