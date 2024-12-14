package tech;

import com.jme3.math.Quaternion;
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

    private float sensitivity = 0.1f;
    private float rotationSpeed = 0.005f;
    private float pitch = 0;
    private float yaw = 0;

    private final float MIN_PITCH = -1.55f;
    private final float MAX_PITCH = 1.55f;

    private boolean is3DMode = false;

    public CameraController(Camera cam) {
        this.cam = cam;
    }

    public void setIs3DMode(boolean is3DMode) {
        this.is3DMode = is3DMode;
    }

    public void updateCameraPosition(float tpf, boolean is3DMode) {
        Vector3f camLocation = cam.getLocation();

        if (is3DMode) {
            Vector3f forward = cam.getDirection().mult(cameraSpeed * tpf);
            Vector3f left = cam.getLeft().mult(cameraSpeed * tpf);

            if (InputHandler.isMoveUp()) camLocation.addLocal(forward);
            if (InputHandler.isMoveDown()) camLocation.subtractLocal(forward);

            if (InputHandler.isMoveLeft()) camLocation.addLocal(left);
            if (InputHandler.isMoveRight()) camLocation.subtractLocal(left);
        } else {
            Vector3f forward = new Vector3f(0, 0, -1);
            Vector3f left = new Vector3f(-1, 0, 0);

            if (InputHandler.isMoveLeft()) camLocation.addLocal(left.mult(-cameraSpeed * tpf));
            if (InputHandler.isMoveRight()) camLocation.addLocal(left.mult(cameraSpeed * tpf));
            if (InputHandler.isMoveUp()) camLocation.addLocal(forward.mult(-cameraSpeed * tpf));
            if (InputHandler.isMoveDown()) camLocation.addLocal(forward.mult(cameraSpeed * tpf));
        }

        camLocation.setX(Math.max(-Grid.GRID_WIDTH * Grid.GRID_SPACING, Math.min(Grid.GRID_WIDTH * Grid.GRID_SPACING, camLocation.x)));
        camLocation.setZ(Math.max(-Grid.GRID_LENGTH * Grid.GRID_SPACING, Math.min(Grid.GRID_LENGTH * Grid.GRID_SPACING, camLocation.z)));

        cam.setLocation(camLocation);

        InputHandler.resetMovement();
    }

    public void zoomIn() {
        if (is3DMode) return;

        currentZoom = Math.max(minZoom, currentZoom - zoomSpeed);
        cam.setLocation(new Vector3f(cam.getLocation().x, currentZoom, cam.getLocation().z));
        cam.lookAt(new Vector3f(cam.getLocation().x, 0, cam.getLocation().z), Vector3f.UNIT_Y);
    }

    public void zoomOut() {
        if (is3DMode) return;

        currentZoom = Math.min(maxZoom, currentZoom + zoomSpeed);
        cam.setLocation(new Vector3f(cam.getLocation().x, currentZoom, cam.getLocation().z));
        cam.lookAt(new Vector3f(cam.getLocation().x, 0, cam.getLocation().z), Vector3f.UNIT_Y);
    }

    public void rotateCamera(float deltaX, float deltaY) {
        if (is3DMode) {
            yaw -= deltaX * rotationSpeed;
            pitch -= deltaY * rotationSpeed;

            pitch = Math.max(Math.min(pitch, MAX_PITCH), MIN_PITCH);
        } else {
            return;
        }

        cam.setRotation(new Quaternion().fromAngles(pitch, yaw, 0));
    }

    public float getCurrentZoom() {
        return currentZoom;
    }

    public Camera getCam() {
        return cam;
    }
}
