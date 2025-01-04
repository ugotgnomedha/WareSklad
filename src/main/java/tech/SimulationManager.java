package tech;

import UndoRedo.UndoManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationManager {
    private WareSkladInit jmeScene;
    private UndoManager undoManager;
    private Node rootNode;
    private AStarPathFind pathFinder;

    public Map<Spatial, List<Pallet>> receivingPalletToRackTODO;

    public SimulationManager(WareSkladInit jmeScene){
        this.jmeScene = jmeScene;
        this.undoManager = jmeScene.undoManager;
        this.rootNode = jmeScene.getRootNode();
        this.pathFinder = new AStarPathFind(jmeScene, 2);
    }

    public void startReceivingSimulation(Geometry selectedArea, List<Pallet> selectedPallets) {
        this.receivingPalletToRackTODO = new HashMap<>();
        System.out.println("[SimulationManager] Receiving Simulation Started");
        System.out.println("Selected Area: " + selectedArea);
        System.out.println("Selected Pallets: " + selectedPallets);

        Map<Spatial, RackSettings> rackSettingsMap = undoManager.getRackSettingsMap();

        for (Spatial rack : rackSettingsMap.keySet()) {
            receivingPalletToRackTODO.putIfAbsent(rack, new ArrayList<>());
        }

        for (Pallet pallet : selectedPallets) {
            boolean assigned = false;

            for (Map.Entry<Spatial, RackSettings> entry : rackSettingsMap.entrySet()) {
                Spatial rack = entry.getKey();
                RackSettings settings = entry.getValue();

                if (canFitPalletInRack(pallet, settings)) {
                    System.out.println("Pallet " + pallet.getId() + " assigned to rack: " + rack.getName());
                    receivingPalletToRackTODO.get(rack).add(pallet);

                    List<Vector3f> path = pathFinder.findPath(selectedArea, (Geometry) rack);
                    System.out.println("Path for pallet " + pallet.getId() + ": " + path);

                    if (!path.isEmpty()) {
                        animateModelAlongPath(path);
                    }

                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                System.out.println("No suitable rack found for pallet: " + pallet.getId());
            }
        }

        logRackAssignments();
    }

    private boolean canFitPalletInRack(Pallet pallet, RackSettings rackSettings) {
        boolean fitsDimensions = pallet.getHeight() <= rackSettings.getHeight()
                && pallet.getWidth() <= rackSettings.getWidth()
                && pallet.getDepth() <= rackSettings.getDepth();

        boolean fitsWeight = pallet.getWeight() <= rackSettings.getPerShelfCapacity();

        return fitsDimensions && fitsWeight;
    }

    private void logRackAssignments() {
        System.out.println("\n[SimulationManager] Rack Assignments:");
        for (Map.Entry<Spatial, List<Pallet>> entry : receivingPalletToRackTODO.entrySet()) {
            Spatial rack = entry.getKey();
            List<Pallet> pallets = entry.getValue();
            System.out.println("Rack: " + rack.getName() + " -> Pallets: " + pallets);
        }
    }

    public void startSendingSimulation(String selectedArea, List<Pallet> selectedPallets) {
        System.out.println("[SimulationManager] Sending Simulation Started");
        System.out.println("Selected Area: " + selectedArea);
        System.out.println("Selected Packages: " + selectedPallets);
    }

    public void animateModelAlongPath(List<Vector3f> path) {
        if (path == null || path.isEmpty()) {
            System.out.println("No path to animate!");
            return;
        }

        jmeScene.enqueue(() -> {
            Spatial forklift = jmeScene.getAssetManager().loadModel("Models/Misc/forkLiftTruck.j3o");
            forklift.setLocalTranslation(path.get(0));
            forklift.setLocalScale(4f);
            rootNode.attachChild(forklift);

            new Thread(() -> {
                try {
                    Quaternion currentRotation = new Quaternion();

                    for (int i = 1; i < path.size(); i++) {
                        Vector3f start = path.get(i - 1);
                        Vector3f end = path.get(i);

                        Vector3f direction = end.subtract(start).normalizeLocal();
                        Quaternion targetRotation = new Quaternion();
                        targetRotation.lookAt(direction, Vector3f.UNIT_Y);

                        float duration = 0.5f;
                        float elapsedTime = 0;

                        while (elapsedTime < duration) {
                            Thread.sleep(20);
                            elapsedTime += 0.02f;

                            float t = elapsedTime / duration;
                            Vector3f interpolatedPosition = start.interpolateLocal(end, t);

                            Quaternion interpolatedRotation = new Quaternion();
                            interpolatedRotation.slerp(currentRotation, targetRotation, t);

                            Vector3f finalPosition = interpolatedPosition.clone();
                            Quaternion finalRotation = interpolatedRotation.clone();
                            jmeScene.enqueue(() -> {
                                forklift.setLocalTranslation(finalPosition);
                                forklift.setLocalRotation(finalRotation);
                            });
                        }

                        Vector3f finalEnd = end.clone();
                        jmeScene.enqueue(() -> {
                            forklift.setLocalTranslation(finalEnd);
                            forklift.setLocalRotation(targetRotation);
                        });

                        currentRotation.set(targetRotation);
                    }

                    jmeScene.enqueue(() -> {
                        rootNode.detachChild(forklift);
                        pathFinder.clearAllLines();
                    });

                } catch (InterruptedException e) {
                    System.err.println("Animation interrupted: " + e.getMessage());
                }
            }).start();
        });
    }
}
