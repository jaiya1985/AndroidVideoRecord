package com.screenshot.jayashanf.videorec;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.Inflater;


public class StartRec extends Fragment implements View.OnClickListener{

    private Button stopbutton = null;
    private Button startbutton = null;
    private CameraDevice camDevice = null;
    private MediaRecorder mediaRecorder = null;
    private File recordedfile = null;
    private Size previewSize = null;
    private RecordTextYiew textureView;
    private CameraCaptureSession cameraCaptureSession = null;
    private CaptureRequest.Builder captureRequestBuilder;
    private HandlerThread backgroundThread = null;
    private Handler backgroundHandler = null;


    public static StartRec newInstance() {

        Bundle args = new Bundle();

        StartRec fragment = new StartRec();
        fragment.setArguments(args);
        return fragment;
    }


    private void startBackgroungThread(){
        backgroundThread = new HandlerThread("BackGroundThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private String getFrontCamera(CameraManager cManager) {
        CameraCharacteristics characteristics = null;
        try {
            for (String camid : cManager.getCameraIdList()) {
                characteristics = cManager.getCameraCharacteristics(camid);
                int camorientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (camorientation == CameraCharacteristics.LENS_FACING_FRONT)
                    return camid;
            }
        } catch (CameraAccessException e) {
            return null;
        }
        return null;
    }

    private CameraDevice.StateCallback camStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {

            camDevice = cameraDevice;
            StartCameraPreview();

            if (textureView != null)
                ConfigTextureSize(textureView.getWidth(), textureView.getHeight());
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            camDevice.close();
            camDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            camDevice.close();
            camDevice = null;
        }
    };

    private CameraCaptureSession.StateCallback cameraCaptureStateCallBack = new CameraCaptureSession.StateCallback(){

        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureS) {
            cameraCaptureSession = cameraCaptureS;
            updateCaptureSession();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureS) {


        }
    };

    private void updateCaptureSession() {



    }

    private void StartCameraPreview() {
        if (camDevice == null || (!textureView.isAvailable()) || previewSize == null)
            return;

        try {
            CloseCameraPreview();
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();

            if (surfaceTexture != null){
                surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                captureRequestBuilder = camDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                Surface surface = new Surface(surfaceTexture);
                captureRequestBuilder.addTarget(surface);
                camDevice.createCaptureSession(Collections.singletonList(surface) ,cameraCaptureStateCallBack, backgroundHandler);



            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void CloseCameraPreview() {
        if (cameraCaptureSession != null){
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

    }




    private void startcamera() {

        //get permission first


        CameraManager cammanager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        String camID = getFrontCamera(cammanager);

        try {
            cammanager.openCamera(camID, camStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private File getfile(){
        File dir = new File(Environment.getExternalStorageDirectory(), this.getClass().getName());
        File file = null;
        boolean direxists = true;

        if (!dir.exists())
            direxists = dir.mkdirs();
        if (direxists)
            file = new File(dir, Calendar.getInstance().getTime().toString()+".mp4");
        return file;

    }

    private boolean SetupMediarecorder(Surface surface){

        boolean success = false;
        if (mediaRecorder == null)
            mediaRecorder = new MediaRecorder();

        recordedfile = getfile();
        mediaRecorder.setPreviewDisplay(surface);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(recordedfile.getAbsolutePath());
        try {
            mediaRecorder.prepare();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;

    }

    private void StartRecording(){


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        startbutton = (Button) view.findViewById(R.id.StartRecButtonID);
        stopbutton = (Button) view.findViewById(R.id.StopRecButtonID);
        textureView = (RecordTextYiew) view.findViewById(R.id.textureView);

        startbutton.setOnClickListener(this);
        stopbutton.setOnClickListener(this);

    }

    private TextureView.SurfaceTextureListener TexSurfaceListener = new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            ConfigTextureSize(i,i1);

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private void ConfigTextureSize(int wideth, int heigth) {

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();

        RectF viewRectangle = new RectF(0,0,wideth, heigth);
        RectF bufferRectangle = new RectF(0, 0, previewSize.getWidth(), previewSize.getHeight());

        matrix.setRectToRect(viewRectangle, bufferRectangle,Matrix.ScaleToFit.FILL);

        textureView.setTransform(matrix);

    }


    @Override
    public void onClick(View view) {

    }
}
