package fi.harism.shaderize;

import java.nio.ByteBuffer;
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

	private Context mContext;
	private final Fbo mFbo = new Fbo();
	private float mFrameRate;

	private ByteBuffer mFullQuadVertices;
	private long mLastRenderTime;

	private final ObjCamera mObjCamera = new ObjCamera();
	private final ObjScene mObjScene = new ObjScene();
	private final Shader mShader = new Shader();

	private final boolean mShaderCompilerSupported[] = new boolean[1];

	private RendererChild mShaderRenderer;
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

		long currentTime = SystemClock.uptimeMillis();
		mFrameRate = mFrameRate * 0.4f + (currentTime - mLastRenderTime) * 0.6f;
		mLastRenderTime = currentTime;

		float viewM[] = mObjCamera.getViewM();
		float projM[] = mObjCamera.getProjM();
		Vector<Obj> objs = mObjScene.getBoxes();
		for (Obj obj : objs) {
			obj.updateMatrices(viewM, projM);
		}

		if (mShaderRenderer != null) {
			mShaderRenderer.onDrawFrame(mFbo, mObjScene);
		}

		// Copy FBO to screen buffer.
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glViewport(0, 0, mWidth, mHeight);
		mShader.useProgram();
		int aPosition = mShader.getHandle("aPosition");
		GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0,
				mFullQuadVertices);
		GLES20.glEnableVertexAttribArray(aPosition);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFbo.getTexture(0));
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mWidth = width;
		mHeight = height;
		mFbo.init(mWidth, mHeight, 1);
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
			String copyVs = Utils.loadRawResource(mContext, R.raw.copy_vs);
			String copyFs = Utils.loadRawResource(mContext, R.raw.copy_fs);
			mShader.setProgram(copyVs, copyFs);
		} catch (Exception ex) {
			showToast(ex.getMessage());
		}

	}

	public void setContext(Context context) {
		mContext = context;
	}

	public void setShaderRenderer(RendererChild shaderRenderer) {
		if (mShaderRenderer != null) {
			mShaderRenderer.onDestroy();
		}
		mShaderRenderer = shaderRenderer;
	}

	public void showToast(final String text) {
		Handler handler = new Handler(mContext.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
			}
		});

	}

}
