package tech.simulations;

public class Forklift {
    private String id;
    private double maxWeightCapacity; // kg
    private double speed; // m/s

    public Forklift(String id, double maxWeightCapacity, double speed) {
        this.id = id;
        this.maxWeightCapacity = maxWeightCapacity;
        this.speed = speed;
    }

    public String getId() {
        return id;
    }

    public double getMaxWeightCapacity() {
        return maxWeightCapacity;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return id + " (Capacity: " + maxWeightCapacity + " kg, Speed: " + speed + " m/s)";
    }
}