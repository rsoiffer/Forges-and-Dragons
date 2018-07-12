package graphics;

import static engine.Activatable.using;
import java.util.Arrays;
import opengl.BufferObject;
import opengl.ShaderProgram;
import opengl.VertexArrayObject;
import org.joml.Vector2d;
import org.joml.Vector4d;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static util.MathUtils.direction;
import static util.MathUtils.rotate;
import util.Resources;

public class Graphics {

    private static final ShaderProgram colorShader = Resources.loadShaderProgram("color");

    private static final float lineVertices[] = {
        0, 0, 0,
        1, 0, 0
    };
    private static final VertexArrayObject lineVAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, lineVertices);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
        glEnableVertexAttribArray(0);
    });

    public static void drawLine(Vector2d p1, Vector2d p2, Vector4d color) {
        Vector2d delta = p2.sub(p1, new Vector2d());
        colorShader.setUniform("projectionMatrix", Camera.getProjectionMatrix());
        colorShader.setUniform("modelViewMatrix", Camera.camera.getWorldMatrix(p1, direction(delta), delta.length(), delta.length()));
        colorShader.setUniform("color", color);
        using(Arrays.asList(colorShader, lineVAO), () -> {
            glDrawArrays(GL_LINES, 0, 2);
        });
    }

    private static final float rectangleVertices[] = {
        1f, 1f, 0.0f, // top right
        1f, 0f, 0.0f, // bottom right
        0f, 0f, 0.0f, // bottom left
        0f, 1f, 0.0f // top left
    };
    private static final int rectangleIndices[] = {
        0, 1, 3, // first Triangle
        1, 2, 3 // second Triangle
    };
    private static final VertexArrayObject rectangleVAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, rectangleVertices);
        BufferObject ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, rectangleIndices);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
        glEnableVertexAttribArray(0);
    });

    public static void drawRectangle(Vector2d position, double rotation, Vector2d size, Vector4d color) {
        colorShader.setUniform("projectionMatrix", Camera.getProjectionMatrix());
        colorShader.setUniform("modelViewMatrix", Camera.camera.getWorldMatrix(position, rotation, size.x, size.y));
        colorShader.setUniform("color", color);
        using(Arrays.asList(colorShader, rectangleVAO), () -> {
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        });
    }

    public static void drawRectangleOutline(Vector2d position, double rotation, Vector2d size, Vector4d color) {
        Vector2d p1 = position;
        Vector2d p2 = rotate(new Vector2d(size.x, 0), rotation).add(position);
        Vector2d p3 = rotate(size, rotation).add(position);
        Vector2d p4 = rotate(new Vector2d(0, size.y), rotation).add(position);
        drawLine(p1, p2, color);
        drawLine(p2, p3, color);
        drawLine(p3, p4, color);
        drawLine(p4, p1, color);
    }
}
