package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;
import tech.parameters.Parameter;
import tech.parameters.ParameterManager;
import tech.parameters.ParameterSection;
import UndoRedo.UndoManager;
import com.jme3.scene.Spatial;
import java.util.ResourceBundle;

public class ParametersFrame extends JFrame {
    private Spatial selectedObject;
    private UndoManager undoManager;
    private PropertiesPanel propertiesPanel;
    private ParameterManager parameterManager;
    private ResourceBundle bundle;
    private JTabbedPane tabbedPane;

    public ParametersFrame(Spatial selectedObject, UndoManager undoManager, PropertiesPanel propertiesPanel, ParameterManager parameterManager, ResourceBundle bundle) {
        this.selectedObject = selectedObject;
        this.undoManager = undoManager;
        this.propertiesPanel = propertiesPanel;
        this.parameterManager = parameterManager;
        this.bundle = bundle;

        setTitle(bundle.getString("manage_parameters"));
        setSize(500, 400);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        refreshTabbedPane();

        JButton addParameterButton = new JButton(bundle.getString("add_parameter"));
        addParameterButton.addActionListener(e -> openAddParameterDialog());

        JButton removeParameterButton = new JButton(bundle.getString("remove_parameter"));
        removeParameterButton.addActionListener(e -> removeSelectedParameter());

        JButton applyPresetButton = new JButton(bundle.getString("apply_preset_button"));
        applyPresetButton.addActionListener(e -> applyPreset());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addParameterButton);
        buttonPanel.add(removeParameterButton);
        buttonPanel.add(applyPresetButton);

        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshTabbedPane() {
        tabbedPane.removeAll();
        List<String> sections = parameterManager.getSections().stream()
                .map(ParameterSection::getSection)
                .collect(Collectors.toList());

        for (String section : sections) {
            JPanel sectionPanel = createSectionPanel(section);
            tabbedPane.addTab(section, sectionPanel);
        }
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    private JPanel createSectionPanel(String section) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        List<Parameter> parameters = parameterManager.getParametersBySection(section);
        for (Parameter parameter : parameters) {
            JCheckBox checkBox = new JCheckBox(parameter.toString());
            checkBox.setSelected(undoManager.hasParameter(selectedObject, parameter));
            checkBox.addActionListener(new ParameterCheckBoxListener(parameter));
            panel.add(checkBox);
        }

        return panel;
    }

    private void openAddParameterDialog() {
        int selectedTabIndex = tabbedPane.getSelectedIndex();
        if (selectedTabIndex == -1) return;

        String selectedSection = parameterManager.getSections().get(selectedTabIndex).getSection();
        if (!parameterManager.getSections().get(selectedTabIndex).isModifiable()) {
            JOptionPane.showMessageDialog(this, bundle.getString("section_not_modifiable_error"), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        AddParameterDialog dialog = new AddParameterDialog(this, bundle);
        dialog.setVisible(true);

        Parameter newParameter = dialog.getNewParameter();
        if (newParameter != null) {
            parameterManager.addParameter(selectedSection, newParameter);
            refreshTabbedPane();
            propertiesPanel.updateParametersSection();
        }
    }

    private void removeSelectedParameter() {
        int selectedTabIndex = tabbedPane.getSelectedIndex();
        if (selectedTabIndex == -1) return;

        String section = parameterManager.getSections().get(selectedTabIndex).getSection();
        if (!parameterManager.getSections().get(selectedTabIndex).isModifiable()) {
            JOptionPane.showMessageDialog(this, bundle.getString("section_not_modifiable_error"), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Parameter> parameters = parameterManager.getParametersBySection(section);

        String[] parameterNames = parameters.stream()
                .map(Parameter::getName)
                .toArray(String[]::new);

        String selectedParameterName = (String) JOptionPane.showInputDialog(
                this,
                bundle.getString("remove_parameter_select_label"),
                bundle.getString("remove_parameter_dialog_title"),
                JOptionPane.PLAIN_MESSAGE,
                null,
                parameterNames,
                null);

        if (selectedParameterName != null) {
            parameterManager.deleteParameter(selectedParameterName);
            refreshTabbedPane();
            propertiesPanel.updateParametersSection();
        }
    }

    private void applyPreset() {
        String[] presetNames = parameterManager.getSections().stream()
                .filter(s -> !s.isModifiable())
                .map(ParameterSection::getSection)
                .toArray(String[]::new);

        String selectedPreset = (String) JOptionPane.showInputDialog(
                this,
                bundle.getString("apply_preset_dialog_message"),
                bundle.getString("apply_preset_button"),
                JOptionPane.PLAIN_MESSAGE,
                null,
                presetNames,
                null);

        if (selectedPreset != null) {
            parameterManager.applyPreset(selectedPreset, selectedObject, undoManager);
            refreshTabbedPane();
            propertiesPanel.updateParametersSection();
        }
    }

    private class ParameterCheckBoxListener implements ActionListener {
        private Parameter parameter;

        public ParameterCheckBoxListener(Parameter parameter) {
            this.parameter = parameter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            if (checkBox.isSelected()) {
                undoManager.addParameterToSpatial(selectedObject, parameter);
            } else {
                undoManager.removeParameterFromSpatial(selectedObject, parameter);
            }
            propertiesPanel.updateParametersSection();
        }
    }
}