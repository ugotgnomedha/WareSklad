package ui;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class UILinesDrawer {

    private List<Geometry> lineGeometries = new ArrayList<>();
    private List<BitmapText> textLabels = new ArrayList<>();

    public void clearLines(Node rootNode) {
        for (Geometry geometry : lineGeometries) {
            rootNode.detachChild(geometry);
        }
        lineGeometries.clear();

        for (BitmapText text : textLabels) {
            rootNode.detachChild(text);
        }
        textLabels.clear();
    }

    public void addLines(SimpleApplication app, Node rootNode) {
        clearLines(rootNode);

        float gridStartX = Grid.GRID_WIDTH * Grid.GRID_SPACING;
        float gridStartZ = -Grid.GRID_LENGTH * Grid.GRID_SPACING;
        float gridEndZ = Grid.GRID_LENGTH * Grid.GRID_SPACING;
        float gridYLevel = Grid.GRID_Y_LEVEL;

        float offset = 10f;

        Line greenLine = new Line(
                new Vector3f(gridStartX + offset, gridYLevel, gridStartZ),
                new Vector3f(gridStartX + offset, gridYLevel, gridEndZ)
        );
        Geometry greenGeometry = new Geometry("GreenLine", greenLine);
        Material greenMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        greenMaterial.setColor("Color", ColorRGBA.Green);
        greenGeometry.setMaterial(greenMaterial);
        rootNode.attachChild(greenGeometry);
        lineGeometries.add(greenGeometry);

        Line redLine = new Line(
                new Vector3f(-Grid.GRID_WIDTH * Grid.GRID_SPACING, gridYLevel, gridEndZ + offset),
                new Vector3f(Grid.GRID_WIDTH * Grid.GRID_SPACING, gridYLevel, gridEndZ + offset)
        );
        Geometry redGeometry = new Geometry("RedLine", redLine);
        Material redMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        redMaterial.setColor("Color", ColorRGBA.Red);
        redGeometry.setMaterial(redMaterial);
        rootNode.attachChild(redGeometry);
        lineGeometries.add(redGeometry);

        for (int i = -Grid.GRID_LENGTH; i <= Grid.GRID_LENGTH; i += 5) {
            String greenTextContent = (i >= 0 ? i + "m" : i + "m");
            float zPosition = i * Grid.GRID_SPACING;
            BitmapText greenText = createText(app, greenTextContent, ColorRGBA.Green);
            greenText.setLocalTranslation(gridStartX + offset + 10f, gridYLevel, zPosition);
            greenText.rotate(FastMath.DEG_TO_RAD * 90, FastMath.DEG_TO_RAD * 90, FastMath.DEG_TO_RAD * 180);
            rootNode.attachChild(greenText);
            textLabels.add(greenText);

            Line greenDash = new Line(
                    new Vector3f(gridStartX + offset - 1f, gridYLevel + 0.2f, zPosition),
                    new Vector3f(gridStartX + offset + 1f, gridYLevel + 0.2f, zPosition)
            );
            Geometry greenDashGeometry = new Geometry("GreenDash", greenDash);
            Material greenDashMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            greenDashMaterial.setColor("Color", ColorRGBA.Green);
            greenDashGeometry.setMaterial(greenDashMaterial);
            rootNode.attachChild(greenDashGeometry);
            lineGeometries.add(greenDashGeometry);
        }

        for (int i = -Grid.GRID_WIDTH; i <= Grid.GRID_WIDTH; i += 5) {
            String redTextContent = (i >= 0 ? Math.abs(i) + "m" : -Math.abs(i) + "m");
            float xPosition = i * Grid.GRID_SPACING;
            BitmapText redText = createText(app, redTextContent, ColorRGBA.Red);
            redText.setLocalTranslation(xPosition, gridYLevel, gridEndZ + offset + 10f);
            redText.rotate(FastMath.DEG_TO_RAD * 90, 0, FastMath.DEG_TO_RAD * 180);
            rootNode.attachChild(redText);
            textLabels.add(redText);

            Line redDash = new Line(
                    new Vector3f(xPosition, gridYLevel + 0.2f, gridEndZ + offset + 1f),
                    new Vector3f(xPosition, gridYLevel + 0.2f, gridEndZ + offset - 1f)
            );
            Geometry redDashGeometry = new Geometry("RedDash", redDash);
            Material redDashMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            redDashMaterial.setColor("Color", ColorRGBA.Red);
            redDashGeometry.setMaterial(redDashMaterial);
            rootNode.attachChild(redDashGeometry);
            lineGeometries.add(redDashGeometry);
        }
    }

    private BitmapText createText(SimpleApplication app, String content, ColorRGBA color) {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        BitmapText text = new BitmapText(font, false);
        text.setSize(font.getCharSet().getRenderedSize() * 0.5f);
        text.setColor(color);
        text.setText(content);
        return text;
    }
}
