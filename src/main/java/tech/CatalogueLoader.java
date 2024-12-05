package tech;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
    }

    public static class Catalogue {
        private List<Folder> folders;

        public List<Folder> getFolders() {
            return folders != null ? folders : new ArrayList<>();
        }
    }

    public Catalogue loadCatalogue(String filePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Catalogue.class);
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON format: " + filePath);
        } catch (NullPointerException e) {
            System.err.println("Null values encountered in JSON: " + filePath);
        }
        return new Catalogue();
    }
}
