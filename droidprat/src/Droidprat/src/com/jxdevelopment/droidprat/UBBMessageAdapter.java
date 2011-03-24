package com.jxdevelopment.droidprat;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UBBMessageAdapter {
    private final Context mCtx;
    private HTTPHelper http = null;
    public Integer latestId = 0;
    public Boolean longpoll = true;
    // Configuration options:
    private final String URL = "http://www.rollspel.nu/forum/ubbthreads.php";
    private String cookiePrefix = "ubb7_";
	
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public UBBMessageAdapter(Context ctx) {
        this.mCtx = ctx;
        this.http = new HTTPHelper();
        this.http.setCookiePrefix(cookiePrefix.concat("ubbt_"));
    	// Silly code to try hostname lookup once first.
    	// FIXME: Remove
    	//lookupHost();
    }

    public String loadMessages(Integer start) {
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
    
    public List<Message> getAllMessages() {
    	return getMessages(0);
    }

    public List<Message> getMessages(Integer start) {
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
    
    public List<Message> getMessages() {
    	return getMessages(latestId);
    }
    
    public String buildURL() {
    	String u = URL.concat("?ubb=listshouts&format=json");
    	return u;
    }
    
    // TODO: Should get a key/value list of query arguments.
    public String buildMessageURL(Integer start, Boolean longpoll) {
    	String urlquery = buildURL();
    	urlquery = urlquery.concat("&showlocal=1");
    	if (start > 0) {
			urlquery = urlquery.concat("&start=").concat(start.toString());
		}
		if (longpoll == true) {
			urlquery = urlquery.concat("&longpoll=1");
		}
		return urlquery;
    }
    
    public String buildUserURL(int[] idlist) {
    	String urlquery = buildURL();
    	urlquery.concat("&action=showuser&userid=").concat(idlist.toString());
    	return urlquery;
    }
   
    public List<User> getUsers(int[] idlist) {
		String urlquery = buildUserURL(idlist);
		String jsonString = http.getData(urlquery);
		List<User> userlist = convertUserJson(jsonString);
    	return userlist;    	
    }
	
    // TODO: Keep a cached list of users, userList connected to userView.
	public List<User> getUser(int id) {
		int[] idlist = {id};
		return getUsers(idlist);
	}
	
	// TODO: Create a generic buildURL method which can take a key/value pair list.
	public Integer login(String username, String password) {
		String url = URL.concat("?ubb=start_page");
		url.concat("&Loginname=").concat(username);
		url.concat("&Loginpass=").concat(password);
		url.concat("&rememberme=1");
		url.concat("&firstlogin=1");
		url.concat("&buttlogin=Logga in");
		String loginpage = http.getData(url);
		Integer cookieUserId = Integer.parseInt(http.getCookie("myid"));
		Log.d("UBBMESSAGE", "Logged in as user:" + cookieUserId.toString());
		return cookieUserId;
	}
	
	public void logout() {
		// FIXME: Not implemented.
	}
	
	public String sendMessage(String message) {
    	String url = URL.concat("?ubb=shoutit&shout=").concat(message);
    	String ret = http.postData(url);
    	return ret;
	}
	
	public String deleteMessage(Integer id) {
    	String url = URL.concat("?ubb=shoutdelete&id=").concat(id.toString());
    	String ret = http.getData(url);
    	return ret;
	}

}
