package com.gardner.drawitlearnit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gardner.drawitlearnit.R;


import android.app.ListActivity;
import android.os.Bundle;
import android.text.style.SuperscriptSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileChooser extends ListActivity {

	private File currentDir;
	private FileArrayAdapter adapter;
	private View fileChooserTitle;
	private ListView listView;
	private boolean inSdcardDir = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_chooser_main);
		currentDir = new File("/sdcard/");
		fill(currentDir);
	}

	private void fill(File f) {
		File[] dirs = f.listFiles(new FileChooserFilter());
		this.setTitle(getText(R.string.filechooser_curr_dir) + " | " + f.getName());
		List<Option> dir = new ArrayList<Option>();
		List<Option> fls = new ArrayList<Option>();
		try {
			for (File ff : dirs) {
				if (ff.isDirectory())
					dir.add(new Option(ff.getName(), (String) getText(R.string.filechooser_folder), ff
							.getAbsolutePath(), false));
				else {
					fls.add(new Option(ff.getName(), (String) getText(R.string.filechooser_file_size) + ": "
							+ ff.length(), ff.getAbsolutePath(), true));
				}
			}
		} catch (Exception e) {

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (!f.getName().equalsIgnoreCase("sdcard")){
			inSdcardDir = false;
			dir.add(0, new Option("..", (String) getText(R.string.filechooser_parent_directory), f.getParent(), false));
		}else{
			inSdcardDir = true;
		}
		adapter = new FileArrayAdapter(FileChooser.this, dir);
		setListAdapter(adapter);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        if(inSdcardDir == false){
	        	Option o = adapter.getItem(0);
	        	currentDir = new File(o.getPath());
				fill(currentDir);
	        	return false;
	        }else{
	        	
	        }
	    }
	    return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		v.setBackgroundColor(0x202020);
		Option o = adapter.getItem(position);
		if (o.getData().equalsIgnoreCase((String) getText(R.string.filechooser_folder))
				|| o.getData().equalsIgnoreCase((String) getText(R.string.filechooser_parent_directory))) {
			currentDir = new File(o.getPath());
			fill(currentDir);
		} else {
			onFileClick(o);
		}
	}

	private void onFileClick(Option o) {
		Toast.makeText(this, getText(R.string.filechooser_file_chosen)  + " :" + o.getPath(), Toast.LENGTH_SHORT)
				.show();
		SoundRecorder.fileChooserFile = o.getPath();
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case 100:
				finish();
				return true;
			default:
			return true;
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
			MenuItem menu_stop_all_sounds = menu.add(0, 100, 0, R.string.filechooser_cancel);
			menu_stop_all_sounds.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}
}
