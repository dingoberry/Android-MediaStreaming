package com.ex.k.cameradecapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Runnable, Camera.PreviewCallback {

    private SurfaceView mSv;
    private SurfaceView mDv;
    private ExecutorService mEs;
    private Camera mC;
    private SurfaceHolder mH;
    private SurfaceHolder mDh;

    private int mCw, mCh;

    private byte[] mBuffer;

    private MediaCodec mMc;
    private MediaCodec mDMc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.loadLibrary("SimpleNative");
        mSv = (SurfaceView) findViewById(R.id.camera_sv);
        mDv = (SurfaceView) findViewById(R.id.decode_sv);
        SurfaceHolder holder = mSv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mH = holder;
        mDh = mDv.getHolder();
        mEs = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            doJob();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                doJob();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 0x23);
            }
        }
    }

    private void doJob() {
        Log.d("yymm", "do Job");
        mH.addCallback(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x23 && grantResults.length > 0) {
            boolean isGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }

            if (isGranted) {
                doJob();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEs.shutdown();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("yymm", "surfaceCreated");
//        mCw = mSv.getWidth();
//        mCh = mSv.getHeight();
        mEs.submit(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("yymm", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("yymm", "surfaceDestroyed");
        Camera camera = mC;
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            mC = null;
        }
    }

    @Override
    @TargetApi(16)
    public void run() {
        Camera camera = mC;
        if (camera == null) {
            camera = mC = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
//            parameters.setPreviewSize(mCw, mCh);
//            camera.setParameters(parameters);
            mCh = parameters.getPreviewSize().height;
            mCw = parameters.getPreviewSize().width;
            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);
            int degrees;
            switch (getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
                default:
                    degrees = 0;
                    break;
            }
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
            degrees = (cameraInfo.orientation - degrees + 360) % 360;
            camera.setDisplayOrientation(degrees);
//            camera.setPreviewCallback(this);
            mBuffer = new byte[mCw * mCh * 3 / 2];
            camera.setPreviewCallbackWithBuffer(this);
            camera.addCallbackBuffer(mBuffer);
        }

        try {
            camera.setPreviewDisplay(mH);
            camera.startPreview();

            MediaFormat mediaFormat = MediaFormat.createVideoFormat("Video/AVC", mCw, mCh);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mMc = MediaCodec.createEncoderByType("Video/AVC");
            mMc.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMc.start();

            mediaFormat = MediaFormat.createVideoFormat("Video/AVC", mCw, mCh);
            mDMc = MediaCodec.createDecoderByType("Video/AVC");
            mDMc.configure(mediaFormat, mDh.getSurface(), null, 0);
            mDMc.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("yymm", e.getMessage());
            mC = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d("yymm", "onPreviewFrame:" + mBuffer.length + ":" + data.length);
//        mHd.sendMessage(m);

        onFrameEncode(data, 0, data.length, 0);

        camera.addCallbackBuffer(mBuffer);
    }

    @TargetApi(16)
    private void onFrameEncode(byte[] buf, int offset, int length, int flag) {
        ByteBuffer[] inputBuffers = mMc.getInputBuffers();
        ByteBuffer[] outputBuffers = mMc.getOutputBuffers();
        int inputBufferIndex = mMc.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
            mMc.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMc.dequeueOutputBuffer(bufferInfo, 0);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
        }
    }

    @TargetApi(16)
    private void onFrameDecode(byte[] buf, int offset, int length, int flag) {

    }

    public void dodo(View v) {
        mC.setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                FileOutputStream f = null;
                try {
                    f = new FileOutputStream(new File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "dd.jpeg"));
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mCw, mCh, null);
                    yuvImage.compressToJpeg(new Rect(0,0,mCw, mCh), 90, f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (f != null) {
                        try {
                            f.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
