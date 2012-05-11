package fi.harism.shaderize;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public class RendererLightning extends Renderer implements
		PrefsSeekBar.Observer {

	private float mAmbientFactor;
	private Context mContext;

	private float mDiffuseFactor;

	private final Shader mShaderLightning = new Shader();
	private float mShininess;
	private float mSpecularFactor;

	@Override
	public void onDestroy() {
		mContext = null;
		mShaderLightning.deleteProgram();
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		fbo.bind();
		fbo.bindTexture(0);

		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glFrontFace(GLES20.GL_CCW);

		mShaderLightning.useProgram();

		int uAmbientFactor = mShaderLightning.getHandle("uAmbientFactor");
		int uDiffuseFactor = mShaderLightning.getHandle("uDiffuseFactor");
		int uSpecularFactor = mShaderLightning.getHandle("uSpecularFactor");
		int uShininess = mShaderLightning.getHandle("uShininess");

		GLES20.glUniform1f(uAmbientFactor, mAmbientFactor);
		GLES20.glUniform1f(uDiffuseFactor, mDiffuseFactor);
		GLES20.glUniform1f(uSpecularFactor, mSpecularFactor);
		GLES20.glUniform1f(uShininess, mShininess * 16f);

		int uModelViewProjM = mShaderLightning.getHandle("uModelViewProjM");
		int uModelViewM = mShaderLightning.getHandle("uModelViewM");
		int uNormalM = mShaderLightning.getHandle("uNormalM");

		int aPosition = mShaderLightning.getHandle("aPosition");
		int aNormal = mShaderLightning.getHandle("aNormal");
		int aColor = mShaderLightning.getHandle("aColor");

		Vector<Obj> objs = scene.getObjs();
		for (Obj obj : objs) {
			GLES20.glUniformMatrix4fv(uModelViewProjM, 1, false,
					obj.getModelViewProjM(), 0);
			GLES20.glUniformMatrix4fv(uModelViewM, 1, false,
					obj.getModelViewM(), 0);
			GLES20.glUniformMatrix4fv(uNormalM, 1, false, obj.getNormalM(), 0);

			Utils.renderObj(obj, aPosition, aNormal, aColor);
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
	}

	@Override
	public void onSeekBarChanged(int key, float value) {
		switch (key) {
		case R.string.prefs_key_lightning_ambient_factor:
			mAmbientFactor = value;
			break;
		case R.string.prefs_key_lightning_diffuse_factor:
			mDiffuseFactor = value;
		case R.string.prefs_key_lightning_specular_factor:
			mSpecularFactor = value;
		case R.string.prefs_key_lightning_shininess:
			mShininess = value;
		}
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.lightning_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.lightning_fs);
		mShaderLightning.setProgram(vertexSource, fragmentSource);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup parent) {
		PrefsSeekBar seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setDefaultValue(30);
		seekBar.setText(R.string.prefs_lightning_ambient_factor);
		seekBar.setPrefs(prefs, R.string.prefs_key_lightning_ambient_factor,
				this);
		parent.addView(seekBar.getView());

		seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setDefaultValue(30);
		seekBar.setText(R.string.prefs_lightning_diffuse_factor);
		seekBar.setPrefs(prefs, R.string.prefs_key_lightning_diffuse_factor,
				this);
		parent.addView(seekBar.getView());

		seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setDefaultValue(30);
		seekBar.setText(R.string.prefs_lightning_specular_factor);
		seekBar.setPrefs(prefs, R.string.prefs_key_lightning_specular_factor,
				this);
		parent.addView(seekBar.getView());

		seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setDefaultValue(50);
		seekBar.setText(R.string.prefs_lightning_shininess);
		seekBar.setPrefs(prefs, R.string.prefs_key_lightning_shininess, this);
		parent.addView(seekBar.getView());
	}

}
