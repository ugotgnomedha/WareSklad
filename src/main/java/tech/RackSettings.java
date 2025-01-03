package tech;

public class RackSettings {

    private double height;
    private double width;
    private double depth;
    private int shelves;
    private int perShelfCapacity;
    private int totalCapacity;

    public RackSettings(double height, double width, double depth, int shelves, int perShelfCapacity, int totalCapacity) {
        this.height = height;
        this.width = width;
        this.depth = depth;
        this.shelves = shelves;
        this.perShelfCapacity = perShelfCapacity;
        this.totalCapacity = totalCapacity;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public int getShelves() {
        return shelves;
    }

    public void setShelves(int shelves) {
        this.shelves = shelves;
    }

    public int getPerShelfCapacity() {
        return perShelfCapacity;
    }

    public void setPerShelfCapacity(int perShelfCapacity) {
        this.perShelfCapacity = perShelfCapacity;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    @Override
    public String toString() {
        return "Rack Settings:\n" +
                "Height: " + height + " cm\n" +
                "Width: " + width + " cm\n" +
                "Depth: " + depth + " cm\n" +
                "Number of Shelves: " + shelves + "\n" +
                "Per Shelf Capacity: " + perShelfCapacity + " kg\n" +
                "Total Rack Capacity: " + totalCapacity + " kg";
    }
}
