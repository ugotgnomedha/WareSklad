package ui;

import UndoRedo.UndoManager;
import com.jme3.scene.Geometry;
import tech.simulations.Forklift;
import tech.simulations.Pallet;
import tech.simulations.SimulationManager;
import tech.WareSkladInit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class SimulationUI {
    private ResourceBundle bundle;
    private UndoManager undoManager;
    private DefaultListModel<String> availablePalletsModel = new DefaultListModel<>();
    private SimulationManager simulationManager;
    private Map<String, Pallet> palletMap = new HashMap<>();
    private DefaultListModel<Forklift> forkliftModel = new DefaultListModel<>();
    private JComboBox<Forklift> forkliftDropdown;
    private WareSkladInit jmeScene;

    public void setupSimulation(UndoManager undoManager, ResourceBundle bundle, WareSkladInit jmeScene) {
        this.undoManager = undoManager;
        this.bundle = bundle;
        this.jmeScene = jmeScene;
        this.simulationManager = new SimulationManager(jmeScene, 2);
    }

    public void openSimulationFrame() {
        JFrame simulationFrame = new JFrame(bundle.getString("simulation"));
        simulationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width / 2;
        int height = screenSize.height / 2;
        simulationFrame.setSize(width, height);
        simulationFrame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel simulateReceivingPanel = createSimulateReceivingPanel(simulationFrame);
        JPanel createForkliftPanel = createForkliftPanel(simulationFrame);
        JPanel createPalletPanel = createCreatePalletPanel(simulationFrame);

        tabbedPane.addTab(bundle.getString("receivingSimulation"), simulateReceivingPanel);
        tabbedPane.addTab(bundle.getString("forklifts"), createForkliftPanel);
        tabbedPane.addTab(bundle.getString("createPallet"), createPalletPanel);

        simulationFrame.add(tabbedPane);
        simulationFrame.setVisible(true);
    }

    private JPanel createForkliftPanel(JFrame simulationFrame) {
        JPanel panel = new JPanel(new BorderLayout());

        JList<Forklift> forkliftList = new JList<>(forkliftModel);
        JScrollPane scrollPane = new JScrollPane(forkliftList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton addButton = new JButton(bundle.getString("addForklift"));
        addButton.addActionListener(e -> {
            String id = JOptionPane.showInputDialog(simulationFrame, bundle.getString("enterForkliftId"));
            String capacity = JOptionPane.showInputDialog(simulationFrame, bundle.getString("enterMaxWeightCapacity"));
            String speed = JOptionPane.showInputDialog(simulationFrame, bundle.getString("enterSpeed"));

            if (id != null && capacity != null && speed != null) {
                try {
                    double capacityValue = validateAndFormatFloatInput(capacity);
                    double speedValue = validateAndFormatFloatInput(speed);

                    if (capacityValue < 0 || speedValue < 0) {
                        JOptionPane.showMessageDialog(simulationFrame, bundle.getString("invalidInput"), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Forklift forklift = new Forklift(id, capacityValue, speedValue);
                    forkliftModel.addElement(forklift);

                    updateForkliftDropdown(forkliftDropdown);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(simulationFrame, bundle.getString("invalidInput"), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton removeButton = new JButton(bundle.getString("removeForklift"));
        removeButton.addActionListener(e -> {
            Forklift selected = forkliftList.getSelectedValue();
            if (selected != null) {
                forkliftModel.removeElement(selected);
                updateForkliftDropdown(forkliftDropdown);
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private double validateAndFormatFloatInput(String input) throws NumberFormatException {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setMaximumFractionDigits(2);
        return Double.parseDouble(df.format(Double.parseDouble(input)));
    }

    private JPanel createCreatePalletPanel(JFrame simulationFrame) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(200);

        JPanel createPanel = new JPanel();
        createPanel.setLayout(new BoxLayout(createPanel, BoxLayout.Y_AXIS));

        JLabel createLabel = new JLabel(bundle.getString("createPallets"));
        createPanel.add(createLabel);

        String[] createColumnNames = {bundle.getString("palletID"), bundle.getString("height"), bundle.getString("width"), bundle.getString("depth"), bundle.getString("weight")};
        DefaultTableModel createTableModel = new DefaultTableModel(createColumnNames, 0);
        String[] editColumnNames = {bundle.getString("palletID"), bundle.getString("height"), bundle.getString("width"), bundle.getString("depth"), bundle.getString("weight")};
        DefaultTableModel editTableModel = new DefaultTableModel(editColumnNames, 0);

        JTable createTable = new JTable(createTableModel);
        JScrollPane createScrollPane = new JScrollPane(createTable);
        createScrollPane.setPreferredSize(new Dimension(400, 150));
        createPanel.add(createScrollPane);

        JButton addRowButton = new JButton(bundle.getString("addRow"));
        addRowButton.addActionListener(e -> {
            String palletId = bundle.getString("newPallet") + " " + (createTableModel.getRowCount() + 1);
            createTableModel.addRow(new Object[]{palletId, "", "", "", ""});
        });
        createPanel.add(addRowButton);

        JButton removeRowButton = new JButton(bundle.getString("removeRow"));
        removeRowButton.addActionListener(e -> {
            int selectedRow = createTable.getSelectedRow();
            if (selectedRow != -1) {
                createTableModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(simulationFrame, bundle.getString("selectRowToRemove"), bundle.getString("error"), JOptionPane.WARNING_MESSAGE);
            }
        });
        createPanel.add(removeRowButton);

        JButton createPalletButton = new JButton(bundle.getString("createPallet"));
        createPalletButton.addActionListener(e -> {
            try {
                for (int i = 0; i < createTableModel.getRowCount(); i++) {
                    String palletId = (String) createTableModel.getValueAt(i, 0);
                    double height = Double.parseDouble(createTableModel.getValueAt(i, 1).toString());
                    double width = Double.parseDouble(createTableModel.getValueAt(i, 2).toString());
                    double depth = Double.parseDouble(createTableModel.getValueAt(i, 3).toString());
                    double weight = Double.parseDouble(createTableModel.getValueAt(i, 4).toString());

                    Pallet newPallet = new Pallet(palletId, height, width, depth, weight);
                    undoManager.addPallet(newPallet);
                }

                createTableModel.setRowCount(0);
                updateExistingPalletsTable(editTableModel);

                updateAvailablePalletsList();

                JOptionPane.showMessageDialog(simulationFrame, bundle.getString("palletCreatedSuccess"), bundle.getString("success"), JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(simulationFrame, bundle.getString("invalidData"), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        });
        createPanel.add(createPalletButton);

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));

        JLabel editLabel = new JLabel(bundle.getString("editPallets"));
        editPanel.add(editLabel);

        JTable editTable = new JTable(editTableModel);
        JScrollPane editScrollPane = new JScrollPane(editTable);
        editScrollPane.setPreferredSize(new Dimension(400, 150));
        editPanel.add(editScrollPane);

        updateExistingPalletsTable(editTableModel);

        JButton deletePalletButton = new JButton(bundle.getString("deletePallet"));
        deletePalletButton.addActionListener(e -> {
            int selectedRow = editTable.getSelectedRow();
            if (selectedRow != -1) {
                String palletId = (String) editTableModel.getValueAt(selectedRow, 0);
                Pallet palletToRemove = undoManager.getPallets().stream()
                        .filter(p -> p.getId().equals(palletId))
                        .findFirst()
                        .orElse(null);
                if (palletToRemove != null) {
                    undoManager.getPallets().remove(palletToRemove);
                    editTableModel.removeRow(selectedRow);

                    updateAvailablePalletsList();

                    JOptionPane.showMessageDialog(simulationFrame, bundle.getString("palletDeletedSuccess"), bundle.getString("success"), JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(simulationFrame, bundle.getString("selectRowToRemove"), bundle.getString("error"), JOptionPane.WARNING_MESSAGE);
            }
        });
        editPanel.add(deletePalletButton);

        JButton saveChangesButton = new JButton(bundle.getString("saveChanges"));
        saveChangesButton.addActionListener(e -> {
            try {
                for (int i = 0; i < editTableModel.getRowCount(); i++) {
                    String palletId = (String) editTableModel.getValueAt(i, 0);
                    double height = Double.parseDouble(editTableModel.getValueAt(i, 1).toString());
                    double width = Double.parseDouble(editTableModel.getValueAt(i, 2).toString());
                    double depth = Double.parseDouble(editTableModel.getValueAt(i, 3).toString());
                    double weight = Double.parseDouble(editTableModel.getValueAt(i, 4).toString());

                    Pallet updatedPallet = new Pallet(palletId, height, width, depth, weight);
                    undoManager.updatePallet(updatedPallet);
                }

                JOptionPane.showMessageDialog(simulationFrame, bundle.getString("palletUpdatedSuccess"), bundle.getString("success"), JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(simulationFrame, bundle.getString("invalidData"), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        });
        editPanel.add(saveChangesButton);

        splitPane.setTopComponent(createPanel);
        splitPane.setBottomComponent(editPanel);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private void updateExistingPalletsTable(DefaultTableModel editTableModel) {
        editTableModel.setRowCount(0);
        for (Pallet pallet : undoManager.getPallets()) {
            editTableModel.addRow(new Object[]{pallet.getId(), pallet.getHeight(), pallet.getWidth(), pallet.getDepth(), pallet.getWeight()});
        }
    }

    private JPanel createSimulateReceivingPanel(JFrame simulationFrame) {
        JPanel panel = new JPanel(new BorderLayout());
        updateAvailablePalletsList();

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JLabel receivingAreaLabel = new JLabel(bundle.getString("selectReceivingArea"));
        JComboBox<Geometry> receivingAreaDropdown = new JComboBox<>();
        populateReceivingSendingAreas(receivingAreaDropdown, undoManager.getPlainAreaCompleteAreas());

        receivingAreaDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Geometry) {
                    setText(((Geometry) value).getName());
                } else {
                    setText("---");
                }
                return this;
            }
        });

        JLabel forkliftLabel = new JLabel(bundle.getString("selectForklift"));
        forkliftDropdown = new JComboBox<>();
        updateForkliftDropdown(forkliftDropdown);

        forkliftDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Forklift) {
                    Forklift forklift = (Forklift) value;
                    setText(forklift.getId() + " (Capacity: " + forklift.getMaxWeightCapacity() + ")");
                } else {
                    setText("---");
                }
                return this;
            }
        });

        JLabel avoidanceDistanceLabel = new JLabel(bundle.getString("avoidanceDistance"));
        JTextField avoidanceDistanceField = new JTextField("2", 5);

        northPanel.add(receivingAreaLabel);
        northPanel.add(receivingAreaDropdown);
        northPanel.add(forkliftLabel);
        northPanel.add(forkliftDropdown);
        northPanel.add(avoidanceDistanceLabel);
        northPanel.add(avoidanceDistanceField);

        panel.add(northPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));

        JList<String> availablePalletsList = new JList<>(availablePalletsModel);
        JScrollPane availableScrollPane = new JScrollPane(availablePalletsList);
        centerPanel.add(new JPanel(new BorderLayout()) {{
            add(new JLabel(bundle.getString("availablePallets")), BorderLayout.NORTH);
            add(availableScrollPane, BorderLayout.CENTER);
        }});

        DefaultListModel<String> selectedPalletsModel = new DefaultListModel<>();
        JList<String> selectedPalletsList = new JList<>(selectedPalletsModel);
        JScrollPane selectedScrollPane = new JScrollPane(selectedPalletsList);
        centerPanel.add(new JPanel(new BorderLayout()) {{
            add(new JLabel(bundle.getString("selectedPallets")), BorderLayout.NORTH);
            add(selectedScrollPane, BorderLayout.CENTER);
        }});

        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        JButton addButton = new JButton("+");
        addButton.addActionListener(e -> {
            for (String selected : availablePalletsList.getSelectedValuesList()) {
                availablePalletsModel.removeElement(selected);
                selectedPalletsModel.addElement(selected);
            }
        });

        JButton removeButton = new JButton("-");
        removeButton.addActionListener(e -> {
            for (String selected : selectedPalletsList.getSelectedValuesList()) {
                selectedPalletsModel.removeElement(selected);
                availablePalletsModel.addElement(selected);
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        JButton startSimulationButton = new JButton(bundle.getString("startSimulation"));
        startSimulationButton.addActionListener(e -> {
            boolean isValid = true;
            StringBuilder errorMessage = new StringBuilder(bundle.getString("validationError") + ":\n");

            Geometry selectedArea = (Geometry) receivingAreaDropdown.getSelectedItem();
            if (selectedArea == null) {
                isValid = false;
                errorMessage.append(bundle.getString("areaNotSelected")).append("\n");
            }

            if (selectedPalletsModel.isEmpty()) {
                isValid = false;
                errorMessage.append(bundle.getString("noPalletSelected")).append("\n");
            }

            Forklift selectedForklift = (Forklift) forkliftDropdown.getSelectedItem();
            if (selectedForklift == null) {
                isValid = false;
                errorMessage.append(bundle.getString("noForkliftSelected")).append("\n");
            }

            float avoidanceDistance = 0;
            try {
                avoidanceDistance = Float.parseFloat(avoidanceDistanceField.getText().replaceAll(",", "."));
                if (avoidanceDistance < 0) {
                    isValid = false;
                    errorMessage.append(bundle.getString("invalidAvoidanceDistance")).append("\n");
                }
            } catch (NumberFormatException ex) {
                isValid = false;
                errorMessage.append(bundle.getString("invalidAvoidanceDistance")).append("\n");
            }

            if (isValid) {
                List<Pallet> selectedPallets = new ArrayList<>();
                double totalWeight = 0;
                for (String palletId : Collections.list(selectedPalletsModel.elements())) {
                    Pallet pallet = palletMap.get(palletId);
                    if (pallet != null) {
                        selectedPallets.add(pallet);
                        totalWeight += pallet.getWeight();
                    }
                }

                if (selectedForklift.getMaxWeightCapacity() < totalWeight) {
                    JOptionPane.showMessageDialog(simulationFrame, bundle.getString("forkliftCapacityExceeded"), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                simulationManager = new SimulationManager(jmeScene, avoidanceDistance);
                simulationManager.startReceivingSimulation(selectedArea, selectedPallets, selectedForklift);
                simulationFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(simulationFrame, errorMessage.toString(), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(startSimulationButton, BorderLayout.SOUTH);

        return panel;
    }

    private void updateForkliftDropdown(JComboBox<Forklift> forkliftDropdown) {
        forkliftDropdown.removeAllItems();
        for (int i = 0; i < forkliftModel.size(); i++) {
            forkliftDropdown.addItem(forkliftModel.getElementAt(i));
        }
    }

    private void updateAvailablePalletsList() {
        availablePalletsModel.clear();
        palletMap.clear();
        for (Pallet pallet : undoManager.getPallets()) {
            availablePalletsModel.addElement(pallet.getId());
            palletMap.put(pallet.getId(), pallet);
        }
    }

    private void populateReceivingSendingAreas(JComboBox<Geometry> dropdown, Map<Geometry, Float> areas) {
        dropdown.addItem(null);
        for (Geometry geometry : areas.keySet()) {
            dropdown.addItem(geometry);
        }
    }
}