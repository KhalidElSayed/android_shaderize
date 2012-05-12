package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.ViewGroup;

public class RendererFlat extends Renderer {

	private Context mContext;
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
		Utils.renderScene(scene, mShaderFlat);
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.flat_scene_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.flat_scene_fs);
		mShaderFlat.setProgram(vertexSource, fragmentSource);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup parent) {
	}

}
