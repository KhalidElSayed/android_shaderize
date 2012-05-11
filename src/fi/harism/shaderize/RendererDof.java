package fi.harism.shaderize;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.view.ViewGroup;

public class RendererDof extends Renderer implements PrefsSeekBar.Observer {

	private Context mContext;
	
	private final Shader mShaderScene = new Shader();
	private final Shader mShaderIn = new Shader();
	private final Shader mShaderOut = new Shader();
	private final Shader mShaderPass1 = new Shader();
	private final Shader mShaderPass2 = new Shader();
	private final Shader mShaderPass3 = new Shader();
	
	private final Fbo mFboFull = new Fbo();
	private final Fbo mFboHalf = new Fbo();

	@Override
	public void onDestroy() {
		mContext = null;
		
		mShaderScene.deleteProgram();
		mShaderIn.deleteProgram();
		mShaderOut.deleteProgram();
		mShaderPass1.deleteProgram();
		mShaderPass2.deleteProgram();
		mShaderPass3.deleteProgram();
		
		mFboFull.reset();
		mFboHalf.reset();
	}

	@Override
	public void onDrawFrame(Fbo fbo, ObjScene scene) {
		mFboFull.bind();
		mFboFull.bindTexture(0);
		
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glFrontFace(GLES20.GL_CCW);

		mShaderScene.useProgram();
		int uModelViewProjM = mShaderScene.getHandle("uModelViewProjM");
		int uNormalM = mShaderScene.getHandle("uNormalM");
		
		
		float mFStop = 2.4f;
		float mFocalPlane = 3f;
		float mFovY = 45f;

		// Aperture size.
		float mAperture = 8f / mFStop;
		// Plane in focus is a value between [zNear, zFar].
		float mPlaneInFocus = mFocalPlane; // mZNear + (mFocalPlane * (mZFar - mZNear));
		// Image plane distance from lense.
		float imageDist = (float) (mAperture / (2.0 * Math.tan(mFovY * Math.PI
				/ 360.0)));
		// 1/focalLength = 1/imageDist + 1/focalPlane
		float focalLength = (imageDist * mPlaneInFocus)
				/ (imageDist + mPlaneInFocus);
		
		int uAperture = mShaderScene.getHandle("uAperture");
		int uFocalLength = mShaderScene.getHandle("uFocalLength");
		int uPlaneInFocus = mShaderScene.getHandle("uPlaneInFocus");

		GLES20.glUniform1f(uAperture, mAperture);
		GLES20.glUniform1f(uFocalLength, focalLength);
		GLES20.glUniform1f(uPlaneInFocus, mPlaneInFocus);
		
		int aPosition = mShaderScene.getHandle("aPosition");
		int aNormal = mShaderScene.getHandle("aNormal");
		int aColor = mShaderScene.getHandle("aColor");

		Vector<Obj> objs = scene.getObjs();
		for (Obj obj : objs) {
			GLES20.glUniformMatrix4fv(uModelViewProjM, 1, false,
					obj.getModelViewProjM(), 0);
			GLES20.glUniformMatrix4fv(uNormalM, 1, false, obj.getNormalM(), 0);
			Utils.renderObj(obj, aPosition, aNormal, aColor);
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		
		
		
		
		
		
		
		
		
		
		float ratioX = (float)Math.min(mFboHalf.getWidth(), mFboHalf.getHeight()) / mFboHalf.getWidth();
		float ratioY = (float)Math.min(mFboHalf.getWidth(), mFboHalf.getHeight()) / mFboHalf.getHeight();
		
		//float angle = (float) (Math.PI * (SystemClock.uptimeMillis() % 10000) / 5000);
		float stepRadius = 0.1f / 5; //camera.mBlurSteps;

		float[][] dir = new float[3][2];
		for (int i = 0; i < 3; i++) {
			double a = i * Math.PI * 2 / 3;
			dir[i][0] = (float) (stepRadius * Math.sin(a) * ratioX);
			dir[i][1] = (float) (stepRadius * Math.cos(a) * ratioY);
		}

		// First pass, downscale image to mFboHalf and apply required presteps
		// before actual lens blur takes place.
		mFboHalf.bind();
		mFboHalf.bindTexture(0);
		mShaderIn.useProgram();
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboFull.getTexture(0));
		GLES20.glUniform1i(mShaderIn.getHandle("sTextureSource"), 0);
		Utils.renderFullQuad(mShaderIn.getHandle("aPosition"));

		// Second pass.
		mFboHalf.bindTexture(1);
		mShaderPass1.useProgram();
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
				mFboHalf.getTexture(0));
		GLES20.glUniform1i(mShaderPass1.getHandle("sTexturePass1"), 0);
		GLES20.glUniform1f(mShaderPass1.getHandle("uSteps"),
				5); //camera.mBlurSteps);
		GLES20.glUniform2fv(mShaderPass1.getHandle("uDelta0"), 1, dir[0], 0);
		Utils.renderFullQuad(mShaderPass1.getHandle("aPosition"));

