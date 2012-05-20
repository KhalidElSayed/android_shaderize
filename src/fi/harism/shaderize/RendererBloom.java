package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class RendererBloom extends Renderer implements PrefsSeekBar.Observer {

	private Context mContext;
	private final ObjFbo mFboFull = new ObjFbo();

	private final ObjFbo mFboQuarter = new ObjFbo();
	private float mRadius;

	private final ObjShader mShaderCube = new ObjShader();
	private final ObjShader mShaderPass1 = new ObjShader();
	private final ObjShader mShaderPass2 = new ObjShader();
	private final ObjShader mShaderPass3 = new ObjShader();

	private float mSourceIntensity, mBloomIntensity;
	private float mThreshold;

	@Override
	public void onDestroy() {
		mContext = null;
		mFboFull.reset();
		mFboQuarter.reset();
		mShaderCube.deleteProgram();
		mShaderPass1.deleteProgram();
		mShaderPass2.deleteProgram();
		mShaderPass3.deleteProgram();
	}

	@Override
	public void onDrawFrame(ObjFbo fbo, ObjScene scene) {
		mFboFull.bind();
		mFboFull.bindTexture(0);
		Utils.renderScene(scene, mShaderCube);

		/**
		 * Instantiate variables for bloom filter.
		 */

		// Pixel sizes.
		float blurSizeH = 1f / mFboQuarter.getWidth();
		float blurSizeV = 1f / mFboQuarter.getHeight();

		// Calculate number of pixels from relative size.
		int numBlurPixelsPerSide = (int) (mRadius * Math.min(
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
		mShaderPass1.useProgram();

		int uThreshold = mShaderPass1.getHandle("uThreshold");
		GLES20.glUniform1f(uThreshold, mThreshold);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboFull.getTexture(0));

		Utils.renderQuad(mShaderPass1.getHandle("aPosition"));

		/**
		 * Second pass, blur texture horizontally.
		 */
		mFboQuarter.bindTexture(1);
		mShaderPass2.useProgram();

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(0));
		GLES20.glUniform3f(mShaderPass2.getHandle("uIncrementalGaussian"),
				(float) incrementalGaussian1, (float) incrementalGaussian2,
				(float) incrementalGaussian3);
		GLES20.glUniform1f(mShaderPass2.getHandle("uNumBlurPixelsPerSide"),
				numBlurPixelsPerSide);
		GLES20.glUniform2f(mShaderPass2.getHandle("uBlurOffset"), blurSizeH, 0f);

		Utils.renderQuad(mShaderPass2.getHandle("aPosition"));

		/**
		 * Third pass, blur texture vertically.
		 */
		mFboQuarter.bindTexture(0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(1));
		GLES20.glUniform2f(mShaderPass2.getHandle("uBlurOffset"), 0f, blurSizeV);

		Utils.renderQuad(mShaderPass2.getHandle("aPosition"));

		/**
		 * Fourth pass, combine source texture and calculated bloom texture into
		 * output texture.
		 */
		fbo.bind();
		fbo.bindTexture(0);
		mShaderPass3.useProgram();

		int uBloomIntensity = mShaderPass3.getHandle("uBloomIntensity");
		int uSourceIntensity = mShaderPass3.getHandle("uSourceIntensity");
		GLES20.glUniform1f(uBloomIntensity, mBloomIntensity);
		GLES20.glUniform1f(uSourceIntensity, mSourceIntensity);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(0));
		GLES20.glUniform1i(mShaderPass3.getHandle("sTextureBloom"), 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboFull.getTexture(0));
		GLES20.glUniform1i(mShaderPass3.getHandle("sTextureSource"), 1);

		Utils.renderQuad(mShaderPass3.getHandle("aPosition"));
	}

	@Override
	public void onSeekBarChanged(int key, float value) {
		switch (key) {
		case R.string.prefs_key_bloom_radius:
			mRadius = 0.01f + 0.09f * value;
			break;
		case R.string.prefs_key_bloom_threshold:
			mThreshold = value;
			break;
		case R.string.prefs_key_bloom_source_intensity:
			mSourceIntensity = value;
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
		vertexSource = Utils.loadRawResource(mContext, R.raw.flat_cube_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.flat_cube_fs);
		mShaderCube.setProgram(vertexSource, fragmentSource);
		vertexSource = Utils.loadRawResource(mContext, R.raw.bloom_quad_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.bloom_pass1_fs);
		mShaderPass1.setProgram(vertexSource, fragmentSource);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.bloom_pass2_fs);
		mShaderPass2.setProgram(vertexSource, fragmentSource);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.bloom_pass3_fs);
		mShaderPass3.setProgram(vertexSource, fragmentSource);
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
		seekBar.setText(R.string.prefs_bloom_radius);
		seekBar.setPrefs(prefs, R.string.prefs_key_bloom_radius, this);
		parent.addView(seekBar);

		seekBar = (PrefsSeekBar) inflater.inflate(R.layout.prefs_seekbar,
				parent, false);
		seekBar.setDefaultValue(30);
		seekBar.setText(R.string.prefs_bloom_threshold);
		seekBar.setPrefs(prefs, R.string.prefs_key_bloom_threshold, this);
		parent.addView(seekBar);

		seekBar = (PrefsSeekBar) inflater.inflate(R.layout.prefs_seekbar,
				parent, false);
		seekBar.setDefaultValue(100);
		seekBar.setText(R.string.prefs_bloom_source_intensity);
		seekBar.setPrefs(prefs, R.string.prefs_key_bloom_source_intensity, this);
		parent.addView(seekBar);

		seekBar = (PrefsSeekBar) inflater.inflate(R.layout.prefs_seekbar,
				parent, false);
		seekBar.setDefaultValue(130);
		seekBar.setText(R.string.prefs_bloom_bloom_intensity);
		seekBar.setPrefs(prefs, R.string.prefs_key_bloom_bloom_intensity, this);
		parent.addView(seekBar);
	}
}
