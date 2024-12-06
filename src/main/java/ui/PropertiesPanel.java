package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.function.Consumer;
import com.jme3.math.Vector3f;
import tech.ObjectControls;

public class PropertiesPanel extends JPanel {

    private JTextField nameField;
    private JTextField positionXField, positionYField, positionZField;
    private JTextField rotationXField, rotationYField, rotationZField;
    private JTextField scaleXField, scaleYField, scaleZField;

    private Consumer<String> onNameChange;
    private Consumer<Vector3f> onPositionChange;
    private Consumer<Vector3f> onRotationChange;
    private Consumer<Vector3f> onScaleChange;

    private boolean isTransformFolded = true;
    private ObjectControls objectControls;

    public void setObjectControls(ObjectControls objectControls){
        this.objectControls = objectControls;
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

        JCheckBox snapToGridCheckbox = new JCheckBox("Snap to Grid");
        add(snapToGridCheckbox);
        snapToGridCheckbox.addActionListener(e -> objectControls.setSnapToGrid(snapToGridCheckbox.isSelected()));

        JCheckBox heightAdjustmentCheckbox = new JCheckBox("Height Adjustment");
        heightAdjustmentCheckbox.setSelected(true);
        add(heightAdjustmentCheckbox);
        heightAdjustmentCheckbox.addActionListener(e -> objectControls.setHeightAdjustment(heightAdjustmentCheckbox.isSelected()));

        add(Box.createVerticalGlue());

        setupRealTimeListeners();
    }

    private JPanel createTransformPanel() {
        JPanel transformContent = new JPanel();
        transformContent.setLayout(new GridLayout(3, 4, 5, 5));

        transformContent.add(new JLabel("Position:"));
        positionXField = createCompactTextField(5);
        positionYField = createCompactTextField(5);
        positionZField = createCompactTextField(5);
        transformContent.add(positionXField);
        transformContent.add(positionYField);
        transformContent.add(positionZField);

        transformContent.add(new JLabel("Rotation:"));
        rotationXField = createCompactTextField(5);
        rotationYField = createCompactTextField(5);
        rotationZField = createCompactTextField(5);
        transformContent.add(rotationXField);
        transformContent.add(rotationYField);
        transformContent.add(rotationZField);

        transformContent.add(new JLabel("Scale:"));
        scaleXField = createCompactTextField(5);
        scaleYField = createCompactTextField(5);
        scaleZField = createCompactTextField(5);
        transformContent.add(scaleXField);
        transformContent.add(scaleYField);
        transformContent.add(scaleZField);

        return transformContent;
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
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
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
                    // Handle invalid input.
                }
            }
        };
        xField.addKeyListener(adapter);
        yField.addKeyListener(adapter);
        zField.addKeyListener(adapter);
    }

    public void updateProperties(String name, Vector3f position, Vector3f rotation, Vector3f scale) {
        nameField.setText(name);
        positionXField.setText(String.format(Locale.US, "%.2f",position.x));
        positionYField.setText(String.format(Locale.US, "%.2f",position.y));
        positionZField.setText(String.format(Locale.US, "%.2f",position.z));

        rotationXField.setText(String.format(Locale.US, "%.2f", Math.toDegrees(rotation.x)));
        rotationYField.setText(String.format(Locale.US, "%.2f", Math.toDegrees(rotation.y)));
        rotationZField.setText(String.format(Locale.US, "%.2f", Math.toDegrees(rotation.z)));

        scaleXField.setText(String.format(Locale.US, "%.2f", scale.x));
        scaleYField.setText(String.format(Locale.US, "%.2f",scale.y));
        scaleZField.setText(String.format(Locale.US, "%.2f",scale.z));
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
