package ui;

import UndoRedo.UndoManager;
import UndoRedo.UndoRedoListener;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import tech.FloorPlacer;
import tech.WareSkladInit;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

public class HierarchyUI extends JPanel implements UndoRedoListener {
    private final WareSkladInit jmeScene;
    private final UndoManager undoManager;
    private JTree objectTree;
    private DefaultMutableTreeNode rootNode;
    private FloorPlacer floorPlacer;
    private ResourceBundle bundle;

    public HierarchyUI(ResourceBundle bundle, WareSkladInit jmeScene, UndoManager undoManager) {
        this.bundle = bundle;
        this.jmeScene = jmeScene;
        this.undoManager = undoManager;
        this.floorPlacer = jmeScene.floorPlacer;
        initializeUI();
        undoManager.addUndoRedoListener(this);
    }

    private void initializeUI() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(bundle.getString("hierarchy")));

        rootNode = new DefaultMutableTreeNode(bundle.getString("scene"));
        objectTree = new JTree(new DefaultTreeModel(rootNode));
        objectTree.setRootVisible(true);
        objectTree.setShowsRootHandles(true);

        JScrollPane scrollPane = new JScrollPane(objectTree);
        this.add(scrollPane, BorderLayout.CENTER);

        objectTree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            if (path != null) {
                Object selectedNode = path.getLastPathComponent();
                if (selectedNode instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectedNode;
                    Object userObject = treeNode.getUserObject();
                    if (userObject instanceof String && userObject.equals("Scene")) {
                        return;
                    }
                    if (userObject instanceof Spatial) {
                        Spatial selectedSpatial = (Spatial) userObject;

                        if (selectedSpatial != null) {
                            jmeScene.enqueue(() -> {
                                jmeScene.selectObject(selectedSpatial);
                                System.out.println("Selected: " + selectedSpatial.getName());
                            });
                        }
                    }
                }
            }
        });
    }


    @Override
    public void onUndoRedo() {
        jmeScene.enqueue(this::updateObjectTree);
    }

    private void updateObjectTree() {

        rootNode.removeAllChildren();
        List<Spatial> currentObjects = undoManager.getCurrentSceneObjects();

        Map<Integer, List<Spatial>> floorGeometriesMap = new HashMap<>();

        for (Spatial object : currentObjects) {
            if (object instanceof Geometry) {
                int floorId;

                if (object.getName().equals("CompleteFloor")) {
                    floorId = getFloorIdFromCompleteFloor(object);
                } else {
                    floorId = getFloorIdFromSegment(object);
                }

                if (floorId != -1) {
                    floorGeometriesMap.computeIfAbsent(floorId, k -> new ArrayList<>()).add(object);
                } else {
                    DefaultMutableTreeNode standaloneNode = new DefaultMutableTreeNode(object);
                    rootNode.add(standaloneNode);
                }
            }
        }

        int floorCounter = 1;
        for (Map.Entry<Integer, List<Spatial>> entry : floorGeometriesMap.entrySet()) {
            List<Spatial> geometries = entry.getValue();

            String folderName = "Complete Floor " + floorCounter++;
            DefaultMutableTreeNode floorFolderNode = new DefaultMutableTreeNode(folderName);

            DefaultMutableTreeNode completeFloorNode = null;
            for (Spatial geometry : geometries) {
                if (geometry instanceof Geometry && geometry.getName().equals("CompleteFloor")) {
                    completeFloorNode = new DefaultMutableTreeNode(geometry);
                } else {
                    floorFolderNode.add(new DefaultMutableTreeNode(geometry));
                }
            }

            if (completeFloorNode != null) {
                floorFolderNode.add(completeFloorNode);
            }

            rootNode.add(floorFolderNode);
        }

        ((DefaultTreeModel) objectTree.getModel()).reload();

    }

    private int getFloorIdFromCompleteFloor(Spatial object) {
        for (Map.Entry<Integer, List<Geometry>> entry : undoManager.getFloorIdToSegments().entrySet()) {
            if (entry.getValue().contains(object)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    private int getFloorIdFromSegment(Spatial object) {
        if (object instanceof Geometry) {
            return undoManager.getFloorSegmentToFloorId().getOrDefault(object, -1);
        }
        return -1;
    }
}
