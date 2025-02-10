package ui;

import tech.CatalogueLoader;
import tech.CatalogueLoader.Folder;
import tech.CatalogueLoader.CatalogueItem;
import com.jme3.math.Vector3f;
import tech.ModelLoader;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AssetsUI {
    private JPanel assetsPanel;
    private JTextField searchField;
    private JPanel contentPanel;
    private CatalogueLoader.Catalogue catalogue;
    private final ModelLoader modelLoader;
    private final ResourceBundle bundle;

    public AssetsUI(ModelLoader modelLoader, ResourceBundle bundle) {
        this.modelLoader = modelLoader;
        this.bundle = bundle;
        assetsPanel = new JPanel(new BorderLayout());
        assetsPanel.setBorder(BorderFactory.createTitledBorder(bundle.getString("assets.title")));

        setupSearchBar();

        contentPanel = new JPanel(new BorderLayout());
        assetsPanel.add(contentPanel, BorderLayout.CENTER);
    }

    public JPanel getAssetsPanel() {
        return assetsPanel;
    }

    private void setupSearchBar() {
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 25));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterAssets(searchField.getText().trim());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterAssets(searchField.getText().trim());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterAssets(searchField.getText().trim());
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.add(new JLabel(bundle.getString("search.label")), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        assetsPanel.add(searchPanel, BorderLayout.NORTH);
    }

    public void loadCatalogue(CatalogueLoader.Catalogue catalogue) {
        this.catalogue = catalogue;
        displayFolders(catalogue.getFolders());
    }

    private void displayFolders(List<Folder> folders) {
        contentPanel.removeAll();

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

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void displayFolderContents(Folder folder) {
        contentPanel.removeAll();

        JPanel contentsPanel = new JPanel();
        contentsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JButton goBackButton = createIconButton("Textures/Images/Icons/go_back.png", bundle.getString("goBack"));
        goBackButton.addActionListener(e -> displayFolders(catalogue.getFolders()));
        contentsPanel.add(goBackButton);

        int itemsPerRow = calculateItemsPerRow();
        int count = 0;

        for (CatalogueItem item : folder.getItems()) {
            if (isValidCatalogueItem(item)) {
                JButton itemButton = createIconButton(item.getItemImage(), item.getName());
                itemButton.addActionListener(e -> loadItemIntoScene(item));
                contentsPanel.add(itemButton);
                count++;
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentsPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void filterAssets(String query) {
        contentPanel.removeAll();

        if (query.isEmpty()) {
            displayFolders(catalogue.getFolders());
            return;
        }

        JPanel resultsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        List<CatalogueItem> matchingItems = catalogue.getFolders().stream()
                .flatMap(folder -> folder.getItems().stream())
                .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        if (matchingItems.isEmpty()) {
            resultsPanel.add(new JLabel(bundle.getString("noMatchingAssets")));
        } else {
            for (CatalogueItem item : matchingItems) {
                JButton itemButton = createIconButton(item.getItemImage(), item.getName());
                itemButton.addActionListener(e -> loadItemIntoScene(item));
                resultsPanel.add(itemButton);
            }
        }

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
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
        if (getClass().getClassLoader().getResource(iconPath) == null) {
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

    private int calculateItemsPerRow() {
        int panelWidth = contentPanel.getWidth();
        int itemWidth = 110;
        return Math.max(panelWidth / itemWidth, 1);
    }
}
