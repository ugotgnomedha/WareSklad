package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import UndoRedo.UndoManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import tech.*;
import tech.layers.Layer;
import tech.layers.LayersManager;
import tech.simulations.RackSettings;

public class PropertiesPanel extends JPanel {

    private JTextField nameField;
    private JTextField positionXField, positionYField, positionZField;
    private JTextField rotationXField, rotationYField, rotationZField;
    private JTextField scaleXField, scaleYField, scaleZField;
    private JPanel transformContent;

    private JButton deleteButton;

    private JComboBox<String> tagDropdown;

    private Consumer<String> onNameChange;
    private Consumer<Vector3f> onPositionChange;
    private Consumer<Vector3f> onRotationChange;
    private Consumer<Vector3f> onScaleChange;
    private Consumer<ColorRGBA> onColorChange;

    private boolean isTransformFolded = true;
    private ObjectControls objectControls;
    private MeasureTool measureTool;
    private DeleteObject deleteObject;

    private JComboBox<String> layerDropdown;
    private JButton addToLayerButton, removeFromLayerButton;
    private LayersManager layersManager;
    private Spatial selectedObject;
    private JLabel currentLayerLabel;
    private ColorRGBA selectedColor = new ColorRGBA(1, 1, 1, 1);
    private JPanel colorPreview;
    private ResourceBundle bundle;
    private UndoManager undoManager;

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

    public void setUndoManager(UndoManager undoManager){
        this.undoManager = undoManager;
    }

    public void setSelectedObject(Spatial object) {
        this.selectedObject = object;

        String currentLayer = getCurrentLayer();
        updateCurrentLayerLabel(currentLayerLabel, currentLayer);

        String selectedLayer = (String) layerDropdown.getSelectedItem();
        updateLayerButtons(selectedLayer);

        deleteButton.setEnabled(selectedObject != null);
        updateTagDropdown(undoManager.getTags());
    }

    public PropertiesPanel(ResourceBundle bundle) {
        this.bundle = bundle;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        nameField = createCompactTextField(15);
        tagDropdown = new JComboBox<>(new String[]{"---"});
        tagDropdown.setMaximumSize(new Dimension(150, 30));
        tagDropdown.addActionListener(e -> tagDropdownActionPerformed());

        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(new JLabel(bundle.getString("name_label")), BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);
        namePanel.add(tagDropdown, BorderLayout.EAST);
        namePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        add(namePanel);

        JPanel transformPanel = createFoldablePanel(bundle.getString("transform_title"), createTransformPanel());
        add(transformPanel);

        dynamicSection = new JPanel();
        dynamicSection.setLayout(new BoxLayout(dynamicSection, BoxLayout.Y_AXIS));
        add(dynamicSection, BorderLayout.CENTER);

        JPanel layersPanel = createFoldablePanel(bundle.getString("layers_title"), createLayersPanel());
        add(layersPanel);

        JPanel colorSelectorPanel = createColorSelectorPanel();
        add(colorSelectorPanel);

        JCheckBox snapToGridCheckbox = new JCheckBox(bundle.getString("snap_to_grid"));
        add(snapToGridCheckbox);
        snapToGridCheckbox.addActionListener(e -> {
            measureTool.setSnapping(snapToGridCheckbox.isSelected());
            objectControls.setSnapToGrid(snapToGridCheckbox.isSelected());
        });

        JCheckBox stackingCheckbox = new JCheckBox(bundle.getString("stacking"));
        add(stackingCheckbox);
        stackingCheckbox.addActionListener(e -> objectControls.setStackingObjects(stackingCheckbox.isSelected()));

        JCheckBox heightAdjustmentCheckbox = new JCheckBox(bundle.getString("height_adjustment"));
        heightAdjustmentCheckbox.setSelected(true);
        add(heightAdjustmentCheckbox);
        heightAdjustmentCheckbox.addActionListener(e -> objectControls.setHeightAdjustment(heightAdjustmentCheckbox.isSelected()));

        deleteButton = new JButton(bundle.getString("delete_button"));
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

    private JPanel createColorSelectorPanel() {
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));
        colorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        colorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton colorButton = new JButton(bundle.getString("color_button"));
        colorPreview = new JPanel();

