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
			holder.avatar = (LoaderImageView) convertView.findViewById(R.id.ivAvatar);

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
		
		// Set avatar image
		if (msg.getAvatar() != "") {
			holder.avatar.setImageDrawable(msg.getAvatar());
		}
		
		// Set body text color
		int textColor = R.color.fgcolor_msg_body;
		if (msg.isSlashMe) {
			Log.d("MRADAPT", "Message is slashme, setting text color.");
			textColor = R.color.fgcolor_slashme;
		}
		holder.body.setTextColor(this.mCtx.getResources().getColor(textColor));
		
/*        Typeface tf = Typeface.createFromAsset(this.mCtx.getAssets(),"fonts/BPreplayExtended.otf");
		holder.body.setTypeface(tf);
*/
		/*		Bitmap avatar = .getAvatar());
	    holder.avatar.setImageBitmap(avatar);		
		 */
		return convertView;
	}

	static class ViewHolder {
		TextView username;
		TextView body;
		LoaderImageView avatar;
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

