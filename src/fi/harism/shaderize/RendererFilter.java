package fi.harism.shaderize;

import java.nio.ByteBuffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public abstract class RendererFilter {

	public static final int FBO_IN = RendererMain.FBO_IN;
	public static final int FBO_OUT = RendererMain.FBO_OUT;

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

	public abstract void setContext(Context context);

	public abstract void setPreferences(SharedPreferences prefs,
			ViewGroup prefsView);
}
