package ui;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class Grid {

    public static int GRID_LENGTH = 20;
    public static int GRID_WIDTH = 20;
    public static final int GRID_SPACING = 10;
    public static final float GRID_Y_LEVEL = 0f;

    private AssetManager assetManager;
    private Node rootNode;

    private List<Geometry> gridGeometries = new ArrayList<>();

    public Grid(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
    }

    public void clearGrid() {
        for (Geometry geometry : gridGeometries) {
            rootNode.detachChild(geometry);
        }
        gridGeometries.clear();
    }

    public void addGrid() {
        clearGrid();

        for (int i = -GRID_WIDTH; i <= GRID_WIDTH; i++) {
            if (Math.abs(i) * GRID_SPACING <= GRID_WIDTH * GRID_SPACING) {
                Line verticalLine = new Line(
                        new Vector3f(i * GRID_SPACING, GRID_Y_LEVEL, -GRID_LENGTH * GRID_SPACING),
                        new Vector3f(i * GRID_SPACING, GRID_Y_LEVEL, GRID_LENGTH * GRID_SPACING)
                );
                Geometry verticalGridLine = new Geometry("VerticalLine", verticalLine);
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", ColorRGBA.White);
                verticalGridLine.setMaterial(mat);
                rootNode.attachChild(verticalGridLine);
                gridGeometries.add(verticalGridLine);
            }
        }

        for (int i = -GRID_LENGTH; i <= GRID_LENGTH; i++) {
            if (Math.abs(i) * GRID_SPACING <= GRID_LENGTH * GRID_SPACING) {
                Line horizontalLine = new Line(
                        new Vector3f(-GRID_WIDTH * GRID_SPACING, GRID_Y_LEVEL, i * GRID_SPACING),
                        new Vector3f(GRID_WIDTH * GRID_SPACING, GRID_Y_LEVEL, i * GRID_SPACING)
                );
                Geometry horizontalGridLine = new Geometry("HorizontalLine", horizontalLine);
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", ColorRGBA.White);
                horizontalGridLine.setMaterial(mat);
                rootNode.attachChild(horizontalGridLine);
                gridGeometries.add(horizontalGridLine);
            }
        }
    }
}
