package fi.harism.shaderize;

import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;

public class RendererFlat implements RendererChild {

	private Shader mShaderFlat = new Shader();

	@Override
	public void onDestroy() {
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		fbo.bind();
		fbo.bindTexture(0);
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glDisable(GLES20.GL_STENCIL_TEST);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glFrontFace(GLES20.GL_CCW);

		mShaderFlat.useProgram();
		int uModelViewProjM = mShaderFlat.getHandle("uModelViewProjM");
		int uNormalM = mShaderFlat.getHandle("uNormalM");
		int aPosition = mShaderFlat.getHandle("aPosition");
		int aNormal = mShaderFlat.getHandle("aNormal");
		int aColor = mShaderFlat.getHandle("aColor");

		for (Obj obj : scene.getBoxes()) {
			GLES20.glUniformMatrix4fv(uModelViewProjM, 1, false,
					obj.getModelViewProjM(), 0);
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

			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
		}
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
	}

	@Override
	public void onSurfaceCreated(Context context) throws Exception {
		String vs = Utils.loadRawResource(context, R.raw.flat_vs);
		String fs = Utils.loadRawResource(context, R.raw.flat_fs);
		mShaderFlat.setProgram(vs, fs);
	}

}
