package com.k.cam.component;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bbq.w.library.CameraUtils;
import com.bbq.w.library.LogLib;
import com.k.cam.Configuration;
import com.k.cam.DirectDrawer;
import com.k.cam.R;

@SuppressWarnings("deprecation")
public class GLOptimizeFragment extends CameraFragment implements Renderer,
		OnFrameAvailableListener {

	private final static int CAMERA_HEIGHT = 600;
	private final static int CAMERA_WIDTH = 800;

	private GLSurfaceView mSv;
	private SurfaceTexture mSt;
	private DirectDrawer mDrawer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogLib.d("onCreateView");
		View content = inflater.inflate(R.layout.gl_optimize_fragment,
				container, false);
		GLSurfaceView gv = (GLSurfaceView) content.findViewById(R.id.sv);
		gv.setEGLContextClientVersion(2);
		gv.setRenderer(this);
		gv.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		mSv = gv;
		return content;
	}

	@Override
	public void onDestroy() {
		releaseCam();
		super.onDestroy();
	}

	private int createTextureID() {
		int[] textures = { 0 };
		GLES20.glGenTextures(1, textures, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		return textures[0];
	}

	@Override
	public void onSurfaceCreated(GL10 gl10, EGLConfig config) {
		LogLib.d("onSurfaceCreated");
		int textureID = createTextureID();
		SurfaceTexture st = new SurfaceTexture(textureID);
		st.setOnFrameAvailableListener(this);
		mSt = st;
		int id = resolveCameraId();
		if (id != INVALID_VALUE) {
			LogLib.d("open cam with id=" + id);
			mDrawer = new DirectDrawer(textureID);
			openCam(id, CAMERA_WIDTH, CAMERA_HEIGHT);
		}
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl10, int width, int height) {
		LogLib.d("onSurfaceChanged");
		GLES20.glViewport(0, 0, width, height);
		if (isPreviewing()) {
			return;
		}
		startPreview(mSt);
	}

	@Override
	public void onDrawFrame(GL10 gl10) {
		// LogLib.d("onDrawFrame");
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		mSt.updateTexImage();
		float[] mtx = new float[16];
		mSt.getTransformMatrix(mtx);
		mDrawer.draw(mtx);
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		// LogLib.d("onFrameAvailable");
		mSv.requestRender();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// LogLib.d("onPreviewFrame:" + data.length);
	}
}
