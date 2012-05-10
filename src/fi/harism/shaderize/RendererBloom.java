package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public class RendererBloom extends RendererFilter {

	private Context mContext;

	private Fbo mFboQuarter = new Fbo();
	private final Shader mShaderBloom1 = new Shader();
	private final Shader mShaderBloom2 = new Shader();
	private final Shader mShaderBloom3 = new Shader();

	@Override
	public void onDestroy() {
		mContext = null;
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);

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
		int aPosition = mShaderBloom1.getHandle("aPosition");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fbo.getTexture(FBO_IN));
		renderFullQuad(aPosition);

		/**
		 * Second pass, blur texture horizontally.
		 */
		mFboQuarter.bindTexture(1);
		mShaderBloom2.useProgram();
		aPosition = mShaderBloom2.getHandle("aPosition");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(0));
		GLES20.glUniform3f(mShaderBloom2.getHandle("uIncrementalGaussian"),
				(float) incrementalGaussian1, (float) incrementalGaussian2,
				(float) incrementalGaussian3);
		GLES20.glUniform1f(mShaderBloom2.getHandle("uNumBlurPixelsPerSide"),
				numBlurPixelsPerSide);
		GLES20.glUniform2f(mShaderBloom2.getHandle("uBlurOffset"), blurSizeH,
				0f);
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
		aPosition = mShaderBloom3.getHandle("aPosition");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboQuarter.getTexture(0));
		GLES20.glUniform1i(mShaderBloom3.getHandle("sTextureBloom"), 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fbo.getTexture(FBO_IN));
		GLES20.glUniform1i(mShaderBloom3.getHandle("sTextureSource"), 1);
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
	}

}
