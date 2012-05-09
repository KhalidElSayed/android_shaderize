package fi.harism.shaderize;

import java.nio.FloatBuffer;

/**
 * Base class for handling object hierarchies etc.
 */
public abstract class Obj {

	// Local model matrix.
	private final float[] mModelM = new float[16];
	// World model-view matrix.
	private final float[] mModelViewM = new float[16];
	// World model-view-projection matrix.
	private final float[] mModelViewProjM = new float[16];
	// World normal matrix.
	private final float[] mNormalM = new float[16];

	private boolean mRecalculateModelM;
	// Local rotation matrix.
	private final float[] mRotateM = new float[16];
	// Local scaling matrix.
	private final float[] mScaleM = new float[16];
	// Local translation matrix.
	private final float[] mTranslateM = new float[16];

	/**
	 * Default constructor.
	 */
	public Obj() {
		// Simply set all matrices to identity.
		android.opengl.Matrix.setIdentityM(mModelM, 0);
		android.opengl.Matrix.setIdentityM(mScaleM, 0);
		android.opengl.Matrix.setIdentityM(mRotateM, 0);
		android.opengl.Matrix.setIdentityM(mTranslateM, 0);
	}

	public abstract FloatBuffer getBufferColors();

	public abstract FloatBuffer getBufferNormals();

	public abstract FloatBuffer getBufferVertices();

	/**
	 * Getter for model-view matrix. This matrix is calculated on call to
	 * updateMatrices(..) which should be called before actual rendering takes
	 * place.
	 * 
	 * @return Current model-view matrix
	 */
	protected float[] getModelViewM() {
		return mModelViewM;
	}

	/**
	 * Getter for model-view-projection matrix. This matrix is calculated on
	 * call to updateMatrices(..) which should be called before actual rendering
	 * takes place.
	 * 
	 * @return Current model-view-projection matrix
	 */
	protected float[] getModelViewProjM() {
		return mModelViewProjM;
	}

	/**
	 * Getter for normal matrix. This matrix is calculated on call to
	 * updateMatrices(..) which should be called before actual rendering takes
	 * place.
	 * 
	 * @return Current normal matrix
	 */
	protected float[] getNormalM() {
		return mNormalM;
	}

	/**
	 * Set position for this object. Object position is relative to its parent
	 * object, and camera view if this is the root object.
	 * 
	 * @param x
	 *            Object x coordinate
	 * @param y
	 *            Object y coordinate
	 * @param z
	 *            Object z coordinate
	 */
	public final void setPosition(float x, float y, float z) {
		android.opengl.Matrix.setIdentityM(mTranslateM, 0);
		android.opengl.Matrix.translateM(mTranslateM, 0, x, y, z);
		mRecalculateModelM = true;
	}

	/**
	 * Sets rotation for this object. Rotation is relative to object's parent
	 * object.
	 * 
	 * @param x
	 *            Rotation around x axis
	 * @param y
	 *            Rotation around y axis
	 * @param z
	 *            Rotation around z axis
	 */
	public final void setRotation(float x, float y, float z) {
		Matrix.setRotateM(mRotateM, 0, x, y, z);
		mRecalculateModelM = true;
	}

	/**
	 * Set scaling factor for this object.
	 * 
	 * @param scale
	 *            Scaling factor
	 */
	public final void setScaling(float scale) {
		android.opengl.Matrix.setIdentityM(mScaleM, 0);
		android.opengl.Matrix.scaleM(mScaleM, 0, scale, scale, scale);
		mRecalculateModelM = true;
	}

	/**
	 * Updates matrices based on given Model View and Projection matrices. This
	 * method should be called before any rendering takes place, and most likely
	 * after scene has been animated.
	 * 
	 * @param mvM
	 *            Model View matrix
	 * @param projM
	 *            Projection matrix
	 */
	public void updateMatrices(float[] viewM, float[] projM) {
		if (mRecalculateModelM) {
			android.opengl.Matrix.multiplyMM(mModelM, 0, mScaleM, 0, mRotateM, 0);
			android.opengl.Matrix.multiplyMM(mModelM, 0, mTranslateM, 0, mModelM, 0);
			mRecalculateModelM = false;
		}

		// Add local model matrix to global model-view matrix.
		android.opengl.Matrix.multiplyMM(mModelViewM, 0, viewM, 0, mModelM, 0);
		// Apply projection matrix to global model-view matrix.
		android.opengl.Matrix.multiplyMM(mModelViewProjM, 0, projM, 0, mModelViewM, 0);
		// Fast inverse-transpose matrix calculation.
		Matrix.invTransposeM(mNormalM, 0, mModelViewM, 0);
	}
}
