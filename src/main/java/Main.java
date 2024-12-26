import com.formdev.flatlaf.themes.FlatMacLightLaf;
import ui.ProjectsView;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ProjectsView projectsView = new ProjectsView();
            JPanel projectsPanel = projectsView.createProjectsPanel();

            JFrame frame = new JFrame("Projects View");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(projectsPanel);
            frame.setVisible(true);
        });
    }
}
