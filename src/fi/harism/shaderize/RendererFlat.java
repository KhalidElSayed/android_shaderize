package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.ViewGroup;

public class RendererFlat extends Renderer {

	private Context mContext;
	private final ObjShader mShaderCube = new ObjShader();

	@Override
	public void onDestroy() {
		mContext = null;
		mShaderCube.deleteProgram();
	}

	@Override
	public void onDrawFrame(ObjFbo fbo, ObjScene scene) {
		fbo.bind();
		fbo.bindTexture(0);
		Utils.renderScene(scene, mShaderCube);
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.flat_cube_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.flat_cube_fs);
		mShaderCube.setProgram(vertexSource, fragmentSource);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup parent) {
	}

}
