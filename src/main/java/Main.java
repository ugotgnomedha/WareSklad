import ui.PlannerUI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            PlannerUI plannerUI = new PlannerUI();
            JFrame frame = plannerUI.createMainFrame();
            plannerUI.initializeUI(frame);
            frame.setVisible(true);
        });
    }
}
