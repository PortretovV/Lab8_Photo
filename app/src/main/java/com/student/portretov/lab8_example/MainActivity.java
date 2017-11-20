package com.student.portretov.lab8_example;

import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    HolderCallback holderCallback;
    Camera camera;
    Toast toast;

    final int CAMERA_ID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();
        surfaceHolder.addCallback(holderCallback);

        findViewById(R.id.btnPhoto).setOnClickListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        camera = Camera.open();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (camera != null) {
            camera.release();
        }
        camera = null;
    }

    private static File getOutputMediaFile(){
        File environment = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File mediaStorageDir = new File(environment.getPath());

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +"IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    @Override
    public void onClick(View view) {
        Button btnClick = null;
        if (view instanceof Button){
            btnClick = (Button) view;
        }

        if (btnClick != null){
            switch (btnClick.getId()){
                case R.id.btnPhoto:
                    toast = Toast.makeText(this, "Фото создано", Toast.LENGTH_LONG);
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes, Camera camera) {
                            try {
                                FileOutputStream photoOutputStream = new FileOutputStream(getOutputMediaFile());
                                photoOutputStream.write(bytes);
                                photoOutputStream.close();
                                toast.show();
                                camera.setPreviewDisplay(surfaceHolder);
                                camera.startPreview();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
            }
        }
    }


    class HolderCallback implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            try{
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            camera.stopPreview();
            setCameraDisplayOrentation(CAMERA_ID);
            try{
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    }

    private void setCameraDisplayOrentation(int cameraId){
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
            result = ((360 - degrees) + info.orientation);
        } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = ((360 - degrees) + info.orientation) + 360;
        }
        result %= 360;
        camera.setDisplayOrientation(result);
    }
}
