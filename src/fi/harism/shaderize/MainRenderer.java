package fi.harism.shaderize;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

public class MainRenderer implements GLSurfaceView.Renderer {

	private static final int TRANSITION_TIME = 1000;

	private Context mContext;
	private final ObjFbo mFboMain = new ObjFbo();
	// private RendererFilter mFilterCurrent = null;
	private final Vector<Renderer> mFilters = new Vector<Renderer>();

	private float mFrameRate;

	private final ObjScene mObjScene = new ObjScene();
	private final boolean mShaderCompilerSupported[] = new boolean[1];
	private final ObjShader mShaderCopy = new ObjShader();

	private long mTimeLastRender;
	private long mTimeSwitchStart;
	private int mWidth, mHeight;

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
					mFilters.get(idx).onSurfaceCreated();
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

		mObjScene.animate();

		if (mFilters.size() > 0 && mFilters.get(0).mInitilized) {
			mFilters.get(0).onDrawFrame(mFboMain, mObjScene);

			// Copy FBO to screen buffer.
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glViewport(0, 0, mWidth, mHeight);

			mShaderCopy.useProgram();

			int uScale = mShaderCopy.getHandle("uScale");
			int uAlpha = mShaderCopy.getHandle("uAlpha");
			GLES20.glUniform1f(uScale, 1f);
			GLES20.glUniform1f(uAlpha, 1f);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboMain.getTexture(0));
			Utils.renderQuad(mShaderCopy.getHandle("aPosition"));
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
			mShaderCopy.useProgram();

			int uScale = mShaderCopy.getHandle("uScale");
			int uAlpha = mShaderCopy.getHandle("uAlpha");
			GLES20.glUniform1f(uScale, 1f + (1f - t) * 3f);
			GLES20.glUniform1f(uAlpha, t);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboMain.getTexture(0));
			Utils.renderQuad(mShaderCopy.getHandle("aPosition"));
		}

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mWidth = width;
		mHeight = height;
		mFboMain.init(mWidth, mHeight, 1, true, false);
		mObjScene.setViewSize(mWidth, mHeight);
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
			vertexSource = Utils.loadRawResource(mContext, R.raw.main_copy_vs);
			fragmentSource = Utils
					.loadRawResource(mContext, R.raw.main_copy_fs);
			mShaderCopy.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showToast(ex.getMessage());
		}

	}

	public void setContext(Context context) {
		mContext = context;
	}

	public void setRendererFilter(Renderer filter) {
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
