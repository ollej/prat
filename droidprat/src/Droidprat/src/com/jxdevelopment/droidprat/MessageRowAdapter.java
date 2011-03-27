/**
 * 
 */
package com.jxdevelopment.droidprat;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author olle
 *
 */
public class MessageRowAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Message> messages;
	private Context mCtx;

	public MessageRowAdapter(Context context, List<Message> messages) {
		this.mCtx = context;
		mInflater = LayoutInflater.from(context);
		this.messages = messages;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.msg_row, null);
			holder = new ViewHolder();
			holder.username = (TextView) convertView.findViewById(R.id.tvUser);
			holder.body = (TextView) convertView.findViewById(R.id.tvBody);
			holder.avatar = (ImageView) convertView.findViewById(R.id.ivAvatar);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Message msg = getItem(position);
		String msgbody = msg.getBody();
		msgbody = msg.parseBody(msgbody);
		
		Log.d("MRADAPT", "Username: " + msg.getUsername() + " Body: " + msgbody);
		holder.username.setText(msg.getUsername());
		holder.body.setText(msgbody);
		// FIXME: Need to download avatar image.
		//holder.avatar.setBackgroundResource(R.drawable.noavatar);
		
		if (msg.isSlashMe) {
			Log.d("MRADAPT", "Message is slashme, setting text color.");
			holder.body.setTextColor(this.mCtx.getResources().getColor(R.color.fgcolor_slashme));
		} else {
			holder.body.setTextColor(this.mCtx.getResources().getColor(R.color.fgcolor_msg_body));
		}

		/*		Bitmap avatar = .getAvatar());
	    holder.avatar.setImageBitmap(avatar);		
		 */
		return convertView;
	}

	static class ViewHolder {
		TextView username;
		TextView body;
		ImageView avatar;
	}

	public int getCount() {
		if (messages != null) {
			return messages.size();
		} else {
			return 0;
		}
	}

	public Message getItem(int position) {
		if (messages != null) {
			return messages.get(position);
		} else {
			return null;
		}
	}

	public long getItemId(int position) {
		return position;
	}

}