		// Third pass.
		mFboHalf.bindTexture(2);
		mShaderPass2.useProgram();
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
				mFboHalf.getTexture(1));
		// TEX_IDX_1 is already bind after previous step.
		GLES20.glUniform1i(mShaderPass2.getHandle("sTexturePass1"), 0);
		GLES20.glUniform1i(mShaderPass2.getHandle("sTexturePass2"), 1);
		GLES20.glUniform1f(mShaderPass2.getHandle("uSteps"),
				5); //camera.mBlurSteps);
		GLES20.glUniform2fv(mShaderPass2.getHandle("uDelta0"), 1, dir[0], 0);
		GLES20.glUniform2fv(mShaderPass2.getHandle("uDelta1"), 1, dir[1], 0);
		Utils.renderFullQuad(mShaderPass2.getHandle("aPosition"));

		// Fourth pass.
		mFboHalf.bindTexture(0);
		mShaderPass3.useProgram();
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
				mFboHalf.getTexture(2));
		// TEX_IDX_2 is already bind.
		GLES20.glUniform1i(mShaderPass3.getHandle("sTexturePass2"), 1);
		GLES20.glUniform1i(mShaderPass3.getHandle("sTexturePass3"), 0);
		GLES20.glUniform1f(mShaderPass3.getHandle("uSteps"),
				5); //camera.mBlurSteps);
		GLES20.glUniform2fv(mShaderPass3.getHandle("uDelta1"), 1, dir[1], 0);
		GLES20.glUniform2fv(mShaderPass3.getHandle("uDelta2"), 1, dir[2], 0);
		Utils.renderFullQuad(mShaderPass3.getHandle("aPosition"));

		// Output pass.
		fbo.bind();
		fbo.bindTexture(0);
		mShaderOut.useProgram();
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboFull.getTexture(0));
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
				mFboHalf.getTexture(0));
		GLES20.glUniform1i(mShaderOut.getHandle("sTextureSource"), 0);
		GLES20.glUniform1i(mShaderOut.getHandle("sTexturePass4"), 1);
		Utils.renderFullQuad(mShaderOut.getHandle("aPosition"));
	}

	@Override
	public void onSeekBarChanged(int key, float value) {
	}

	@Override
	public void onSurfaceChanged(int width, int height) throws Exception {
		mFboFull.init(width, height, 1, true, false);
		mFboHalf.init(width, height, 3);
	}

	@Override
	public void onSurfaceCreated() throws Exception {
		String vertexSource, fragmentSource;
		vertexSource = Utils.loadRawResource(mContext, R.raw.dof_scene_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.dof_scene_fs);
		mShaderScene.setProgram(vertexSource, fragmentSource);
		vertexSource = Utils.loadRawResource(mContext, R.raw.dof_quad_vs);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.dof_in_fs);
		mShaderIn.setProgram(vertexSource, fragmentSource);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.dof_out_fs);
		mShaderOut.setProgram(vertexSource, fragmentSource);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.dof_pass1_fs);
		mShaderPass1.setProgram(vertexSource, fragmentSource);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.dof_pass2_fs);
		mShaderPass2.setProgram(vertexSource, fragmentSource);
		fragmentSource = Utils.loadRawResource(mContext, R.raw.dof_pass3_fs);
		mShaderPass3.setProgram(vertexSource, fragmentSource);		
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public void setPreferences(SharedPreferences prefs, ViewGroup parent) {
		/*
		PrefsSeekBar seekBar = new PrefsSeekBar(mContext, parent);
		seekBar.setText(R.string.prefs_flat_saturation);
		seekBar.setPrefs(prefs, R.string.prefs_key_flat_saturation, this);
		parent.addView(seekBar.getView());
		*/
	}

}
