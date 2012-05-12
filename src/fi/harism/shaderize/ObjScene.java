package fi.harism.shaderize;

import java.util.Vector;

import android.os.SystemClock;

public class ObjScene {

	private final long CAMERA_ANIM_LENGTH = 5000;

	private final Vector<ObjBox> mBoxes = new Vector<ObjBox>();
	private long mCameraAnimStart;
	private final float mCameraPosition[] = new float[3];
	private final float mCameraPositionSource[] = new float[3];
	private final float mCameraPositionTarget[] = new float[3];
	private final float mCameraProjM[] = new float[16];

	private final float mCameraViewM[] = new float[16];

	public ObjScene() {
		ObjBox box = new ObjBox();
		box.setColor(Utils.rand(.5f, 1f), Utils.rand(.5f, 1f),
				Utils.rand(.5f, 1f));
		box.setSize(-10f, -10f, -10f);
		mBoxes.add(box);

		for (int i = 0; i < 100; ++i) {
			box = new ObjBox();
			box.setSize(.4f, .4f, .4f);
			box.setColor(Utils.rand(.5f, 1f), Utils.rand(.5f, 1f),
					Utils.rand(.5f, 1f));
			box.setPosition(Utils.rand(-4f, 4f), Utils.rand(-4f, 4f),
					Utils.rand(-4f, 4f));
			mBoxes.add(box);
		}
	}

	public void animate() {
		long currentTime = SystemClock.uptimeMillis();
		if (currentTime > mCameraAnimStart + CAMERA_ANIM_LENGTH) {
			mCameraAnimStart = currentTime;
			for (int i = 0; i < 3; ++i) {
				mCameraPositionSource[i] = mCameraPositionTarget[i];
				mCameraPositionTarget[i] = Utils.rand(-3f, 3f);
			}
		}

		float t = (float) (currentTime - mCameraAnimStart) / CAMERA_ANIM_LENGTH;
		t = t * t * (3 - 2 * t);
		for (int i = 0; i < 3; ++i) {
			mCameraPosition[i] = mCameraPositionSource[i]
					+ (mCameraPositionTarget[i] - mCameraPositionSource[i]) * t;
		}
		android.opengl.Matrix.setLookAtM(mCameraViewM, 0, mCameraPosition[0],
				mCameraPosition[1], mCameraPosition[2], 0, 0, 0, 0f, 1f, 0f);

		for (ObjBox box : mBoxes) {
			box.updateMatrices(mCameraViewM, mCameraProjM);
		}
	}

	public Vector<ObjBox> getBoxes() {
		return mBoxes;
	}

	private void setRandColor(ObjBox box) {
	}

	public void setViewSize(int width, int height) {
		// float aspectRatioX = Math.max(width, height) / (float) height;
		// float aspectRatioY = Math.max(width, height) / (float) width;
		// Matrix.orthoM(mProjM, 0, -aspectRatioX, aspectRatioX, -aspectRatioY,
		// aspectRatioY, 2f, 12f);

		Matrix.setPerspectiveM(mCameraProjM, 45f, (float) width / height, .1f,
				20f);

	}

}
