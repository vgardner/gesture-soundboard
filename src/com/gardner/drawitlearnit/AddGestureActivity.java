package com.gardner.drawitlearnit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.flurry.android.FlurryAgent;
import com.gardner.drawitlearnit.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AddGestureActivity extends Activity {
	private static final float LENGTH_THRESHOLD = 120.0f;

	private Gesture mGesture;
	public Button btRecord;
	public Button btPlay;
	public SoundRecorder recorder;
	private GesturesDbAdapter mDbHelper;
	SharedPreferences sharedPrefs;
	boolean messageTwo;
	Map<String, String>  addGestureFlurryEvent = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_gesture);
		
		sharedPrefs = getSharedPreferences("gesturesoundboard",
				MODE_WORLD_WRITEABLE);
		messageTwo = sharedPrefs.getBoolean("message_2", true);
		showFirstGestureMessage();

		GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
		// overlay.setGestureColor(R.color.gesture_color);
		overlay.addOnGestureListener(new GesturesProcessor());
		
		btRecord = (Button) findViewById(R.id.btRecord);
		btPlay = (Button) findViewById(R.id.btPlay);
		
		recorder = new SoundRecorder(btRecord, btPlay, this);
		recorder.setCurrentOrientation(getResources().getConfiguration().orientation);
		recorder.setFlurryEventData(addGestureFlurryEvent);
		
		if(getLastNonConfigurationInstance() != null){
			recorder.player = (MediaPlayer) getLastNonConfigurationInstance();
			recorder.player.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					btPlay.setText(R.string.add_gesture_play);
					recorder.playing = false;
				}
			});
			btPlay.setEnabled(true);
		}
		
	}

	public void onResume() {
		super.onResume();

		if (SoundRecorder.fileChooserFile != null) {
			Log.i("testingsound", SoundRecorder.fileChooserFile);
			recorder.setPlayerFile(SoundRecorder.fileChooserFile);
		}
	}
	
	public void onPause(){
		if(recorder.player != null && recorder.player.isPlaying()){
			recorder.stopPlay();
		}
		if(recorder.recording == true){
			recorder.stopRecord();
		}
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			final TextView input = (TextView) findViewById(R.id.gesture_name);
			final CharSequence name = input.getText();
			if (name.length() != 0 || mGesture != null || recorder.newplayer == false) {
				showBackButtonDialog();
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mGesture != null) {
			outState.putParcelable("gesture", mGesture);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mGesture = savedInstanceState.getParcelable("gesture");
		if (mGesture != null) {
			final GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
			overlay.post(new Runnable() {
				public void run() {
					overlay.setGesture(mGesture);
				}
			});
		}
	}

	@SuppressWarnings({ "UnusedDeclaration" })
	public void addGesture() {
		final TextView input = (TextView) findViewById(R.id.gesture_name);
		final CharSequence name = input.getText();
		final GestureLibrary store = GestureSoundboardListActivity.getStore();
		if (name.length() == 0) {
			input.setError(getString(R.string.error_missing_name));
			
			//flurry
			addGestureFlurryEvent.put("validation_error", "missing_title");
			return;
		}
		if (store.getGestures(name.toString()) != null) {
			input.setError(String.format(getResources().getString(R.string.add_gesture_another_title), name));
			
			//flurry
			addGestureFlurryEvent.put("validation_error", "same_title");
			return;
		}
		if(recorder.newplayer == true){
			showSoundMandatoryDialog();
			
			//flurry
			addGestureFlurryEvent.put("validation_error", "missing_sound");
			return;
		}
		if (mGesture != null && mGesture.getLength() > LENGTH_THRESHOLD) {
			
			store.addGesture(name.toString(), mGesture);
			store.save();
			setResult(RESULT_OK);
			
			//save info in db
			mDbHelper = new GesturesDbAdapter(this);
	        mDbHelper.open();
	        mDbHelper.createGesture(name.toString(), recorder.getCurrentSoundPath());
	        mDbHelper.close();
	        
	        //flurry
	    	addGestureFlurryEvent.put("save_gesture", name.toString());
			
	        renewRecorder();
	        setFirstGestureMessageFalse();
			final String path = new File(
					Environment.getExternalStorageDirectory() + "/GestureSoundboard", "gestures")
					.getAbsolutePath();
			Toast.makeText(this, getString(R.string.save_success, path),
					Toast.LENGTH_LONG).show();
		} else {
			showGestureMandatoryDialog();
			
			//flurry
			addGestureFlurryEvent.put("validation_error", "missing_gesture");
			return;
		}
		
		//flurry
		FlurryAgent.onEvent("add_gesture", addGestureFlurryEvent);
		
		finish();
	}

	@SuppressWarnings({ "UnusedDeclaration" })
	public void cancelGesture(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.layout.add_gesture_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_gesture_save:
			try {
				addGesture();
			} catch (Exception e) {
				Log.i("menu_gesture_save", e.toString());
			}
			return true;
		case R.id.menu_gesture_cancel:
			renewRecorder();
			
			//flurry
			addGestureFlurryEvent.put("cancel_gesture", "menu_button");
			FlurryAgent.onEvent("add_gesture", addGestureFlurryEvent);
			
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class GesturesProcessor implements
			GestureOverlayView.OnGestureListener {
		public void onGestureStarted(GestureOverlayView overlay,
				MotionEvent event) {
			mGesture = null;
		}

		public void onGesture(GestureOverlayView overlay, MotionEvent event) {
		}

		public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
			mGesture = overlay.getGesture();
			if (mGesture.getLength() < LENGTH_THRESHOLD) {
				overlay.clear(false);
			}
		}

		public void onGestureCancelled(GestureOverlayView overlay,
				MotionEvent event) {
		}
	}

	private void showGestureMandatoryDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.add_gesture_draw_gesture_error)
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void showSoundMandatoryDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.add_gesture_sound_error)
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showBackButtonDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.add_gesture_save_confirmation)
				.setCancelable(false)
				.setPositiveButton(R.string.add_gesture_yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								addGesture();
								dialog.cancel();
								return;
							}
						})
				.setNegativeButton(R.string.add_gesture_no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								renewRecorder();
								//flurry
								addGestureFlurryEvent.put("cancel_gesture", "back_button");
								FlurryAgent.onEvent("add_gesture", addGestureFlurryEvent);
								finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
	public void renewRecorder(){
		recorder.newplayer = true;
		btPlay.setEnabled(false);
		recorder.songExists = false;
	}
	@Override
	public Object onRetainNonConfigurationInstance() {
	  if (recorder.player != null){
	      return(recorder.player);
	  }
	  return super.onRetainNonConfigurationInstance();
	}
	public void showFirstGestureMessage(){
		if (messageTwo) {
			AlertDialog.Builder builder;
			AlertDialog alertDialog;

			LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.second_message_dialog,
			                               (ViewGroup) ((Activity) this).findViewById(R.id.welcome_dialog));
			builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			builder.setPositiveButton(this.getText(R.string.about_dialog_ok), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			alertDialog = builder.create();
			alertDialog.show();
		}
	}
	public void setFirstGestureMessageFalse() {
		if (messageTwo) {
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putBoolean("message_2", false);
			editor.commit();
		}
	}
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, "R18N8TESJ5JQEFAGC2N9");
	}

	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
}
