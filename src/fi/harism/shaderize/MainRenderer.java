package fi.harism.shaderize;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;

public class MainRenderer implements GLSurfaceView.Renderer {

	private float mFrameRate;
	private long mLastRenderTime;

	public float getFramesPerSecond() {
		return 1000f / mFrameRate;
	}

	@Override
	public void onDrawFrame(GL10 gl) {

		long currentTime = SystemClock.uptimeMillis();
		mFrameRate = mFrameRate * 0.4f + (currentTime - mLastRenderTime) * 0.6f;
		mLastRenderTime = currentTime;

		GLES20.glClearColor(.5f, .2f, .8f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// GLES20.glViewport(0, 150, width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig configd) {
	}

}
