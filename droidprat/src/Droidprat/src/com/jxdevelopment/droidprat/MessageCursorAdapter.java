package com.jxdevelopment.droidprat;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MessageCursorAdapter extends SimpleCursorAdapter {
	public MessageCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	//private LayoutInflater mInflater;
/*
	public MessageCursorAdapter(Context context, Cursor c) {
		super(context, c);
		//mInflater = LayoutInflater.from(context);
	}*/

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Get view holder
/*		ViewHolder holder;
		if (view == null) {
			view = mInflater.inflate(R.layout.msg_row, null);
			holder = new ViewHolder();
			holder.username = (TextView) view.findViewById(R.id.tvUser);
			holder.body = (TextView) view.findViewById(R.id.tvBody);
			holder.avatar = (LoaderImageView) view.findViewById(R.id.ivAvatar);

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
*/
		
		// Get view refs.
		TextView username = (TextView) view.findViewById(R.id.tvUser);
		TextView body = (TextView) view.findViewById(R.id.tvBody);
		LoaderImageView avatar = (LoaderImageView) view.findViewById(R.id.ivAvatar);

		String msg_body = cursor.getString(cursor.getColumnIndex("body"));
		String msg_username = cursor.getString(cursor.getColumnIndex("username"));
		String msg_avatar = cursor.getString(cursor.getColumnIndex("avatar"));
		int msg_bodycolor = cursor.getInt(cursor.getColumnIndex("bodycolor"));
		
		Log.d("MRADAPT", "Username: " + msg_username + " Body: " + msg_body);
		username.setText(msg_username);
		body.setText(msg_body);
		
		// Set avatar image
		if (msg_avatar != "") {
			avatar.setImageDrawable(msg_avatar);
		}
		
		// Set body text color
		body.setTextColor(context.getResources().getColor(msg_bodycolor));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.msg_row, parent, false);
		bindView(v, context, cursor);
		return v;
	}

	static class ViewHolder {
		TextView username;
		TextView body;
		LoaderImageView avatar;
	}

}
