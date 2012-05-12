package fi.harism.shaderize;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class ObjBox {

	private static final int FACE_COUNT = 6;
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int FLOATS_PER_VERTEX = 3;
	private static final int VERTICES_PER_FACE = 6;

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
	// Local scaling matrix.
	private final float[] mScaleM = new float[16];
	// Local translation matrix.
	private final float[] mTranslateM = new float[16];
	private FloatBuffer mVertexBuffer;
	private final float[] mViewM = new float[16];

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

		// Simply set all matrices to identity.
		android.opengl.Matrix.setIdentityM(mModelM, 0);
		android.opengl.Matrix.setIdentityM(mScaleM, 0);
		android.opengl.Matrix.setIdentityM(mRotateM, 0);
		android.opengl.Matrix.setIdentityM(mTranslateM, 0);
	}

	/**
	 * Updates matrices based on earlier given View matrix. This method should
	 * be called before any rendering takes place, and in most cases after scene
	 * has been animated.
	 */
	public final void calculate() {
		if (mRecalculateModelM) {
			android.opengl.Matrix.multiplyMM(mModelM, 0, mScaleM, 0, mRotateM,
					0);
			android.opengl.Matrix.multiplyMM(mModelM, 0, mTranslateM, 0,
					mModelM, 0);
			mRecalculateModelM = false;
		}

		// Add local model matrix to global model-view matrix.
		android.opengl.Matrix.multiplyMM(mModelViewM, 0, mViewM, 0, mModelM, 0);
		// Fast inverse-transpose matrix calculation.
		Matrix.invTransposeM(mNormalM, 0, mModelViewM, 0);
	}

	public final FloatBuffer getBufferNormals() {
		return mNormalBuffer;
	}

	public final FloatBuffer getBufferVertices() {
		return mVertexBuffer;
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

	public final void setMatrices(float[] viewM, float[] projM) {
		// Copy view and projection -matrices as-is.
		for (int i = 0; i < 16; ++i) {
			mViewM[i] = viewM[i];
			mProjM[i] = projM[i];
		}
	}

	private final void setNormal(int face, float x, float y, float z) {
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
	 *            Rotation around rx axis
	 * @param y
	 *            Rotation around ry axis
	 * @param z
	 *            Rotation around rz axis
	 */
	public final void setRotation(float rx, float ry, float rz) {
		Matrix.setRotateM(mRotateM, 0, rx, ry, rz);
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

	private final void setSideCoordinates(int face, int is, int it, int iu,
			float s1, float t1, float s2, float t2, float u) {
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

	public final void setSize(float width, float height, float depth) {
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

}
