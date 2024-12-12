package ui;

import UndoRedo.UndoManager;
import UndoRedo.UndoRedoListener;
import UndoRedo.FloorPlacementAction;
import UndoRedo.UndoableAction;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import tech.WareSkladInit;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

public class HierarchyUI extends JPanel implements UndoRedoListener {
    private final WareSkladInit jmeScene;
    private final UndoManager undoManager;
    private JTree objectTree;
    private DefaultMutableTreeNode rootNode;

    public HierarchyUI(WareSkladInit jmeScene, UndoManager undoManager) {
        this.jmeScene = jmeScene;
        this.undoManager = undoManager;
        initializeUI();
        undoManager.addUndoRedoListener(this);
    }

    private void initializeUI() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder("Hierarchy"));

        rootNode = new DefaultMutableTreeNode("Scene");
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
        updateObjectTree();
    }

    private void updateObjectTree() {
        rootNode.removeAllChildren();
        List<Spatial> currentObjects = undoManager.getCurrentSceneObjects();

        for (Spatial object : currentObjects) {
            if (object instanceof Geometry && object.getName().equals("CompleteFloor")) {
                FloorPlacementAction floorAction = findFloorPlacementAction(object);
                if (floorAction != null) {
                    DefaultMutableTreeNode floorNode = new DefaultMutableTreeNode("Completed Floor");
                    for (Spatial segment : floorAction.getFloorGeometries()) {
                        floorNode.add(new DefaultMutableTreeNode(segment));
                    }
                    rootNode.add(floorNode);
                }
            } else if (object instanceof Geometry) {
                boolean isFloorSegment = false;
                for (UndoableAction action : undoManager.getUndoStack()) {
                    if (action instanceof FloorPlacementAction) {
                        FloorPlacementAction floorAction = (FloorPlacementAction) action;
                        for (Spatial segment : floorAction.getFloorGeometries()) {
                            if (segment == object) {
                                isFloorSegment = true;
                                break;
                            }
                        }
                    }
                    if (isFloorSegment) {
                        break;
                    }
                }
                if (!isFloorSegment) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(object);
                    rootNode.add(node);
                }
            }
        }

        ((DefaultTreeModel) objectTree.getModel()).reload();
    }

    private FloorPlacementAction findFloorPlacementAction(Spatial object) {
        for (UndoableAction action : undoManager.getUndoStack()) {
            if (action instanceof FloorPlacementAction) {
                for (Spatial segment : ((FloorPlacementAction) action).getFloorGeometries()) {
                    if (segment == object) {
                        return (FloorPlacementAction) action;
                    }
                }
            }
        }
        return null;
    }
}
