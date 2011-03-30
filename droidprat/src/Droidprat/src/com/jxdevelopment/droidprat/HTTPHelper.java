package com.jxdevelopment.droidprat;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class HTTPHelper {
	//private AndroidHttpClient hc;
	//private String userAgent = "Droidprat/1.0";
	private HttpClient hc;
	private BasicCookieStore cookieStore;
	private HttpContext httpContext;
	private String cookiePrefix = "";

	public HTTPHelper() {
		//this.hc = AndroidHttpClient.newInstance(userAgent);
		this.hc = new DefaultHttpClient();
		cookieStore = new BasicCookieStore();
		httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	/**
	 * The prefix will be prepended to the cookie name when getting/setting cookies.
	 * @param prefix Prefix to be prepended to cookie names.
	 */
	public void setCookiePrefix(String prefix) {
		cookiePrefix = prefix;
	}

	public HttpClient createHttpClient() {
		HttpClient httpclient = new DefaultHttpClient();
		// FIXME: Setup cookie store
		return httpclient;
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

	public InputStream getInputStreamFromUrl(String url) {
		InputStream contentStream = null;     
		try {
			HttpClient httpclient = createHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(url));
			contentStream = response.getEntity().getContent();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return contentStream;
	}

	public Bitmap getImage(String url) {
		Bitmap bitmap = null;
		InputStream in = null;        
		try {
			in = getInputStreamFromUrl(url);
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return bitmap;
	}

	public String getData(String urlquery) {
		String str = "";
		try {
			HttpGet get = new HttpGet(urlquery);
			Log.d("HTTPHELPER", "GET to URL: " + urlquery);
			HttpClient httpclient = createHttpClient();
			HttpResponse rp = httpclient.execute(get, httpContext);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				str = EntityUtils.toString(rp.getEntity());
			}
			//getCookies();
		} catch (ClientProtocolException e) {
			Log.d("HTTPHELPER", "Error: ClientProtocolException " + e.getMessage());
		} catch (IOException e) {
			Log.d("HTTPHELPER", "Error: IOException " + e.getMessage());
		}
		return str;
	}

	public String postData(String urlquery, List<NameValuePair> data) {
		String str = "";
		try {
			Log.d("HTTPHELPER", "POST to URL: " + urlquery);
			List<Cookie> cookies = getCookies(); // why? possibly to print list of cookies before post.
			HttpPost post = new HttpPost(urlquery);
			if (data != null) {
				post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			}
			HttpResponse rp = hc.execute(post, httpContext);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.d("HTTPHELPER", "postData status OK");
				str = EntityUtils.toString(rp.getEntity());
			} else {
				Log.d("HTTPHELPER", "postData error, status:" + rp.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			Log.d("HTTPHELPER", "Error: ClientProtocolException " + e.getMessage());
		} catch (IOException e) {
			Log.d("HTTPHELPER", "Error: IOException " + e.getMessage());
		}
		return str;
	}

	/**
	 * 
	 */
	public List<Cookie> getCookies() {
		List<Cookie> cookies = cookieStore.getCookies();
		Log.d("HTTPHELPER", "Cookies:");
		for (int i = 0; i < cookies.size(); i++) {
			Cookie c = cookies.get(i);
			Log.d("HTTPHELPER", "Cookie: " + c.getName() + "=" + c.getValue());
		}
		return cookies;
	}

	public String getCookie(String name) {
		String pfxName = cookiePrefix.concat(name);
		Log.d("HTTPHELPER", "getCookie: " + pfxName);
		List<Cookie> cookies = getCookies();
		if (!cookies.isEmpty()) {
			for (int j = 0; j < cookies.size(); j++) {
				Cookie cookie = cookies.get(j);
				String cName = cookie.getName();
				if (cName != null && cName.compareTo(pfxName) == 0) {
					return cookie.getValue();
				}
			}
		} else {
			Log.d("HTTPHELPER", "No cookies found.");
		}
		return null;
	}

	public Boolean setCookie(String name, String value) {
		Cookie cookie = new BasicClientCookie(name, value);
		cookieStore.addCookie(cookie);
		// FIXME: Implement
		return true;
	}

}
