package ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProjectsView {

    private static final String RECENT_PROJECTS_FILE = "recent_projects.json";
    public static DefaultListModel<String> projectListModel;
    private JList<String> projectList;
    private static Gson gson;
    private ResourceBundle bundle;

    public static String CURRENT_PROJECT_NAME;
    public static String CURRENT_PROJECT_PATH;

    public ProjectsView(ResourceBundle bundle) {
        this.bundle = bundle;
        gson = new Gson();
        projectListModel = new DefaultListModel<>();
        projectList = new JList<>(projectListModel);
    }

    public JPanel createProjectsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(bundle.getString("recentProjects"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(projectList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());

        JButton createNewButton = new JButton(bundle.getString("createNewProject"));
        createNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewProject();
            }
        });

        JButton openSelectedButton = new JButton(bundle.getString("openSelected"));
        openSelectedButton.setEnabled(false);
        openSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSelectedProject();
            }
        });

        JButton findProjectButton = new JButton(bundle.getString("findProject"));
        findProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findProjectFile();
            }
        });

        projectList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                openSelectedButton.setEnabled(projectList.getSelectedValue() != null);
            }
        });

        JButton deleteButton = new JButton(bundle.getString("deleteProject"));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedProject();
            }
        });

        buttonsPanel.add(createNewButton);
        buttonsPanel.add(openSelectedButton);
        buttonsPanel.add(findProjectButton);
        buttonsPanel.add(deleteButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        loadRecentProjects();

        return panel;
    }

    private void loadRecentProjects() {
        try {
            File file = new File(RECENT_PROJECTS_FILE);
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                Type projectListType = new TypeToken<List<String>>(){}.getType();
                List<String> recentProjects = gson.fromJson(fileReader, projectListType);
                fileReader.close();

                if (recentProjects != null) {
                    for (String project : recentProjects) {
                        projectListModel.addElement(project);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openSelectedProject() {
        String selectedProject = projectList.getSelectedValue();
        if (selectedProject != null) {
            CURRENT_PROJECT_NAME = selectedProject;
            CURRENT_PROJECT_PATH = getProjectPathFromJson(selectedProject);

            if (CURRENT_PROJECT_PATH != null && CURRENT_PROJECT_PATH.endsWith(".json")) {
                File projectFile = new File(CURRENT_PROJECT_PATH);
                if (projectFile.exists()) {
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(projectList);
                    parentFrame.dispose();

                    openSavePlannerUI();
                } else {
                    JOptionPane.showMessageDialog(null, bundle.getString("invalidProjectFile"));
                }
            } else {
                JOptionPane.showMessageDialog(null, bundle.getString("invalidExtension"));
            }
        } else {
            JOptionPane.showMessageDialog(null, bundle.getString("selectProject"));
        }
    }

    private void findProjectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(bundle.getString("findProjectTitle"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(bundle.getString("jsonFiles"), "json"));
        int userChoice = fileChooser.showOpenDialog(null);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.exists() && selectedFile.getName().endsWith(".json")) {
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(projectList);
                parentFrame.dispose();

                CURRENT_PROJECT_PATH = selectedFile.getAbsolutePath();
                PlannerUI plannerUI = new PlannerUI(bundle);
                JFrame frame = plannerUI.createMainFrame();
                plannerUI.initializeUI(frame);
                frame.setVisible(true);
                plannerUI.loadProject(CURRENT_PROJECT_PATH);
            } else {
                JOptionPane.showMessageDialog(null, bundle.getString("invalidFile"));
            }
        }
    }

    private void deleteSelectedProject() {
        String selectedProject = projectList.getSelectedValue();
        if (selectedProject != null) {
            projectListModel.removeElement(selectedProject);
            saveRecentProjects();
        } else {
            JOptionPane.showMessageDialog(null, bundle.getString("selectDeleteProject"));
        }
    }

    public static void saveRecentProjects() {
        try {
            List<String> projects = new ArrayList<>();
            for (int i = 0; i < projectListModel.size(); i++) {
                String projectInfo = projectListModel.get(i);
                if (!projectInfo.endsWith(".json")) {
                    projectInfo += ".json";
                }
                projects.add(projectInfo);
            }

            FileWriter fileWriter = new FileWriter(RECENT_PROJECTS_FILE);
            gson.toJson(projects, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNewProject() {
        CURRENT_PROJECT_NAME = JOptionPane.showInputDialog(null, bundle.getString("enterProjectName"));

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(projectList);
        parentFrame.dispose();

        openPlannerUI();
    }

    private void openPlannerUI() {
        PlannerUI plannerUI = new PlannerUI(bundle);
        JFrame frame = plannerUI.createMainFrame();
        plannerUI.initializeUI(frame);
        frame.setVisible(true);
    }

    private void openSavePlannerUI() {
        PlannerUI plannerUI = new PlannerUI(bundle);
        JFrame frame = plannerUI.createMainFrame();
        plannerUI.initializeUI(frame);
        frame.setVisible(true);
        plannerUI.loadProject(CURRENT_PROJECT_PATH);
    }

    private String getProjectPathFromJson(String projectName) {
        for (int i = 0; i < projectListModel.size(); i++) {
            String projectInfo = projectListModel.get(i);
            if (projectInfo.startsWith(projectName)) {
                return projectInfo.split(" - ")[1];
            }
        }
        return null;
    }
}
