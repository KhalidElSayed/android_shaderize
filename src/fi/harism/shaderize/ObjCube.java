package fi.harism.shaderize;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class ObjCube {

	private static final int FACE_COUNT = 6;
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int FLOATS_PER_TEXPOS = 2;
	private static final int FLOATS_PER_VERTEX = 3;
	private static final int VERTICES_PER_FACE = 6;
	private FloatBuffer mBufferNormal;
	private FloatBuffer mBufferTexPosition;
	private FloatBuffer mBufferVertex;
	// Local model matrix.
	private final float[] mModelM = new float[16];

	// World model-view matrix.
	private final float[] mModelViewM = new float[16];
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
	private final float[] mViewM = new float[16];

	public ObjCube() {
		int sz = FLOAT_SIZE_BYTES * FLOATS_PER_VERTEX * FACE_COUNT
				* VERTICES_PER_FACE;

		ByteBuffer buffer = ByteBuffer.allocateDirect(sz);
		mBufferVertex = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();

		buffer = ByteBuffer.allocateDirect(sz);
		mBufferNormal = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();

		sz = FLOAT_SIZE_BYTES * FLOATS_PER_TEXPOS * FACE_COUNT
				* VERTICES_PER_FACE;

		buffer = ByteBuffer.allocateDirect(sz);
		mBufferTexPosition = buffer.order(ByteOrder.nativeOrder())
				.asFloatBuffer();

		final float VERTICES[][] = { { -1, 1, 1 }, { -1, -1, 1 }, { 1, 1, 1 },
				{ 1, -1, 1 }, { -1, 1, -1 }, { -1, -1, -1 }, { 1, 1, -1 },
				{ 1, -1, -1 } };
		final float NORMALS[][] = { { 0, 0, 1 }, { 0, 0, -1 }, { -1, 0, 0 },
				{ 1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 } };
		final float TEXPOS[][] = { { 0, 1 }, { 0, 0 }, { 1, 1 }, { 0, 0 },
				{ 1, 0 }, { 1, 1 } };
		final int INDICES[][][] = { { { 0, 1, 2, 1, 3, 2 }, { 0 } },
				{ { 5, 4, 7, 4, 6, 7 }, { 1 } },
				{ { 0, 4, 1, 4, 5, 1 }, { 2 } },
				{ { 3, 7, 2, 7, 6, 2 }, { 3 } },
				{ { 2, 6, 0, 6, 4, 0 }, { 4 } },
				{ { 1, 5, 3, 5, 7, 3 }, { 5 } } };

		for (int[][] indices : INDICES) {
			for (int i = 0; i < 6; ++i) {
				mBufferVertex.put(VERTICES[indices[0][i]]);
				mBufferNormal.put(NORMALS[indices[1][0]]);
				mBufferTexPosition.put(TEXPOS[i]);
			}
		}

		mBufferVertex.position(0);
		mBufferNormal.position(0);
		mBufferTexPosition.position(0);

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
		return mBufferNormal;
	}

	public final FloatBuffer getBufferTexPositions() {
		return mBufferTexPosition;
	}

	public final FloatBuffer getBufferVertices() {
		return mBufferVertex;
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

}
