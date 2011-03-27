package com.jxdevelopment.droidprat;

import org.apache.commons.lang3.StringEscapeUtils;

import android.util.Log;

public class Message {
	String id;
	String user_id;
	String username;
	String body;
	String time;
	String avatar;
	Boolean isSlashMe = false;
	
	public String getId() {
		return this.id;
	}
	public String getUser_id() {
		return this.user_id;
	}
	public String getUsername() {
		return this.username;
	}
	public String getBody() {
		return this.body;
	}
	public String getTime() {
		return this.time;
	}
	public String getAvatar() {
		return this.avatar;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
	public String parseBody(String body) {
		Log.d("PARSEBODY", "Unescaping HTML: " + body);
		body = StringEscapeUtils.unescapeHtml4(body);
		Log.d("PARSEBODY", "After unescape: " + body);
		
		// Convert /me lines, should also be colored by MessageRowAdapter
		if (body.startsWith("/me ")) {
			Log.d("PARSEBODY", "Slashme message.");
			this.isSlashMe = true;
			body = body.substring(3);
			body = "* ".concat(this.getUsername()).concat(body);
		}
		return body;
	}
}
