package com.k.cam;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class DirectDrawer {

	private final String vertexShaderCode = "attribute vec4 vPosition;"
			+ "attribute vec2 inputTextureCoordinate;"
			+ "varying vec2 textureCoordinate;" + "void main()" + "{"
			+ "gl_Position = vPosition;"
			+ "textureCoordinate = inputTextureCoordinate;" + "}";

	private final String fragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n"
			+ "precision mediump float;"
			+ "varying vec2 textureCoordinate;\n"
			+ "uniform samplerExternalOES s_texture;\n"
			+ "void main() {"
			+ "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n"
			+ "}";

	private FloatBuffer mVertexBuffer, mTextureVerticesBuffer;
	private ShortBuffer mDrawListBuffer;
	private final int mProgram;
	private int mPositionHandle;
	private int mTextureCoordHandle;

	private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

	// number of coordinates per vertex in this array
	private static final int COORDS_PER_VERTEX = 2;

	private final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4; // 4 bytes per
															// vertex

	private static float squareCoords[] = { -1.0f, 1.0f, -1.0f, -1.0f, 1.0f,
			-1.0f, 1.0f, 1.0f, };
	private static float textureVertices[] = { 0.0f, 1.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 0.0f, };

	private int mTexture;
	private boolean mShouldFlip;

	public DirectDrawer(int texture, boolean shouldFlip) {
		this.mTexture = texture;
		mShouldFlip = shouldFlip;

		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		mVertexBuffer = bb.asFloatBuffer();
		mVertexBuffer.put(squareCoords);
		mVertexBuffer.position(0);

		// initialize byte buffer for the draw list
		ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		mDrawListBuffer = dlb.asShortBuffer();
		mDrawListBuffer.put(drawOrder);
		mDrawListBuffer.position(0);

		ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
		bb2.order(ByteOrder.nativeOrder());
		mTextureVerticesBuffer = bb2.asFloatBuffer();
		mTextureVerticesBuffer.put(textureVertices);
		mTextureVerticesBuffer.position(0);

		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // create empty OpenGL ES Program
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader
														// to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment
															// shader to program
		GLES20.glLinkProgram(mProgram); // creates OpenGL ES program executables
	}

	public void draw(float[] mtx) {
		GLES20.glUseProgram(mProgram);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexture);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Prepare the <insert shape here> coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

		mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram,
				"inputTextureCoordinate");
		GLES20.glEnableVertexAttribArray(mTextureCoordHandle);

		if (mShouldFlip) {
			mTextureVerticesBuffer.clear();
			mTextureVerticesBuffer.put(flipTextureCoordinates(textureVertices));
			mTextureVerticesBuffer.position(0);
		}

		GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, VERTEX_STRIDE, mTextureVerticesBuffer);

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
				GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
	}

	private int loadShader(int type, String shaderCode) {

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	private float[] flipTextureCoordinates(float[] coords) {
		float[] result = new float[coords.length];
		for (int i = coords.length - 1, j = 0; i >= 0; i -= 2, j += 2) {
			result[j] = coords[i - 1];
			result[j + 1] = coords[i];
		}
		return result;
	}

	@SuppressWarnings("unused")
	private float[] transformTextureCoordinates(float[] coords, float[] matrix) {
		float[] result = new float[coords.length];
		float[] vt = new float[4];

		for (int i = 0; i < coords.length; i += 2) {
			float[] v = { coords[i], coords[i + 1], 0, 1 };
			Matrix.multiplyMV(vt, 0, matrix, 0, v, 0);
			result[i] = vt[0];
			result[i + 1] = vt[1];
		}
		return result;
	}
}
