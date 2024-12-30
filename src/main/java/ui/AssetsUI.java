package ui;

import tech.CatalogueLoader;
import tech.CatalogueLoader.Folder;
import tech.CatalogueLoader.CatalogueItem;
import com.jme3.math.Vector3f;
import tech.ModelLoader;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AssetsUI {
    private JPanel assetsPanel;
    private CatalogueLoader.Catalogue catalogue;
    private Folder currentFolder;
    private final ModelLoader modelLoader;

    public AssetsUI(ModelLoader modelLoader) {
        this.modelLoader = modelLoader;
        assetsPanel = new JPanel(new BorderLayout());
        assetsPanel.setBorder(BorderFactory.createTitledBorder("Assets"));
    }

    public JPanel getAssetsPanel() {
        return assetsPanel;
    }

    public void loadCatalogue(CatalogueLoader.Catalogue catalogue) {
        this.catalogue = catalogue;
        displayFolders(catalogue.getFolders());
    }

    private void displayFolders(List<Folder> folders) {
        currentFolder = null;
        assetsPanel.removeAll();

        JPanel foldersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        for (Folder folder : folders) {
            JButton folderButton = createIconButton(
                    "Textures/Images/Icons/asset_folder.png", folder.getName()
            );
            folderButton.addActionListener(e -> displayFolderContents(folder));
            foldersPanel.add(folderButton);
        }

        JScrollPane scrollPane = new JScrollPane(foldersPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        assetsPanel.add(scrollPane, BorderLayout.CENTER);
        assetsPanel.revalidate();
        assetsPanel.repaint();
    }

    private void displayFolderContents(Folder folder) {
        currentFolder = folder;
        assetsPanel.removeAll();

        JPanel contentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JButton goBackButton = createIconButton("Textures/Images/Icons/go_back.png", "Go To Root");
        goBackButton.addActionListener(e -> displayFolders(catalogue.getFolders()));
        contentsPanel.add(goBackButton);

        // Subfolders
        for (Folder subfolder : folder.getSubFolders()) {
            JButton subfolderButton = createIconButton(
                    "Textures/Images/Icons/asset_folder.png", subfolder.getName()
            );
            subfolderButton.addActionListener(e -> displayFolderContents(subfolder));
            contentsPanel.add(subfolderButton);
        }

        // Items
        for (CatalogueItem item : folder.getItems()) {
            if (isValidCatalogueItem(item)) {
                JButton itemButton = createIconButton(item.getItemImage(), item.getName());
                itemButton.addActionListener(e -> loadItemIntoScene(item));
                contentsPanel.add(itemButton);
            } else {
                System.err.println("Skipping invalid item: " + item.getName());
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentsPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        assetsPanel.add(scrollPane, BorderLayout.CENTER);
        assetsPanel.revalidate();
        assetsPanel.repaint();
    }

    private boolean isValidCatalogueItem(CatalogueItem item) {
        return item.getName() != null && item.getItemImage() != null && item.getModelPath() != null;
    }

    private void loadItemIntoScene(CatalogueItem item) {
        String modelPath = item.getModelPath();
        if (modelPath != null && !modelPath.isEmpty()) {
            modelLoader.loadAndPlaceModel(modelPath, new Vector3f(0, 0, 0));
        } else {
            System.err.println("Model path is missing for item: " + item.getName());
        }
    }

    private JButton createIconButton(String iconPath, String text) {
        if (getClass().getClassLoader().getResource(iconPath) == null){
            return new JButton(text);
        }
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(iconPath));

        if (icon.getIconWidth() == -1) {
            System.err.println("Error: Image not found for " + text);
        }

        Image image = icon.getImage();

        int width = 50;
        int height = 50;

        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        icon = new ImageIcon(scaledImage);

        JButton button = new JButton(text);
        button.setIcon(icon);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setPreferredSize(new Dimension(100, 100));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);

        return button;
    }
}
