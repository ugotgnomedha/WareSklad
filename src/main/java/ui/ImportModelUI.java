package ui;

import com.jme3.asset.AssetManager;
import tech.CatalogueLoader;
import tech.CatalogueLoader.Folder;
import tech.CatalogueLoader.CatalogueItem;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ImportModelUI extends JFrame {
    private final AssetsUI assetsUI;
    private final AssetManager assetManager;
    private final CatalogueLoader.Catalogue catalogue;
    private final String cataloguePath;
    private final ResourceBundle bundle;

    public ImportModelUI(AssetsUI assetsUI, AssetManager assetManager, CatalogueLoader.Catalogue catalogue, String cataloguePath, ResourceBundle bundle) {
        this.assetsUI = assetsUI;
        this.assetManager = assetManager;
        this.catalogue = catalogue;
        this.cataloguePath = cataloguePath;
        this.bundle = bundle;

        setTitle(bundle.getString("importModel.title"));
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel importPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        importPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel(bundle.getString("importModel.nameLabel"));
        JTextField nameField = new JTextField();

        JLabel folderLabel = new JLabel(bundle.getString("importModel.folderLabel"));
        JComboBox<String> folderComboBox = new JComboBox<>();
        for (Folder folder : catalogue.getFolders()) {
            if (!folder.getName().equals("Layout tools")) {
                folderComboBox.addItem(folder.getName());
            }
        }
        folderComboBox.addItem(bundle.getString("importModel.createFolder"));

        JLabel fileLabel = new JLabel(bundle.getString("importModel.fileLabel"));
        JButton fileButton = new JButton(bundle.getString("importModel.chooseFile"));
        JLabel filePathLabel = new JLabel(bundle.getString("importModel.noFileSelected"));

        JButton importButton = new JButton(bundle.getString("importModel.importButton"));
        JButton cancelButton = new JButton(bundle.getString("importModel.cancelButton"));

        fileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathLabel.setText(selectedFile.getAbsolutePath());
            }
        });

        folderComboBox.addActionListener(e -> {
            if (folderComboBox.getSelectedItem().equals(bundle.getString("importModel.createFolder"))) {
                String newFolderName = JOptionPane.showInputDialog(this, bundle.getString("importModel.enterFolderName"));
                if (newFolderName != null && !newFolderName.trim().isEmpty()) {
                    if (newFolderName.equals("Layout tools")) {
                        JOptionPane.showMessageDialog(this, bundle.getString("importModel.invalidFolderName"), bundle.getString("importModel.error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    File newFolderDir = new File("src/main/resources/Models/" + newFolderName);
                    if (!newFolderDir.exists()) {
                        newFolderDir.mkdirs();
                    }

                    Folder newFolder = new Folder();
                    newFolder.setName(newFolderName);
                    newFolder.setItems(new ArrayList<>());
                    catalogue.getFolders().add(newFolder);
                    folderComboBox.addItem(newFolderName);
                    folderComboBox.setSelectedItem(newFolderName);
                }
            }
        });

        CatalogueLoader.Catalogue[] catalogueHolder = new CatalogueLoader.Catalogue[]{catalogue};

        importButton.addActionListener(e -> {
            String modelName = nameField.getText();
            String folderName = (String) folderComboBox.getSelectedItem();
            String filePath = filePathLabel.getText();

            if (modelName.isEmpty() || folderName.isEmpty() || filePath.equals(bundle.getString("importModel.noFileSelected"))) {
                JOptionPane.showMessageDialog(this, bundle.getString("importModel.fillAllFields"), bundle.getString("importModel.error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            File sourceFile = new File(filePath);
            if (!isValidModelFile(sourceFile)) {
                JOptionPane.showMessageDialog(this, bundle.getString("importModel.invalidFileType"), bundle.getString("importModel.error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                File folderDir = new File("src/main/resources/Models/" + folderName);
                if (!folderDir.exists()) {
                    folderDir.mkdirs();
                }

                File destFile = new File(folderDir, normalizeFileName(sourceFile.getName()));
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                CatalogueItem newItem = new CatalogueItem();
                newItem.setName(modelName);
                newItem.setItemImage("Textures/Images/Items/default.png");
                newItem.setModelPath("Models/" + folderName + "/" + destFile.getName());

                Folder selectedFolder = catalogueHolder[0].getFolders().stream()
                        .filter(f -> f.getName().equals(folderName))
                        .findFirst()
                        .orElse(null);

                if (selectedFolder != null) {
                    selectedFolder.getItems().add(newItem);
                    CatalogueLoader.saveCatalogue(catalogueHolder[0], cataloguePath);
                    catalogueHolder[0] = CatalogueLoader.refreshCatalogue(catalogueHolder[0], cataloguePath, "src/main/resources/Models");
                    assetManager.clearCache();
                    assetsUI.loadCatalogue(catalogueHolder[0]);
                    JOptionPane.showMessageDialog(this, bundle.getString("importModel.success"), bundle.getString("importModel.successTitle"), JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, bundle.getString("importModel.failed") + ex.getMessage(), bundle.getString("importModel.error"), JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        importPanel.add(nameLabel);
        importPanel.add(nameField);
        importPanel.add(folderLabel);
        importPanel.add(folderComboBox);
        importPanel.add(fileLabel);
        importPanel.add(fileButton);
        importPanel.add(filePathLabel);
        importPanel.add(new JLabel());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(importButton);
        buttonPanel.add(cancelButton);

        JPanel catalogueViewPanel = new JPanel(new BorderLayout());
        catalogueViewPanel.setBorder(BorderFactory.createTitledBorder(bundle.getString("importModel.catalogueView")));

        JTree catalogueTree = createCatalogueTree();
        JScrollPane scrollPane = new JScrollPane(catalogueTree);
        catalogueViewPanel.add(scrollPane, BorderLayout.CENTER);

        JButton deleteButton = new JButton(bundle.getString("importModel.deleteButton"));
        deleteButton.addActionListener(e -> deleteSelectedItem(catalogueTree));
        catalogueViewPanel.add(deleteButton, BorderLayout.SOUTH);

        add(importPanel, BorderLayout.NORTH);
        add(catalogueViewPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                dispose();
            }
        });
    }

    private JTree createCatalogueTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(bundle.getString("importModel.catalogueRoot"));

        for (Folder folder : catalogue.getFolders()) {
            DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder.getName());
            for (CatalogueItem item : folder.getItems()) {
                folderNode.add(new DefaultMutableTreeNode(item.getName()));
            }
            root.add(folderNode);
        }

        return new JTree(root);
    }

    private void deleteSelectedItem(JTree catalogueTree) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) catalogueTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, bundle.getString("importModel.noItemSelected"), bundle.getString("importModel.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedName = selectedNode.getUserObject().toString();

        boolean isFolder = false;
        Folder targetFolder = null;
        CatalogueItem targetItem = null;

        for (Folder folder : catalogue.getFolders()) {
            if (folder.getName().equals(selectedName)) {
                isFolder = true;
                targetFolder = folder;
                break;
            }
            for (CatalogueItem item : folder.getItems()) {
                if (item.getName().equals(selectedName)) {
                    targetItem = item;
                    break;
                }
            }
            if (targetFolder != null || targetItem != null) {
                break;
            }
        }

        if (isFolder && targetFolder != null) {
            if (targetFolder.getName().equals("Layout tools")) {
                JOptionPane.showMessageDialog(this, bundle.getString("importModel.cannotModifyLayoutTools"), bundle.getString("importModel.error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            File folderDir = new File("src/main/resources/Models/" + targetFolder.getName());
            if (folderDir.exists()) {
                deleteDirectoryRecursively(folderDir);
            }

            catalogue.getFolders().remove(targetFolder);
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) catalogueTree.getModel().getRoot();
            rootNode.remove((DefaultMutableTreeNode) selectedNode.getParent());
        } else if (targetItem != null) {
            for (Folder folder : catalogue.getFolders()) {
                if (folder.getName().equals("Layout tools") && folder.getItems().contains(targetItem)) {
                    JOptionPane.showMessageDialog(this, bundle.getString("importModel.cannotModifyLayoutTools"), bundle.getString("importModel.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            File itemFile = new File("src/main/resources/" + targetItem.getModelPath());
            if (itemFile.exists()) {
                itemFile.delete();
            }

            for (Folder folder : catalogue.getFolders()) {
                if (folder.getItems().contains(targetItem)) {
                    folder.getItems().remove(targetItem);
                    break;
                }
            }

            ((DefaultMutableTreeNode) selectedNode.getParent()).remove(selectedNode);
        } else {
            JOptionPane.showMessageDialog(this, bundle.getString("importModel.itemNotFound"), bundle.getString("importModel.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        CatalogueLoader.saveCatalogue(catalogue, cataloguePath);
        assetsUI.loadCatalogue(catalogue);
        catalogueTree.updateUI();
        JOptionPane.showMessageDialog(this, bundle.getString("importModel.deleteSuccess"), bundle.getString("importModel.successTitle"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteDirectoryRecursively(File file) {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                deleteDirectoryRecursively(subFile);
            }
        }
        file.delete();
    }


    private boolean isValidModelFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".j3o") || fileName.endsWith(".obj") || fileName.endsWith(".fbx");
    }

    private String normalizeFileName(String fileName) {
        return Normalizer.normalize(fileName, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }
}