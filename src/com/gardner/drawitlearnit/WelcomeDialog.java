package com.gardner.drawitlearnit;
import com.gardner.drawitlearnit.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;


public class WelcomeDialog extends Dialog {
	
	Context mContext;
	Intent newIntent;
	boolean messageOne;
	SharedPreferences sharedPrefs;
	WelcomeDialog thisDialog;
	
	public WelcomeDialog(Context context, boolean messageOne, SharedPreferences sharedPrefs) {
		super(context);
		mContext = context;
		this.messageOne = messageOne;
		this.sharedPrefs = sharedPrefs;
		thisDialog = this;
		
	}
	public boolean onCreateOptionsMenu(Menu menu){
		
		menu.add(0, R.id.menu_gestures_list, 0, R.string.menu_gestures_list)
		.setIcon(R.drawable.ic_gestures_list)
		.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if(messageOne){
					SharedPreferences.Editor editor = sharedPrefs.edit();
					editor.putBoolean("message_1", false);
					editor.commit();
				}
				thisDialog.cancel();
				newIntent = new Intent(mContext, GestureSoundboardListActivity.class);
				mContext.startActivity(newIntent);
				return false;
			}
		});
		
		menu.add(1, R.id.menu_about, 0, R.string.menu_about)
		.setIcon(android.R.drawable.ic_menu_info_details)
		.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				GestureSoundboardActivity.showAboutDialog(mContext);
				return true;
			}
		});
		
		return true;
	}
}
