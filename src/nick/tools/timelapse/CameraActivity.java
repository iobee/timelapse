package nick.tools.timelapse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import nick.tools.timelapse.ui.CameraPreview;
import android.app.Activity;
import android.graphics.Path.FillType;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SimpleAdapter;

public class CameraActivity extends Activity {
	private static final String TAG = CameraActivity.class.getName();

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_TIMELAPSE_MP4 = 2;
	public static final int MEDIA_TYPE_TIMELAPSE_TGA = 3;

	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mMediaRecorder;

	private Button btCapture;
	private EditText etRate;
	private EditText etTotalTime;
	private Chronometer chronometerCountUp;

	private Boolean isRecoding = false;

	/** 统计已经拍摄了几张照片 */
	private int hasCaptureNum;

	private Handler myHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "-->onCreate");

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_camera);

		chronometerCountUp = (Chronometer) findViewById(R.id.Chronometer_CountUp);

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview, 0);

		preview.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCamera.autoFocus(new Camera.AutoFocusCallback() {

					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						// TODO Auto-generated method stub
						Log.i(TAG, success + "");
					}
				});
			}
		});

		btCapture = (Button) findViewById(R.id.Button_Capture);
		etRate = (EditText) findViewById(R.id.EditText_Rate);

		btCapture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//startCaptureAsMP4();
				startCaptureAsTGA();
			}
		});
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
			Camera.Parameters parameters = c.getParameters();
			List<String> focusModes = parameters.getSupportedFocusModes();
			if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				Log.i(TAG, "true");
			}
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			c.setParameters(parameters);
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		// mCamera.takePicture(null, null, mPicture);

		return true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		releaseMediaRecorder();
		releaseCamera();
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub

			try {
				File jpegFile = getOutputFilePath(MEDIA_TYPE_TIMELAPSE_TGA, "TEST");
				if (!jpegFile.exists())
					jpegFile.createNewFile();

				FileOutputStream fOS = new FileOutputStream(jpegFile);
				fOS.write(data);
				fOS.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private boolean prepareVideoRecorder(double rate) {
		mCamera = getCameraInstance();
		mMediaRecorder = new MediaRecorder();

		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);

		// mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		mMediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_TIME_LAPSE_1080P));

		mMediaRecorder.setOutputFile(getOutputFilePath(0, null).toString());

		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

		mMediaRecorder.setCaptureRate(rate);

		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG,
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private File getOutputFilePath(int type, String fileName) {
		String timeStamp;
		if (fileName == null) {
			timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());
		} else {
			timeStamp = fileName;
		}
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), "timelapse");
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("TimeLapse", "failed to create directory");
				return null;
			}
		}

		File mediaFile;
		if (type == MEDIA_TYPE_TIMELAPSE_TGA) {
			File mediaStorageTimelapse = new File(mediaStorageDir.getPath(),
					fileName);
			if (!mediaStorageTimelapse.exists()) {
				if (!mediaStorageTimelapse.mkdir()) {
					Log.d(timeStamp, "failed to create directory");
					return null;
				}
			}

			mediaFile = new File(mediaStorageTimelapse.getPath()
					+ File.separator + fileName + "_" + hasCaptureNum + ".jpg");
			hasCaptureNum++;
		} else if (type == MEDIA_TYPE_TIMELAPSE_MP4) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "PIC_" + timeStamp + ".jpg");
		} else {
			mediaFile = null;
		}

		return mediaFile;
	}

	private void startCaptureAsMP4() {
		double rate = Double.parseDouble(etRate.getText().toString());

		if (isRecoding) {
			chronometerCountUp.stop();
			mMediaRecorder.stop();
			releaseMediaRecorder();

		} else {
			releaseCamera();

			if (prepareVideoRecorder(rate)) {
				mMediaRecorder.start();
				isRecoding = true;
				btCapture.setText("stop");
				chronometerCountUp.start();
			} else {
				releaseMediaRecorder();
			}
		}
	}

	private void startCaptureAsTGA() {
		if (!isRecoding) {
			myHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mCamera.takePicture(null, null, mPicture);
					myHandler.postDelayed(this, 1000);
				}
			}, 1000);
			isRecoding = true;
		} else {
			isRecoding = false;
			myHandler.removeCallbacks(null, null);
		}
	}

}
