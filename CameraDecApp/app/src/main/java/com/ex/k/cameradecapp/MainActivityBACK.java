package com.ex.k.cameradecapp;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
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
import android.widget.ImageView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivityBACK extends AppCompatActivity implements SurfaceHolder.Callback, Runnable, Camera.PreviewCallback, Handler.Callback {

    private SurfaceView mSv;
    private SurfaceView mDv;
    private ExecutorService mEs;
    private Camera mC;
    private SurfaceHolder mH;
    private SurfaceHolder mDh;

    private HandlerThread mHt;
    private Handler mHd;

    private int mCw, mCh;
    private Bitmap mBitmap;
    private Paint mP;

    private byte[] mBuffer;
    private int[] rgbs;

    public void dodo(View view) {
        final int[] color = rgbs;
        if (color == null) {
            return;
        }
        Log.d("yymm", "shodw");
        new Dialog(this) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.dialog);
                setCancelable(true);
                Bitmap bitmap = Bitmap.createBitmap(color, mCw, mCh, Bitmap.Config.ARGB_8888);
                ImageView v = (ImageView) findViewById(R.id.im);
                v.setImageBitmap(bitmap);
            }
        }.show();
    }

    private long mParseSpent;
    private long mBitmapSpent;

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            doJob();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                doJob();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA
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

        if (mHt != null) {
            mHt.quit();
            mHd = null;
            mHt = null;
        }

        Log.d("yymm", "parse spent:" + mParseSpent + ", bitmap spent:" + mBitmapSpent);
    }

    @Override
    public void run() {
        Camera camera = mC;
        if (camera == null) {
            camera = mC = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
//            parameters.setPreviewSize(mCw, mCh);
//            camera.setParameters(parameters);
            mCh = parameters.getPreviewSize().height;
            mCw = parameters.getPreviewSize().width;
            mBitmap = Bitmap.createBitmap(mCw, mCh, Bitmap.Config.ARGB_8888);
            mP = new Paint();
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
            Log.d("yymm", parameters.getPreviewFormat() + ";");
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
            degrees = (cameraInfo.orientation - degrees + 360) % 360;
            camera.setDisplayOrientation(degrees);
            mBuffer = new byte[mCw * mCh * 3 / 2];
            camera.setPreviewCallbackWithBuffer(this);
            camera.addCallbackBuffer(mBuffer);
        }

        try {
            camera.setPreviewDisplay(mH);
            camera.startPreview();
            mHt = new HandlerThread("OPT");
            mHt.start();
            mHd = new Handler(mHt.getLooper(), this);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("yymm", e.getMessage());
            mC = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        Message m = mHd.obtainMessage(0);
//        m.obj = data;
//        Log.d("yymm", "onPreviewFrame:" + data);
//        mHd.sendMessage(m);
        Canvas canvas = mDh.lockCanvas();
        long time = SystemClock.elapsedRealtime();
        int[] rgbs = ImageUtils.Nv21ToARGB888(data, mCh, mCw);
        mParseSpent += SystemClock.elapsedRealtime() - time;
this.rgbs = rgbs;
        time = SystemClock.elapsedRealtime();
        mBitmap.setPixels(rgbs, 0, mCw, 0, 0, mCw, mCh);
        canvas.drawBitmap(mBitmap, 0, 0, mP);
        mDh.unlockCanvasAndPost(canvas);
        mBitmapSpent += SystemClock.elapsedRealtime() - time;
        camera.addCallbackBuffer(mBuffer);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == 0) {
            Log.d("yymm", "handleMessage:" + msg.obj);
            long t = SystemClock.elapsedRealtime();
            Canvas canvas = mDh.lockCanvas();
            Log.d("yymm", "lock time:" + (SystemClock.elapsedRealtime() - t));
            mBitmap.setPixels(ImageUtils.Nv21ToARGB888((byte[]) msg.obj, mCw, mCh), 0, mCw, 0, 0, mCw, mCh);
            Log.d("yymm", "generate rgb:" + (SystemClock.elapsedRealtime() - t));
            canvas.drawBitmap(mBitmap, 0, 0, mP);
            Log.d("yymm", "draw rgb:" + (SystemClock.elapsedRealtime() - t));
            mDh.unlockCanvasAndPost(canvas);
            Log.d("yymm", "finish:" + (SystemClock.elapsedRealtime() - t));
        }
        return true;
    }
}
