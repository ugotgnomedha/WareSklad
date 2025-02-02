package ui;

import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeCanvasContext;
import saverLoader.OBJExporter;
import saverLoader.ProjectLoader;
import saverLoader.ProjectSaver;
import tech.*;
import tech.KPIs.KPIViewer;
import tech.layers.LayersManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

public class PlannerUI {

    private WareSkladInit jmeScene;
    private final PropertiesPanel propertiesPanel;
    private final LayersManager layersManager = new LayersManager();
    private final GridSettingUI gridSettingUI = new GridSettingUI();
    private ProjectSaver projectSaver;
    private ProjectLoader projectLoader;
    private KPIViewer kpiViewer;
    private TagsUI tagsUI;
    private final SimulationUI simulationUI = new SimulationUI();
    private AssetsUI assetsUI;
    private CatalogueLoader.Catalogue catalogue;
    private final ResourceBundle bundle;
    private RackPlacementUI rackPlacementUI;

    public PlannerUI(ResourceBundle bundle){
        this.bundle = bundle;
        propertiesPanel = new PropertiesPanel(bundle);
    }

    public JFrame createMainFrame() {
        JFrame frame = new JFrame(bundle.getString("plannerTitle"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(true);
        return frame;
    }

    public void initializeUI(JFrame frame) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JMenuBar menuBar = createMenuBar(frame);
        frame.setJMenuBar(menuBar);

        JPanel plannerPanel = createPlannerPanel();
        JPanel assetsPanel = createAssetsPanel();
        JPanel layoutPanel = createLayoutPanel();

        rackPlacementUI = new RackPlacementUI(bundle, jmeScene.undoManager);
        rackPlacementUI.setRackPlacementManager(jmeScene.rackPlacementManager);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(plannerPanel, BorderLayout.CENTER);
        centerPanel.add(assetsPanel, BorderLayout.SOUTH);
        centerPanel.add(propertiesPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(layoutPanel, BorderLayout.WEST);

        frame.setContentPane(mainPanel);
    }

    private JMenuBar createMenuBar(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();

        JMenu projectMenu = new JMenu(bundle.getString("project"));

        JMenuItem saveItem = new JMenuItem(bundle.getString("save"));
        saveItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(bundle.getString("saveProject"));
            fileChooser.setApproveButtonText(bundle.getString("save"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            String fileName = "";
            if (ProjectsView.CURRENT_PROJECT_NAME != null) {
                fileName = ProjectsView.CURRENT_PROJECT_NAME.replace(".json", "");
            }
            fileChooser.setSelectedFile(new File(fileName));
            int userChoice = fileChooser.showSaveDialog(frame);
            if (userChoice == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();

                String[] options = { ".json", ".obj" };
                int formatChoice = JOptionPane.showOptionDialog(
                        frame,
                        "Choose file format:",
                        "Save As",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (formatChoice == 0) {
                    // Save as .json
                    filePath = filePath.endsWith(".json") ? filePath : filePath + ".json";
                    projectSaver.saveProject(filePath);
                } else if (formatChoice == 1) {
                    // Save as .obj
                    filePath = filePath.endsWith(".obj") ? filePath : filePath + ".obj";
                    OBJExporter objExporter = new OBJExporter();
                    try {
                        java.util.List<Spatial> sceneObjects = jmeScene.undoManager.getCurrentSceneObjects();
                        objExporter.exportSceneToOBJ(sceneObjects, filePath);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error saving .obj file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                ProjectsView.CURRENT_PROJECT_PATH = filePath;
                String projectInfo = ProjectsView.CURRENT_PROJECT_NAME + " - " + ProjectsView.CURRENT_PROJECT_PATH;
                boolean projectExists = false;
                for (int i = 0; i < ProjectsView.projectListModel.size(); i++) {
                    if (ProjectsView.projectListModel.get(i).contains(ProjectsView.CURRENT_PROJECT_NAME)) {
                        ProjectsView.projectListModel.set(i, projectInfo);
                        projectExists = true;
                        break;
                    }
                }
                if (!projectExists) {
                    ProjectsView.projectListModel.addElement(projectInfo);
                }

                ProjectsView.saveRecentProjects();
            }
        });

        JMenuItem closeItem = new JMenuItem(bundle.getString("close"));
        closeItem.addActionListener(e -> {
            shutdownJme();
            frame.dispose();
            SwingUtilities.invokeLater(() -> {
                ProjectsView projectsView = new ProjectsView(bundle);
                JFrame projectsFrame = new JFrame(bundle.getString("projects"));
                projectsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                projectsFrame.setSize(600, 400);
                projectsFrame.add(projectsView.createProjectsPanel());
                projectsFrame.setVisible(true);
            });
        });

        projectMenu.add(saveItem);
        projectMenu.add(closeItem);

        JMenu editMenu = new JMenu(bundle.getString("edit"));
        JMenuItem undoAction = new JMenuItem(bundle.getString("undo"));
        undoAction.addActionListener(e -> jmeScene.undoManager.undo());
        editMenu.add(undoAction);
        JMenuItem redoAction = new JMenuItem(bundle.getString("redo"));
        redoAction.addActionListener(e -> jmeScene.undoManager.redo());
        editMenu.add(redoAction);

        LayersUI layersUI = new LayersUI(layersManager);
        JMenu layersMenu = layersUI.createLayersMenu(frame);
        editMenu.add(layersMenu);

        tagsUI = new TagsUI(bundle);
        JMenu tagsMenu = tagsUI.createTagsMenu(frame);
        editMenu.add(tagsMenu);

        JMenu viewMenu = new JMenu(bundle.getString("view"));
        JMenuItem twoDimSetting = new JMenuItem(bundle.getString("enter2DView"));
        twoDimSetting.addActionListener(e -> {
            jmeScene.setTwoDView();
        });
        viewMenu.add(twoDimSetting);
        JMenuItem threeDimSetting = new JMenuItem(bundle.getString("enter3DView"));
        threeDimSetting.addActionListener(e -> {
            jmeScene.setThreeDView();
        });
        viewMenu.add(threeDimSetting);
        JMenuItem simulationItem = new JMenuItem(bundle.getString("simulation"));
        simulationItem.addActionListener(e -> {
            simulationUI.openSimulationFrame();
        });
        viewMenu.add(simulationItem);
        JMenuItem racksPlacementItem = new JMenuItem(bundle.getString("racksPlacement"));
        racksPlacementItem.addActionListener(e -> {
            rackPlacementUI.openRacksPlacementFrame();
        });
        viewMenu.add(racksPlacementItem);

        JMenu kpiMenu = new JMenu(bundle.getString("kpiMenu"));
        JMenuItem viewKpisItem = new JMenuItem(bundle.getString("viewKpis"));
        viewKpisItem.addActionListener(e -> {
            kpiViewer.setVisible(true);
        });
        kpiViewer = new KPIViewer(bundle);

        JMenuItem editKpisItem = new JMenuItem(bundle.getString("editKpis"));
        editKpisItem.addActionListener(e -> System.out.println("Edit KPIs selected."));
        kpiMenu.add(viewKpisItem);
        kpiMenu.add(editKpisItem);

        JMenu settingsMenu = new JMenu(bundle.getString("settings"));
        JMenuItem gridSetting = new JMenuItem(bundle.getString("editGridSize"));
        gridSetting.addActionListener(e -> {
            gridSettingUI.showGridDialog(frame);
        });
        settingsMenu.add(gridSetting);
        JMenuItem importModelSetting = new JMenuItem(bundle.getString("importModel"));
        importModelSetting.addActionListener(e -> {
            ImportModelUI importModelUI = new ImportModelUI(assetsUI, jmeScene.getAssetManager(), catalogue, "catalogue_items.json", bundle);
            importModelUI.setVisible(true);
        });
        settingsMenu.add(importModelSetting);

        menuBar.add(projectMenu);
        menuBar.add(editMenu);
        menuBar.add(kpiMenu);
        menuBar.add(settingsMenu);
        menuBar.add(viewMenu);

        return menuBar;
    }

    private void openRacksPlacementFrame() {
        JFrame racksFrame = new JFrame(bundle.getString("racksPlacementTitle"));
        racksFrame.setSize(600, 400);
        racksFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        racksFrame.setLayout(new BorderLayout());

        String[] columnNames = {bundle.getString("rackDimensions"), bundle.getString("select")};
        Object[][] data = {
                {"200x180x110", Boolean.FALSE},
                {"250x200x120", Boolean.FALSE},
                {"300x250x150", Boolean.FALSE}
        };
        JTable racksTable = new JTable(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 1 ? Boolean.class : String.class;
            }
        };
        JScrollPane scrollPane = new JScrollPane(racksTable);
        racksFrame.add(scrollPane, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout());
        JComboBox<String> floorSelector = new JComboBox<>();
        floorSelector.addItem("---");

        Map<Geometry, Float> floorCompleteAreas = jmeScene.undoManager.getFloorCompleteAreas();
        floorCompleteAreas.keySet().forEach(geometry -> floorSelector.addItem(geometry.getName()));

        controlsPanel.add(new JLabel(bundle.getString("selectFloor")));
        controlsPanel.add(floorSelector);

        JButton runButton = new JButton(bundle.getString("generatePlacement"));
        runButton.addActionListener(e -> {
            String selectedFloor = (String) floorSelector.getSelectedItem();
            if (!"---".equals(selectedFloor)) {
                System.out.println("Selected floor for rack placement: " + selectedFloor);
            } else {
                System.out.println("No floor selected.");
            }
        });
        controlsPanel.add(runButton);

        racksFrame.add(controlsPanel, BorderLayout.SOUTH);
        racksFrame.setVisible(true);
    }

    private JPanel createPlannerPanel() {
        JPanel plannerPanel = new JPanel(new BorderLayout());
//        plannerPanel.setBorder(BorderFactory.createTitledBorder("Planner"));

        jmeScene = new WareSkladInit();

        jmeScene.createCanvas();
        JmeCanvasContext ctx = (JmeCanvasContext) jmeScene.getContext();
        ctx.setSystemListener(jmeScene);
        jmeScene.setPauseOnLostFocus(false);

        jmeScene.startCanvas();

        // Wait for JME initialization.
        try {
            jmeScene.waitForInitialization();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        jmeScene.setPropertiesPanel(propertiesPanel, layersManager, bundle);

        plannerPanel.add(jmeScene.getCanvas());
        propertiesPanel.setObjectControls(jmeScene.objectControls);
        propertiesPanel.setMeasureTool(jmeScene.measureTool);
        propertiesPanel.setDeleteObject(jmeScene.deleteObject);
        gridSettingUI.setWareSkladInit(this.jmeScene);
        projectSaver = new ProjectSaver(jmeScene.undoManager, jmeScene.modelLoader, jmeScene.floorPlacer);
        projectLoader = new ProjectLoader(jmeScene.undoManager, jmeScene.getAssetManager(), jmeScene);
        tagsUI.setUndoManager(jmeScene.undoManager);
        tagsUI.setPropertiesPanel(propertiesPanel);
        simulationUI.setupSimulation(jmeScene.undoManager, bundle, jmeScene);
        kpiViewer.setJmeScene(jmeScene);
        return plannerPanel;
    }

    private JPanel createAssetsPanel() {
        JPanel assetsPanel = new JPanel(new BorderLayout());
        assetsPanel.setBorder(BorderFactory.createTitledBorder("Assets"));
        assetsPanel.setPreferredSize(new Dimension(300, 150));

        this.assetsUI = new AssetsUI(jmeScene.modelLoader);
        assetsPanel = assetsUI.getAssetsPanel();

        CatalogueLoader loader = new CatalogueLoader();
        catalogue = loader.loadCatalogue("catalogue_items.json");
        assetsUI.loadCatalogue(catalogue);

        return assetsPanel;
    }

    public void loadProject(String absolutePath) {
        projectLoader.loadProject(absolutePath);
    }

    public void shutdownJme() {
        if (jmeScene != null) {
            jmeScene.stopCanvas();

            jmeScene.cleanup();
        }
    }

    private JPanel createLayoutPanel() {
        return new HierarchyUI(bundle, jmeScene, jmeScene.undoManager);
    }
}
