
package com.release.perfectapplock;

import static android.content.ContentValues.TAG;
import static android.content.Context.WINDOW_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class CameraView implements SurfaceHolder.Callback, PictureCallback,
        ErrorCallback {

    private Context context = null;

    private WindowManager winMan;
    // a surface holder
    private SurfaceHolder sHolder;

    private static Camera camera;


    private Parameters parameters;

    private AudioManager audioMgr = null;

    private WindowManager.LayoutParams params = null;

    private SurfaceView surfaceView = null;

    public CameraView(Context ctx) {
        context = ctx;
        audioMgr = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onError(int error, Camera camera) {
        Log.d(TAG, "Camera Error : " + error, null);

        WindowManager winMan = (WindowManager) context
                .getSystemService(WINDOW_SERVICE);
        winMan.removeView(surfaceView);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        if (data != null) {

            WindowManager winMan = (WindowManager) context
                    .getSystemService(WINDOW_SERVICE);
            winMan.removeView(surfaceView);

            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                        data.length, opts);
                bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, false);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int newWidth = 600;
                int newHeight = 600;

                // calculate the scale - in this case = 0.4f
                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;

                // createa matrix for the manipulation
                Matrix matrix = new Matrix();
                // resize the bit map
                matrix.postScale(scaleWidth, scaleHeight);
                // rotate the Bitmap
                matrix.postRotate(-90);
                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        width, height, matrix, true);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80,
                        bytes);
                File folder = new File(context.getFilesDir() + File.separator + "pic");
                if(!folder.exists()){
                    folder.mkdir();
                }else{
                    File[] list = folder.listFiles();
                    if(list != null && list.length > 10){
                        Arrays.sort(list, new Comparator<File>() {
                            @Override
                            public int compare(File file, File t1) {
                                return file.compareTo(t1);
                            }
                        });
                        list[0].delete();
                    }
                }
                File file = new File(folder, System.currentTimeMillis() + ".jpg");
                FileOutputStream fo =  new FileOutputStream(file);
                fo.write(bytes.toByteArray());
                // remember close de FileOutput
                fo.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // get camera parameters
        parameters = camera.getParameters();
        camera.enableShutterSound(false);

        // set camera parameters
        camera.setParameters(parameters);
        camera.startPreview();

        audioMgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);


        camera.takePicture(null, null, this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Log.d(TAG, "Camera Opened");
            camera.setPreviewDisplay(sHolder);
        } catch (IOException exception) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        // stop the preview
        camera.stopPreview();

        // release the camera
        camera.release();
        Log.d(TAG, "Camera released");

        // unbind the camera from this object
        camera = null;

    }
    public void capturePhoto() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) return;

        surfaceView = new SurfaceView(context);
        winMan = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                1,
                1,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        winMan.addView(surfaceView, params);

        surfaceView.setZOrderOnTop(true);

        SurfaceHolder holder = surfaceView.getHolder();

        holder.setFormat(PixelFormat.TRANSPARENT);

        sHolder = holder;
        sHolder.addCallback(this);
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // The Surface has been created, acquire the camera
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
}