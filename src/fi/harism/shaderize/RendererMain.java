package fi.harism.shaderize;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

public class RendererMain implements GLSurfaceView.Renderer {

	public static final int FBO_IN = 0;
	public static final int FBO_OUT = 1;

	private static final int TRANSITION_TIME = 1000;

	private Context mContext;
	private final Fbo mFboMain = new Fbo();
	private final Fbo mFboSwitch = new Fbo();
	// private RendererFilter mFilterCurrent = null;
	private final Vector<RendererFilter> mFilters = new Vector<RendererFilter>();

	private float mFrameRate;
	private ByteBuffer mFullQuadVertices;
	private final ObjCamera mObjCamera = new ObjCamera();

	private final ObjScene mObjScene = new ObjScene();
	private final boolean mShaderCompilerSupported[] = new boolean[1];
	private final Shader mShaderCopy = new Shader();

	private final Shader mShaderScene = new Shader();
	private final Shader mShaderTransform = new Shader();
	private long mTimeLastRender;
	private long mTimeSwitchStart;
	private int mWidth, mHeight;

	public RendererMain() {
		// Create full scene quad buffer.
		final byte FULL_QUAD_COORDS[] = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mFullQuadVertices = ByteBuffer.allocateDirect(4 * 2);
		mFullQuadVertices.put(FULL_QUAD_COORDS).position(0);
	}

	public float getFramesPerSecond() {
		return 1000f / mFrameRate;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// If shader compiler is not supported, clear screen buffer only.
		if (mShaderCompilerSupported[0] == false) {
			GLES20.glClearColor(0f, 0f, 0f, 1f);
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			mFrameRate = 0f;
			return;
		}

		long timeCurrent = SystemClock.uptimeMillis();
		mFrameRate = mFrameRate * 0.4f + (timeCurrent - mTimeLastRender) * 0.6f;
		mTimeLastRender = timeCurrent;

		if (mFilters.size() > 1
				&& mTimeSwitchStart + TRANSITION_TIME < timeCurrent) {
			if (mFilters.get(0).mInitilized) {
				mFilters.get(0).onDestroy();
			}
			mFilters.remove(0);
			mTimeSwitchStart = timeCurrent;
		}

		for (int idx = 0; idx < 2 && idx < mFilters.size();) {
			try {
				if (!mFilters.get(idx).mInitilized) {
					mFilters.get(idx).onSurfaceCreated(mContext);
					mFilters.get(idx).onSurfaceChanged(mWidth, mHeight);
					mFilters.get(idx).mInitilized = true;
				}
				++idx;
			} catch (Exception ex) {
				mFilters.get(idx).onDestroy();
				mFilters.remove(idx);
				showToast(ex.getMessage());
			}
		}

		mObjCamera.animate();
		float viewM[] = mObjCamera.getViewM();
		float projM[] = mObjCamera.getProjM();

		mFboMain.bind();
		mFboMain.bindTexture(FBO_IN);
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glFrontFace(GLES20.GL_CCW);

		mShaderScene.useProgram();
		int uModelViewProjM = mShaderScene.getHandle("uModelViewProjM");
		int uNormalM = mShaderScene.getHandle("uNormalM");
		int aPosition = mShaderScene.getHandle("aPosition");
		int aNormal = mShaderScene.getHandle("aNormal");
		int aColor = mShaderScene.getHandle("aColor");

		Vector<Obj> objs = mObjScene.getBoxes();
		for (Obj obj : objs) {
			obj.updateMatrices(viewM, projM);

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

			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
					vertexBuffer.capacity() / 3);
		}

		if (mFilters.size() > 0 && mFilters.get(0).mInitilized) {
			mFilters.get(0).onDrawFrame(mFboMain, mObjScene);

			// Copy FBO to screen buffer.
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glViewport(0, 0, mWidth, mHeight);
			mShaderCopy.useProgram();
			aPosition = mShaderCopy.getHandle("aPosition");
			GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false,
					0, mFullQuadVertices);
			GLES20.glEnableVertexAttribArray(aPosition);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
					mFboMain.getTexture(FBO_OUT));
			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		}

		if (mFilters.size() > 1 && mFilters.get(1).mInitilized) {
			mFilters.get(1).onDrawFrame(mFboMain, mObjScene);

			float t = (float) (timeCurrent - mTimeSwitchStart)
					/ TRANSITION_TIME;
			t *= t * t * (3 - 2 * t);

			// Copy FBO to screen buffer.
			GLES20.glEnable(GLES20.GL_BLEND);
			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
					GLES20.GL_ONE_MINUS_SRC_ALPHA);

			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glViewport(0, 0, mWidth, mHeight);
			mShaderTransform.useProgram();
			aPosition = mShaderTransform.getHandle("aPosition");
			int uScale = mShaderTransform.getHandle("uScale");
			int uAlpha = mShaderTransform.getHandle("uAlpha");
			GLES20.glUniform1f(uScale, 1f + (1f - t) * 3f);
			GLES20.glUniform1f(uAlpha, t);
			GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false,
					0, mFullQuadVertices);
			GLES20.glEnableVertexAttribArray(aPosition);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
					mFboMain.getTexture(FBO_OUT));
			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		}

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mWidth = width;
		mHeight = height;
		mFboMain.init(mWidth, mHeight, 2, true, false);
		mFboSwitch.init(width / 4, height / 4, 1);
		mObjCamera.setViewSize(mWidth, mHeight);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		try {
			// Check if shader compiler is supported.
			GLES20.glGetBooleanv(GLES20.GL_SHADER_COMPILER,
					mShaderCompilerSupported, 0);

			// If not, show user an error message and return immediately.
			if (mShaderCompilerSupported[0] == false) {
				throw new Exception(
						mContext.getString(R.string.error_shader_compiler));
			}

			// Instantiate shaders.
			String vertexSource, fragmentSource;
			vertexSource = Utils.loadRawResource(mContext, R.raw.copy_vs);
			fragmentSource = Utils.loadRawResource(mContext, R.raw.copy_fs);
			mShaderCopy.setProgram(vertexSource, fragmentSource);
			vertexSource = Utils.loadRawResource(mContext, R.raw.scene_vs);
			fragmentSource = Utils.loadRawResource(mContext, R.raw.scene_fs);
			mShaderScene.setProgram(vertexSource, fragmentSource);
			vertexSource = Utils.loadRawResource(mContext, R.raw.transform_vs);
			fragmentSource = Utils
					.loadRawResource(mContext, R.raw.transform_fs);
			mShaderTransform.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showToast(ex.getMessage());
		}

	}

	public void setContext(Context context) {
		mContext = context;
	}

	public void setRendererFilter(RendererFilter filter) {
		mFilters.add(filter);
		while (mFilters.size() > 3) {
			mFilters.remove(mFilters.size() - 1);
		}
		if (mFilters.size() <= 2) {
			mTimeSwitchStart = SystemClock.uptimeMillis();
		}
	}

	private void showToast(final String text) {
		Handler handler = new Handler(mContext.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
			}
		});

	}

}
