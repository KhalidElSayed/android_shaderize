package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public class RendererFxaa extends Renderer implements PrefsSeekBar.Observer {

	private Context mContext;
	private final ObjFbo mFboFull = new ObjFbo();
	private final ObjShader mShaderCube = new ObjShader();
	private final ObjShader mShaderPass1 = new ObjShader();

	@Override
	public void onDestroy() {
		mContext = null;
		mShaderCube.deleteProgram();
		mShaderPass1.deleteProgram();
		mFboFull.reset();
	}

	@Override
	public void onDrawFrame(ObjFbo fbo, ObjScene scene) {
		mFboFull.bind();
		mFboFull.bindTexture(0);
		Utils.renderScene(scene, mShaderCube);

		final float N = 0.5f;
		final float rcpOptW = N / fbo.getWidth();
		final float rcpOptH = N / fbo.getHeight();

		final float rcpOpt2W = 2.0f / fbo.getWidth();
		final float rcpOpt2H = 2.0f / fbo.getHeight();

		fbo.bind();
		fbo.bindTexture(0);

		mShaderPass1.useProgram();
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboFull.getTexture(0));
		GLES20.glUniform4f(mShaderPass1.getHandle("uFxaaConsoleRcpFrameOpt"),
				-rcpOptW, -rcpOptH, rcpOptW, rcpOptH);
		GLES20.glUniform4f(mShaderPass1.getHandle("uFxaaConsoleRcpFrameOpt2"),
				-rcpOpt2W, -rcpOpt2H, rcpOpt2W, rcpOpt2H);
		GLES20.glUniform4f(mShaderPass1.getHandle("uFrameSize"),
				mFboFull.getWidth(), mFboFull.getHeight(),
				1f / mFboFull.getWidth(), 1f / mFboFull.getHeight());
		Utils.renderQuad(mShaderPass1.getHandle("aPosition"));
	}

	@Override
	public void onSeekBarChanged(int key, float value) {
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
		mFboFull.init(width, height, 1, true, false);
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.flat_cube_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.flat_cube_fs);
		mShaderCube.setProgram(vertexSource, fragmentSource);
		vertexSource = Utils.loadRawResource(mContext, R.raw.fxaa_pass1_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.fxaa_pass1_fs);
		mShaderPass1.setProgram(vertexSource, fragmentSource);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup parent) {
		/*
		 * PrefsSeekBar seekBar = new PrefsSeekBar(mContext, parent);
		 * seekBar.setText(R.string.prefs_flat_saturation);
		 * seekBar.setPrefs(prefs, R.string.prefs_key_flat_saturation, this);
		 * parent.addView(seekBar.getView());
		 */
	}

}
