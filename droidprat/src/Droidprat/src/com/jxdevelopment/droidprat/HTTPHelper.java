package com.jxdevelopment.droidprat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HTTPHelper {
    private DefaultHttpClient hc;
    private String cookiePrefix = "";

    public HTTPHelper() {
    	this.hc = new DefaultHttpClient();
    }
    
    /**
     * The prefix will be prepended to the cookie name when getting/setting cookies.
     * @param prefix Prefix to be prepended to cookie names.
     */
	public void setCookiePrefix(String prefix) {
		cookiePrefix = prefix;
	}

	// FIXME: Remove.
    public InetAddress lookupHost(String host) {
    	InetAddress i = null;
    	try {
	    	Log.d("HTTPHELPER", "Looking up host: " + host);
    		i = InetAddress.getByName(host);
	    } catch (UnknownHostException e1) {
	    	e1.printStackTrace();
	    }
	    return i;
    }
    
    public String getData(String urlquery) {
    	String str = "";
    	try {
	    	HttpGet get = new HttpGet(urlquery);
	    	Log.d("HTTPHELPER", "GET to URL: " + urlquery);
	    	HttpResponse rp = hc.execute(get);
	    	if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	    		str = EntityUtils.toString(rp.getEntity());
	    	}
	        //getCookies();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return str;
    }
    
    public String postData(String urlquery) {
    	String str = "";
    	try {
	    	HttpPost post = new HttpPost(urlquery);
	    	Log.d("HTTPHELPER", "POST to URL: " + urlquery);
	    	// FIXME: Add post key/value pairs
	    	HttpResponse rp = hc.execute(post);
	    	if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	    		str = EntityUtils.toString(rp.getEntity());
	    	}
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return str;
    }
    
	/**
	 * 
	 */
	public List<Cookie> getCookies() {
		List<Cookie> cookies = hc.getCookieStore().getCookies();
		return cookies;
	}
	
	public String getCookie(String name) {
		List<Cookie> cookies = getCookies();
		if (!cookies.isEmpty()) {
			// FIXME: retrieve cookie.
		}
		return "";
	}
	
	public Boolean setCookie(String name, String value) {
		// FIXME: Implement
		return true;
	}
	
}
