package com.gardner.drawitlearnit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.gardner.services.DictionaryEntry;
import com.gardner.services.Settings;
import com.gardner.services.LongmanAPIHelper;

/**
 * A simple Activity listening for Gestures
 * 
 * visit http://www.hascode.com
 * 
 * @param <GestureActivity>
 */
public class GestureSoundboardActivity extends Activity {

	private GestureLibrary gLib;
	private static final String TAG = "com.gardner.gesturesoundboard";
	private final File mStoreFile = new File(
			Environment.getExternalStorageDirectory() + "/GestureSoundboard",
			"gestures");
	File storageDir = new File(Environment.getExternalStorageDirectory(),
			"GestureSoundboard");
	String path = Environment.getExternalStorageDirectory()
			+ "/GestureSoundboard/";
	String filename = "gestures";
	Bundle bundle;
	Intent newIntent;
	GesturesDbAdapter mDbHelper;
	Cursor cursor;
	List<MediaPlayer> soundGestureStack;
	Toast toastMessage;
	Context mContext;
	SharedPreferences sharedPrefs;
	boolean messageOne;
	public static int orientationWhenPaused;
	public static String query = "lizard";
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		if (getLastNonConfigurationInstance() != null) {
			soundGestureStack = (ArrayList<MediaPlayer>) getLastNonConfigurationInstance();
		} else {
			soundGestureStack = new ArrayList<MediaPlayer>();
		}
		orientationWhenPaused = getResources().getConfiguration().orientation;

		sharedPrefs = getSharedPreferences("gesturesoundboard",
				MODE_WORLD_WRITEABLE);
		messageOne = sharedPrefs.getBoolean("message_1", true);
		mContext = this;
		showWelcomeMessageDialog();

