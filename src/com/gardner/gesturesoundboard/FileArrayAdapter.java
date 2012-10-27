package com.gardner.gesturesoundboard;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileArrayAdapter extends BaseAdapter {

	private Context c;
	private List<Option> items;

	public FileArrayAdapter(Context context, List<Option> objects) {
		this.c = context;
		this.items = objects;
	}
	
	@Override
	public Option getItem(int i) {
		return items.get(i);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.file_view, null);
		}
		// if (v == null) {

		// LayoutInflater vi =
		// (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// v = vi.inflate(id, null);
		// }
		final Option o = items.get(position);
		if (o != null) {
			TextView t1 = (TextView) v.findViewById(R.id.TextView01);
			TextView t2 = (TextView) v.findViewById(R.id.TextView02);
			ImageView file_icon = (ImageView) v.findViewById(R.id.file_list_icon);

			if (t1 != null){
				t1.setText(o.getName());
			}
			if (t2 != null){
				t2.setText(o.getData());
			}
			if(o.isFile() == true){
				file_icon.setImageResource(R.drawable.music);
			}else{
				file_icon.setImageResource(R.drawable.ic_menu_archive);
			}
		}
		return v;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
