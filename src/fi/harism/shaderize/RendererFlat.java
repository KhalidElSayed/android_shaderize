package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class RendererFlat extends RendererFilter {

	private Context mContext;
	private SharedPreferences mPrefs;
	private float mSaturate;
	private final Shader mShaderFlat = new Shader();

	@Override
	public void onDestroy() {
		mContext = null;
		mPrefs = null;
		mShaderFlat.deleteProgram();
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		fbo.bind();
		fbo.bindTexture(FBO_OUT);

		mShaderFlat.useProgram();
		int aPosition = mShaderFlat.getHandle("aPosition");
		int uSaturate = mShaderFlat.getHandle("uSaturate");

		GLES20.glUniform1f(uSaturate, mSaturate);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fbo.getTexture(FBO_IN));

		renderFullQuad(aPosition);
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.flat_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.flat_fs);
		mShaderFlat.setProgram(vertexSource, fragmentSource);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup prefsView) {
		mPrefs = prefs;
		mSaturate = mPrefs.getInt(
				mContext.getString(R.string.prefs_key_flat_saturate), 0);

		SeekBar seekBar = (SeekBar) prefsView
				.findViewById(R.id.prefs_flat_saturate);
		seekBar.setProgress((int) mSaturate);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mSaturate = (float) progress / seekBar.getMax();
				mPrefs.edit()
						.putInt(mContext
								.getString(R.string.prefs_key_flat_saturate),
								progress).commit();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		mSaturate /= seekBar.getMax();
	}

}
