package fi.harism.shaderize;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public class RendererFxaa extends Renderer implements PrefsSeekBar.Observer {

	private Context mContext;
	private final Fbo mFboFull = new Fbo();
	private final Shader mShaderPass1 = new Shader();
	private final Shader mShaderScene = new Shader();

	@Override
	public void onDestroy() {
		mContext = null;
		mShaderScene.deleteProgram();
		mShaderPass1.deleteProgram();
		mFboFull.reset();
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		mFboFull.bind();
		mFboFull.bindTexture(0);

		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glFrontFace(GLES20.GL_CCW);

		mShaderScene.useProgram();
		int uModelViewProjM = mShaderScene.getHandle("uModelViewProjM");
		int uNormalM = mShaderScene.getHandle("uNormalM");
		int aPosition = mShaderScene.getHandle("aPosition");
		int aNormal = mShaderScene.getHandle("aNormal");
		int aColor = mShaderScene.getHandle("aColor");

		Vector<Obj> objs = scene.getObjs();
		for (Obj obj : objs) {
			GLES20.glUniformMatrix4fv(uModelViewProjM, 1, false,
					obj.getModelViewProjM(), 0);
			GLES20.glUniformMatrix4fv(uNormalM, 1, false, obj.getNormalM(), 0);
			Utils.renderObj(obj, aPosition, aNormal, aColor);
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);

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
		Utils.renderFullQuad(mShaderPass1.getHandle("aPosition"));
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
		vertexSource = Utils.loadRawResource(mContext, R.raw.fxaa_scene_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.fxaa_scene_fs);
		mShaderScene.setProgram(vertexSource, fragmentSource);
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