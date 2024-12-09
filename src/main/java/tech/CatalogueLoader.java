package tech;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CatalogueLoader {

    // Folders.
    public class Folder {
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

    // Items.
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
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON format: " + filePath);
        }

        Catalogue defaultCatalogue = getDefaultCatalogue();

        if (loadedCatalogue != null) {
            defaultCatalogue.getFolders().addAll(loadedCatalogue.getFolders());
        }

        return defaultCatalogue;
    }

    private Catalogue getDefaultCatalogue() {
        Catalogue defaultCatalogue = new Catalogue();

        Folder defaultFolder = new Folder();
        defaultFolder.setName("Layout");

        CatalogueItem defaultItem1 = new CatalogueItem();
        defaultItem1.setName("Floor-01");
        defaultItem1.setItemImage("Textures/Images/Icons/default_floor01.png");
        defaultItem1.setModelPath("Models/default_floor01.j3o");

        CatalogueItem defaultItem2 = new CatalogueItem();
        defaultItem2.setName("Wall-01");
        defaultItem2.setItemImage("Textures/Images/Icons/default_wall01.png");
        defaultItem2.setModelPath("Models/default_wall01.j3o");

        List<CatalogueItem> items = new ArrayList<>();
        items.add(defaultItem1);
        items.add(defaultItem2);
        defaultFolder.setItems(items);

        List<Folder> folders = new ArrayList<>();
        folders.add(defaultFolder);
        defaultCatalogue.setFolders(folders);

        return defaultCatalogue;
    }

    public static List<String> getDefaultModelPaths() {
        return Arrays.asList(
                "Models/default_floor01.j3o",
                "Models/default_wall01.j3o"
        );
    }
}
