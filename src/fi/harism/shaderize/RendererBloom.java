package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class RendererBloom extends RendererFilter {

	private float mBloomSaturation, mBloomIntensity;
	private Context mContext;

	private Fbo mFboQuarter = new Fbo();

	private SharedPreferences mPrefs;

	private final Shader mShaderBloom1 = new Shader();
	private final Shader mShaderBloom2 = new Shader();
	private final Shader mShaderBloom3 = new Shader();

	private float mSourceSaturation, mSourceIntensity;
	private float mThreshold;

	@Override
	public void onDestroy() {
		mContext = null;
		mPrefs = null;
		mFboQuarter.reset();
		mShaderBloom1.deleteProgram();
		mShaderBloom2.deleteProgram();
		mShaderBloom3.deleteProgram();
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {

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
		mShaderBloom1.useProgram();

		int uThreshold = mShaderBloom1.getHandle("uThreshold");
		GLES20.glUniform1f(uThreshold, mThreshold);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fbo.getTexture(FBO_IN));

		int aPosition = mShaderBloom1.getHandle("aPosition");
		renderFullQuad(aPosition);

		/**
		 * Second pass, blur texture horizontally.
		 */
		mFboQuarter.bindTexture(1);
		mShaderBloom2.useProgram();

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(0));
		GLES20.glUniform3f(mShaderBloom2.getHandle("uIncrementalGaussian"),
				(float) incrementalGaussian1, (float) incrementalGaussian2,
				(float) incrementalGaussian3);
		GLES20.glUniform1f(mShaderBloom2.getHandle("uNumBlurPixelsPerSide"),
				numBlurPixelsPerSide);
		GLES20.glUniform2f(mShaderBloom2.getHandle("uBlurOffset"), blurSizeH,
				0f);

		aPosition = mShaderBloom2.getHandle("aPosition");
		renderFullQuad(aPosition);

		/**
		 * Third pass, blur texture vertically.
		 */
		mFboQuarter.bindTexture(0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(1));
		GLES20.glUniform2f(mShaderBloom2.getHandle("uBlurOffset"), 0f,
				blurSizeV);

		renderFullQuad(aPosition);

		/**
		 * Fourth pass, combine source texture and calculated bloom texture into
		 * output texture.
		 */
		fbo.bind();
		fbo.bindTexture(FBO_OUT);
		mShaderBloom3.useProgram();

		int uBloomSaturation = mShaderBloom3.getHandle("uBloomSaturation");
		int uBloomIntensity = mShaderBloom3.getHandle("uBloomIntensity");
		int uSourceSaturation = mShaderBloom3.getHandle("uSourceSaturation");
		int uSourceIntensity = mShaderBloom3.getHandle("uSourceIntensity");
		GLES20.glUniform1f(uBloomSaturation, mBloomSaturation);
		GLES20.glUniform1f(uBloomIntensity, mBloomIntensity);
		GLES20.glUniform1f(uSourceSaturation, mSourceSaturation);
		GLES20.glUniform1f(uSourceIntensity, mSourceIntensity);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(0));
		GLES20.glUniform1i(mShaderBloom3.getHandle("sTextureBloom"), 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fbo.getTexture(FBO_IN));
		GLES20.glUniform1i(mShaderBloom3.getHandle("sTextureSource"), 1);

		aPosition = mShaderBloom3.getHandle("aPosition");
		renderFullQuad(aPosition);
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
		mFboQuarter.init(width / 4, height / 4, 2);
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vs = Utils.loadRawResource(mContext, R.raw.bloom_vs);
		String fs = Utils.loadRawResource(mContext, R.raw.bloom_pass1_fs);
		mShaderBloom1.setProgram(vs, fs);
		fs = Utils.loadRawResource(mContext, R.raw.bloom_pass2_fs);
		mShaderBloom2.setProgram(vs, fs);
		fs = Utils.loadRawResource(mContext, R.raw.bloom_pass3_fs);
		mShaderBloom3.setProgram(vs, fs);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup prefsView) {
		mPrefs = prefs;
		mThreshold = mPrefs.getInt(
				mContext.getString(R.string.prefs_key_bloom_threshold), 40);
		mBloomSaturation = mPrefs.getInt(
				mContext.getString(R.string.prefs_key_bloom_bloom_saturation),
				100);
		mBloomIntensity = mPrefs.getInt(
				mContext.getString(R.string.prefs_key_bloom_bloom_intensity),
				130);
		mSourceSaturation = mPrefs.getInt(
				mContext.getString(R.string.prefs_key_bloom_source_saturation),
				100);
		mSourceIntensity = mPrefs.getInt(
				mContext.getString(R.string.prefs_key_bloom_source_intensity),
				100);

		SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				switch (seekBar.getId()) {
				case R.id.prefs_bloom_threshold:
					mThreshold = (float) progress / seekBar.getMax();
					mPrefs.edit()
							.putInt(mContext
									.getString(R.string.prefs_key_bloom_threshold),
									progress).commit();
					break;
				case R.id.prefs_bloom_bloom_saturation:
					mBloomSaturation = (float) progress / seekBar.getMax();
					mPrefs.edit()
							.putInt(mContext
									.getString(R.string.prefs_key_bloom_bloom_saturation),
									progress).commit();
					break;
				case R.id.prefs_bloom_bloom_intensity:
					mBloomIntensity = (float) progress
							/ (seekBar.getMax() >> 1);
					mPrefs.edit()
							.putInt(mContext
									.getString(R.string.prefs_key_bloom_bloom_intensity),
									progress).commit();
					break;
				case R.id.prefs_bloom_source_saturation:
					mSourceSaturation = (float) progress / seekBar.getMax();
					mPrefs.edit()
							.putInt(mContext
									.getString(R.string.prefs_key_bloom_source_saturation),
									progress).commit();
					break;
				case R.id.prefs_bloom_source_intensity:
					mSourceIntensity = (float) progress
							/ (seekBar.getMax() >> 1);
					mPrefs.edit()
							.putInt(mContext
									.getString(R.string.prefs_key_bloom_source_intensity),
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
				.findViewById(R.id.prefs_bloom_threshold);
		seekBar.setProgress((int) mThreshold);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		mThreshold /= seekBar.getMax();

		seekBar = (SeekBar) prefsView
				.findViewById(R.id.prefs_bloom_bloom_saturation);
		seekBar.setProgress((int) mBloomSaturation);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		mBloomSaturation /= seekBar.getMax();

		seekBar = (SeekBar) prefsView
				.findViewById(R.id.prefs_bloom_bloom_intensity);
		seekBar.setProgress((int) mBloomIntensity);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		mBloomIntensity /= seekBar.getMax() >> 1;

		seekBar = (SeekBar) prefsView
				.findViewById(R.id.prefs_bloom_source_saturation);
		seekBar.setProgress((int) mSourceSaturation);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		mSourceSaturation /= seekBar.getMax();

		seekBar = (SeekBar) prefsView
				.findViewById(R.id.prefs_bloom_source_intensity);
		seekBar.setProgress((int) mSourceIntensity);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		mSourceIntensity /= seekBar.getMax() >> 1;
	}

}
