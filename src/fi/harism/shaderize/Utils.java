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

	public static final void renderFullQuad(int aPosition) {
		GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0,
				mFullQuadVertices);
		GLES20.glEnableVertexAttribArray(aPosition);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	public static final void renderObj(Obj obj, int aPosition, int aNormal,
			int aColor) {
		FloatBuffer vertexBuffer = obj.getBufferVertices();
		vertexBuffer.position(0);
		GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0,
				vertexBuffer);
		GLES20.glEnableVertexAttribArray(aPosition);

		FloatBuffer normalBuffer = obj.getBufferNormals();
		normalBuffer.position(0);
		GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0,
				normalBuffer);
		GLES20.glEnableVertexAttribArray(aNormal);

		FloatBuffer colorBuffer = obj.getBufferColors();
		colorBuffer.position(0);
		GLES20.glVertexAttribPointer(aColor, 3, GLES20.GL_FLOAT, false, 0,
				colorBuffer);
		GLES20.glEnableVertexAttribArray(aColor);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffer.capacity() / 3);
	}

}
