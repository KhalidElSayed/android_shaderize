package fi.harism.shaderize;

import java.util.Vector;

import android.os.SystemClock;

public class ObjScene {

	private final long BOX_ROTATION_LENGTH = 7000;
	private final long CAMERA_ANIM_LENGTH = 5000;

	private long mBoxRotationStart;

	private long mCameraAnimStart;
	private final float mCameraPosition[] = new float[3];
	private final float mCameraPositionAnim[] = new float[6];
	private final float mCameraProjM[] = new float[16];
	private final float mCameraViewM[] = new float[16];
	private final Vector<StructCube> mCubes = new Vector<StructCube>();
	private long mSaturationTime;

	public ObjScene() {
		StructCube cube = new StructCube();
		cube.mCube.setScaling(-5f);
		mCubes.add(cube);

		for (int i = 0; i < 100; ++i) {
			cube = new StructCube();
			cube.mCube.setScaling(.2f);
			for (int j = 0; j < 3; ++j) {
				cube.mLocation[j] = cube.mLocationAnim[j + 3] = Utils.rand(-4f,
						4f);
			}
			mCubes.add(cube);
		}

		for (StructCube b : mCubes) {
			b.mColor[0] = .9f;
			b.mColor[1] = Utils.rand(0, 1f) < .5f ? .7f : .9f;
			b.mColor[2] = Utils.rand(0, 1f) < .5f ? 0f : .4f;

			for (int i = 0; i < 5; ++i) {
				int idx1 = (int) Utils.rand(0f, 3f);
				int idx2 = (int) Utils.rand(0f, 3f);

				float col1 = b.mColor[idx1];
				b.mColor[idx1] = b.mColor[idx2];
				b.mColor[idx2] = col1;
			}
		}

	}

	public void animate() {
		long currentTime = SystemClock.uptimeMillis();
		if (currentTime > mCameraAnimStart + CAMERA_ANIM_LENGTH) {
			mCameraAnimStart = currentTime;
			for (int i = 0; i < 3; ++i) {
				mCameraPositionAnim[i] = mCameraPositionAnim[i + 3];
				mCameraPositionAnim[i + 3] = Utils.rand(-3f, 3f);
			}
		}
		float t = (float) (currentTime - mCameraAnimStart) / CAMERA_ANIM_LENGTH;
		t = t * t * (3 - 2 * t);
		interpolate(mCameraPosition, mCameraPositionAnim, t);
		android.opengl.Matrix.setLookAtM(mCameraViewM, 0, mCameraPosition[0],
				mCameraPosition[1], mCameraPosition[2], 0, 0, 0, 0f, 1f, 0f);

		if (currentTime > mBoxRotationStart + BOX_ROTATION_LENGTH) {
			mBoxRotationStart = currentTime;
			for (int i = 1; i < mCubes.size(); ++i) {
				StructCube box = mCubes.get(i);
				for (int j = 0; j < 3; ++j) {
					box.mRotationAnim[j] = box.mRotationAnim[j + 3];
					if (Utils.rand(0f, 1f) < 0.3f) {
						box.mRotationAnim[j + 3] = Utils.rand(0, 180f);
					}
				}
			}
		}
		t = (float) (currentTime - mBoxRotationStart) / BOX_ROTATION_LENGTH;
		t = t * t * (3 - 2 * t);
		for (int i = 1; i < mCubes.size(); ++i) {
			StructCube box = mCubes.get(i);
			interpolate(box.mRotation, box.mRotationAnim, t);
		}

		if (Utils.rand(0f, 1f) < (currentTime - mSaturationTime) / 200f) {
			for (int i = 0; i < 3; ++i) {
				mCubes.get((int) Utils.rand(0f, mCubes.size())).mSaturation = 1f;
			}
		}
		t = Math.max(0f, 1f - (currentTime - mSaturationTime) * .001f);
		mSaturationTime = currentTime;

		for (StructCube box : mCubes) {
			box.mSaturation *= t;
		}

		for (StructCube box : mCubes) {
			box.mCube.setMatrices(mCameraViewM, mCameraProjM);
			box.mCube.setPosition(box.mLocation[0], box.mLocation[1],
					box.mLocation[2]);
			box.mCube.setRotation(box.mRotation[0], box.mRotation[1],
					box.mRotation[2]);
		}
	}

	public Vector<StructCube> getCubes() {
		return mCubes;
	}

	private final void interpolate(float dst[], float srcAnim[], float t) {
		for (int i = 0; i < 3; ++i) {
			dst[i] = srcAnim[i] + (srcAnim[i + 3] - srcAnim[i]) * t;
		}
	}

	public void setViewSize(int width, int height) {
		// float aspectRatioX = Math.max(width, height) / (float) height;
		// float aspectRatioY = Math.max(width, height) / (float) width;
		// Matrix.orthoM(mProjM, 0, -aspectRatioX, aspectRatioX, -aspectRatioY,
		// aspectRatioY, 2f, 12f);

		Matrix.setPerspectiveM(mCameraProjM, 45f, (float) width / height, .1f,
				20f);
	}

	public static class StructCube {
		public final float mColor[] = new float[3];
		public final ObjCube mCube = new ObjCube();
		public final float mLocation[] = new float[3];
		private final float mLocationAnim[] = new float[6];
		public final float mRotation[] = new float[3];
		private final float mRotationAnim[] = new float[6];
		public float mSaturation;
	}
}
