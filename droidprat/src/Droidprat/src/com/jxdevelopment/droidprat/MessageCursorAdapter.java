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

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Get view holder
		final ViewHolder holder = (ViewHolder) view.getTag();

		// FIXME: Create a Message object specific version, or a generic for Message/User
		String msg_body = cursor.getString(cursor.getColumnIndex("body"));
		String msg_username = cursor.getString(cursor.getColumnIndex("username"));
		String msg_avatar = cursor.getString(cursor.getColumnIndex("avatar"));
		int msg_bodycolor = cursor.getInt(cursor.getColumnIndex("bodycolor"));

		Log.d("MRADAPT", "Username: " + msg_username + " Body: " + msg_body);
		holder.username.setText(msg_username);
		holder.body.setText(msg_body);

		// Set avatar image
		if (msg_avatar != "") {
			holder.avatar.setImageDrawable(msg_avatar);
		}

		// Set body text color
		holder.body.setTextColor(context.getResources().getColor(msg_bodycolor));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.msg_row, parent, false);

		// Create view holder with references to views.
		ViewHolder holder;
		v = inflater.inflate(R.layout.msg_row, null);
		holder = new ViewHolder();
		holder.username = (TextView) v.findViewById(R.id.tvUser);
		holder.body = (TextView) v.findViewById(R.id.tvBody);
		holder.avatar = (LoaderImageView) v.findViewById(R.id.ivAvatar);
		v.setTag(holder);

		bindView(v, context, cursor);
		return v;
	}

	static class ViewHolder {
		TextView username;
		TextView body;
		LoaderImageView avatar;
	}

}
