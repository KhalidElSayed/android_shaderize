package fi.harism.shaderize;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class RendererLightning extends RendererFilter {

	private float mAmbientFactor;
	private Context mContext;

	private float mDiffuseFactor;

	private SharedPreferences mPrefs;
	private final Shader mShaderLightning = new Shader();
	private float mShininess;
	private float mSpecularFactor;

	@Override
	public void onDestroy() {
		mContext = null;
		mPrefs = null;
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

			renderScene(obj, aPosition, aNormal, aColor);
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
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
	public void setPreferences(SharedPreferences prefs, ViewGroup prefsView) {
		mPrefs = prefs;
		mAmbientFactor = mPrefs
				.getInt(mContext
						.getString(R.string.prefs_key_lightning_ambient_factor),
						30);
		mDiffuseFactor = mPrefs
				.getInt(mContext
						.getString(R.string.prefs_key_lightning_diffuse_factor),
						30);
		mSpecularFactor = mPrefs.getInt(mContext
				.getString(R.string.prefs_key_lightning_specular_factor), 30);
		mShininess = mPrefs.getInt(
				mContext.getString(R.string.prefs_key_lightning_shininess), 50);

		SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				switch (seekBar.getId()) {
				case R.id.prefs_lightning_ambient_factor:
					mAmbientFactor = (float) progress / seekBar.getMax();
					mPrefs.edit()
							.putInt(mContext
									.getString(R.string.prefs_key_lightning_ambient_factor),
									progress).commit();
					break;
				case R.id.prefs_lightning_diffuse_factor:
					mDiffuseFactor = (float) progress / seekBar.getMax();
					mPrefs.edit()
							.putInt(mContext
									.getString(R.string.prefs_key_lightning_diffuse_factor),
									progress).commit();
					break;
				case R.id.prefs_lightning_specular_factor:
					mSpecularFactor = (float) progress / seekBar.getMax();
					mPrefs.edit()
							.putInt(mContext
									.getString(R.string.prefs_key_lightning_specular_factor),
									progress).commit();
					break;
				case R.id.prefs_lightning_shininess:
					mShininess = (float) progress / seekBar.getMax();
					mPrefs.edit()
							.putInt(mContext
									.getString(R.string.prefs_key_lightning_shininess),
									progress).commit();
					break;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		};

		SeekBar seekBar = (SeekBar) prefsView
				.findViewById(R.id.prefs_lightning_ambient_factor);
		seekBar.setProgress((int) mAmbientFactor);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		mAmbientFactor /= seekBar.getMax();

		seekBar = (SeekBar) prefsView
				.findViewById(R.id.prefs_lightning_diffuse_factor);
		seekBar.setProgress((int) mDiffuseFactor);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		mDiffuseFactor /= seekBar.getMax();

		seekBar = (SeekBar) prefsView
				.findViewById(R.id.prefs_lightning_specular_factor);
		seekBar.setProgress((int) mSpecularFactor);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		mSpecularFactor /= seekBar.getMax();

		seekBar = (SeekBar) prefsView
				.findViewById(R.id.prefs_lightning_shininess);
		seekBar.setProgress((int) mShininess);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		mShininess /= seekBar.getMax();
	}

}
