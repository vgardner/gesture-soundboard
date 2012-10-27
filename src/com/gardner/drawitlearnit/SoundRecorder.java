package com.gardner.drawitlearnit;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.gardner.drawitlearnit.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SoundRecorder {
	private static final String APP_TAG = "GestureSoundboard";

	private MediaRecorder recorder;
	public MediaPlayer player;
	public Button btRecord;
	public Button btPlay;
	private TextView resultView;

	public boolean recording = false;
	public boolean playing = false;
	private File outfile = null;
	private Activity mContext;
	public boolean newplayer = true;
	public static boolean songExists = false;
	public static String fileChooserFile;
	public String recordYourSound;
	public String chooseFromSd;
	public String[] items;
	public String currentSoundPath;
	ProgressDialog progressDialogBuilder;
	public int currentOrientation;
	public  Map<String, String>  flurryEventData;

	public SoundRecorder(Button btRecord, Button btPlay, Activity mContext) {
		this.btPlay = btPlay;
		this.btRecord = btRecord;
		this.mContext = mContext;
		recordYourSound = (String) mContext
				.getText(R.string.add_gesture_record_your_sound);
		chooseFromSd = (String) mContext
				.getText(R.string.add_gesture_choose_file);
		
		btRecord.setOnClickListener(handleRecordClick);
		btPlay.setOnClickListener(handlePlayClick);
		
	}

	public void initializeSoundRecorder() {
		recorder = new MediaRecorder();
		player = new MediaPlayer();
		try {
			// the soundfile
			File storageDir = new File(
					Environment.getExternalStorageDirectory(),
					"GestureSoundboard");
			storageDir.mkdir();
			Log.d(APP_TAG, "Storage directory set to " + storageDir);
			outfile = File.createTempFile("gesturesoundboard", ".3gp",
					storageDir);

			// init recorder
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(outfile.getAbsolutePath());
			Log.i("file", outfile.getAbsolutePath());
			currentSoundPath = outfile.getAbsolutePath();
			// init player
			newplayer = true;
			player.setDataSource(outfile.getAbsolutePath());
			player.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					btPlay.setText(R.string.add_gesture_play);
					playing = false;
				}
			});
		} catch (IOException e) {
			Log.w(APP_TAG, "File not accessible ", e);
		} catch (IllegalArgumentException e) {
			Log.w(APP_TAG, "Illegal argument ", e);
		} catch (IllegalStateException e) {
			Log.w(APP_TAG, "Illegal state, call reset/restore", e);
		}
	}

	public final OnClickListener handleRecordClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			flurryEventData.put("add_sound_button", "clicked");
			if(playing){
				stopPlay();
			}
			showSoundSelectionDialog();
		}
	};

	public final OnClickListener handlePlayClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			flurryEventData.put("play_button", "clicked");
			if (!playing) {
				startPlay();
			} else {
				stopPlay();
			}
		}
	};

	private void startRecord() {
		try {
			recorder.prepare();
			recorder.start();
			recording = true;
			songExists = true;
			btPlay.setEnabled(true);
			mContext.setRequestedOrientation(getCurrentOrientation());
		} catch (IllegalStateException e) {
			Log.w(APP_TAG,
					"Invalid recorder state .. reset/release should have been called");
		} catch (IOException e) {
			Log.w(APP_TAG, "Could not write to sd card");
		}
	}

	public void stopRecord() {
		recorder.stop();
		recorder.reset();
		recording = false;
		progressDialogBuilder.dismiss();
		if (newplayer) {
			try {
				player.prepare();
			}catch (IOException e) {
					Log.w(APP_TAG, "Could not write to sd card " + e.toString());
			 }
			newplayer = false;
		}
		mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	private void startPlay() {
		Log.d(APP_TAG, "starting playback..");
		// printResult("start playing..");
		try {
			playing = true;
			player.start();
			btPlay.setText(R.string.add_gesture_stop);
		} catch (IllegalStateException e) {
			Log.w(APP_TAG, "illegal state .. player should be reset");
		}
	}

	public void stopPlay() {
		Log.d(APP_TAG, "stopping playback..");
		// printResult("stop playing..");
		player.pause();
		player.seekTo(200);
		// player.reset();
		btPlay.setText(R.string.add_gesture_play);
		// player.release();
		playing = false;
	}

	private void printResult(String result) {
		resultView.setText(result);
	}

	private void showRecorOverwriteConfirmationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage(R.string.add_gesture_overwrite)
				.setCancelable(false)
				.setPositiveButton(R.string.add_gesture_yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								handleRecordButton();
								dialog.cancel();
							}
						})
				.setNegativeButton(R.string.add_gesture_no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void buildRecordStatusDialog() {
		progressDialogBuilder = new ProgressDialog(mContext);
		progressDialogBuilder.setMessage(mContext.getText(R.string.add_gesture_recording));
		progressDialogBuilder.setCancelable(false);
		progressDialogBuilder.setButton(mContext.getText(R.string.add_gesture_stop),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								stopRecord();
								dialog.cancel();
							}
						});
	}
	
	private void showRecordStatusDialog() {
		buildRecordStatusDialog();
		progressDialogBuilder.show();
	}

	private void handleRecordButton() {
		if (!recording) {
			initializeSoundRecorder();
			showRecordStatusDialog();
			startRecord();
		} else {
			stopRecord();
		}
	}

	private void soundSelectionRecord() {
		if (songExists == true) {
			showRecorOverwriteConfirmationDialog();
		} else {
			handleRecordButton();
		}
	}
	
	private void showSoundSelectionDialog() {
		loadSoundSource();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.add_gesture_sound_chooser);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					soundSelectionRecord();
					//flurry
					flurryEventData.put("sound_source", "record");
					break;
				case 1:
					showFileChooser();
					flurryEventData.put("sound_source", "choose_file");
					break;
				}
				dialog.cancel();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showFileChooser() {
		Intent newIntent = new Intent(mContext.getApplicationContext(),
				FileChooser.class);
		mContext.startActivity(newIntent);
	}

	public void setPlayerFile(String path) {
		player = new MediaPlayer();
		
		try {
			// init player
			newplayer = false;
			player.setDataSource(path);
			player.prepare();
			currentSoundPath = path;
			btPlay.setEnabled(true);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				btPlay.setText(R.string.add_gesture_play);
				playing = false;
			}
		});
	}

	/**
	 * Create all the choices for the list
	 */
	private void loadSoundSource() {
		items = new String[2];

		// define the display string, the image, and the value to use
		// when the choice is selected
		items[0] = recordYourSound;
		items[1] = chooseFromSd;
	}
	public String getCurrentSoundPath(){
		return currentSoundPath;
	}
	public void addSoundDialog(){
		Dialog dialog = new Dialog(mContext);

		dialog.setContentView(R.layout.add_sound_dialog);
		dialog.setTitle("Custom Dialog");
		dialog.show();
	}

	public int getCurrentOrientation() {
		return currentOrientation;
	}

	public void setCurrentOrientation(int currentOrientation) {
		this.currentOrientation = currentOrientation;
	}
	
	public void setFlurryEventData(Map<String, String> flurryEventData) {
		this.flurryEventData = flurryEventData;
	}
}