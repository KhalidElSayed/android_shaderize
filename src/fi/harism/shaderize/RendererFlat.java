package fi.harism.shaderize;

import android.content.Context;
import android.opengl.GLES20;

public class RendererFlat extends RendererFilter {

	private final Shader mShaderCopy = new Shader();

	@Override
	public void onDestroy() {
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		fbo.bind();
		fbo.bindTexture(FBO_OUT);

		mShaderCopy.useProgram();
		int aPosition = mShaderCopy.getHandle("aPosition");

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fbo.getTexture(FBO_IN));

		renderFullQuad(aPosition);
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
	}

	@Override
	public void onSurfaceCreated(Context context) throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(context, R.raw.copy_vs);
		fragmentSource = Utils.loadRawResource(context, R.raw.copy_fs);
		mShaderCopy.setProgram(vertexSource, fragmentSource);
	}

}
