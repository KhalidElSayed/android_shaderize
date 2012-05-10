package fi.harism.shaderize;

import java.nio.FloatBuffer;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.view.ViewGroup;

public class RendererLightning extends RendererFilter {

	private Context mContext;
	private SharedPreferences mPrefs;

	private final Shader mShaderCopy = new Shader();
	private final Shader mShaderLightning = new Shader();

	@Override
	public void onDestroy() {
		mContext = null;
		mPrefs = null;
		mShaderCopy.deleteProgram();
		mShaderLightning.deleteProgram();
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		fbo.bind();
		fbo.bindTexture(FBO_OUT);

		mShaderCopy.useProgram();

		int uColorFactor = mShaderCopy.getHandle("uColorFactor");
		GLES20.glUniform1f(uColorFactor, 0.3f);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fbo.getTexture(FBO_IN));

		int aPosition = mShaderCopy.getHandle("aPosition");
		renderFullQuad(aPosition);

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

		mShaderLightning.useProgram();
		int uModelViewProjM = mShaderLightning.getHandle("uModelViewProjM");
		int uModelViewM = mShaderLightning.getHandle("uModelViewM");
		int uNormalM = mShaderLightning.getHandle("uNormalM");
		aPosition = mShaderLightning.getHandle("aPosition");
		int aNormal = mShaderLightning.getHandle("aNormal");
		int aColor = mShaderLightning.getHandle("aColor");

		int uSpecularFactor = mShaderLightning.getHandle("uSpecularFactor");
		int uDiffuseFactor = mShaderLightning.getHandle("uDiffuseFactor");

		GLES20.glUniform1f(uSpecularFactor, .7f);
		GLES20.glUniform1f(uDiffuseFactor, .6f);

		Vector<Obj> objs = scene.getObjs();
		for (Obj obj : objs) {
			GLES20.glUniformMatrix4fv(uModelViewProjM, 1, false,
					obj.getModelViewProjM(), 0);
			GLES20.glUniformMatrix4fv(uModelViewM, 1, false,
					obj.getModelViewM(), 0);
			GLES20.glUniformMatrix4fv(uNormalM, 1, false, obj.getNormalM(), 0);

			FloatBuffer vertexBuffer = obj.getBufferVertices();
			vertexBuffer.position(0);
			GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false,
					0, vertexBuffer);
			GLES20.glEnableVertexAttribArray(aPosition);

			FloatBuffer normalBuffer = obj.getBufferNormals();
			normalBuffer.position(0);
			GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0,
					normalBuffer);
			GLES20.glEnableVertexAttribArray(aNormal);

			FloatBuffer colorBuffer = obj.getBufferColors();
			colorBuffer.position(0);
			GLES20.glVertexAttribPointer(aColor, 3, GLES20.GL_FLOAT, false, 0,
					colorBuffer);
			GLES20.glEnableVertexAttribArray(aColor);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
					vertexBuffer.capacity() / 3);
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glDisable(GLES20.GL_BLEND);
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.lightning_copy_vs);
		fragmentSource = Utils.loadRawResource(mContext,
				R.raw.lightning_copy_fs);
		mShaderCopy.setProgram(vertexSource, fragmentSource);
		vertexSource = Utils.loadRawResource(mContext, R.raw.lightning_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.lightning_fs);
		mShaderLightning.setProgram(vertexSource, fragmentSource);
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup prefsView) {
	}

}