		gLib = GestureLibraries.fromFile(mStoreFile);
		if (!gLib.load()) {
			saveFileAs(R.raw.gestures);
			gLib = GestureLibraries.fromFile(mStoreFile);
			if (!gLib.load()) {
				Log.w(TAG, "could not load gesture library again!");
				showUnmountPhoneDialog();
			} else {
				mDbHelper = new GesturesDbAdapter(this);
				mDbHelper.open();
			}
		} else {
			mDbHelper = new GesturesDbAdapter(this);
			mDbHelper.open();
		}
		GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
		gestures.addOnGesturePerformedListener(handleGestureListener);
		gestures.setGestureColor(Color.LTGRAY);
	}

	@Override
	protected void onResume() {
		gLib = GestureLibraries.fromFile(mStoreFile);
		if (!gLib.load()) {
			Log.w(TAG, "could not load gesture library");
			finish();
		}

		super.onResume();
	}

	@Override
	protected void onPause() {
		if (toastMessage != null) {
			toastMessage.cancel();
		}
		if (cursor != null) {
			cursor.close();
		}
		if (!orientationChanged()) {
			stopAllGestureSounds();
		}else{
			FlurryAgent.onEvent("orientation_changed", null);
		}

		super.onPause();
	}

	/**
	 * our gesture listener
	 */
	private OnGesturePerformedListener handleGestureListener = new OnGesturePerformedListener() {
		@Override
		public void onGesturePerformed(GestureOverlayView gestureView,
				Gesture gesture) {

			ArrayList<Prediction> predictions = gLib.recognize(gesture);

			// one prediction needed
			if (predictions.size() > 0) {
				Prediction prediction = predictions.get(0);
				// checking prediction
				if (prediction.score > 1.0) {
					// and action
					// Toast.makeText(GestureSoundboardActivity.this,
					// prediction.name,
					// Toast.LENGTH_SHORT).show();
					playGestureSound(prediction.name);
				}else{
					FlurryAgent.onEvent("gesture_not_recognized", null);
				}
			}else{
				FlurryAgent.onEvent("prediction_not_found", null);
			}

		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		// case R.id.menu_sample_search:
		// ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
		// .showSoftInput(list, 0);
		// return true;
		case R.id.menu_about:
			showAboutDialog(this);
			return true;
		case R.id.menu_add_gesture:
			newIntent = new Intent(getApplicationContext(),
					AddGestureActivity.class);
			startActivity(newIntent);
			return true;
		case 100:
			stopAllGestureSounds();
			FlurryAgent.onEvent("stop_all_sounds", null);
			return true;
		case R.id.menu_gestures_list:
			setShowWelcomeMessageFalse();
			newIntent = new Intent(getApplicationContext(),
					GestureSoundboardListActivity.class);
			startActivity(newIntent);
			FlurryAgent.onEvent("show_gestures_list", null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeItem(100);
		if (isAnySoundPlaying()) {
			MenuItem menu_stop_all_sounds = menu.add(0, 100, 0,
					R.string.menu_stop_all_sounds);
			menu_stop_all_sounds
					.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		}
		return true;
	}

	public void playGestureSound(String name) {
		cursor = mDbHelper.fetchNotebyName(name);
		
		//flurry
		Map<String, String>  gestureItem = new HashMap<String, String>();
		gestureItem.put("gesture_name", name);
		FlurryAgent.onEvent("play_gesture_sound", gestureItem);
		
		if (cursor.getCount() > 0) {
			String path = cursor.getString(cursor
					.getColumnIndex(mDbHelper.KEY_PATH));
			if (toastMessage != null) {
				toastMessage.cancel();
			}
			// Call Pearson Api
			getPearsonInformation(name);
			
			toastMessage = Toast.makeText(GestureSoundboardActivity.this, name,
					Toast.LENGTH_SHORT);
			toastMessage.show();
		} else {
			if (toastMessage != null) {
				toastMessage.cancel();
			}
			toastMessage = Toast.makeText(GestureSoundboardActivity.this,
					R.string.gesture_no_gestures_found, Toast.LENGTH_SHORT);
			toastMessage.show();
			FlurryAgent.onEvent("gesture_not_found", gestureItem);
		}
	}

	private void getPearsonInformation(String name) {
		callWebService(name);
	}
	
	public void callWebService(String q){
		showProgressDialog();
		new RetrieveDictionaryEntryTask().execute(q);
    }
	Dialog settingsDialog;
	private void completeEntryLoad(DictionaryEntry entry) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		if (entry != null) {
			String entryText = entry.toString();
			toastMessage = Toast.makeText(GestureSoundboardActivity.this, entryText,
					Toast.LENGTH_SHORT);
			toastMessage.show();
			
			MediaPlayer player = new MediaPlayer();
			try {
				player.setDataSource(entry.getSoundPath());
				player.prepare();
				//soundGestureStack.add(player);
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
					mp.release();
				}
			});
			//player.start();
			
			// Show image dialog.
			Log.i(Settings.LOG_TAG, entry.getImagePath());
			try {
				settingsDialog = new Dialog(this);
				//ImageView image = (ImageView) settingsDialog.findViewById(R.id.dialog_image);
				//Bitmap bmImg = getImageBitmap(entry.getImagePath());
				//image.setImageBitmap(bmImg);
				settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
				settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.image_dialog
				        , null));
				settingsDialog.show();
			} catch(NullPointerException e){
				e.printStackTrace();
			}
			
		} else {
			String entryText = "Formatting error in returned response. Please try again.";
			toastMessage = Toast.makeText(GestureSoundboardActivity.this, entryText,
					Toast.LENGTH_SHORT);
			toastMessage.show();
		}
    }
	public void imageDialogClickHandler(View target) {
		settingsDialog.dismiss();
    }
    private Bitmap getImageBitmap(String imagePath){
    	URL url;
    	Bitmap bm = null;
    	InputStream is = null;
			try {
			    url = new URL(imagePath);
		        URLConnection conn;
				try {
					conn = url.openConnection();
					conn.connect();
				    is = conn.getInputStream();
				
		       
			        BufferedInputStream bis = new BufferedInputStream(is);
			        bm = BitmapFactory.decodeStream(bis);
			
			        bis.close();
			        is.close();	        
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return bm;
    }
    
    Bitmap downloadFile(String fileUrl){
          URL myFileUrl =null;          
          Bitmap bmImg = null;
          try {
               myFileUrl= new URL(fileUrl);
          } catch (MalformedURLException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
          }
          try {
               HttpURLConnection conn= (HttpURLConnection)myFileUrl.openConnection();
               conn.setDoInput(true);
               conn.connect();
               InputStream is = conn.getInputStream();
               
               bmImg = BitmapFactory.decodeStream(is);
          } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
          }
          return bmImg;
     }
    
	private void showProgressDialog() {
		progressDialog = ProgressDialog.show(this, "", "Getting stuff...", true);
	}

    protected class RetrieveDictionaryEntryTask extends AsyncTask<String, Void, DictionaryEntry> {
         @Override
         protected DictionaryEntry doInBackground(String... params) {
        	Log.w(Settings.LOG_TAG, "BALBALBABL" + params[0]);
         	try {
 				return new LongmanAPIHelper().getRandomDictionaryEntry(params[0]);
 			} catch (Exception e) {
 				Log.w(Settings.LOG_TAG, e.getClass().getSimpleName() + ", " + e.getMessage());
 				return null;
 			}
         }

         @Override
         protected void onPostExecute(DictionaryEntry entry) {
             completeEntryLoad(entry);
         }
     }
	
	public void stopAllGestureSounds() {
		for (MediaPlayer i : soundGestureStack) {
			try {
				if (i.isPlaying()) {
					i.stop();
					i.release();
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isAnySoundPlaying() {
		Log.i("is any sound playing?", "test");
		for (MediaPlayer i : soundGestureStack) {
			try {
				if (i.isPlaying()) {
					return true;
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (ConcurrentModificationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void addGestureSoundToStack(MediaPlayer newPLayer) {
		Log.i("adding to stack", newPLayer.toString());
		try {
			soundGestureStack.add(newPLayer);
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
		}
	}

	public boolean saveFileAs(int ressound) {
		byte[] buffer = null;
		InputStream fIn = getBaseContext().getResources().openRawResource(
				ressound);
		int size = 0;

		try {
			size = fIn.available();
			buffer = new byte[size];
			fIn.read(buffer);
			fIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}

		boolean exists = (new File(path)).exists();
		if (!exists) {
			new File(path).mkdirs();
		}

		FileOutputStream save;
		try {
			save = new FileOutputStream(path + filename);
			save.write(buffer);
			save.flush();
			save.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return exists;
	}

	public void showUnmountPhoneDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.gesture_please_disconnect)
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void showWelcomeMessageDialog() {
		if (messageOne) {
			WelcomeDialog dialog = new WelcomeDialog(mContext, messageOne,
					sharedPrefs);
			dialog.setContentView(R.layout.welcome_dialog);
			dialog.setTitle(R.string.welcome_title);
			dialog.show();
		}
	}

	public void setShowWelcomeMessageFalse() {
		if (messageOne) {
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putBoolean("message_1", false);
			editor.commit();
		}
	}

	public static void showAboutDialog(Context mContext) {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.about_dialog,
				(ViewGroup) ((Activity) mContext)
						.findViewById(R.id.welcome_dialog));
		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout);
		builder.setPositiveButton(mContext.getText(R.string.about_dialog_ok),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});
		alertDialog = builder.create();
		alertDialog.show();
		
		//flurry
		FlurryAgent.onEvent("show_about_dialog", null);
	}

	public boolean orientationChanged() {
		if (orientationWhenPaused != getResources().getConfiguration().orientation) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (soundGestureStack != null) {
			return (soundGestureStack);
		}
		return super.onRetainNonConfigurationInstance();
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