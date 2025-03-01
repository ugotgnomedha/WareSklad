import com.formdev.flatlaf.themes.FlatMacLightLaf;
import mdlaf.MaterialLookAndFeel;
import ui.ProjectsView;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main {

    public static void main(String[] args) {
        try {
            JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
            UIManager.setLookAndFeel(new MaterialLookAndFeel(new mdlaf.themes.MaterialLiteTheme()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        Locale systemLocale = Locale.getDefault();
//        Locale systemLocale = Locale.ENGLISH;
        ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", systemLocale);

        SwingUtilities.invokeLater(() -> {
            ProjectsView projectsView = new ProjectsView(bundle);
            JPanel projectsPanel = projectsView.createProjectsPanel();

            JFrame frame = new JFrame(bundle.getString("projects"));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(projectsPanel);
            projectsView.addMenuBar(frame);
            frame.setVisible(true);
        });
    }
}
