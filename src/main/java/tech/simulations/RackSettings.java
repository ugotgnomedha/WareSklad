package tech.simulations;

import tech.simulations.Pallet;

import java.util.ArrayList;
import java.util.List;

public class RackSettings {

    private double height;
    private double width;
    private double depth;
    private int shelves;
    private int perShelfCapacity;
    private int totalCapacity;
    private List<Pallet> storedPallets = new ArrayList<>();

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

    public boolean addPallet(Pallet pallet) {
        if (canFitPallet(pallet)) {
            storedPallets.add(pallet);
            return true;
        }
        return false;
    }

    public boolean removePallet(Pallet pallet) {
        return storedPallets.remove(pallet);
    }

    public List<Pallet> getStoredPallets() {
        return storedPallets;
    }

    public boolean canFitPallet(Pallet pallet) {
        return storedPallets.size() < totalCapacity && pallet.getWeight() <= perShelfCapacity;
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
