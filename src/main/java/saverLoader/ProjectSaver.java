package saverLoader;

import UndoRedo.UndoManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import tech.FloorPlacer;
import tech.ModelLoader;
import tech.tags.RackSettings;
import tech.tags.Tag;
import ui.TagsUI;
import tech.parameters.Parameter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProjectSaver {
    private final UndoManager undoManager;
    private final Gson gson;
    private final ModelLoader modelLoader;
    private final FloorPlacer floorPlacer;

    public ProjectSaver(UndoManager undoManager, ModelLoader modelLoader, FloorPlacer floorPlacer) {
        this.undoManager = undoManager;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.modelLoader = modelLoader;
        this.floorPlacer = floorPlacer;
    }

    public void saveProject(String filePath) {
        List<Spatial> currentObjects = undoManager.getCurrentSceneObjects();
        JsonArray sceneArray = new JsonArray();

        for (Spatial object : currentObjects) {
            JsonObject objectJson = new JsonObject();
            objectJson.addProperty("name", object.getName());

            String modelPath = modelLoader.getModelPath(object);
            if (modelPath != null) {
                objectJson.addProperty("modelPath", modelPath);
            }

            Vector3f position = object.getLocalTranslation();
            float[] rotationArray = object.getLocalRotation().toAngles(null);
            Vector3f rotation = new Vector3f(rotationArray[0], rotationArray[1], rotationArray[2]);
            Vector3f scale = object.getLocalScale();

            JsonObject positionJson = new JsonObject();
            positionJson.addProperty("x", position.x);
            positionJson.addProperty("y", position.y);
            positionJson.addProperty("z", position.z);

            JsonObject rotationJson = new JsonObject();
            rotationJson.addProperty("x", rotation.x);
            rotationJson.addProperty("y", rotation.y);
            rotationJson.addProperty("z", rotation.z);

            JsonObject scaleJson = new JsonObject();
            scaleJson.addProperty("x", scale.x);
            scaleJson.addProperty("y", scale.y);
            scaleJson.addProperty("z", scale.z);

            objectJson.add("position", positionJson);
            objectJson.add("rotation", rotationJson);
            objectJson.add("scale", scaleJson);

            saveParameters(object, objectJson);

            if (object instanceof Geometry && object.getName().contains("FloorSegment")) {
                saveFloorSegmentData((Geometry) object, objectJson);
            } else if (object instanceof Geometry && object.getName().equals("CompleteFloor")) {
                saveCompleteFloorData((Geometry) object, objectJson);
            }

            sceneArray.add(objectJson);
        }

        JsonObject rootJson = new JsonObject();
        rootJson.add("scene", sceneArray);

        writeToFile(filePath, rootJson);
    }

    private void saveParameters(Spatial object, JsonObject objectJson) {
        Map<Parameter, String> parameters = undoManager.getParametersForSpatial(object);

        if (parameters != null && !parameters.isEmpty()) {
            JsonArray parametersArray = new JsonArray();

            for (Map.Entry<Parameter, String> entry : parameters.entrySet()) {
                Parameter parameter = entry.getKey();
                String userValue = entry.getValue();

                JsonObject parameterJson = new JsonObject();
                parameterJson.addProperty("name", parameter.getName());
                parameterJson.addProperty("type", parameter.getType().toString());
                parameterJson.addProperty("unit", parameter.getUnit());
                parameterJson.addProperty("value", userValue);

                parametersArray.add(parameterJson);
            }

            objectJson.add("parameters", parametersArray);
        }
    }

    private void addPropertyWithUnit(JsonObject jsonObject, String propertyName, double value, String unit) {
        JsonObject valueWithUnit = new JsonObject();
        valueWithUnit.addProperty("value", value);
        valueWithUnit.addProperty("unit", unit);
        jsonObject.add(propertyName, valueWithUnit);
    }

    private void saveFloorSegmentData(Geometry segment, JsonObject objectJson) {
        Integer floorId = undoManager.getFloorSegmentToFloorId().get(segment);
        List<Vector3f> vertices = undoManager.getFloorSegmentVertices().get(segment);

        if (floorId != null) {
            objectJson.addProperty("floorId", floorId);
        }

        if (vertices != null) {
            JsonArray verticesArray = new JsonArray();
            for (Vector3f vertex : vertices) {
                JsonObject vertexJson = new JsonObject();
                vertexJson.addProperty("x", vertex.x);
                vertexJson.addProperty("y", vertex.y);
                vertexJson.addProperty("z", vertex.z);
                verticesArray.add(vertexJson);
            }
            objectJson.add("vertices", verticesArray);
        }

        Float segmentLength = undoManager.getFloorSegmentDistances().get(segment);
        if (segmentLength != null) {
            objectJson.addProperty("length", segmentLength);
        }
    }

    private void saveCompleteFloorData(Geometry floor, JsonObject objectJson) {
        List<Vector3f> vertices = undoManager.getCompleteFloorVertices().get(floor);

        if (vertices != null) {
            JsonArray verticesArray = new JsonArray();
            for (Vector3f vertex : vertices) {
                JsonObject vertexJson = new JsonObject();
                vertexJson.addProperty("x", vertex.x);
                vertexJson.addProperty("y", vertex.y);
                vertexJson.addProperty("z", vertex.z);
                verticesArray.add(vertexJson);
            }
            objectJson.add("vertices", verticesArray);
        }

        Float area = undoManager.getFloorCompleteAreas().get(floor);
        if (area != null) {
            objectJson.addProperty("area", area);
        }

        int floorId = undoManager.getFloorSegmentToFloorId().get(floor);
        objectJson.addProperty("floorId", floorId);

        Vector3f center = undoManager.getCompleteFloorCenters().get(floorId);
        if (center != null) {
            JsonObject centerJson = new JsonObject();
            centerJson.addProperty("x", center.x);
            centerJson.addProperty("y", center.y);
            centerJson.addProperty("z", center.z);
            objectJson.add("center", centerJson);
        }
    }

    private void writeToFile(String fileName, JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            gson.toJson(jsonObject, fileWriter);
            System.out.println("Project saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving project: " + e.getMessage());
        }
    }
}