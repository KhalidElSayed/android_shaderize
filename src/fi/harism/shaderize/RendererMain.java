package fi.harism.shaderize;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.os.SystemClock;

public class RendererMain implements GLSurfaceView.Renderer {

	private float mFrameRate;
	private long mLastRenderTime;
	private RendererChild mShaderRenderer;

	public float getFramesPerSecond() {
		return 1000f / mFrameRate;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		long currentTime = SystemClock.uptimeMillis();
		mFrameRate = mFrameRate * 0.4f + (currentTime - mLastRenderTime) * 0.6f;
		mLastRenderTime = currentTime;

		if (mShaderRenderer != null) {
			mShaderRenderer.onDrawFrame();
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}

	public void setShaderRenderer(RendererChild shaderRenderer) {
		if (mShaderRenderer != null) {
			mShaderRenderer.onDestroy();
		}
		mShaderRenderer = shaderRenderer;
	}

}
