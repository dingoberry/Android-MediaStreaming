package com.k.cam;

import android.hardware.Camera.CameraInfo;

@SuppressWarnings("deprecation")
public class Configuration {

	public static final boolean DEBUG = true;
	
	public static final int COMPONENT_SURFACE = 0x1;
	public static final int COMPONENT_GL_SURFACE = 0x2;
	
	public static final int CURRENT_CAMERA_ID = CameraInfo.CAMERA_FACING_FRONT;
	public static final int CURRENT_COMPONENT = COMPONENT_GL_SURFACE;
}
