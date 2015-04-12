package com.dhl.camerawithtimer;

import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Audio.Media;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CameraUsingTimerActivity extends Activity implements
		OnClickListener, SurfaceHolder.Callback, Camera.PictureCallback {
	SurfaceView cameraView;
	SurfaceHolder surfaceHolder;
	Camera camera;

	Button startStopButton;
	TextView countdownTextView;
	Handler timerUpdateHandler;
	boolean timelapseRunning = false;
	int currentTime = 0;
	final int SECONDS_BETWEEN_PHOTOS = 15;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		cameraView = (SurfaceView) this.findViewById(R.id.CameraView);
		surfaceHolder = cameraView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);

		countdownTextView = (TextView) findViewById(R.id.CountDownTextView);
		startStopButton = (Button) findViewById(R.id.CountDownButton);
		startStopButton.setOnClickListener(this);
		timerUpdateHandler = new Handler();
	}

	public void onClick(View v) {
		if (!timelapseRunning) {
			startStopButton.setText("Stop");
			timelapseRunning = true;
			timerUpdateHandler.post(timerUpdateTask);
		} else {
			startStopButton.setText("Start");
			timelapseRunning = false;
			timerUpdateHandler.removeCallbacks(timerUpdateTask);
		}
	}

	private Runnable timerUpdateTask = new Runnable() {
		public void run() {
			if (currentTime < SECONDS_BETWEEN_PHOTOS) {
				currentTime++;
			} else {
				camera.takePicture(null, null, null, CameraUsingTimerActivity.this);
				currentTime = 0;
			}

			timerUpdateHandler.postDelayed(timerUpdateTask, 1000);
			countdownTextView.setText("" + currentTime);
		}
	};

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		camera.startPreview();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
			Camera.Parameters parameters = camera.getParameters();
			if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				parameters.set("orientation", "portrait");
				//camera.setDisplayOrientation(90);
			}
			camera.setParameters(parameters);
		} catch (IOException exception) {
			camera.release();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		Uri imageFileUri = getContentResolver().insert(
				Media.EXTERNAL_CONTENT_URI, new ContentValues());
		try {
			OutputStream imageFileOS = getContentResolver().openOutputStream(
					imageFileUri);
			imageFileOS.write(data);
			imageFileOS.flush();
			imageFileOS.close();

			Toast t = Toast.makeText(this, "Saved JPEG!", Toast.LENGTH_SHORT);
			t.show();
		} catch (Exception e) {
			Toast t = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
			t.show();
		}
		camera.startPreview();
	}
}