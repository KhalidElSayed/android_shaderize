package fi.harism.shaderize;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;

public class Utils {

	private static ByteBuffer mFullQuadVertices;

	static {
		// Create full scene quad buffer.
		final byte FULL_QUAD_COORDS[] = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mFullQuadVertices = ByteBuffer.allocateDirect(4 * 2);
		mFullQuadVertices.put(FULL_QUAD_COORDS).position(0);
	}

	public static final String loadRawResource(Context context, int resourceId)
			throws Exception {
		InputStream is = context.getResources().openRawResource(resourceId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return baos.toString();
	}

	public static final float rand(float min, float max) {
		return min + (float) (Math.random() * (max - min));
	}

	public static final void renderQuad(int aPosition) {
		GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0,
				mFullQuadVertices);
		GLES20.glEnableVertexAttribArray(aPosition);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	public static final void renderScene(ObjScene scene, ObjShader shader) {
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glFrontFace(GLES20.GL_CCW);

		shader.useProgram();

		int uModelViewM = shader.getHandle("uModelViewM");
		int uProjM = shader.getHandle("uProjM");
		int uNormalM = shader.getHandle("uNormalM");

		int uColor = shader.getHandle("uColor");
		int uSaturation = shader.getHandle("uSaturation");

		int aPosition = shader.getHandle("aPosition");
		int aNormal = shader.getHandle("aNormal");
		int aTexPosition = shader.getHandle("aTexPosition");

		for (ObjScene.StructCube structBox : scene.getCubes()) {
			final ObjCube box = structBox.mCube;
			box.calculate();

			GLES20.glUniformMatrix4fv(uModelViewM, 1, false,
					box.getModelViewM(), 0);
			GLES20.glUniformMatrix4fv(uProjM, 1, false, box.getProjM(), 0);
			GLES20.glUniformMatrix4fv(uNormalM, 1, false, box.getNormalM(), 0);

			GLES20.glUniform3fv(uColor, 1, structBox.mColor, 0);
			GLES20.glUniform1f(uSaturation, structBox.mSaturation);

			FloatBuffer vertexBuffer = box.getBufferVertices();
			GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false,
					0, vertexBuffer);
			GLES20.glEnableVertexAttribArray(aPosition);

			FloatBuffer normalBuffer = box.getBufferNormals();
			GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0,
					normalBuffer);
			GLES20.glEnableVertexAttribArray(aNormal);

			FloatBuffer texPositionBuffer = box.getBufferTexPositions();
			GLES20.glVertexAttribPointer(aTexPosition, 2, GLES20.GL_FLOAT,
					false, 0, texPositionBuffer);
			GLES20.glEnableVertexAttribArray(aTexPosition);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
					vertexBuffer.capacity() / 3);
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
	}

}
