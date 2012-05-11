package fi.harism.shaderize;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public class RendererBloom extends RendererFilter implements
		PrefsSeekBar.Observer {

	private float mBloomSaturation, mBloomIntensity;
	private Context mContext;

	private final Fbo mFboFull = new Fbo();
	private final Fbo mFboQuarter = new Fbo();

	private final Shader mShaderBloomPass1 = new Shader();
	private final Shader mShaderBloomPass2 = new Shader();
	private final Shader mShaderBloomPass3 = new Shader();
	private final Shader mShaderBloomScene = new Shader();

	private float mSourceSaturation, mSourceIntensity;
	private float mThreshold;

	@Override
	public void onDestroy() {
		mContext = null;
		mFboFull.reset();
		mFboQuarter.reset();
		mShaderBloomScene.deleteProgram();
		mShaderBloomPass1.deleteProgram();
		mShaderBloomPass2.deleteProgram();
		mShaderBloomPass3.deleteProgram();
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

		mShaderBloomScene.useProgram();

		int uModelViewProjM = mShaderBloomScene.getHandle("uModelViewProjM");
		int uNormalM = mShaderBloomScene.getHandle("uNormalM");

		int aPosition = mShaderBloomScene.getHandle("aPosition");
		int aNormal = mShaderBloomScene.getHandle("aNormal");
		int aColor = mShaderBloomScene.getHandle("aColor");

		Vector<Obj> objs = scene.getObjs();
		for (Obj obj : objs) {
			GLES20.glUniformMatrix4fv(uModelViewProjM, 1, false,
					obj.getModelViewProjM(), 0);
			GLES20.glUniformMatrix4fv(uNormalM, 1, false, obj.getNormalM(), 0);
			Utils.renderObj(obj, aPosition, aNormal, aColor);
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);

		/**
		 * Instantiate variables for bloom filter.
		 */

		// Pixel sizes.
		float blurSizeH = 1f / mFboQuarter.getWidth();
		float blurSizeV = 1f / mFboQuarter.getHeight();

		// Calculate number of pixels from relative size.
		int numBlurPixelsPerSide = (int) (0.05f * Math.min(
				mFboQuarter.getWidth(), mFboQuarter.getHeight()));
		if (numBlurPixelsPerSide < 1)
			numBlurPixelsPerSide = 1;
		double sigma = 1.0 + numBlurPixelsPerSide * 0.5;

		// Values needed for incremental gaussian blur.
		double incrementalGaussian1 = 1.0 / (Math.sqrt(2.0 * Math.PI) * sigma);
		double incrementalGaussian2 = Math.exp(-0.5 / (sigma * sigma));
		double incrementalGaussian3 = incrementalGaussian2
				* incrementalGaussian2;

		/**
		 * First pass, store color values exceeding given threshold into blur
		 * texture.
		 */
		mFboQuarter.bind();
		mFboQuarter.bindTexture(0);
		mShaderBloomPass1.useProgram();

		int uThreshold = mShaderBloomPass1.getHandle("uThreshold");
		GLES20.glUniform1f(uThreshold, mThreshold);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboFull.getTexture(0));

		aPosition = mShaderBloomPass1.getHandle("aPosition");
		Utils.renderFullQuad(aPosition);

		/**
		 * Second pass, blur texture horizontally.
		 */
		mFboQuarter.bindTexture(1);
		mShaderBloomPass2.useProgram();

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(0));
		GLES20.glUniform3f(mShaderBloomPass2.getHandle("uIncrementalGaussian"),
				(float) incrementalGaussian1, (float) incrementalGaussian2,
				(float) incrementalGaussian3);
		GLES20.glUniform1f(
				mShaderBloomPass2.getHandle("uNumBlurPixelsPerSide"),
				numBlurPixelsPerSide);
		GLES20.glUniform2f(mShaderBloomPass2.getHandle("uBlurOffset"),
				blurSizeH, 0f);

		aPosition = mShaderBloomPass2.getHandle("aPosition");
		Utils.renderFullQuad(aPosition);

		/**
		 * Third pass, blur texture vertically.
		 */
		mFboQuarter.bindTexture(0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(1));
		GLES20.glUniform2f(mShaderBloomPass2.getHandle("uBlurOffset"), 0f,
				blurSizeV);

		Utils.renderFullQuad(aPosition);

		/**
		 * Fourth pass, combine source texture and calculated bloom texture into
		 * output texture.
		 */
		fbo.bind();
		fbo.bindTexture(0);
		mShaderBloomPass3.useProgram();

		int uBloomSaturation = mShaderBloomPass3.getHandle("uBloomSaturation");
		int uBloomIntensity = mShaderBloomPass3.getHandle("uBloomIntensity");
		int uSourceSaturation = mShaderBloomPass3
				.getHandle("uSourceSaturation");
		int uSourceIntensity = mShaderBloomPass3.getHandle("uSourceIntensity");
		GLES20.glUniform1f(uBloomSaturation, mBloomSaturation);
		GLES20.glUniform1f(uBloomIntensity, mBloomIntensity);
		GLES20.glUniform1f(uSourceSaturation, mSourceSaturation);
		GLES20.glUniform1f(uSourceIntensity, mSourceIntensity);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(0));
		GLES20.glUniform1i(mShaderBloomPass3.getHandle("sTextureBloom"), 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboFull.getTexture(0));
		GLES20.glUniform1i(mShaderBloomPass3.getHandle("sTextureSource"), 1);

		aPosition = mShaderBloomPass3.getHandle("aPosition");
		Utils.renderFullQuad(aPosition);
	}

	@Override
	public void onSeekBarChanged(int key, float value) {
		switch (key) {
		case R.string.prefs_key_bloom_threshold:
			mThreshold = value;
			break;
		case R.string.prefs_key_bloom_source_saturation:
			mSourceSaturation = value;
			break;
		case R.string.prefs_key_bloom_source_intensity:
			mSourceIntensity = value;
			break;
		case R.string.prefs_key_bloom_bloom_saturation:
			mBloomSaturation = value;
			break;
		case R.string.prefs_key_bloom_bloom_intensity:
			mBloomIntensity = value;
			break;
		}
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
		mFboFull.init(width, height, 1, true, false);
		mFboQuarter.init(width / 4, height / 4, 2);
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.bloom_scene_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.bloom_scene_fs);
		mShaderBloomScene.setProgram(vertexSource, fragmentSource);
		vertexSource = Utils.loadRawResource(mContext, R.raw.bloom_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.bloom_pass1_fs);
		mShaderBloomPass1.setProgram(vertexSource, fragmentSource);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.bloom_pass2_fs);
		mShaderBloomPass2.setProgram(vertexSource, fragmentSource);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.bloom_pass3_fs);
		mShaderBloomPass3.setProgram(vertexSource, fragmentSource);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup parent) {
		PrefsSeekBar seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setDefaultValue(30);
		seekBar.setText(R.string.prefs_bloom_threshold);
		seekBar.setPrefs(prefs, R.string.prefs_key_bloom_threshold, this);
		parent.addView(seekBar.getView());

		seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setDefaultValue(100);
		seekBar.setText(R.string.prefs_bloom_source_saturation);
		seekBar.setPrefs(prefs, R.string.prefs_key_bloom_source_saturation,
				this);
		parent.addView(seekBar.getView());

		seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setDefaultValue(100);
		seekBar.setText(R.string.prefs_bloom_source_intensity);
		seekBar.setPrefs(prefs, R.string.prefs_key_bloom_source_intensity, this);
		parent.addView(seekBar.getView());

		seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setDefaultValue(100);
		seekBar.setText(R.string.prefs_bloom_bloom_saturation);
		seekBar.setPrefs(prefs, R.string.prefs_key_bloom_bloom_saturation, this);
		parent.addView(seekBar.getView());

		seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setDefaultValue(130);
		seekBar.setText(R.string.prefs_bloom_bloom_intensity);
		seekBar.setPrefs(prefs, R.string.prefs_key_bloom_bloom_intensity, this);
		parent.addView(seekBar.getView());
	}
}
