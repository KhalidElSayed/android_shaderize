package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public class RendererBumpMap extends Renderer {

	private Context mContext;
	private final Fbo mFboBumpMap = new Fbo();
	private final Shader mShaderBumpMap = new Shader();
	private final Shader mShaderTex = new Shader();

	@Override
	public void onDestroy() {
		mContext = null;
		mShaderBumpMap.deleteProgram();
		mShaderTex.deleteProgram();
		mFboBumpMap.reset();
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		mFboBumpMap.bind();
		mFboBumpMap.bindTexture(0);
		mShaderTex.useProgram();
		Utils.renderQuad(mShaderTex.getHandle("aPosition"));

		fbo.bind();
		fbo.bindTexture(0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboBumpMap.getTexture(0));

		Utils.renderScene(scene, mShaderBumpMap);
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
		int bumpMapSize = Math.min(width, height);
		mFboBumpMap.init(bumpMapSize, bumpMapSize, 1);

	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.bumpmap_scene_vs);
		fragmentSource = Utils
				.loadRawResource(mContext, R.raw.bumpmap_scene_fs);
		mShaderBumpMap.setProgram(vertexSource, fragmentSource);
		vertexSource = Utils.loadRawResource(mContext, R.raw.bumpmap_tex_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.bumpmap_tex_fs);
		mShaderTex.setProgram(vertexSource, fragmentSource);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup parent) {
	}

}
