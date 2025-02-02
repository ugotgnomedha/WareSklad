package saverLoader;

import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Matrix4f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.mesh.IndexBuffer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class OBJExporter {

    public void exportSceneToOBJ(List<Spatial> sceneObjects, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            int vertexOffset = 0;
            int texCoordOffset = 0;
            int normalOffset = 0;

            for (Spatial spatial : sceneObjects) {
                writer.write("o " + spatial.getName() + "\n");

                List<Vector3f> vertices = new ArrayList<>();
                List<Vector3f> normals = new ArrayList<>();
                List<Vector2f> texCoords = new ArrayList<>();
                List<Face> faces = new ArrayList<>();

                traverseModel(spatial, vertices, normals, texCoords, faces);

                Matrix4f transform = getWorldTransform(spatial);
                for (Vector3f vertex : vertices) {
                    Vector3f transformedVertex = transform.mult(vertex);
                    vertex.set(transformedVertex.x, transformedVertex.y, transformedVertex.z);
                }
                for (Vector3f normal : normals) {
                    Vector3f transformedNormal = transform.mult(normal).normalize();
                    normal.set(transformedNormal.x, transformedNormal.y, transformedNormal.z);
                }

                for (Vector3f vertex : vertices) {
                    writer.write(String.format("v %f %f %f\n", vertex.x, vertex.y, vertex.z));
                }

                for (Vector2f texCoord : texCoords) {
                    writer.write(String.format("vt %f %f\n", texCoord.x, texCoord.y));
                }

                for (Vector3f normal : normals) {
                    writer.write(String.format("vn %f %f %f\n", normal.x, normal.y, normal.z));
                }

                for (Face face : faces) {
                    writer.write("f ");
                    for (int i = 0; i < face.indices.length; i++) {
                        writer.write(String.format("%d/%d/%d",
                                face.indices[i] + 1 + vertexOffset,
                                face.texIndices[i] + 1 + texCoordOffset,
                                face.normalIndices[i] + 1 + normalOffset));
                        if (i < face.indices.length - 1) {
                            writer.write(" ");
                        }
                    }
                    writer.write("\n");
                }

                vertexOffset += vertices.size();
                texCoordOffset += texCoords.size();
                normalOffset += normals.size();
            }
        }
    }

    public void exportModelToOBJ(Spatial model, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            List<Vector3f> vertices = new ArrayList<>();
            List<Vector3f> normals = new ArrayList<>();
            List<Vector2f> texCoords = new ArrayList<>();
            List<Face> faces = new ArrayList<>();

            traverseModel(model, vertices, normals, texCoords, faces);

            Matrix4f transform = getWorldTransform(model);
            for (Vector3f vertex : vertices) {
                Vector3f transformedVertex = transform.mult(vertex);
                vertex.set(transformedVertex.x, transformedVertex.y, transformedVertex.z);
            }
            for (Vector3f normal : normals) {
                Vector3f transformedNormal = transform.mult(normal).normalize();
                normal.set(transformedNormal.x, transformedNormal.y, transformedNormal.z);
            }

            for (Vector3f vertex : vertices) {
                writer.write(String.format("v %f %f %f\n", vertex.x, vertex.y, vertex.z));
            }

            for (Vector2f texCoord : texCoords) {
                writer.write(String.format("vt %f %f\n", texCoord.x, texCoord.y));
            }

            for (Vector3f normal : normals) {
                writer.write(String.format("vn %f %f %f\n", normal.x, normal.y, normal.z));
            }

            for (Face face : faces) {
                writer.write("f ");
                for (int i = 0; i < face.indices.length; i++) {
                    writer.write(String.format("%d/%d/%d",
                            face.indices[i] + 1,
                            face.texIndices[i] + 1,
                            face.normalIndices[i] + 1));
                    if (i < face.indices.length - 1) {
                        writer.write(" ");
                    }
                }
                writer.write("\n");
            }
        }
    }

    private void traverseModel(Spatial spatial, List<Vector3f> vertices, List<Vector3f> normals, List<Vector2f> texCoords, List<Face> faces) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                traverseModel(child, vertices, normals, texCoords, faces);
            }
        } else if (spatial instanceof Geometry) {
            Geometry geometry = (Geometry) spatial;
            Mesh mesh = geometry.getMesh();

            FloatBuffer vertexBuffer = mesh.getFloatBuffer(VertexBuffer.Type.Position);
            FloatBuffer normalBuffer = mesh.getFloatBuffer(VertexBuffer.Type.Normal);
            FloatBuffer texCoordBuffer = mesh.getFloatBuffer(VertexBuffer.Type.TexCoord);

            int vertexCount = mesh.getVertexCount();
            int[] vertexIndices = new int[vertexCount];
            int[] normalIndices = new int[vertexCount];
            int[] texCoordIndices = new int[vertexCount];

            for (int i = 0; i < vertexCount; i++) {
                Vector3f vertex = new Vector3f(
                        vertexBuffer.get(i * 3),
                        vertexBuffer.get(i * 3 + 1),
                        vertexBuffer.get(i * 3 + 2)
                );
                vertices.add(vertex);
                vertexIndices[i] = vertices.size() - 1;

                if (normalBuffer != null) {
                    Vector3f normal = new Vector3f(
                            normalBuffer.get(i * 3),
                            normalBuffer.get(i * 3 + 1),
                            normalBuffer.get(i * 3 + 2)
                    );
                    normals.add(normal);
                    normalIndices[i] = normals.size() - 1;
                }

                if (texCoordBuffer != null) {
                    Vector2f texCoord = new Vector2f(
                            texCoordBuffer.get(i * 2),
                            texCoordBuffer.get(i * 2 + 1)
                    );
                    texCoords.add(texCoord);
                    texCoordIndices[i] = texCoords.size() - 1;
                }
            }

            IndexBuffer indexBuffer = mesh.getIndicesAsList();
            if (indexBuffer != null) {
                int numIndices = indexBuffer.size();
                for (int i = 0; i < numIndices; i += 3) {
                    int v1 = indexBuffer.get(i);
                    int v2 = indexBuffer.get(i + 1);
                    int v3 = indexBuffer.get(i + 2);

                    Face face = new Face(
                            new int[]{vertexIndices[v1], vertexIndices[v2], vertexIndices[v3]},
                            new int[]{texCoordIndices[v1], texCoordIndices[v2], texCoordIndices[v3]},
                            new int[]{normalIndices[v1], normalIndices[v2], normalIndices[v3]}
                    );
                    faces.add(face);
                }
            }
        }
    }

    private Matrix4f getWorldTransform(Spatial spatial) {
        Matrix4f transform = new Matrix4f();
        transform.setTranslation(spatial.getWorldTranslation());
        transform.setRotationQuaternion(spatial.getWorldRotation());
        transform.setScale(spatial.getWorldScale());
        return transform;
    }

    private static class Face {
        int[] indices;
        int[] texIndices;
        int[] normalIndices;

        Face(int[] indices, int[] texIndices, int[] normalIndices) {
            this.indices = indices;
            this.texIndices = texIndices;
            this.normalIndices = normalIndices;
        }
    }
}