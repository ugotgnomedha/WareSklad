package saverLoader;

import UndoRedo.UndoManager;
import com.google.gson.*;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;
import tech.FloorPlacer;
import tech.WareSkladInit;
import tech.parameters.Parameter;
import tech.parameters.ParameterType;
import ui.Grid;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectLoader {

    private final UndoManager undoManager;
    private final Gson gson;
    private final AssetManager assetManager;
    private final Node rootNode;
    private final WareSkladInit jmeScene;
    private final FloorPlacer floorPlacer;

    public ProjectLoader(UndoManager undoManager, AssetManager assetManager, WareSkladInit jmeScene) {
        this.undoManager = undoManager;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.assetManager = assetManager;
        this.rootNode = jmeScene.getRootNode();
        this.jmeScene = jmeScene;
        this.floorPlacer = jmeScene.floorPlacer;
    }

    public void loadProject(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            clearCurrentScene();
            JsonObject rootJson = gson.fromJson(reader, JsonObject.class);
            JsonArray sceneArray = rootJson.getAsJsonArray("scene");
            List<Spatial> loadedObjects = new ArrayList<>();

            jmeScene.enqueue(() -> {
                for (int i = 0; i < sceneArray.size(); i++) {
                    JsonObject objectJson = sceneArray.get(i).getAsJsonObject();
                    Spatial spatial = loadSpatial(objectJson);
                    if (spatial != null) {
                        rootNode.attachChild(spatial);
                        spatial.updateModelBound();
                        spatial.updateGeometricState();
                        loadedObjects.add(spatial);

                        // Load parameters for the spatial
                        loadParametersForSpatial(spatial, objectJson);
                    }
                }

                undoManager.setSceneObjects(loadedObjects);
            });

        } catch (IOException e) {
            System.err.println("Error loading project: " + e.getMessage());
        }
    }

    private void clearCurrentScene() {
        List<Spatial> currentObjects = undoManager.getCurrentSceneObjects();
        if (currentObjects != null) {
            for (Spatial spatial : currentObjects) {
                if (spatial.getParent() != null) {
                    spatial.removeFromParent();
                }
            }
            currentObjects.clear();
        }
        System.out.println("Scene cleared.");
    }

    public Spatial loadSpatial(JsonObject objectJson) {
        String name = objectJson.get("name").getAsString();
        String modelPath = objectJson.has("modelPath") ? objectJson.get("modelPath").getAsString() : null;
        Vector3f position = new Vector3f(
                objectJson.getAsJsonObject("position").get("x").getAsFloat(),
                objectJson.getAsJsonObject("position").get("y").getAsFloat(),
                objectJson.getAsJsonObject("position").get("z").getAsFloat()
        );
        Vector3f rotation = new Vector3f(
                objectJson.getAsJsonObject("rotation").get("x").getAsFloat(),
                objectJson.getAsJsonObject("rotation").get("y").getAsFloat(),
                objectJson.getAsJsonObject("rotation").get("z").getAsFloat()
        );
        Vector3f scale = new Vector3f(
                objectJson.getAsJsonObject("scale").get("x").getAsFloat(),
                objectJson.getAsJsonObject("scale").get("y").getAsFloat(),
                objectJson.getAsJsonObject("scale").get("z").getAsFloat()
        );

        Spatial spatial = null;
        if (name.startsWith("FloorSegment")) {
            spatial = createFloorSegment(objectJson);
        } else if (name.equals("CompleteFloor")) {
            spatial = createCompleteFloor(objectJson);
        } else if (modelPath != null && !modelPath.isEmpty()) {
            spatial = assetManager.loadModel(modelPath);
        }

        if (spatial == null) {
            spatial = new Geometry(name, new Box(1, 1, 1));
            Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", ColorRGBA.Blue);
            spatial.setMaterial(material);
        }

        spatial.setName(name);
        spatial.setLocalTranslation(position);
        Quaternion quatRotation = new Quaternion();
        quatRotation.fromAngles(rotation.x, rotation.y, rotation.z);
        spatial.setLocalRotation(quatRotation);
        spatial.setLocalScale(scale);

        return spatial;
    }

    private void loadParametersForSpatial(Spatial spatial, JsonObject objectJson) {
        if (objectJson.has("parameters")) {
            JsonArray parametersArray = objectJson.getAsJsonArray("parameters");
            Map<Parameter, String> parametersMap = undoManager.getOrCreateParametersForSpatial(spatial);

            for (JsonElement element : parametersArray) {
                JsonObject parameterJson = element.getAsJsonObject();
                String name = parameterJson.get("name").getAsString();
                ParameterType type = ParameterType.valueOf(parameterJson.get("type").getAsString().toUpperCase());
                String unit = parameterJson.get("unit").getAsString();
                String value = parameterJson.get("value").getAsString();

                Parameter parameter = new Parameter(name, type, unit, value);
                parametersMap.put(parameter, value);
            }
        }
    }

    private Geometry createFloorSegment(JsonObject objectJson) {
        JsonArray verticesArray = objectJson.getAsJsonArray("vertices");
        List<Vector3f> vertices = new ArrayList<>();
        for (JsonElement element : verticesArray) {
            JsonObject vertexObj = element.getAsJsonObject();
            Vector3f vertex = new Vector3f(
                    vertexObj.get("x").getAsFloat(),
                    vertexObj.get("y").getAsFloat(),
                    vertexObj.get("z").getAsFloat()
            );
            vertices.add(vertex);
        }

        float length = objectJson.get("length").getAsFloat();
        Geometry lineGeometry = new Geometry("FloorSegment", new Box(length * Grid.GRID_SPACING / 2, 0.25f, 0.5f));

        Material lineMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", ColorRGBA.LightGray);
        lineGeometry.setMaterial(lineMaterial);

        Vector3f start = vertices.get(0);
        lineGeometry.setLocalTranslation(start);

        int floorId = objectJson.get("floorId").getAsInt();
        undoManager.getFloorSegmentToFloorId().put(lineGeometry, floorId);
        undoManager.getFloorIdToSegments().computeIfAbsent(floorId, k -> new ArrayList<>()).add(lineGeometry);

        undoManager.getFloorSegmentDistances().put(lineGeometry, length);

        undoManager.getFloorSegmentVertices().put(lineGeometry, vertices);

        rootNode.attachChild(lineGeometry);

        return lineGeometry;
    }

    private Geometry createCompleteFloor(JsonObject objectJson) {
        JsonArray verticesArray = objectJson.getAsJsonArray("vertices");
        List<Vector3f> floorVertices = new ArrayList<>();
        for (JsonElement element : verticesArray) {
            JsonObject vertexObj = element.getAsJsonObject();
            Vector3f vertex = new Vector3f(
                    vertexObj.get("x").getAsFloat(),
                    vertexObj.get("y").getAsFloat(),
                    vertexObj.get("z").getAsFloat()
            );
            floorVertices.add(vertex);
        }

        if (floorVertices.size() < 3) {
            return null;
        }

        JsonObject centerJson = objectJson.getAsJsonObject("center");
        Vector3f center = new Vector3f(
                centerJson.get("x").getAsFloat(),
                centerJson.get("y").getAsFloat(),
                centerJson.get("z").getAsFloat()
        );

        Vector3f[] vertices = floorVertices.toArray(new Vector3f[0]);
        int[] indices = floorPlacer.triangulate(vertices);

        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
        mesh.updateBound();

        Geometry floorGeometry = new Geometry("CompleteFloor", mesh);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Gray);
        floorGeometry.setMaterial(material);

        floorGeometry.setLocalTranslation(center.setY(Grid.GRID_Y_LEVEL));

        float area = objectJson.get("area").getAsFloat();
        undoManager.getFloorCompleteAreas().put(floorGeometry, area);

        int floorId = objectJson.get("floorId").getAsInt();
        undoManager.getFloorSegmentToFloorId().put(floorGeometry, floorId);
        undoManager.getFloorIdToSegments().computeIfAbsent(floorId, k -> new ArrayList<>()).add(floorGeometry);
        undoManager.getCompleteFloorVertices().put(floorGeometry, floorVertices);
        undoManager.getCompleteFloorCenters().put(floorId, center);

        rootNode.attachChild(floorGeometry);

        return floorGeometry;
    }
}