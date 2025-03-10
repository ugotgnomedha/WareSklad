package tech;

public enum RayExcludedObjects {
    OUTLINE("Outline"),
    HIGHLIGHT("Highlight"),
    FLOORSEGMENT("FloorSegment"),
    COMPLETEFLOOR("CompleteFloor"),
    PLAINAREA("PlainArea"),
    PREVIEWPLAINAREA("PreviewPlainArea");

    private final String objectName;

    RayExcludedObjects(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }
}
