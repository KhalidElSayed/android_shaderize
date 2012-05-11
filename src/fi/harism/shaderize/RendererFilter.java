package fi.harism.shaderize;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public abstract class RendererFilter {

	private ByteBuffer mFullQuadVertices;

	public boolean mInitilized = false;

	public RendererFilter() {
		// Create full scene quad buffer.
		final byte FULL_QUAD_COORDS[] = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mFullQuadVertices = ByteBuffer.allocateDirect(4 * 2);
		mFullQuadVertices.put(FULL_QUAD_COORDS).position(0);
	}

	public abstract void onDestroy();

	public abstract void onDrawFrame(Fbo fbo, ObjScene scene);

	public abstract void onSurfaceChanged(int width, int height)
			throws Exception;

	public abstract void onSurfaceCreated() throws Exception;

	protected final void renderFullQuad(int aPosition) {
		GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0,
				mFullQuadVertices);
		GLES20.glEnableVertexAttribArray(aPosition);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	protected void renderScene(Obj obj, int aPosition, int aNormal, int aColor) {
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

	public abstract void setContext(Context context);

	public abstract void setPreferences(SharedPreferences prefs,
			ViewGroup prefsView);
}
