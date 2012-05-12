package fi.harism.shaderize;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class ObjBox {

	private static final int FACE_COUNT = 6;
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int FLOATS_PER_VERTEX = 3;
	private static final int VERTICES_PER_FACE = 6;

	private final float mColor[] = new float[3];
	// Local model matrix.
	private final float[] mModelM = new float[16];

	// World model-view matrix.
	private final float[] mModelViewM = new float[16];

	private FloatBuffer mNormalBuffer;
	// World normal matrix.
	private final float[] mNormalM = new float[16];

	// Projection matrix.
	private final float[] mProjM = new float[16];

	private boolean mRecalculateModelM;
	// Local rotation matrix.
	private final float[] mRotateM = new float[16];
	private float mSaturation;

	// Local scaling matrix.
	private final float[] mScaleM = new float[16];
	// Local translation matrix.
	private final float[] mTranslateM = new float[16];
	private FloatBuffer mVertexBuffer;

	public ObjBox() {
		int sz = FACE_COUNT * VERTICES_PER_FACE * FLOATS_PER_VERTEX
				* FLOAT_SIZE_BYTES;
		ByteBuffer buffer = ByteBuffer.allocateDirect(sz);
		mVertexBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer = ByteBuffer.allocateDirect(sz);
		mNormalBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();

		setNormal(0, 0f, 0f, 1f);
		setNormal(1, 0f, 0f, -1f);
		setNormal(2, 0f, 1f, 0f);
		setNormal(3, 0f, -1f, 0f);
		setNormal(4, 1f, 0f, 0f);
		setNormal(5, -1f, 0f, 0f);

		setSize(1f, 1f, 1f);
		setColor(.5f, .5f, .5f);

		// Simply set all matrices to identity.
		android.opengl.Matrix.setIdentityM(mModelM, 0);
		android.opengl.Matrix.setIdentityM(mScaleM, 0);
		android.opengl.Matrix.setIdentityM(mRotateM, 0);
		android.opengl.Matrix.setIdentityM(mTranslateM, 0);
	}

	public FloatBuffer getBufferNormals() {
		return mNormalBuffer;
	}

	public FloatBuffer getBufferVertices() {
		return mVertexBuffer;
	}

	public float[] getColor() {
		return mColor;
	}

	/**
	 * Getter for model-view matrix. This matrix is calculated on call to
	 * updateMatrices(..) which should be called before actual rendering takes
	 * place.
	 * 
	 * @return Current model-view matrix
	 */
	public final float[] getModelViewM() {
		return mModelViewM;
	}

	/**
	 * Getter for normal matrix. This matrix is calculated on call to
	 * updateMatrices(..) which should be called before actual rendering takes
	 * place.
	 * 
	 * @return Current normal matrix
	 */
	public final float[] getNormalM() {
		return mNormalM;
	}

	/**
	 * Getter for projection matrix. This matrix is stored during call to
	 * updateMatrices(..) which should be called before actual rendering takes
	 * place.
	 * 
	 * @return Current projection matrix
	 */
	public final float[] getProjM() {
		return mProjM;
	}

	public float getSaturation() {
		return mSaturation;
	}

	public void setColor(float r, float g, float b) {
		mColor[0] = r;
		mColor[1] = g;
		mColor[2] = b;
	}

	private void setNormal(int face, float x, float y, float z) {
		int i = face * VERTICES_PER_FACE * FLOATS_PER_VERTEX;
		for (int j = 0; j < VERTICES_PER_FACE; ++j) {
			mNormalBuffer.put(i + (j * FLOATS_PER_VERTEX) + 0, x);
			mNormalBuffer.put(i + (j * FLOATS_PER_VERTEX) + 1, y);
			mNormalBuffer.put(i + (j * FLOATS_PER_VERTEX) + 2, z);
		}
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

	public void setSaturation(float saturation) {
		mSaturation = saturation;
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

	private void setSideCoordinates(int face, int is, int it, int iu, float s1,
			float t1, float s2, float t2, float u) {
		int i = face * VERTICES_PER_FACE * FLOATS_PER_VERTEX;

		mVertexBuffer.put(i + is, s1);
		mVertexBuffer.put(i + it, t1);
		mVertexBuffer.put(i + iu, u);
		i += FLOATS_PER_VERTEX;

		mVertexBuffer.put(i + is, s1);
		mVertexBuffer.put(i + it, t2);
		mVertexBuffer.put(i + iu, u);
		i += FLOATS_PER_VERTEX;

		mVertexBuffer.put(i + is, s2);
		mVertexBuffer.put(i + it, t1);
		mVertexBuffer.put(i + iu, u);
		i += FLOATS_PER_VERTEX;

		mVertexBuffer.put(i + is, s1);
		mVertexBuffer.put(i + it, t2);
		mVertexBuffer.put(i + iu, u);
		i += FLOATS_PER_VERTEX;

		mVertexBuffer.put(i + is, s2);
		mVertexBuffer.put(i + it, t2);
		mVertexBuffer.put(i + iu, u);
		i += FLOATS_PER_VERTEX;

		mVertexBuffer.put(i + is, s2);
		mVertexBuffer.put(i + it, t1);
		mVertexBuffer.put(i + iu, u);
	}

	public void setSize(float width, float height, float depth) {
		float w = width / 2f;
		float h = height / 2f;
		float d = depth / 2f;

		setSideCoordinates(0, 0, 1, 2, -w, h, w, -h, d);
		setSideCoordinates(1, 0, 1, 2, w, h, -w, -h, -d);
		setSideCoordinates(2, 0, 2, 1, -w, -d, w, d, h);
		setSideCoordinates(3, 0, 2, 1, w, -d, -w, d, -h);
		setSideCoordinates(4, 2, 1, 0, d, h, -d, -h, w);
		setSideCoordinates(5, 2, 1, 0, -d, h, d, -h, -w);
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
			android.opengl.Matrix.multiplyMM(mModelM, 0, mScaleM, 0, mRotateM,
					0);
			android.opengl.Matrix.multiplyMM(mModelM, 0, mTranslateM, 0,
					mModelM, 0);
			mRecalculateModelM = false;
		}

		// Add local model matrix to global model-view matrix.
		android.opengl.Matrix.multiplyMM(mModelViewM, 0, viewM, 0, mModelM, 0);
		// Fast inverse-transpose matrix calculation.
		Matrix.invTransposeM(mNormalM, 0, mModelViewM, 0);
		// Copy project matrix as-is.
		for (int i = 0; i < 16; ++i) {
			mProjM[i] = projM[i];
		}
	}

}
