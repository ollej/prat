package com.jxdevelopment.droidprat;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UBBMessageAdapter {
	private final Context mCtx;
	private HTTPHelper http = null;
	public Integer latestId = 0;
	public Boolean longpoll = true;
	// Configuration options:
	private String URL = "";
	private String cookiePrefix = "";

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public UBBMessageAdapter(Context ctx) {
		this.mCtx = ctx;
		this.http = new HTTPHelper();
		
		readPrefs();
		
		this.http.setCookiePrefix(cookiePrefix.concat("ubbt_"));
		// Silly code to try hostname lookup once first.
		// FIXME: Remove
		//lookupHost();
	
	}

	public void readPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.mCtx);
        cookiePrefix = prefs.getString("prefCookiePrefix", "ubb7_");
        URL = prefs.getString("prefBaseURL", "");
        Log.d("UBBMESSAGE", "Configured Cookie Prefix: " + cookiePrefix);        
        Log.d("UBBMESSAGE", "Configured Base URL: " + URL);		
	}
	
	public String loadMessages(Integer start) throws URLException {
		//String jsonString = "[{\"id\": \"478076\",\"user_id\": \"165\",\"username\": \"hjk\",\"body\": \"Rekommenderar Misfits.\",\"time\": \"1300749197\",\"avatar\": \"\"}]";
		String urlquery = buildMessageURL(start, longpoll);
		String jsonString = http.getData(urlquery);

		return jsonString;
	}

	public List<Message> convertMessageJson(String jsonString) {
		Type listType = new TypeToken<Collection<Message>>(){}.getType();
		List<Message> msglist = new Gson().fromJson(jsonString, listType);
		return msglist;
	}

	// FIXME: Need to extract user list from user attribute.
	public List<User> convertUserJson(String jsonString) {
		Type listType = new TypeToken<Collection<User>>(){}.getType();
		List<User> userlist = new Gson().fromJson(jsonString, listType);
		return userlist;
	}

	public List<Message> getAllMessages() throws URLException {
		return getMessages(0);
	}

	public List<Message> getMessages(Integer start) throws URLException {
		String jsonString = loadMessages(start);
		Log.d("UBBMESSAGE", "jsonString read:" + jsonString);
		if (jsonString == null || jsonString.trim().equals("")) {
			return null;
		}
		List<Message> msglist = convertMessageJson(jsonString);
		latestId = getHighestId(msglist);
		return msglist;		
	}

	/**
	 * @param msglist
	 */
	public int getHighestId(List<Message> msglist) {
		int highestId = 0;
		for (int i = 0; i < msglist.size(); i++) {
			Integer id = Integer.parseInt(msglist.get(i).getId());
			if (id > highestId) {
				highestId = id;
			}
		}
		Log.d("UBBMESSAGE", "Highest id:" + highestId);
		return highestId;
	}

	public List<Message> getMessages() throws URLException {
		return getMessages(latestId);
	}
	
	public String getURL() throws URLException {
		if (URL.length() == 0) {
			readPrefs();
		}
		if (URL.length() == 0) {
			throw new URLException("No URL configured.");
		}
		return URL;
	}

	public String buildURL(String params) throws URLException {
		String u = getURL();
		if (params != "") {
			u = u.concat(params);
		}
		return u;
	}
	//"?ubb=listshouts&format=json""?ubb=listshouts&format=json"

	// TODO: Should get a key/value list of query arguments.
	public String buildMessageURL(Integer start, Boolean longpoll) throws URLException {
		String urlquery = buildURL("?ubb=listshouts&format=json&showlocal=1");
		if (start > 0) {
			urlquery = urlquery.concat("&start=").concat(start.toString());
		}
		if (longpoll == true) {
			urlquery = urlquery.concat("&longpoll=1");
		}
		return urlquery;
	}

	public String buildUserURL(int[] idlist) throws URLException {
		String urlquery = buildURL("?ubb=listshouts&format=json&action=showuser&userid=".concat(idlist.toString()));
		return urlquery;
	}

	public List<User> getUsers(int[] idlist) throws URLException {
		String urlquery = buildUserURL(idlist);
		String jsonString = http.getData(urlquery);
		List<User> userlist = convertUserJson(jsonString);
		return userlist;    	
	}

	// TODO: Keep a cached list of users, userList connected to userView.
	public List<User> getUser(int id) throws URLException {
		int[] idlist = {id};
		return getUsers(idlist);
	}

	public Bitmap getAvatar(String url) {
		Bitmap avatar = http.getImage(url);
		return avatar;
	}

	// TODO: Create a generic buildURL method which can take a key/value pair list.
	public String login(String username, String password) throws URLException {
		//String url = URL.concat("?ubb=start_page");

		// Add data to post
		List<NameValuePair> data = new ArrayList<NameValuePair>(6);
		data.add(new BasicNameValuePair("ubb", "start_page"));
		data.add(new BasicNameValuePair("Loginname", username));
		data.add(new BasicNameValuePair("Loginpass", password));
		data.add(new BasicNameValuePair("rememberme", "1"));
		data.add(new BasicNameValuePair("firstlogin", "1"));
		data.add(new BasicNameValuePair("buttlogin", "Logga in"));

		String u = getURL();
		String loginpage = http.postData(u, data);
		//Integer cookieUserId = Integer.parseInt(http.getCookie("myid"));
		String cookieUserId = http.getCookie("myid");
		if (cookieUserId != null) {
			Log.d("UBBMESSAGE", "Logged in as user:" + cookieUserId.toString());
		} else {
			Log.d("UBBMESSAGE", "Didn't get a user cookie, not logged in.");
			//Log.d("UBBMESSAGE", loginpage);
		}
		return cookieUserId;
		//return 0;
	}

	public void logout() {
		// FIXME: Not implemented.
	}

	public String sendMessage(String message) {
		//String url = URL.concat("?ubb=shoutit&shout=").concat(message);
		List<NameValuePair> data = new ArrayList<NameValuePair>(2);
		data.add(new BasicNameValuePair("ubb", "shoutit"));
		data.add(new BasicNameValuePair("shout", message));

		String ret = http.postData(URL, data);
		Log.d("UBBMESSAGE", "Post data returned: " + ret);
		return ret;
	}

	public String deleteMessage(Integer id) throws URLException {
		// FIXME: ALlow NameValuePair list to getData.
		String u = getURL();
		String url = u.concat("?ubb=shoutdelete&id=").concat(id.toString());
		String ret = http.getData(url);
		return ret;
	}

}
