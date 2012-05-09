package fi.harism.shaderize;

public class ObjCamera {

	private float mProjM[] = new float[16];
	private float mViewM[] = new float[16];

	public float[] getProjM() {
		android.opengl.Matrix.frustumM(mProjM, 0, -2f, 2f, -2f, 2f, .1f, 20f);
		return mProjM;
	}

	public float[] getViewM() {
		android.opengl.Matrix.setLookAtM(mViewM, 0, 0f, 0f, -5f, 0, 0, 0, 0f, 1f, 0f);
		return mViewM;
	}

}
