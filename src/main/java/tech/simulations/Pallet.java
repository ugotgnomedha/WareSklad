package tech.simulations;

public class Pallet {
    private String id;
    private double height;
    private double width;
    private double depth;
    private double weight;

    public Pallet(String id, double height, double width, double depth, double weight) {
        this.id = id;
        this.height = height;
        this.width = width;
        this.depth = depth;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getDepth() {
        return depth;
    }

    public double getWeight() {
        return weight;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return id;
    }
}