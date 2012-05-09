/*
   Copyright 2011 Harri Smått

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package fi.harism.shaderize;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * TODO: This class will go through major rewriting once I get to writing a new
 * class for geometry handling.
 */
public final class ObjBox extends Obj {

	private static final int FACE_COUNT = 6;
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int FLOATS_PER_VERTEX = 3;
	private static final int VERTICES_PER_FACE = 6;

	private FloatBuffer mColorBuffer;
	private FloatBuffer mNormalBuffer;
	private FloatBuffer mVertexBuffer;

	public ObjBox() {
		int sz = FACE_COUNT * VERTICES_PER_FACE * FLOATS_PER_VERTEX
				* FLOAT_SIZE_BYTES;
		ByteBuffer buffer = ByteBuffer.allocateDirect(sz);
		mVertexBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer = ByteBuffer.allocateDirect(sz);
		mNormalBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer = ByteBuffer.allocateDirect(sz);
		mColorBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();

		setNormal(0, 0f, 0f, 1f);
		setNormal(1, 0f, 0f, -1f);
		setNormal(2, 0f, 1f, 0f);
		setNormal(3, 0f, -1f, 0f);
		setNormal(4, 1f, 0f, 0f);
		setNormal(5, -1f, 0f, 0f);

		setSize(1f, 1f, 1f);
		setColor(.5f, .5f, .5f);
	}

	@Override
	public FloatBuffer getBufferColors() {
		return mColorBuffer;
	}

	@Override
	public FloatBuffer getBufferNormals() {
		return mNormalBuffer;
	}

	@Override
	public FloatBuffer getBufferVertices() {
		return mVertexBuffer;
	}

	public void setColor(float r, float g, float b) {
		for (int i = 0; i < FACE_COUNT; ++i) {
			setColor(i, r, g, b);
		}
	}

	public void setColor(int face, float r, float g, float b) {
		int i = face * VERTICES_PER_FACE * FLOATS_PER_VERTEX;
		for (int j = 0; j < VERTICES_PER_FACE; ++j) {
			mColorBuffer.put(i + (j * FLOATS_PER_VERTEX), r);
			mColorBuffer.put(i + (j * FLOATS_PER_VERTEX) + 1, g);
			mColorBuffer.put(i + (j * FLOATS_PER_VERTEX) + 2, b);
		}
	}

	private void setNormal(int face, float x, float y, float z) {
		int i = face * VERTICES_PER_FACE * FLOATS_PER_VERTEX;
		for (int j = 0; j < VERTICES_PER_FACE; ++j) {
			mNormalBuffer.put(i + (j * FLOATS_PER_VERTEX), x);
			mNormalBuffer.put(i + (j * FLOATS_PER_VERTEX) + 1, y);
			mNormalBuffer.put(i + (j * FLOATS_PER_VERTEX) + 2, z);
		}
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

}
