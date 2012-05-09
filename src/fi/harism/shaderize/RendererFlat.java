package fi.harism.shaderize;

import android.opengl.GLES20;

public class RendererFlat implements RendererChild {

	@Override
	public void onDestroy() {
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		fbo.bind();
		fbo.bindTexture(0);
		GLES20.glClearColor(.5f, .2f, .8f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
	}

	@Override
	public void onSurfaceCreated() {
	}

}
