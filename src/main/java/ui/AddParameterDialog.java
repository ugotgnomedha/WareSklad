package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import tech.parameters.Parameter;
import tech.parameters.ParameterType;

import java.util.Objects;
import java.util.ResourceBundle;

public class AddParameterDialog extends JDialog {
    private JTextField nameField;
    private JComboBox<String> typeComboBox;
    private JTextField unitField;
    private JTextField defaultValueField;
    private JButton addButton;
    private Parameter newParameter;
    private ResourceBundle bundle;

    public AddParameterDialog(JFrame parent, ResourceBundle bundle) {
        super(parent, bundle.getString("add_parameter_dialog_title"), true);
        this.bundle = bundle;
        setSize(400, 300);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel(bundle.getString("add_parameter_name_label")));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel(bundle.getString("add_parameter_type_label")));
        typeComboBox = new JComboBox<>(new String[]{
                bundle.getString("parameter_boolean"),
                bundle.getString("parameter_string"),
                bundle.getString("parameter_number")
        });
        panel.add(typeComboBox);

        panel.add(new JLabel(bundle.getString("add_parameter_unit_label")));
        unitField = new JTextField();
        panel.add(unitField);

        panel.add(new JLabel(bundle.getString("add_parameter_default_value_label")));
        defaultValueField = new JTextField();
        panel.add(defaultValueField);

        addButton = new JButton(bundle.getString("add_parameter_button"));
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String typeString = (String) typeComboBox.getSelectedItem();
            String unit = unitField.getText();
            String defaultValue = defaultValueField.getText();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(AddParameterDialog.this, bundle.getString("validation_required_field"), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ParameterType type;
            switch (Objects.requireNonNull(typeString)) {
                case "boolean" -> type = ParameterType.BOOLEAN;
                case "string" -> type = ParameterType.STRING;
                case "number" -> type = ParameterType.NUMBER;
                default -> {
                    JOptionPane.showMessageDialog(AddParameterDialog.this, "Invalid parameter type.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (type == ParameterType.BOOLEAN) {
                if (!defaultValue.equalsIgnoreCase("true") && !defaultValue.equalsIgnoreCase("false")) {
                    JOptionPane.showMessageDialog(AddParameterDialog.this, bundle.getString("validation_invalid_boolean"), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (type == ParameterType.NUMBER) {
                try {
                    Double.parseDouble(defaultValue);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AddParameterDialog.this, bundle.getString("validation_invalid_number"), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            newParameter = new Parameter(name, type, unit, defaultValue);
            dispose();
        });
        panel.add(addButton);

        add(panel);
    }

    public Parameter getNewParameter() {
        return newParameter;
    }
}