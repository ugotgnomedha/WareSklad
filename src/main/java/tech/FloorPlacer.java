package tech;

import UndoRedo.FloorPlacementAction;
import UndoRedo.UndoManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;
import ui.Grid;

import java.util.*;

public class FloorPlacer {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final InputManager inputManager;
    private final Camera cam;
    private final UndoManager undoManager;
    private WareSkladInit jmeScene;

    private Vector3f startPoint = null;
    private List<Vector3f> floorPoints = new ArrayList<>();
    public List<Geometry> floorSegmentGeometries = new ArrayList<>();
    private Geometry previewFloorGeometry = null;

    private final float FLOOR_THICKNESS = 0.5f;

    private boolean floorMode = false;

    private Node measurementNode;

    private Random random = new Random();

    public FloorPlacer(Node rootNode, AssetManager assetManager, InputManager inputManager, Camera cam, UndoManager undoManager, WareSkladInit jmeScene) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.inputManager = inputManager;
        this.cam = cam;
        this.undoManager = undoManager;
        this.jmeScene = jmeScene;

        this.measurementNode = new Node("MeasurementNode");
        this.rootNode.attachChild(this.measurementNode);
        setupInput();
    }

    public boolean isFloorMode() {
        return floorMode;
    }

    public List<Vector3f> getFloorPoints() {
        return floorPoints;
    }

    public void setFloorMode(boolean floorMode) {
        this.floorMode = floorMode;
    }

    private void setupInput() {
        inputManager.addMapping("PlaceFloor", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("CancelPlacement", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(floorPlacementListener, "PlaceFloor");
        inputManager.addListener(cancelPlacementListener, "CancelPlacement");
    }

    private final ActionListener floorPlacementListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            if (!floorMode) {
                return;
            }

            Vector3f gridPoint = getMouseGridIntersection();
            if (gridPoint == null) return;

            gridPoint = snapToGrid(gridPoint);

            if (startPoint == null) {
                startPoint = gridPoint;
                floorPoints.add(gridPoint);
            } else {
                floorPoints.add(gridPoint);
                placeFloorSegment(startPoint, gridPoint);

                if (gridPoint.equals(floorPoints.get(0))) {
                    createCompleteFloor();
                } else {
                    startPoint = gridPoint;
                }
            }
        }
    };

    private final ActionListener cancelPlacementListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            resetFloorPlacement();
            floorMode = false;
        }
    };

    private Vector3f getMouseGridIntersection() {
        Vector2f cursorPosition = inputManager.getCursorPosition();
        Ray ray = new Ray(cam.getWorldCoordinates(cursorPosition, 0f),
                cam.getWorldCoordinates(cursorPosition, 1f).subtractLocal(
                        cam.getWorldCoordinates(cursorPosition, 0f)));

        Plane gridPlane = new Plane(Vector3f.UNIT_Y, Grid.GRID_Y_LEVEL);

        Vector3f intersection = new Vector3f();
        if (ray.intersectsWherePlane(gridPlane, intersection)) {
            return intersection;
        }

        return null;
    }

    private Vector3f snapToGrid(Vector3f point) {
        float snappedX = Math.round(point.x / Grid.GRID_SPACING) * Grid.GRID_SPACING;
        float snappedZ = Math.round(point.z / Grid.GRID_SPACING) * Grid.GRID_SPACING;
        return new Vector3f(snappedX, Grid.GRID_Y_LEVEL, snappedZ);
    }

    public void placeFloorSegment(Vector3f start, Vector3f end) {
        Vector3f direction = end.subtract(start);
        float length = direction.length();
        direction.normalizeLocal();

        float width = 1.0f;
        Box floorBox = new Box(length / 2, FLOOR_THICKNESS / 2, width / 2);
        Geometry floorGeometry = new Geometry("FloorSegment", floorBox);

        Material floorMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMaterial.setColor("Color", ColorRGBA.LightGray);
        floorGeometry.setMaterial(floorMaterial);

        Vector3f midpoint = start.add(end).multLocal(0.5f);
        floorGeometry.setLocalTranslation(midpoint.setY(Grid.GRID_Y_LEVEL));

        float angle = (float) Math.atan2(direction.z, direction.x);
        angle = -angle;

        floorGeometry.rotate(0, angle, 0);

        rootNode.attachChild(floorGeometry);
        floorSegmentGeometries.add(floorGeometry);

        undoManager.getFloorSegmentToFloorId().put(floorGeometry, -1);

        Vector3f[] segmentVertices = {
                start,
                new Vector3f(end.x, start.y, start.z),
                end,
                new Vector3f(start.x, start.y, end.z)
        };
        undoManager.getFloorSegmentVertices().put(floorGeometry, Arrays.asList(segmentVertices));

        float segmentDistance = showMeasurements(false);
        undoManager.getFloorSegmentDistances().put(floorGeometry, segmentDistance);
    }

    public void createCompleteFloor() {
        jmeScene.enqueue(() -> {
            if (floorPoints.size() < 3) {
                floorPoints.clear();
                startPoint = null;
                return;
            }

            Vector3f center = calculateCenter(floorPoints);

            List<Vector3f> adjustedPoints = new ArrayList<>();
            for (Vector3f point : floorPoints) {
                adjustedPoints.add(point.subtract(center));
            }

            adjustedPoints.remove(adjustedPoints.size() - 1);

            Vector3f[] vertices = adjustedPoints.toArray(new Vector3f[0]);
            int[] indices = triangulate(vertices);

            Mesh mesh = new Mesh();
            mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
            mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
            mesh.updateBound();

            Geometry floorGeometry = new Geometry("CompleteFloor", mesh);
            Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", ColorRGBA.Gray);
            floorGeometry.setMaterial(material);

            floorGeometry.setLocalTranslation(center.setY(Grid.GRID_Y_LEVEL));
            rootNode.attachChild(floorGeometry);

            undoManager.getCompleteFloorVertices().put(floorGeometry, Arrays.asList(vertices));

            int floorId = random.nextInt(90000) + 10000;
            undoManager.getFloorIdToSegments().put(floorId, new ArrayList<>());

            undoManager.getFloorSegmentToFloorId().put(floorGeometry, floorId);
            undoManager.getFloorIdToSegments().get(floorId).add(floorGeometry);

            for (Geometry segment : floorSegmentGeometries) {
                undoManager.getFloorSegmentToFloorId().put(segment, floorId);
                undoManager.getFloorIdToSegments().get(floorId).add(segment);
            }

            undoManager.getCompleteFloorCenters().put(floorId, center);

            Map<Geometry, Float> floorCompleteAreasCopy = new HashMap<>();
            Map<Geometry, Float> floorSegmentDistancesCopy = new HashMap<>();
            Map<Geometry, Integer> floorSegmentToFloorIdCopy = new HashMap<>();
            Map<Integer, List<Geometry>> floorIdToSegmentsCopy = new HashMap<>();
            Map<Geometry, List<Vector3f>> floorSegmentVerticesCopy = new HashMap<>();
            Map<Geometry, List<Vector3f>> completeFloorVerticesCopy = new HashMap<>();

            for (Geometry geometry : floorSegmentGeometries) {
                floorSegmentDistancesCopy.put(geometry, undoManager.getFloorSegmentDistances().get(geometry));
                floorSegmentToFloorIdCopy.put(geometry, undoManager.getFloorSegmentToFloorId().get(geometry));
                floorSegmentVerticesCopy.put(geometry, undoManager.getFloorSegmentVertices().get(geometry));
            }

            floorCompleteAreasCopy.put(floorGeometry, showMeasurements(true));
            completeFloorVerticesCopy.put(floorGeometry, Arrays.asList(vertices));
            floorIdToSegmentsCopy.put(floorId, new ArrayList<>(undoManager.getFloorIdToSegments().get(floorId)));

            List<Geometry> floorGeometriesCopy = new ArrayList<>(floorSegmentGeometries);
            floorGeometriesCopy.add(floorGeometry);

            undoManager.addAction(new FloorPlacementAction(
                    floorGeometriesCopy,
                    rootNode,
                    floorCompleteAreasCopy,
                    floorSegmentDistancesCopy,
                    Map.of(floorId, center),
                    floorSegmentToFloorIdCopy,
                    floorIdToSegmentsCopy,
                    floorSegmentVerticesCopy,
                    completeFloorVerticesCopy
            ));

            float floorArea = showMeasurements(true);
            undoManager.getFloorCompleteAreas().put(floorGeometry, floorArea);

            floorSegmentGeometries.clear();
            floorPoints.clear();
            startPoint = null;

            if (previewFloorGeometry != null) {
                rootNode.detachChild(previewFloorGeometry);
                previewFloorGeometry = null;
            }
        });
    }

    public Vector3f calculateCenter(List<Vector3f> points) {
        Vector3f center = new Vector3f(0, 0, 0);
        for (Vector3f point : points) {
            center.addLocal(point);
        }
        center.divideLocal(points.size());
        return snapToGrid(center);
    }

    public int[] triangulate(Vector3f[] vertices) {
        List<Integer> indices = new ArrayList<>();
        int n = vertices.length;

        if (n < 3) return new int[0];

        int[] V = new int[n];
        if (isClockwise(vertices)) {
            for (int i = 0; i < n; i++) V[i] = i;
        } else {
            for (int i = 0; i < n; i++) V[i] = (n - 1) - i;
        }

        int nv = n;
        int count = 2 * nv;
        for (int v = nv - 1; nv > 2;) {
            if ((count--) <= 0) break;

            int u = v;
            if (nv <= u) u = 0;
            v = u + 1;
            if (nv <= v) v = 0;
            int w = v + 1;
            if (nv <= w) w = 0;

            if (snip(vertices, u, v, w, nv, V)) {
                int a = V[u], b = V[v], c = V[w];
                indices.add(a);
                indices.add(b);
                indices.add(c);

                for (int s = v, t = v + 1; t < nv; s++, t++) V[s] = V[t];
                nv--;
                count = 2 * nv;
            }
        }

        int[] result = new int[indices.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = indices.get(i);
        }
        return result;
    }

    private boolean snip(Vector3f[] vertices, int u, int v, int w, int n, int[] V) {
        Vector3f A = vertices[V[u]];
        Vector3f B = vertices[V[v]];
        Vector3f C = vertices[V[w]];

        if (Vector3f.UNIT_Y.dot(B.subtract(A).cross(C.subtract(A))) <= 0) return false;

        for (int p = 0; p < n; p++) {
            if (p == u || p == v || p == w) continue;
            if (isPointInsideTriangle(A, B, C, vertices[V[p]])) return false;
        }

        return true;
    }

    private boolean isPointInsideTriangle(Vector3f A, Vector3f B, Vector3f C, Vector3f P) {
        Vector3f v0 = C.subtract(A);
        Vector3f v1 = B.subtract(A);
        Vector3f v2 = P.subtract(A);

        float dot00 = v0.dot(v0);
        float dot01 = v0.dot(v1);
        float dot02 = v0.dot(v2);
        float dot11 = v1.dot(v1);
        float dot12 = v1.dot(v2);

        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return (u >= 0) && (v >= 0) && (u + v < 1);
    }

    private boolean isClockwise(Vector3f[] vertices) {
        float sum = 0;
        for (int i = 0; i < vertices.length; i++) {
            Vector3f current = vertices[i];
            Vector3f next = vertices[(i + 1) % vertices.length];
            sum += (next.x - current.x) * (next.z + current.z);
        }
        return sum > 0;
    }

    private void resetFloorPlacement() {
        floorPoints.clear();
        startPoint = null;

        for (Geometry geom : floorSegmentGeometries) {
            rootNode.detachChild(geom);
        }
        floorSegmentGeometries.clear();

        if (previewFloorGeometry != null) {
            rootNode.detachChild(previewFloorGeometry);
            previewFloorGeometry = null;
        }
        clearMeasurements();
    }

    private void updatePreviewGeometry(Vector3f gridPoint) {
        if (startPoint == null) return;

        if (previewFloorGeometry != null) {
            rootNode.detachChild(previewFloorGeometry);
        }

        Vector3f direction = gridPoint.subtract(startPoint);
        float length = direction.length();
        direction.normalizeLocal();

        float width = 1.0f;
        Box floorBox = new Box(length / 2, FLOOR_THICKNESS / 2, width / 2);
        previewFloorGeometry = new Geometry("PreviewFloorSegment", floorBox);

        Material previewMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        previewMaterial.setColor("Color", ColorRGBA.Yellow);
        previewFloorGeometry.setMaterial(previewMaterial);

        Vector3f midpoint = startPoint.add(gridPoint).multLocal(0.5f);
        previewFloorGeometry.setLocalTranslation(midpoint.setY(Grid.GRID_Y_LEVEL));

        float angle = (float) Math.atan2(direction.z, direction.x);
        angle = -angle;

        previewFloorGeometry.rotate(0, angle, 0);
        rootNode.attachChild(previewFloorGeometry);
    }

    public void updatePreview() {
        if (floorMode) {
            Vector3f gridPoint = getMouseGridIntersection();
            if (gridPoint != null && startPoint != null) {
                gridPoint = snapToGrid(gridPoint);
                updatePreviewGeometry(gridPoint);
            }
        }
    }

    private float showMeasurements(boolean isFloorComplete) {
        clearMeasurements();

        if (isFloorComplete) {
            float area = calculateArea(floorPoints)/100;
            Vector3f center = calculateCenter(floorPoints);

            BitmapText measurementText = createBitmapText("Area: " + area + " m^2");
            measurementText.setLocalScale(0.5f);
            measurementText.setLocalTranslation(center.x, center.y + 1, center.z);
            measurementText.rotate((float) Math.toRadians(90), 0, (float) Math.toRadians(180));
            measurementNode.attachChild(measurementText);
            return area;
        } else if (floorPoints.size() >= 1) {
            Vector3f lastPoint = floorPoints.get(floorPoints.size() - 1);
            float distance = startPoint != null ? startPoint.distance(lastPoint) : 0;
            String formattedDistance = String.format(Locale.US, "%.2f", distance / 10);

            BitmapText measurementText = createBitmapText("Distance: " + formattedDistance + " m");
            measurementText.setLocalScale(0.5f);
            measurementText.setLocalTranslation(lastPoint.x, lastPoint.y + 1, lastPoint.z);
            measurementText.rotate((float) Math.toRadians(90), 0, (float) Math.toRadians(180));
            measurementNode.attachChild(measurementText);
            return Float.parseFloat(formattedDistance);
        }
        return 0;
    }

    public void clearMeasurements() {
        measurementNode.detachAllChildren();
    }

    private BitmapText createBitmapText(String text) {
        BitmapText bitmapText = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"), false);
        bitmapText.setText(text);
        bitmapText.setColor(ColorRGBA.White);
        return bitmapText;
    }

    private float calculateArea(List<Vector3f> points) {
        if (points.size() < 3) return 0;
        List<Vector3f> uniquePoints = new ArrayList<>(points);
        if (uniquePoints.get(0).equals(uniquePoints.get(uniquePoints.size() - 1))) {
            uniquePoints.remove(uniquePoints.size() - 1);
        }
        float area = 0;
        int n = uniquePoints.size();
        for (int i = 0; i < n; i++) {
            Vector3f current = uniquePoints.get(i);
            Vector3f next = uniquePoints.get((i + 1) % n);
            area += (current.x * next.z - next.x * current.z);
        }
        return Math.abs(area / 2.0f);
    }

    public float calculateTotalFloorArea() {
        float totalArea = 0.0f;
        for (Float area : undoManager.getFloorCompleteAreas().values()) {
            totalArea += area;
        }
        return totalArea;
    }
}
