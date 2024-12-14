package ui;

import tech.WareSkladInit;

import javax.swing.*;
import java.awt.*;

public class GridSettingUI {
    private WareSkladInit jmeScene;

    public void setWareSkladInit(WareSkladInit wareSkladInit){
        this.jmeScene = wareSkladInit;
    }

    public void showGridDialog(JFrame parentFrame) {
        JDialog gridDialog = new JDialog(parentFrame, "Edit Grid Size", true);
        gridDialog.setSize(300, 200);
        gridDialog.setLocationRelativeTo(parentFrame);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel xLabel = new JLabel("Grid X Size:");
        JTextField xSizeField = new JTextField(String.valueOf(Grid.GRID_WIDTH));
        JLabel yLabel = new JLabel("Grid Y Size:");
        JTextField ySizeField = new JTextField(String.valueOf(Grid.GRID_LENGTH));

        panel.add(xLabel);
        panel.add(xSizeField);
        panel.add(yLabel);
        panel.add(ySizeField);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            try {
                int xSize = Integer.parseInt(xSizeField.getText());
                int ySize = Integer.parseInt(ySizeField.getText());

                if (xSize > 1 && ySize > 1) {
                    Grid.GRID_WIDTH = xSize;
                    Grid.GRID_LENGTH = ySize;

                    updateGridAndLines();
                    gridDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(gridDialog, "Grid size must be greater than 1.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(gridDialog, "Please enter valid numbers.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> gridDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gridDialog.add(panel, BorderLayout.CENTER);
        gridDialog.add(buttonPanel, BorderLayout.SOUTH);

        gridDialog.setVisible(true);
    }

    private void updateGridAndLines() {
        if (jmeScene != null) {
            jmeScene.recreateGridAndLines();
        }
    }
}