        Color previewColor = new Color(
                selectedColor.getRed(),
                selectedColor.getGreen(),
                selectedColor.getBlue(),
                selectedColor.getAlpha());

        colorPreview.setBackground(previewColor);
        colorPreview.setPreferredSize(new Dimension(30, 20));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, bundle.getString("color_chooser"), previewColor);
            if (newColor != null) {
                selectedColor = new ColorRGBA(
                        newColor.getRed() / 255f,
                        newColor.getGreen() / 255f,
                        newColor.getBlue() / 255f,
                        newColor.getAlpha() / 255f
                );

                colorPreview.setBackground(newColor);
                if (onColorChange != null) {
                    onColorChange.accept(selectedColor);
                }
            }
        });

        colorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        colorPanel.add(colorButton);
        colorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        colorPanel.add(colorPreview);

        return colorPanel;
    }

    private JPanel createTransformPanel() {
        transformContent = new JPanel();
        transformContent.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        transformContent.add(new JLabel(bundle.getString("position")), gbc);

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
        transformContent.add(new JLabel(bundle.getString("rotation_label")), gbc);

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
        transformContent.add(new JLabel(bundle.getString("scale_label")), gbc);

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

        currentLayerLabel = new JLabel(bundle.getString("current_layer_label"));
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
        addToLayerButton = new JButton(bundle.getString("add_to_layer"));
        removeFromLayerButton = new JButton(bundle.getString("remove_from_layer"));

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
            label.setText(bundle.getString("current_layer_label"));
        } else {
            label.setText(bundle.getString("current_layer_label") + layerName);
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
        dynamicSection.add(new JLabel(bundle.getString("floor_segment_distance") + String.format("%.2f", distance)));
        dynamicSection.revalidate();
        dynamicSection.repaint();
    }

    public void updateDynamicSectionToCompleteFloorProperties(float area) {
        dynamicSection.removeAll();
        dynamicSection.add(new JLabel(bundle.getString("floor_area") + String.format("%.2f", area)));
        dynamicSection.revalidate();
        dynamicSection.repaint();
    }

    public void updateDynamicSectionToPlainAreaProperties(float area){
        dynamicSection.removeAll();
        dynamicSection.add(new JLabel(bundle.getString("plain_area_area") + String.format("%.2f", area)));
        dynamicSection.revalidate();
        dynamicSection.repaint();
    }

    public void updateDynamicSectionToRackShelf() {
        dynamicSection.removeAll();

        int transformContentWidth = transformContent.getPreferredSize().width;

        JPanel dynamicPanel = new JPanel();
        dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(dynamicPanel);
        scrollPane.setPreferredSize(new Dimension(transformContentWidth, 200));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        Dimension fieldSize = new Dimension(transformContentWidth, 25);

        dynamicPanel.add(new JLabel(bundle.getString("rack_physical_dimensions")));

        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, Double.MAX_VALUE, 0.01));
        heightSpinner.setPreferredSize(fieldSize);
        dynamicPanel.add(new JLabel(bundle.getString("rack_height")));
        dynamicPanel.add(heightSpinner);

        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, Double.MAX_VALUE, 0.01));
        widthSpinner.setPreferredSize(fieldSize);
        dynamicPanel.add(new JLabel(bundle.getString("rack_width")));
        dynamicPanel.add(widthSpinner);

        JSpinner depthSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, Double.MAX_VALUE, 0.01));
        depthSpinner.setPreferredSize(fieldSize);
        dynamicPanel.add(new JLabel(bundle.getString("rack_depth")));
        dynamicPanel.add(depthSpinner);

        JSpinner shelvesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        shelvesSpinner.setPreferredSize(fieldSize);
        dynamicPanel.add(new JLabel(bundle.getString("rack_shelves")));
        dynamicPanel.add(shelvesSpinner);

        dynamicPanel.add(new JLabel(bundle.getString("rack_weight_capacity")));

        JSpinner perShelfCapacitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        perShelfCapacitySpinner.setPreferredSize(fieldSize);
        dynamicPanel.add(new JLabel(bundle.getString("shelf_capacity")));
        dynamicPanel.add(perShelfCapacitySpinner);

        JSpinner totalCapacitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        totalCapacitySpinner.setPreferredSize(fieldSize);
        dynamicPanel.add(new JLabel(bundle.getString("total_capacity")));
        dynamicPanel.add(totalCapacitySpinner);

        if (selectedObject != null) {
            RackSettings existingSettings = undoManager.getRackSettingsMap().get(selectedObject);
            if (existingSettings != null) {
                heightSpinner.setValue(existingSettings.getHeight());
                widthSpinner.setValue(existingSettings.getWidth());
                depthSpinner.setValue(existingSettings.getDepth());
                shelvesSpinner.setValue(existingSettings.getShelves());
                perShelfCapacitySpinner.setValue(existingSettings.getPerShelfCapacity());
                totalCapacitySpinner.setValue(existingSettings.getTotalCapacity());
            }
        }

        dynamicSection.add(scrollPane);

        JButton setSettingsButton = new JButton(bundle.getString("set_settings"));
        setSettingsButton.addActionListener(e -> {
            double height = (Double) heightSpinner.getValue();
            double width = (Double) widthSpinner.getValue();
            double depth = (Double) depthSpinner.getValue();
            int shelves = (Integer) shelvesSpinner.getValue();
            int perShelfCapacity = (Integer) perShelfCapacitySpinner.getValue();
            int totalCapacity = (Integer) totalCapacitySpinner.getValue();

            RackSettings rackSettings = new RackSettings(height, width, depth, shelves, perShelfCapacity, totalCapacity);

            if (selectedObject != null) {
                undoManager.setRackSettings(selectedObject, rackSettings);
            }

            System.out.println(rackSettings);
        });

        dynamicPanel.add(setSettingsButton);

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

    public void updateTagDropdown(ArrayList<Tag> currentTags) {
        String[] tagNames = new String[currentTags.size() + 1];
        tagNames[0] = "---";

        for (int i = 0; i < currentTags.size(); i++) {
            tagNames[i + 1] = currentTags.get(i).getName();
        }
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(tagNames);

        if (selectedObject != null) {
            Tag selectedTag = undoManager.getTagMap().get(selectedObject);

            if (selectedTag != null) {
                model.setSelectedItem(selectedTag.getName());
            } else {
                model.setSelectedItem("---");
            }
        } else {
            model.setSelectedItem("---");
        }

        tagDropdown.setModel(model);
    }

    private void tagDropdownActionPerformed() {
        if (selectedObject != null) {
            String selectedTagName = (String) tagDropdown.getSelectedItem();

            if (selectedTagName != null && !selectedTagName.equals("---")) {
                Tag tagToAdd = null;
                for (Tag tag : undoManager.getTags()) {
                    if (tag.getName().equals(selectedTagName)) {
                        tagToAdd = tag;
                        break;
                    }
                }

                if (tagToAdd != null) {
                    undoManager.addSpatialWithTag(selectedObject, tagToAdd);
                    System.out.println("Added tag '" + selectedTagName + "' to selected object.");
                }
            } else {
                undoManager.removeSpatialTag(selectedObject);
                System.out.println("Removed all tags from selected object.");
            }
        } else {
            System.out.println("No object selected.");
        }
    }

    public void updateProperties(String name, Vector3f position, Vector3f rotation, Vector3f scale, ColorRGBA color) {
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

        colorPreview.setBackground(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
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

    public void setOnColorChange(Consumer<ColorRGBA> callback) {
        this.onColorChange = callback;
    }
}
