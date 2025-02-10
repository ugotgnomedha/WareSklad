package tech.simulations;

import UndoRedo.UndoManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import tech.WareSkladInit;
import ui.Grid;

import java.util.*;

public class SimulationManager {
    private WareSkladInit jmeScene;
    private UndoManager undoManager;
    private Node rootNode;
    private AStarPathFind pathFinder;
    private Spatial forkliftModel;
    private float avoidanceDistance;

    public SimulationManager(WareSkladInit jmeScene, float avoidanceDistance) {
        this.jmeScene = jmeScene;
        this.undoManager = jmeScene.undoManager;
        this.rootNode = jmeScene.getRootNode();
        this.avoidanceDistance = avoidanceDistance;
        this.pathFinder = new AStarPathFind(jmeScene, avoidanceDistance);
    }

    public void startReceivingSimulation(Geometry selectedArea, List<Pallet> selectedPallets, Forklift forklift) {
        System.out.println("Avoidance: " + avoidanceDistance);

        Map<Spatial, RackSettings> rackSettingsMap = undoManager.getRackSettingsMap();
        List<PalletTask> palletQueue = new ArrayList<>();

        for (Pallet pallet : selectedPallets) {
            boolean assigned = false;
            for (Map.Entry<Spatial, RackSettings> entry : rackSettingsMap.entrySet()) {
                Spatial rack = entry.getKey();
                RackSettings settings = entry.getValue();

                if (canFitPalletInRack(pallet, settings)) {
                    List<Vector3f> pathToRack = pathFinder.findPath(selectedArea, (Geometry) rack);
                    if (pathToRack.isEmpty()) continue;

                    List<Vector3f> pathBack = new ArrayList<>(pathToRack);
                    Collections.reverse(pathBack);

                    palletQueue.add(new PalletTask(pallet, pathToRack, pathBack));
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                System.out.println("No suitable rack found for pallet: " + pallet.getId());
            }
        }

        if (!palletQueue.isEmpty()) {
            processPalletQueue(palletQueue, forklift, selectedArea);
        }
    }

    private void processPalletQueue(List<PalletTask> palletQueue, Forklift forklift, Geometry startArea) {
        jmeScene.enqueue(() -> {
            if (forkliftModel == null) {
                forkliftModel = jmeScene.getAssetManager().loadModel("Models/Misc/forkLiftTruck.j3o");
                forkliftModel.setLocalScale(4f);
                rootNode.attachChild(forkliftModel);
            }

            Vector3f startPosition = startArea.getWorldTranslation();
            forkliftModel.setLocalTranslation(startPosition);

            new Thread(() -> {
                try {
                    for (PalletTask task : palletQueue) {
                        System.out.println("Moving pallet " + task.pallet.getId() + " to rack...");
                        animateModelAlongPath(forkliftModel, task.pathToRack, forklift.getSpeed());

                        Thread.sleep(500); // Unload pallet to rack time.
                        System.out.println("Returning to start...");
                        animateModelAlongPath(forkliftModel, task.pathBack, forklift.getSpeed());
                    }

                    jmeScene.enqueue(() -> {
                        rootNode.detachChild(forkliftModel);
                        forkliftModel = null;
                        pathFinder.clearAllLines();
                    });

                } catch (InterruptedException e) {
                    System.err.println("Animation interrupted: " + e.getMessage());
                }
            }).start();
        });
    }

    private boolean canFitPalletInRack(Pallet pallet, RackSettings rackSettings) {
        return pallet.getHeight() <= rackSettings.getHeight()
                && pallet.getWidth() <= rackSettings.getWidth()
                && pallet.getDepth() <= rackSettings.getDepth()
                && pallet.getWeight() <= rackSettings.getPerShelfCapacity();
    }

    private void animateModelAlongPath(Spatial model, List<Vector3f> path, double forkliftSpeed) {
        if (path == null || path.isEmpty()) return;

        try {
            Quaternion currentRotation = new Quaternion();
            float gridSpeed = (float) (forkliftSpeed * Grid.GRID_LENGTH);

            for (int i = 1; i < path.size(); i++) {
                Vector3f start = path.get(i - 1);
                Vector3f end = path.get(i);
                Vector3f direction = end.subtract(start).normalizeLocal();
                Quaternion targetRotation = new Quaternion();
                targetRotation.lookAt(direction, Vector3f.UNIT_Y);

                float distance = start.distance(end);
                float duration = distance / gridSpeed;
                float elapsedTime = 0;
                float frameTime = 0.016f;

                while (elapsedTime < duration) {
                    Thread.sleep((long) (frameTime * 1000));
                    elapsedTime += frameTime;

                    float t = elapsedTime / duration;
                    Vector3f interpolatedPosition = start.interpolateLocal(end, t);
                    Quaternion interpolatedRotation = new Quaternion();
                    interpolatedRotation.slerp(currentRotation, targetRotation, t);

                    jmeScene.enqueue(() -> {
                        model.setLocalTranslation(interpolatedPosition);
                        model.setLocalRotation(interpolatedRotation);
                    });
                }

                jmeScene.enqueue(() -> {
                    model.setLocalTranslation(end);
                    model.setLocalRotation(targetRotation);
                });

                currentRotation.set(targetRotation);
            }
        } catch (InterruptedException e) {
            System.err.println("Animation interrupted: " + e.getMessage());
        }
    }

    private static class PalletTask {
        Pallet pallet;
        List<Vector3f> pathToRack;
        List<Vector3f> pathBack;

        public PalletTask(Pallet pallet, List<Vector3f> pathToRack, List<Vector3f> pathBack) {
            this.pallet = pallet;
            this.pathToRack = pathToRack;
            this.pathBack = pathBack;
        }
    }
}