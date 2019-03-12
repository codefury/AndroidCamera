package com.codefury16.androidcamera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.aviary.android.feather.sdk.AviaryIntent;
import com.aviary.android.feather.sdk.internal.filters.ToolLoaderFactory;
import com.aviary.android.feather.sdk.internal.headless.utils.MegaPixels;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("deprecation")
public class AndroidCamera extends AppCompatActivity {

    private static boolean flashFlag = false;
    private static boolean lastCamFront;
    private Context myContext;
    private Camera mCamera;
    OnClickListener flashListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                Camera.Parameters pFlash = mCamera.getParameters();
                if (!flashFlag) {
                    pFlash.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    mCamera.setParameters(pFlash);
                    flash.setImageResource(R.drawable.flashon);
                    flashFlag = true;
                } else {
                    pFlash.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(pFlash);
                    flash.setImageResource(R.drawable.flashoff);
                    flashFlag = false;
                }
            } else {
                Utils.showSnackBar(AndroidCamera.this, "Your phone does not support flash.");
            }
        }
    };
    private int timerValue = 0, timerCount = 0;
    private Animation timerAnim, shootAnim;
    private CameraPreview mPreview;
    private PictureCallback mPicture;
    private ImageButton capture, switchCamera, flash, gallery, timer, stopTimer;
    private TextViewThin timerText;
    OnClickListener galleryListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            File myDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidCamera");
            if (!myDir.exists()) myDir.mkdir();
            Intent intent = new Intent(AndroidCamera.this, SimpleImageActivity.class);
            intent.putExtra(ImageConstant.Extra.FRAGMENT_INDEX, ImageGridFragment.INDEX);
            startActivity(intent);

            //clear timer
            timerValue = 0;
            timerCount = 0;
            timerText.setVisibility(View.GONE);
        }
    };
    private File pictureFile;
    private ProgressDialog progressCapture;
    private RelativeLayout stopTimerLayout, cameraButtons;
    private boolean cameraFront, isTimerOn = false;
    private CameraInfo info;
    OnClickListener switchCameraListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int camerasNumber = Camera.getNumberOfCameras(); // get the number of cameras
            if (camerasNumber > 1) {

                releaseCamera(); // release the old camera instance
                chooseCamera(); // switch camera, from the front and the back and vice versa
            } else {
                Toast toast = Toast.makeText(myContext,
                        "Sorry, your phone has only one camera!",
                        Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };
    private ToolLoaderFactory.Tools[] tools = {
            ToolLoaderFactory.Tools.EFFECTS,
            ToolLoaderFactory.Tools.LIGHTING,
            ToolLoaderFactory.Tools.CROP,
            ToolLoaderFactory.Tools.ORIENTATION};
    private boolean isRunning = false;
    private android.os.Handler mHandler;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            updateTimer();
            int mInterval = 1000; // 5 seconds by default, can be changed later
            mHandler.postDelayed(mStatusChecker, mInterval);
            if (timerValue == -1)
                captureAfterTimer();
        }
    };
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isRunning) {
                isRunning = true;
                if (timerValue > 0) {
                    capture.startAnimation(shootAnim);
                    startRepeatingTask();
                    closeTimer();
                    capture.setEnabled(false);
                } else {
                    capture.startAnimation(shootAnim);
                    mCamera.takePicture(null, null, mPicture);
                    capture.setEnabled(false);
                }
            } else
                Toast.makeText(AndroidCamera.this, "Please wait...", Toast.LENGTH_SHORT).show();
        }
    };
    OnClickListener captureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (timerValue > 0) {
                isRunning = true;
                capture.startAnimation(shootAnim);
                startRepeatingTask();
                closeTimer();
                capture.setEnabled(false);
            } else {
                capture.startAnimation(shootAnim);
                mCamera.takePicture(null, null, mPicture);
                capture.setEnabled(false);
            }
        }
    };
    OnClickListener timerStopListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            capture.setEnabled(true);
            stopRepeatingTask();
            timerCount = 0;
            timerValue = 0;
            timerText.setVisibility(View.GONE);
            timerText.clearAnimation();
            closeTimer();
        }
    };
    OnClickListener timerListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if (timerCount == 0) {
                timerValue = 3;
            }
            if (timerCount == 1) {
                timerValue = 5;
            }
            if (timerCount == 2) {
                timerValue = 10;
            }
            if (timerCount == 3) {
                timerValue = 0;
                timerText.setVisibility(View.GONE);
            } else {
                timerText.setVisibility(View.VISIBLE);
            }
            timerCount++;
            timerCount = timerCount % 4;
            timerText.setText(String.valueOf(timerValue));
            timerText.setTextColor(Color.parseColor("#FFFFFF"));
            timerText.setTextSize(150);
            stopRepeatingTask();
        }
    };

    /**
     * A safe way to get an instance of the Camera object.
     */

    public static Camera getCameraInstance(int cam) {
        Camera c = null;
        try {
            c = Camera.open(cam); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
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

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    // make picture and save to a folder
    private static File getOutputMediaFile() {
        // make a new file directory inside the "sdcard" folder
        File myDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidCamera");

        if (!myDir.exists()) {
            // if you cannot make this folder return
            if (!myDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "AndroidCamera" + timeStamp + ".jpg";
        return new File(myDir, filename);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.android_camera);
        //screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //full brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);

        myContext = this;

        timerText = findViewById(R.id.cameraText);
        timerText.setVisibility(View.GONE);

        timerAnim = AnimationUtils.loadAnimation(this, R.anim.timeranim);
        shootAnim = AnimationUtils.loadAnimation(this, R.anim.shootanim);

        initialize();
        /*Intent intent = AviaryIntent.createCdsInitIntent(getBaseContext());
        startService(intent);*/

        mHandler = new android.os.Handler();

        findViewById(R.id.frameCamera).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();

                    Rect touchRect = new Rect(
                            (int) (x - 100),
                            (int) (y - 100),
                            (int) (x + 100),
                            (int) (y + 100));
                    final Rect targetFocusRect = new Rect(
                            touchRect.left * 2000 / mPreview.getWidth() - 1000,
                            touchRect.top * 2000 / mPreview.getHeight() - 1000,
                            touchRect.right * 2000 / mPreview.getWidth() - 1000,
                            touchRect.bottom * 2000 / mPreview.getHeight() - 1000);
                    if (!cameraFront) {
                        mPreview.doTouchFocus(targetFocusRect);
                        ImageView imageView = findViewById(R.id.focusImage);
                        imageView.setX(x - 100);
                        imageView.setY(y - 100);
                        imageView.setVisibility(View.VISIBLE);
                    }

                    // Remove the square after some time
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            findViewById(R.id.focusImage).setVisibility(View.INVISIBLE);
                        }
                    }, 1000);

                }
                return true;
            }
        });
    }

    public void initialize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        capture = findViewById(R.id.button_capture);
        android.view.ViewGroup.LayoutParams params = capture.getLayoutParams();
        params.width = width / 5;
        params.height = width / 5;
        capture.setLayoutParams(params);
        capture.setOnClickListener(captureListener);

        switchCamera = findViewById(R.id.button_ChangeCamera);
        android.view.ViewGroup.LayoutParams params2 = switchCamera.getLayoutParams();
        params2.width = width / 5;
        params2.height = width / 5;
        switchCamera.setLayoutParams(params2);
        switchCamera.setOnClickListener(switchCameraListener);

        timer = findViewById(R.id.button_timer);
        android.view.ViewGroup.LayoutParams params3 = timer.getLayoutParams();
        params3.width = width / 5;
        params3.height = width / 5;
        timer.setLayoutParams(params3);
        timer.setOnClickListener(timerListener);

        flash = findViewById(R.id.button_flash);
        android.view.ViewGroup.LayoutParams params4 = flash.getLayoutParams();
        params4.width = width / 5;
        params4.height = width / 5;
        flash.setLayoutParams(params4);
        flash.setOnClickListener(flashListener);

        gallery = findViewById(R.id.button_gallery);
        android.view.ViewGroup.LayoutParams params5 = gallery.getLayoutParams();
        params5.width = width / 5;
        params5.height = width / 5;
        gallery.setLayoutParams(params5);
        gallery.setOnClickListener(galleryListener);

        stopTimer = findViewById(R.id.stopTimer);
        stopTimer.setOnClickListener(timerStopListener);
        stopTimerLayout = findViewById(R.id.stopTimerLayout);
        stopTimerLayout.setVisibility(View.GONE);
        cameraButtons = findViewById(R.id.cameraButtons);
        lastCamFront = true;
    }

    public void onResume() {
        super.onResume();
        capture.setEnabled(true);
        closeTimer();
        capture.clearAnimation();
        setUpCamera();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SINGLE_PRESS");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void setUpCamera() {
        if (!checkCameraHardware(myContext)) {
            Toast.makeText(myContext,
                    "Sorry, your phone does not have a camera!",
                    Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (mCamera == null) {
                if (!lastCamFront) {
                    mCamera = getCameraInstance(findBackFacingCamera());
                    lastCamFront = false;
                    setCameraDisplayOrientation(AndroidCamera.this, 0, mCamera);
                    flash.setEnabled(true);
                    flash.setAlpha(1.0f);
                } else {
                    if (!myContext.getPackageManager().hasSystemFeature(
                            PackageManager.FEATURE_CAMERA_FRONT)) {
                        Utils.showSnackBar(AndroidCamera.this, "No front facing camera found.");
                        switchCamera.setAlpha(0.3f);
                        switchCamera.setEnabled(false);
                        mCamera = getCameraInstance(findBackFacingCamera());
                        lastCamFront = false;
                        setCameraDisplayOrientation(AndroidCamera.this, 0, mCamera);
                        flash.setEnabled(true);
                        flash.setAlpha(1.0f);
                    } else {
                        mCamera = getCameraInstance(findFrontFacingCamera());
                        setCameraDisplayOrientation(AndroidCamera.this, 1, mCamera);
                        lastCamFront = true;
                        flash.setEnabled(false);
                        flash.setAlpha(0.3f);
                    }
                }
            }
            mPicture = getPictureCallback();
            mPreview = new CameraPreview(myContext, mCamera);
            ((FrameLayout) findViewById(R.id.frameCamera)).addView(mPreview);
        }
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        info = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    private void closeTimer() {
        capture.setEnabled(true);
        if (isTimerOn) {
            stopTimerLayout.setVisibility(View.VISIBLE);
            cameraButtons.setVisibility(View.GONE);
            capture.clearAnimation();
        } else {
            isRunning = false;
            stopTimerLayout.setVisibility(View.GONE);
            cameraButtons.setVisibility(View.VISIBLE);
        }

    }

    private void galleryAddPic(String s) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(s);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Bundle extra = data.getExtras();
                    if (null != extra) {
                        startActivity(data.setClass(AndroidCamera.this, ShareSelfie.class));
                    }
                    break;
            }
        }
    }

    private PictureCallback getPictureCallback() {
        return new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                pictureFile = getOutputMediaFile();
                if (pictureFile == null) {
                    return;
                }
                new NextActivity().execute(data);
            }
        };
    }

    private void releaseCamera() {
        // stop and release camera
        mCamera.release();
        mPreview.getHolder().removeCallback(mPreview);

    }

    public void chooseCamera() {
        // if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            lastCamFront = false;
            if (cameraId >= 0) {
                mCamera = getCameraInstance(cameraId);
                setCameraDisplayOrientation(this, cameraId, mCamera);
                flash.setVisibility(View.VISIBLE);
                flash.setEnabled(true);
                flash.setAlpha(1.0f);
                //mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);

            }
        } else {
            int cameraId = findFrontFacingCamera();
            lastCamFront = true;
            if (cameraId >= 0) {
                mCamera = getCameraInstance(cameraId);
                setCameraDisplayOrientation(this, cameraId, mCamera);
                flash.setEnabled(false);
                flash.setAlpha(0.3f);
                //mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    void updateTimer() {
        if (timerValue >= 0) {
            timerText.setText(String.valueOf(timerValue));
            timerText.startAnimation(timerAnim);
            timerValue--;
        }
    }


    void startRepeatingTask() {
        isTimerOn = true;
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        isTimerOn = false;
        mHandler.removeCallbacks(mStatusChecker);
    }

    void captureAfterTimer() {
        stopRepeatingTask();
        mCamera.takePicture(null, null, mPicture);
        timerText.setVisibility(View.GONE);
        timerValue = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.lock();
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mCamera.takePicture(null, null, mPicture);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    class NextActivity extends AsyncTask<byte[], Void, Void> {
        String filepath;

        public NextActivity() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressCapture = new ProgressDialog(AndroidCamera.this, ProgressDialog.THEME_HOLO_LIGHT);
            progressCapture.setMessage("Hold on...");
            progressCapture.setIndeterminate(true);
            progressCapture.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressCapture.dismiss();

            Uri uri = Uri.parse(filepath);
            final Intent newIntent = new AviaryIntent.Builder(getApplicationContext())
                    .setData(uri) // input image src
                    .withOutput(uri) // output file
                    .withOutputFormat(Bitmap.CompressFormat.JPEG) // output format
                    .withOutputQuality(95)// output quality
                    .withOutputSize(MegaPixels.Mp5)
                    .saveWithNoChanges(true)
                    .withNoExitConfirmation(false)
                    .withToolList(tools)
                    .build();
            startActivityForResult(newIntent, 1);

            mPreview.refreshCamera(mCamera);
            isRunning = false;
        }

        @Override
        protected Void doInBackground(byte[]... data) {

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data[0]);
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            filepath = pictureFile.getAbsolutePath();
            galleryAddPic(filepath);
            return null;
        }
    }

    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private final String TAG = "Preview";
        private SurfaceHolder mHolder;
        private Camera.Size previewSize;
        //private Camera.Size pictureSize;
        private List<Camera.Size> mSupportedPreviewSizes;
        private Camera mCamera;
        Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {

            @Override
            public void onAutoFocus(boolean arg0, Camera arg1) {
                if (arg0) {
                    mCamera.cancelAutoFocus();
                }
            }
        };

        @SuppressWarnings("deprecation")
        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }
            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;

            if (mCamera != null) {
                Camera.Parameters params = mCamera.getParameters();
                requestLayout();
                invalidate();
                params.setPreviewSize(previewSize.width, previewSize.height);

                if (cameraFront && (currentapiVersion < android.os.Build.VERSION_CODES.LOLLIPOP)) {
                    List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
                    Camera.Size size = sizes.get(0);
                    params.setPreviewSize(size.width, size.height);
                }
                List<Camera.Size> pSizes = params.getSupportedPictureSizes();
                Camera.Size pSelected = pSizes.get(1);
                params.setPictureSize(pSelected.width, pSelected.height);

                List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                if (!cameraFront)
                    params.setRotation(90);
                else
                    params.setRotation(270);
                mCamera.setParameters(params);
            }
            try {
                assert mCamera != null;
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mCamera == null) {
                try {
                    mCamera.startPreview();
                    setCamera(mCamera);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We purposely disregard child measurements because act as a
            // wrapper to a SurfaceView that centers the camera preview instead
            // of stretching it.
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);
            if (mSupportedPreviewSizes != null) {
                previewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
            float ratio;
            if (previewSize.height >= previewSize.width)
                ratio = (float) previewSize.height / (float) previewSize.width;
            else
                ratio = (float) previewSize.width / (float) previewSize.height;
            setMeasuredDimension(width, (int) (width * ratio));
        }

        public void refreshCamera(Camera camera) {
            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }
            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }
            setCamera(camera);
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
                Log.d(VIEW_LOG_TAG,
                        "Error starting camera preview: " + e.getMessage());
            }
        }

        public void setCamera(Camera camera) {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            mCamera = camera;
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(previewSize.width, previewSize.height);
            if (cameraFront && (currentapiVersion < android.os.Build.VERSION_CODES.LOLLIPOP)) {
                List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
                Camera.Size size = sizes.get(0);
                params.setPreviewSize(size.width, size.height);
            }


            List<Camera.Size> pSizes = params.getSupportedPictureSizes();
            Camera.Size pSelected = pSizes.get(1);
            params.setPictureSize(pSelected.width, pSelected.height);

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            if (!cameraFront)
                params.setRotation(90);
            else
                params.setRotation(270);
            mCamera.setParameters(params);
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) h / w;

            if (sizes == null) return null;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;


            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }

            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - h) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - h);
                    }
                }
            }
            return optimalSize;
        }

        public void doTouchFocus(final Rect tfocusRect) {
            Log.i(TAG, "TouchFocus");
            if (!lastCamFront) {
                try {
                    mCamera.cancelAutoFocus();
                    final List<Camera.Area> focusList = new ArrayList<>();
                    Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
                    focusList.add(focusArea);

                    Camera.Parameters para = mCamera.getParameters();
                    para.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    para.setFocusAreas(focusList);
                    para.setMeteringAreas(focusList);
                    mCamera.setParameters(para);

                    mCamera.autoFocus(myAutoFocusCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "Unable to auto-focus");
                }
            }
        }
    }
}
