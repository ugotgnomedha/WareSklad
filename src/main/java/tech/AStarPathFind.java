package tech;

import UndoRedo.UndoManager;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Line;
import ui.Grid;

import java.util.*;

public class AStarPathFind {

    private static final int STRAIGHT_COST = 10;
    private static final int DIAGONAL_COST = 14;
    private final Node rootNode;
    private final AssetManager assetManager;
    private final WareSkladInit jmeScene;
    private final List<Geometry> drawnLines = new ArrayList<>();
    private final float avoidanceDistance;

    public AStarPathFind(WareSkladInit jmeScene, float avoidanceDistance) {
        this.rootNode = jmeScene.getRootNode();
        this.assetManager = jmeScene.getAssetManager();
        this.jmeScene = jmeScene;
        this.avoidanceDistance = avoidanceDistance;
    }

    private static class PathNode {
        int x, z, gCost, hCost, fCost;
        PathNode parent;

        public PathNode(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public void calculateCosts(PathNode end, int newGCost) {
            this.gCost = newGCost;
            this.hCost = Math.abs(x - end.x) + Math.abs(z - end.z) * STRAIGHT_COST;
            this.fCost = gCost + hCost;
        }
    }

    private Set<Vector3f> getObstacles(Geometry startObject, Geometry endObject) {
        Set<Vector3f> obstacles = new HashSet<>();

        UndoManager undoManager = jmeScene.undoManager;
        List<Spatial> sceneObjects = undoManager.getCurrentSceneObjects();
        Map<Geometry, Float> floorCompleteAreas = undoManager.getFloorCompleteAreas();
        Map<Geometry, Float> floorSegmentDistances = undoManager.getFloorSegmentDistances();

        for (Spatial object : sceneObjects) {
            if (!isFloorRelated(object, floorCompleteAreas, floorSegmentDistances) && object != startObject && object != endObject) {
                BoundingBox boundingBox = (BoundingBox) object.getWorldBound();
                Vector3f min = boundingBox.getMin(new Vector3f());
                Vector3f max = boundingBox.getMax(new Vector3f());

                float extendedMinX = min.x - avoidanceDistance;
                float extendedMinZ = min.z - avoidanceDistance;
                float extendedMaxX = max.x + avoidanceDistance;
                float extendedMaxZ = max.z + avoidanceDistance;

                for (float x = extendedMinX; x <= extendedMaxX; x += Grid.GRID_SPACING) {
                    for (float z = extendedMinZ; z <= extendedMaxZ; z += Grid.GRID_SPACING) {
                        Vector3f position = new Vector3f(x, min.y, z);
                        obstacles.add(position);
                    }
                }
            }
        }

        return obstacles;
    }

