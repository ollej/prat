package com.jxdevelopment.droidprat;

public class Message {
	String id;
	String user_id;
	String username;
	String body;
	String time;
	String avatar;
	
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
}
