package ui;

import com.jme3.system.JmeCanvasContext;
import tech.CatalogueLoader;
import tech.WareSkladInit;

import javax.swing.*;
import java.awt.*;

public class PlannerUI {

    private WareSkladInit jmeScene;
    private PropertiesPanel propertiesPanel = new PropertiesPanel();

    public JFrame createMainFrame() {
        JFrame frame = new JFrame("Planner Application");
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

        JMenu projectMenu = new JMenu("Project");
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> System.out.println("Save clicked"));
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> System.exit(0));
        projectMenu.add(saveItem);
        projectMenu.add(closeItem);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoAction = new JMenuItem("Undo");
        undoAction.addActionListener(e -> jmeScene.undoManager.undo());
        editMenu.add(undoAction);
        JMenuItem redoAction = new JMenuItem("Redo");
        redoAction.addActionListener(e -> jmeScene.undoManager.redo());
        editMenu.add(redoAction);

        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.add(new JMenuItem("Setting 1"));
        settingsMenu.add(new JMenuItem("Setting 2"));

        menuBar.add(projectMenu);
        menuBar.add(editMenu);
        menuBar.add(settingsMenu);

        return menuBar;
    }

    private JPanel createPlannerPanel() {
        JPanel plannerPanel = new JPanel(new BorderLayout());
//        plannerPanel.setBorder(BorderFactory.createTitledBorder("Planner"));

        jmeScene = new WareSkladInit();

        jmeScene.createCanvas();
        JmeCanvasContext ctx = (JmeCanvasContext) jmeScene.getContext();
        ctx.setSystemListener(jmeScene);

        Canvas jmeCanvas = ctx.getCanvas();
        jmeCanvas.setPreferredSize(new Dimension(800, 600));
        plannerPanel.add(jmeCanvas, BorderLayout.CENTER);

        jmeScene.startCanvas();

        // Wait for JME initialization.
        try {
            jmeScene.waitForInitialization();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        jmeScene.setPropertiesPanel(propertiesPanel);

        JPanel jmePanel = new JPanel(new BorderLayout());
        jmePanel.add(jmeScene.getCanvas(), BorderLayout.CENTER);

        plannerPanel.add(jmePanel, BorderLayout.CENTER);
        return plannerPanel;
    }

    private JPanel createAssetsPanel() {
        JPanel assetsPanel = new JPanel(new BorderLayout());
        assetsPanel.setBorder(BorderFactory.createTitledBorder("Assets"));
        assetsPanel.setPreferredSize(new Dimension(300, 150));

        AssetsUI assetsUI = new AssetsUI(jmeScene.modelLoader);
        assetsPanel = assetsUI.getAssetsPanel();

        CatalogueLoader loader = new CatalogueLoader();
        CatalogueLoader.Catalogue catalogue = loader.loadCatalogue("catalogue_items.json");
        assetsUI.loadCatalogue(catalogue);

        return assetsPanel;
    }

    private JPanel createLayoutPanel() {
        JPanel layoutPanel = new JPanel();
        layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
        layoutPanel.setBorder(BorderFactory.createTitledBorder("Layout"));
        return layoutPanel;
    }
}
