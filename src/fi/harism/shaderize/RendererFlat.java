package fi.harism.shaderize;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public class RendererFlat extends Renderer implements PrefsSeekBar.Observer {

	private Context mContext;
	private float mSaturation;
	private final Shader mShaderFlat = new Shader();

	@Override
	public void onDestroy() {
		mContext = null;
		mShaderFlat.deleteProgram();
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		fbo.bind();
		fbo.bindTexture(0);

		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glFrontFace(GLES20.GL_CCW);

		mShaderFlat.useProgram();
		int uModelViewProjM = mShaderFlat.getHandle("uModelViewProjM");
		int uNormalM = mShaderFlat.getHandle("uNormalM");
		int uSaturation = mShaderFlat.getHandle("uSaturation");
		int aPosition = mShaderFlat.getHandle("aPosition");
		int aNormal = mShaderFlat.getHandle("aNormal");
		int aColor = mShaderFlat.getHandle("aColor");

		GLES20.glUniform1f(uSaturation, mSaturation);

		Vector<Obj> objs = scene.getObjs();
		for (Obj obj : objs) {
			GLES20.glUniformMatrix4fv(uModelViewProjM, 1, false,
					obj.getModelViewProjM(), 0);
			GLES20.glUniformMatrix4fv(uNormalM, 1, false, obj.getNormalM(), 0);
			Utils.renderObj(obj, aPosition, aNormal, aColor);
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
	}

	@Override
	public void onSeekBarChanged(int key, float value) {
		mSaturation = value;
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
	public void setPreferences(SharedPreferences prefs, ViewGroup parent) {
		PrefsSeekBar seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setText(R.string.prefs_flat_saturation);
		seekBar.setPrefs(prefs, R.string.prefs_key_flat_saturation, this);
		parent.addView(seekBar.getView());
	}

}
