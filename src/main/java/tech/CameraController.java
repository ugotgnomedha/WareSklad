package tech;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import ui.Grid;

public class CameraController {

    private Camera cam;
    private float cameraSpeed = 100f;
    private float zoomSpeed = 10f;
    private float minZoom = 20f;
    private float maxZoom = 800f;
    private float currentZoom = 100f;

    public CameraController(Camera cam) {
        this.cam = cam;
    }

    public void updateCameraPosition(float tpf) {
        Vector3f camLocation = cam.getLocation();

        Vector3f forward = new Vector3f(0, 0, -1);
        Vector3f left = new Vector3f(-1, 0, 0);

        if (InputHandler.isMoveLeft()) camLocation.addLocal(left.mult(-cameraSpeed * tpf));
        if (InputHandler.isMoveRight()) camLocation.addLocal(left.mult(cameraSpeed * tpf));
        if (InputHandler.isMoveUp()) camLocation.addLocal(forward.mult(-cameraSpeed * tpf));
        if (InputHandler.isMoveDown()) camLocation.addLocal(forward.mult(cameraSpeed * tpf));

        camLocation.setX(Math.max(-Grid.GRID_WIDTH * Grid.GRID_SPACING, Math.min(Grid.GRID_WIDTH * Grid.GRID_SPACING, camLocation.x)));
        camLocation.setZ(Math.max(-Grid.GRID_LENGTH * Grid.GRID_SPACING, Math.min(Grid.GRID_LENGTH * Grid.GRID_SPACING, camLocation.z)));

        cam.setLocation(camLocation);

        InputHandler.resetMovement();
    }

    public void zoomIn() {
        currentZoom = Math.max(minZoom, currentZoom - zoomSpeed);
        cam.setLocation(new Vector3f(cam.getLocation().x, currentZoom, cam.getLocation().z));
        cam.lookAt(new Vector3f(cam.getLocation().x, 0, cam.getLocation().z), Vector3f.UNIT_Y);
    }

    public void zoomOut() {
        currentZoom = Math.min(maxZoom, currentZoom + zoomSpeed);
        cam.setLocation(new Vector3f(cam.getLocation().x, currentZoom, cam.getLocation().z));
        cam.lookAt(new Vector3f(cam.getLocation().x, 0, cam.getLocation().z), Vector3f.UNIT_Y);
    }

    public float getCurrentZoom() {
        return currentZoom;
    }

    public Camera getCam() {
        return cam;
    }
}
