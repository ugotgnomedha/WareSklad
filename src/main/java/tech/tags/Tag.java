package tech.tags;

import java.awt.Color;

public class Tag {

    private String name;
    private boolean isCustomColor;
    private Color color;

    public Tag(String name, boolean isCustomColor, Color color) {
        this.name = name;
        this.isCustomColor = isCustomColor;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCustomColor() {
        return isCustomColor;
    }

    public void setCustomColor(boolean customColor) {
        isCustomColor = customColor;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "name='" + name + '\'' +
                ", isCustomColor=" + isCustomColor +
                ", color=" + color +
                '}';
    }
}
