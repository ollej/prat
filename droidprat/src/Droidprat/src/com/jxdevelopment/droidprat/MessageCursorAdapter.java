package com.jxdevelopment.droidprat;

import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.github.droidfu.widgets.WebImageView;

public class MessageCursorAdapter extends SimpleCursorAdapter {
	private SharedPreferences prefs;
	private Pattern reUsername;
	private Context mContext;

	public MessageCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		mContext = context;
		Log.d("MSGCURSORADAPT", "Initializing MessageCursorAdapter and reading preferences.");
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        
        String username = prefs.getString("prefUsername", "");
        reUsername = Pattern.compile(Pattern.quote(username), Pattern.CASE_INSENSITIVE);
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

		// Set values.
		Log.d("MRADAPT", "Username: " + msg_username + " Body: " + msg_body);
		holder.username.setText(msg_username);
		holder.body.setText(msg_body);

		// Set avatar image
		holder.avatar.reset();
		holder.avatar.setNoImageDrawable(R.drawable.noavatar);
		if (msg_avatar == "" || msg_avatar.equals("http://")) {
			//Log.d("MRADAPT", "Clearing image url and setting noavatar as foreground.");
			//holder.avatar.setImageUrl("");
			//holder.avatar.reset();
			//holder.avatar.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.noavatar));
		} else {
			Log.d("MRADAPT", "Setting image URL: " + msg_avatar);
			holder.avatar.setImageUrl(msg_avatar);
			holder.avatar.loadImage();
		}

		// Set body text color
		holder.body.setTextColor(context.getResources().getColor(msg_bodycolor));
		
		// Highlight rows with users name in them.
		if (reUsername.matcher(msg_body).find() == true) {
			holder.row.setBackgroundResource(R.color.bgcolor_row_highlight);
		} else {
			holder.row.setBackgroundResource(R.color.bgcolor_row);
		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.msg_row, parent, false);

		// Create view holder with references to views.
		ViewHolder holder;
		v = inflater.inflate(R.layout.msg_row, null);
		holder = new ViewHolder();
		holder.row = (View) v.findViewById(R.id.rlMessageRow);
		holder.username = (TextView) v.findViewById(R.id.tvUser);
		holder.body = (TextView) v.findViewById(R.id.tvBody);
		holder.avatar = (WebImageView) v.findViewById(R.id.ivAvatar);
		v.setTag(holder);

		bindView(v, context, cursor);
		return v;
	}

	static class ViewHolder {
		TextView username;
		TextView body;
		WebImageView avatar;
		View row;
	}

}
