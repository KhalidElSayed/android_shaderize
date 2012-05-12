package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.LayoutInflater;
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
		mShaderLightning.useProgram();

		int uAmbientFactor = mShaderLightning.getHandle("uAmbientFactor");
		int uDiffuseFactor = mShaderLightning.getHandle("uDiffuseFactor");
		int uSpecularFactor = mShaderLightning.getHandle("uSpecularFactor");
		int uShininess = mShaderLightning.getHandle("uShininess");

		GLES20.glUniform1f(uAmbientFactor, mAmbientFactor);
		GLES20.glUniform1f(uDiffuseFactor, mDiffuseFactor);
		GLES20.glUniform1f(uSpecularFactor, mSpecularFactor);
		GLES20.glUniform1f(uShininess, mShininess * 16f);

		Utils.renderScene(scene, mShaderLightning);
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
		vertexSource = Utils
				.loadRawResource(mContext, R.raw.lightning_scene_vs);
		fragmentSource = Utils.loadRawResource(mContext,
				R.raw.lightning_scene_fs);
		mShaderLightning.setProgram(vertexSource, fragmentSource);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(mContext);

		PrefsSeekBar seekBar;
		seekBar = (PrefsSeekBar) inflater.inflate(R.layout.prefs_seekbar,
				parent, false);
		seekBar.setDefaultValue(30);
		seekBar.setText(R.string.prefs_lightning_ambient_factor);
		seekBar.setPrefs(prefs, R.string.prefs_key_lightning_ambient_factor,
				this);
		parent.addView(seekBar);

		seekBar = (PrefsSeekBar) inflater.inflate(R.layout.prefs_seekbar,
				parent, false);
		seekBar.setDefaultValue(30);
		seekBar.setText(R.string.prefs_lightning_diffuse_factor);
		seekBar.setPrefs(prefs, R.string.prefs_key_lightning_diffuse_factor,
				this);
		parent.addView(seekBar);

		seekBar = (PrefsSeekBar) inflater.inflate(R.layout.prefs_seekbar,
				parent, false);
		seekBar.setDefaultValue(30);
		seekBar.setText(R.string.prefs_lightning_specular_factor);
		seekBar.setPrefs(prefs, R.string.prefs_key_lightning_specular_factor,
				this);
		parent.addView(seekBar);

		seekBar = (PrefsSeekBar) inflater.inflate(R.layout.prefs_seekbar,
				parent, false);
		seekBar.setDefaultValue(50);
		seekBar.setText(R.string.prefs_lightning_shininess);
		seekBar.setPrefs(prefs, R.string.prefs_key_lightning_shininess, this);
		parent.addView(seekBar);
	}

}
