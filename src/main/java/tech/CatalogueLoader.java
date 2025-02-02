package tech;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CatalogueLoader {

    public static class Folder {
        private String name;
        private List<CatalogueItem> items;
        private List<Folder> subfolders;

        public String getName() {
            return name;
        }

        public List<CatalogueItem> getItems() {
            return items != null ? items : new ArrayList<>();
        }

        public List<Folder> getSubFolders() {
            return subfolders != null ? subfolders : new ArrayList<>();
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setItems(List<CatalogueItem> items) {
            this.items = items;
        }

        public void setSubFolders(List<Folder> subfolders) {
            this.subfolders = subfolders;
        }
    }

    public static class CatalogueItem {
        private String name;
        private String itemImage;
        private String modelPath;

        public String getName() {
            return name;
        }

        public String getItemImage() {
            return itemImage;
        }

        public String getModelPath() {
            return modelPath;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setItemImage(String itemImage) {
            this.itemImage = itemImage;
        }

        public void setModelPath(String modelPath) {
            this.modelPath = modelPath;
        }
    }

    public static class Catalogue {
        private List<Folder> folders;

        public List<Folder> getFolders() {
            return folders != null ? folders : new ArrayList<>();
        }

        public void setFolders(List<Folder> folders) {
            this.folders = folders;
        }
    }

    public Catalogue loadCatalogue(String filePath) {
        Catalogue loadedCatalogue = null;
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            loadedCatalogue = gson.fromJson(reader, Catalogue.class);
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
        }

        Catalogue defaultCatalogue = getDefaultCatalogue();

        if (loadedCatalogue != null) {
            defaultCatalogue.getFolders().addAll(loadedCatalogue.getFolders());
        }

        return defaultCatalogue;
    }

    public static void saveCatalogue(Catalogue catalogue, String filePath) {
        Catalogue catalogueToSave = new Catalogue();
        List<Folder> foldersToSave = new ArrayList<>();

        for (Folder folder : catalogue.getFolders()) {
            if (!folder.getName().equals("Layout tools")) {
                foldersToSave.add(folder);
            }
        }

        catalogueToSave.setFolders(foldersToSave);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(catalogueToSave, writer);
            System.out.println("Catalogue saved successfully to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving catalogue to file: " + filePath);
            e.printStackTrace();
        }
    }

    public static Catalogue refreshCatalogue(Catalogue catalogue, String cataloguePath, String modelsDirectory) {
        File modelsDir = new File(modelsDirectory);

        if (modelsDir.exists() && modelsDir.isDirectory()) {
            Map<String, Folder> folderMap = catalogue.getFolders().stream()
                    .collect(Collectors.toMap(f -> normalizeName(f.getName()), f -> f, (existing, replacement) -> existing));

            for (File folder : modelsDir.listFiles(File::isDirectory)) {
                String normalizedFolderName = normalizeName(folder.getName());

                Folder catFolder = folderMap.get(normalizedFolderName);
                if (catFolder == null) {
                    catFolder = new Folder();
                    catFolder.setName(folder.getName());
                    catFolder.setItems(new ArrayList<>());
                    catalogue.getFolders().add(catFolder);
                    folderMap.put(normalizedFolderName, catFolder);
                }

                for (File modelFile : folder.listFiles()) {
                    String modelFileName = modelFile.getName();
                    String modelPath = "Models/" + folder.getName() + "/" + modelFileName;

                    boolean modelExists = catFolder.getItems().stream()
                            .anyMatch(item -> item.getModelPath().equals(modelPath));

                    if (!modelExists) {
                        String normalizedItemName = normalizeName(modelFileName.replace(".j3o", "").replace(".obj", ""));
                        CatalogueItem newItem = new CatalogueItem();
                        newItem.setName(normalizedItemName);
                        newItem.setItemImage("Textures/Images/Items/default.png");
                        newItem.setModelPath(modelPath);

                        catFolder.getItems().add(newItem);
                        break;
                    }
                }
            }
        }

        saveCatalogue(catalogue, cataloguePath);
        return catalogue;
    }

    private static String normalizeName(String name) {
        return name.toLowerCase().replaceAll("\\s+", "");
    }

    private Catalogue getDefaultCatalogue() {
        Catalogue defaultCatalogue = new Catalogue();

        Folder defaultFolder = new Folder();
        defaultFolder.setName("Layout tools");

        CatalogueItem defaultItem1 = new CatalogueItem();
        defaultItem1.setName("Floor-01");
        defaultItem1.setItemImage("Textures/Images/Icons/default_floor01.png");
        defaultItem1.setModelPath("Models/default_floor01.j3o");

        CatalogueItem defaultItem2 = new CatalogueItem();
        defaultItem2.setName("Wall-01");
        defaultItem2.setItemImage("Textures/Images/Icons/default_wall01.png");
        defaultItem2.setModelPath("Models/default_wall01.j3o");

        CatalogueItem defaultItem4 = new CatalogueItem();
        defaultItem4.setName("MeasureTool-01");
        defaultItem4.setItemImage("Textures/Images/Icons/default_measureTool01.png");
        defaultItem4.setModelPath("Models/default_measureTool01.j3o");

        CatalogueItem defaultItem5 = new CatalogueItem();
        defaultItem5.setName("Plain-Area");
        defaultItem5.setItemImage("Textures/Images/Icons/default_plainArea.png");
        defaultItem5.setModelPath("Models/default_plainArea.j3o");

        List<CatalogueItem> items = new ArrayList<>();
        items.add(defaultItem1);
        items.add(defaultItem2);
        items.add(defaultItem4);
        items.add(defaultItem5);
        defaultFolder.setItems(items);

        List<Folder> folders = new ArrayList<>();
        folders.add(defaultFolder);
        defaultCatalogue.setFolders(folders);

        return defaultCatalogue;
    }

    public static List<String> getDefaultModelPaths() {
        return Arrays.asList(
                "Models/default_floor01.j3o",
                "Models/default_wall01.j3o",
                "Models/default_measureTool01.j3o",
                "Models/default_plainArea.j3o"
        );
    }
}