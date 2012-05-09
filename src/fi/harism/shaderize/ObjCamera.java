package fi.harism.shaderize;

import android.os.SystemClock;

public class ObjCamera {

	private long mAnimLength = 5000;
	private long mAnimStart;

	private float mPosition[] = new float[3];
	private float mPositionSource[] = new float[3];
	private float mPositionTarget[] = new float[3];

	private float mProjM[] = new float[16];
	private float mViewM[] = new float[16];

	public void animate() {
		long curTime = SystemClock.uptimeMillis();
		if (curTime > mAnimStart + mAnimLength) {
			mAnimStart = curTime;
			for (int i = 0; i < 3; ++i) {
				mPositionSource[i] = mPositionTarget[i];
				mPositionTarget[i] = Utils.rand(-3f, 3f);
			}
		}

		float t = (float) (curTime - mAnimStart) / mAnimLength;
		t = t * t * (3 - 2 * t);
		for (int i = 0; i < 3; ++i) {
			mPosition[i] = mPositionSource[i]
					+ (mPositionTarget[i] - mPositionSource[i]) * t;
		}
	}

	public float[] getProjM() {
		return mProjM;
	}

	public float[] getViewM() {
		android.opengl.Matrix.setLookAtM(mViewM, 0, mPosition[0], mPosition[1],
				mPosition[2], 0, 0, 0, 0f, 1f, 0f);
		return mViewM;
	}

	public void setViewSize(int width, int height) {
		// float aspectRatioX = Math.max(width, height) / (float) height;
		// float aspectRatioY = Math.max(width, height) / (float) width;
		// Matrix.orthoM(mProjM, 0, -aspectRatioX, aspectRatioX, -aspectRatioY,
		// aspectRatioY, 2f, 12f);

		Matrix.setPerspectiveM(mProjM, 45f, (float) width / height, .1f, 20f);

	}

}
