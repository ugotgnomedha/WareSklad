package tech.KPIs;

import tech.WareSkladInit;

import java.util.ResourceBundle;

public class StorageSpaceUtilisation {
    private WareSkladInit jmeScene;
    private float totalFloorArea;
    private float totalModelsSpaceUsed;
    private ResourceBundle bundle;

    public StorageSpaceUtilisation(WareSkladInit jmeScene, ResourceBundle bundle) {
        this.jmeScene = jmeScene;
        this.totalModelsSpaceUsed = jmeScene.getModelUsedSpace();
        this.totalFloorArea = jmeScene.getTotalFloorArea();
        this.bundle = bundle;
    }

    public String getTitle() {
        return bundle.getString("titleStorageSpaceUtilisation");
    }

    public String getDescription() {
        return bundle.getString("descriptionStorageSpaceUtilisation");
    }

    public String getResults() {
        float utilizationPercentage = 0.0f;
        if (totalFloorArea > 0) {
            utilizationPercentage = (totalModelsSpaceUsed / totalFloorArea) * 100;
        }

        return bundle.getString("storageSpaceAnalysisResults") + "\n"
                + bundle.getString("totalStorageSpace") + totalFloorArea + " " + bundle.getString("squareMeters") + "\n"
                + bundle.getString("usedStorageSpace") + String.format("%.2f", totalModelsSpaceUsed) + " " + bundle.getString("squareMeters") + "\n"
                + bundle.getString("utilization") + String.format("%.2f", utilizationPercentage) + "%";
    }
}
