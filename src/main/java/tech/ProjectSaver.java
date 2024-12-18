package tech;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jme3.material.Material;
import com.jme3.material.MatParam;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import UndoRedo.UndoManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

            if (object instanceof Geometry && object.getName().contains("FloorSegment")) {
                saveFloorSegmentData((Geometry) object, objectJson);
            } else if (object instanceof Geometry && object.getName().equals("CompleteFloor")) {
                saveCompleteFloorData((Geometry) object, objectJson);
            }

            sceneArray.add(objectJson);
        }

        JsonObject rootJson = new JsonObject();
        rootJson.add("scene", sceneArray);

        writeToFile(filePath + ".json", rootJson);
    }

    private void saveFloorSegmentData(Geometry segment, JsonObject objectJson) {
        Integer floorId = floorPlacer.floorSegmentToFloorId.get(segment);
        List<Vector3f> vertices = floorPlacer.floorSegmentVertices.get(segment);

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

        Float segmentLength = floorPlacer.floorSegmentDistances.get(segment);
        if (segmentLength != null) {
            objectJson.addProperty("length", segmentLength);
        }
    }

    private void saveCompleteFloorData(Geometry floor, JsonObject objectJson) {
        List<Vector3f> vertices = floorPlacer.completeFloorVertices.get(floor);

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

        Float area = floorPlacer.floorCompleteAreas.get(floor);
        if (area != null) {
            objectJson.addProperty("area", area);
        }

        int floorId = floorPlacer.floorSegmentToFloorId.get(floor);
        objectJson.addProperty("floorId", floorId);

        Vector3f center = floorPlacer.completeFloorCenters.get(floorId);
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