    private void updateGridWithObstacles(PathNode[][] grid, Set<Vector3f> obstacles, int gridSpacing, Vector3f start, Vector3f end) {
        int gridLength = Grid.GRID_LENGTH * 2;
        int gridWidth = Grid.GRID_WIDTH * 2;

        float originX = gridLength * gridSpacing / 2f;
        float originZ = gridWidth * gridSpacing / 2f;

        int startX = (int) ((start.x + originX) / gridSpacing);
        int startZ = (int) ((start.z + originZ) / gridSpacing);
        int endX = (int) ((end.x + originX) / gridSpacing);
        int endZ = (int) ((end.z + originZ) / gridSpacing);

        for (Vector3f obstacle : obstacles) {
            int x = (int) ((obstacle.x + originX) / gridSpacing);
            int z = (int) ((obstacle.z + originZ) / gridSpacing);

            if ((x == startX && z == startZ) || (x == endX && z == endZ)) {
                continue;
            }

            if (x >= 0 && x < grid.length && z >= 0 && z < grid[0].length) {
                if (grid[x][z] != null) {
                    grid[x][z] = null;
                }

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int nx = x + dx;
                        int nz = z + dz;

                        if (nx >= 0 && nx < grid.length && nz >= 0 && nz < grid[0].length) {
                            if (dx != 0 || dz != 0) {
                                if (grid[nx][nz] != null) {
                                    grid[nx][nz] = null;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public List<Vector3f> findPath(Geometry startGeo, Geometry endGeo) {
        Vector3f start = startGeo.getWorldTranslation();
        Vector3f end = endGeo.getWorldTranslation();
        int gridLength = Grid.GRID_LENGTH * 2;
        int gridWidth = Grid.GRID_WIDTH * 2;
        int gridSpacing = Grid.GRID_SPACING;

        float originX = gridLength * gridSpacing / 2f;
        float originZ = gridWidth * gridSpacing / 2f;

        int startX = (int) ((start.x + originX) / gridSpacing);
        int startZ = (int) ((start.z + originZ) / gridSpacing);
        int endX = (int) ((end.x + originX) / gridSpacing);
        int endZ = (int) ((end.z + originZ) / gridSpacing);

        startX = Math.max(0, Math.min(startX, gridLength - 1));
        startZ = Math.max(0, Math.min(startZ, gridWidth - 1));
        endX = Math.max(0, Math.min(endX, gridLength - 1));
        endZ = Math.max(0, Math.min(endZ, gridWidth - 1));

        PathNode[][] grid = new PathNode[gridLength][gridWidth];
        for (int x = 0; x < gridLength; x++) {
            for (int z = 0; z < gridWidth; z++) {
                grid[x][z] = new PathNode(x, z);
            }
        }

        Set<Vector3f> obstacles = getObstacles(startGeo, endGeo);
        updateGridWithObstacles(grid, obstacles, gridSpacing, start, end);

        PathNode startNode = grid[startX][startZ];
        PathNode endNode = grid[endX][endZ];

        PriorityQueue<PathNode> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));
        Set<PathNode> closedSet = new HashSet<>();

        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            if (current == endNode) {
                List<Vector3f> path = constructPath(current, gridSpacing);
                drawPath(path);
                return path;
            }

            closedSet.add(current);

            for (PathNode neighbor : getNeighbors(current, grid, gridLength, gridWidth)) {
                if (closedSet.contains(neighbor)) continue;

                int tentativeGCost = current.gCost + (isDiagonal(current, neighbor) ? DIAGONAL_COST : STRAIGHT_COST);
                if (!openSet.contains(neighbor) || tentativeGCost < neighbor.gCost) {
                    neighbor.calculateCosts(endNode, tentativeGCost);
                    neighbor.parent = current;

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    private List<PathNode> getNeighbors(PathNode node, PathNode[][] grid, int gridLength, int gridWidth) {
        List<PathNode> neighbors = new ArrayList<>();
        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            int nx = node.x + dir[0];
            int nz = node.z + dir[1];

            if (nx >= 0 && nx < gridLength && nz >= 0 && nz < gridWidth) {
                PathNode neighbor = grid[nx][nz];

                if (neighbor != null) {
                    neighbors.add(neighbor);
                }
            }
        }

        return neighbors;
    }

    private boolean isDiagonal(PathNode current, PathNode neighbor) {
        return Math.abs(current.x - neighbor.x) + Math.abs(current.z - neighbor.z) == 2;
    }

    private boolean isFloorRelated(Spatial object, Map<Geometry, Float> floorCompleteAreas, Map<Geometry, Float> floorSegmentDistances) {
        return floorCompleteAreas.containsKey(object) || floorSegmentDistances.containsKey(object);
    }

    private List<Vector3f> constructPath(PathNode endNode, int gridSpacing) {
        List<Vector3f> path = new ArrayList<>();
        PathNode current = endNode;

        float originX = Grid.GRID_LENGTH * gridSpacing;
        float originZ = Grid.GRID_WIDTH * gridSpacing;

        while (current != null) {
            float worldX = current.x * gridSpacing - originX;
            float worldZ = current.z * gridSpacing - originZ;
            path.add(0, new Vector3f(worldX, Grid.GRID_Y_LEVEL, worldZ));
            current = current.parent;
        }

        return path;
    }

    private void drawPath(List<Vector3f> path) {
        jmeScene.enqueue(() -> {
            for (int i = 0; i < path.size() - 1; i++) {
                Vector3f start = path.get(i);
                Vector3f end = path.get(i + 1);

                Line line = new Line(start, end);
                Geometry lineGeometry = new Geometry("PathLine", line);
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", ColorRGBA.Yellow);
                lineGeometry.setMaterial(mat);

                rootNode.attachChild(lineGeometry);
                drawnLines.add(lineGeometry);
            }
        });
    }

    public void clearAllLines() {
        jmeScene.enqueue(() -> {
            for (Geometry line : drawnLines) {
                rootNode.detachChild(line);
            }
            drawnLines.clear();
        });
    }
}
