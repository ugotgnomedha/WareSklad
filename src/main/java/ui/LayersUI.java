package ui;

import com.jme3.scene.Spatial;
import tech.layers.Layer;
import tech.layers.LayersManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

public class LayersUI extends JPanel {

    private final LayersManager layersManager;
    private JButton removeObjectButton;
    private ResourceBundle bundle;

    public LayersUI(LayersManager layersManager) {
        this.layersManager = layersManager;
        this.bundle = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault());
        this.setLayout(new BorderLayout());
    }

    public JMenu createLayersMenu(JFrame frame) {
        JMenu layersMenu = new JMenu(bundle.getString("layersMenu"));

        JMenuItem addLayerItem = new JMenuItem(bundle.getString("addLayer"));
        addLayerItem.addActionListener(e -> showAddLayerDialog(frame));
        layersMenu.add(addLayerItem);

        JMenuItem editLayerItem = new JMenuItem(bundle.getString("editLayer"));
        editLayerItem.addActionListener(e -> showEditLayerDialog(frame));
        layersMenu.add(editLayerItem);

        return layersMenu;
    }

    private void showAddLayerDialog(JFrame parentFrame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int dialogWidth = screenSize.width / 3;
        int dialogHeight = screenSize.height / 2;

        JDialog addLayerDialog = new JDialog(parentFrame, bundle.getString("addLayer"), true);
        addLayerDialog.setSize(dialogWidth, dialogHeight);
        addLayerDialog.setLayout(new GridLayout(4, 2));

        JLabel nameLabel = new JLabel(bundle.getString("layerName"));
        JTextField nameField = new JTextField();
        addLayerDialog.add(nameLabel);
        addLayerDialog.add(nameField);

        JLabel opacityLabel = new JLabel(bundle.getString("opacity"));
        JSlider opacitySlider = new JSlider(0, 100, 50);
        opacitySlider.setMajorTickSpacing(10);
        opacitySlider.setMinorTickSpacing(1);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        addLayerDialog.add(opacityLabel);
        addLayerDialog.add(opacitySlider);

        JLabel lockLabel = new JLabel(bundle.getString("lockEdit"));
        JCheckBox lockCheckBox = new JCheckBox();
        addLayerDialog.add(lockLabel);
        addLayerDialog.add(lockCheckBox);

        JButton okButton = new JButton(bundle.getString("ok"));
        okButton.addActionListener(e -> {
            String layerName = nameField.getText();
            float opacity = opacitySlider.getValue();
            boolean lockEdit = lockCheckBox.isSelected();

            layersManager.addLayer(layerName, opacity, lockEdit);
            System.out.println(bundle.getString("layerAdded") + ": " + layerName);
            addLayerDialog.dispose();
        });
        addLayerDialog.add(okButton);

        JButton cancelButton = new JButton(bundle.getString("cancel"));
        cancelButton.addActionListener(e -> addLayerDialog.dispose());
        addLayerDialog.add(cancelButton);

        addLayerDialog.setLocationRelativeTo(parentFrame);
        addLayerDialog.setVisible(true);
    }

    private void showEditLayerDialog(JFrame parentFrame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int dialogWidth = screenSize.width / 3;
        int dialogHeight = screenSize.height / 2;

        JDialog editLayerDialog = new JDialog(parentFrame, bundle.getString("editLayer"), true);
        editLayerDialog.setSize(dialogWidth, dialogHeight);
        editLayerDialog.setLayout(new BorderLayout());

        JPanel layerSelectionPanel = new JPanel();
        layerSelectionPanel.setLayout(new BoxLayout(layerSelectionPanel, BoxLayout.Y_AXIS));

        String[] layerNames = layersManager.getAllLayers().keySet().toArray(new String[0]);
        String[] allLayerNames = new String[layerNames.length + 1];
        allLayerNames[0] = bundle.getString("selectExistingLayer");
        System.arraycopy(layerNames, 0, allLayerNames, 1, layerNames.length);

        JComboBox<String> layerComboBox = new JComboBox<>(allLayerNames);
        layerComboBox.addActionListener(e -> showLayerEditingFields(editLayerDialog, (String) layerComboBox.getSelectedItem()));

        layerSelectionPanel.add(new JLabel(bundle.getString("selectLayer")));
        layerSelectionPanel.add(layerComboBox);

        JPanel editingPanel = new JPanel();
        editingPanel.setLayout(new GridLayout(4, 2));

        JLabel nameLabel = new JLabel(bundle.getString("layerName"));
        JTextField nameField = new JTextField();
        editingPanel.add(nameLabel);
        editingPanel.add(nameField);

        JLabel opacityLabel = new JLabel(bundle.getString("opacity"));
        JSlider opacitySlider = new JSlider(0, 100, 50);
        opacitySlider.setMajorTickSpacing(10);
        opacitySlider.setMinorTickSpacing(1);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        editingPanel.add(opacityLabel);
        editingPanel.add(opacitySlider);

        JLabel lockLabel = new JLabel(bundle.getString("lockEdit"));
        JCheckBox lockCheckBox = new JCheckBox();
        editingPanel.add(lockLabel);
        editingPanel.add(lockCheckBox);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(bundle.getString("objects"));
        JTree objectTree = new JTree(rootNode);
        JScrollPane objectScrollPane = new JScrollPane(objectTree);
        objectScrollPane.setPreferredSize(new Dimension(dialogWidth / 3, dialogHeight / 2));

        objectTree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            if (path != null) {
                Object selectedNode = path.getLastPathComponent();
                if (selectedNode instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectedNode;
                    Object userObject = treeNode.getUserObject();
                    if (userObject instanceof Spatial) {
                        Spatial selectedSpatial = (Spatial) userObject;
                        System.out.println("Selected: " + selectedSpatial.getName());
                        removeObjectButton.setEnabled(true);
                        removeObjectButton.putClientProperty("spatial", selectedSpatial);
                    }
                }
            }
        });

        JPanel buttonsPanel = new JPanel();
        removeObjectButton = new JButton(bundle.getString("removeObjectFromLayer"));
        removeObjectButton.setEnabled(false);
        removeObjectButton.addActionListener(e -> {
            String selectedLayerName = (String) layerComboBox.getSelectedItem();
            Spatial selectedSpatial = (Spatial) removeObjectButton.getClientProperty("spatial");
            if (selectedLayerName != null && selectedSpatial != null) {
                layersManager.removeSpatialFromLayer(selectedLayerName, selectedSpatial);
                System.out.println("Removed object: " + selectedSpatial.getName() + " from layer: " + selectedLayerName);
                updateObjectTree(objectTree, selectedLayerName);
            }
        });

        buttonsPanel.add(removeObjectButton);

        JScrollPane layerSelectionScrollPane = new JScrollPane(layerSelectionPanel);
        editLayerDialog.add(layerSelectionScrollPane, BorderLayout.NORTH);
        editLayerDialog.add(editingPanel, BorderLayout.CENTER);
        editLayerDialog.add(objectScrollPane, BorderLayout.EAST);
        editLayerDialog.add(buttonsPanel, BorderLayout.SOUTH);

        JButton okButton = new JButton(bundle.getString("ok"));
        okButton.addActionListener(e -> {
            String selectedLayerName = (String) layerComboBox.getSelectedItem();
            if ("-Select an existing layer-".equals(selectedLayerName)) {
                JOptionPane.showMessageDialog(editLayerDialog, bundle.getString("selectValidLayer"), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            Layer layer = layersManager.getLayer(selectedLayerName);
            if (layer != null) {
                String newLayerName = nameField.getText();
                float opacity = opacitySlider.getValue();
                boolean lockEdit = lockCheckBox.isSelected();

                layersManager.editLayer(selectedLayerName, newLayerName, opacity, lockEdit);
                System.out.println(bundle.getString("layerUpdated") + ": " + newLayerName);
                editLayerDialog.dispose();
            }
        });
        buttonsPanel.add(okButton);

        JButton deleteButton = new JButton(bundle.getString("delete"));
        deleteButton.addActionListener(e -> {
            String selectedLayerName = (String) layerComboBox.getSelectedItem();
            if ("-Select an existing layer-".equals(selectedLayerName)) {
                JOptionPane.showMessageDialog(editLayerDialog, bundle.getString("selectValidLayer"), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirmation = JOptionPane.showConfirmDialog(editLayerDialog,
                    String.format(bundle.getString("confirmDelete"), selectedLayerName),
                    bundle.getString("error"),
                    JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                layersManager.removeLayer(selectedLayerName);
                System.out.println(bundle.getString("layerDeleted") + ": " + selectedLayerName);
                editLayerDialog.dispose();
            }
        });
        buttonsPanel.add(deleteButton);

        JButton cancelButton = new JButton(bundle.getString("cancel"));
        cancelButton.addActionListener(e -> editLayerDialog.dispose());
        buttonsPanel.add(cancelButton);

        editLayerDialog.setLocationRelativeTo(parentFrame);
        editLayerDialog.setVisible(true);
    }

    private void updateObjectTree(JTree objectTree, String layerName) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) objectTree.getModel().getRoot();
        rootNode.removeAllChildren();

        Layer layer = layersManager.getLayer(layerName);
        if (layer != null) {
            for (Spatial spatial : layer.getSpatials()) {
                DefaultMutableTreeNode spatialNode = new DefaultMutableTreeNode(spatial);
                rootNode.add(spatialNode);
            }
        }

        ((DefaultTreeModel) objectTree.getModel()).reload();
    }

    private void showLayerEditingFields(JDialog editLayerDialog, String selectedLayer) {
        Layer layer = layersManager.getLayer(selectedLayer);

        JTextField nameField = ((JTextField) ((JPanel) editLayerDialog.getContentPane().getComponent(1)).getComponent(1));
        JSlider opacitySlider = ((JSlider) ((JPanel) editLayerDialog.getContentPane().getComponent(1)).getComponent(3));
        JCheckBox lockCheckBox = ((JCheckBox) ((JPanel) editLayerDialog.getContentPane().getComponent(1)).getComponent(5));

        if (layer != null) {
            nameField.setText(layer.getName());
            int originalOpacity = (int) (layer.getOpacity() * 100);
            opacitySlider.setValue(originalOpacity);
            lockCheckBox.setSelected(layer.isLockEdit());
            updateObjectTree((JTree) ((JScrollPane) editLayerDialog.getContentPane().getComponent(2)).getViewport().getView(), selectedLayer);
        }
    }
}
