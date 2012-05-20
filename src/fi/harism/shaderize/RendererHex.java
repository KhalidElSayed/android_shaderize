package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class RendererHex extends Renderer implements PrefsSeekBar.Observer {

	private Context mContext;
	private final ObjFbo mFboFull = new ObjFbo();
	private float mRadius = 1f;
	private final ObjShader mShaderCube = new ObjShader();
	private final ObjShader mShaderHex = new ObjShader();

	@Override
	public void onDestroy() {
		mContext = null;
		mShaderCube.deleteProgram();
		mFboFull.reset();
	}

	@Override
	public void onDrawFrame(ObjFbo fbo, ObjScene scene) {
		mFboFull.bind();
		mFboFull.bindTexture(0);
		Utils.renderScene(scene, mShaderCube);

		fbo.bind();
		fbo.bindTexture(0);
		mShaderHex.useProgram();

		int uRadius = mShaderHex.getHandle("uRadius");
		int uTextureSize = mShaderHex.getHandle("uTextureSize");

		GLES20.glUniform1f(uRadius, mRadius);
		GLES20.glUniform2f(uTextureSize, mFboFull.getWidth(),
				mFboFull.getHeight());

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboFull.getTexture(0));

		Utils.renderQuad(mShaderHex.getHandle("aPosition"));
	}

	@Override
	public void onSeekBarChanged(int key, float value) {
		switch (key) {
		case R.string.prefs_key_hex_radius:
			mRadius = 1f + 99f * value;
			break;
		}
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
		mFboFull.init(width, height, 1);
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.flat_cube_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.flat_cube_fs);
		mShaderCube.setProgram(vertexSource, fragmentSource);
		vertexSource = Utils.loadRawResource(mContext, R.raw.hex_quad_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.hex_pass1_fs);
		mShaderHex.setProgram(vertexSource, fragmentSource);
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
		seekBar.setDefaultValue(20);
		seekBar.setText(R.string.prefs_hex_radius);
		seekBar.setPrefs(prefs, R.string.prefs_key_hex_radius, this);
		parent.addView(seekBar);
	}

}
