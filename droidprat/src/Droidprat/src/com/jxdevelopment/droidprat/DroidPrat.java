package com.jxdevelopment.droidprat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.droidfu.activities.BetterDefaultActivity;

public class DroidPrat extends BetterDefaultActivity {
	private Context ctx;
	private UBBMessageAdapter msgHelper;
	private MessageCursorAdapter messageAdapter;
	private MatrixCursor msgCursor;
    private String[] msgCols = new String[] { "_id", "body", "username", "avatar", "bodycolor" };
    private int[] to = new int[] { 0, R.id.tvBody, R.id.tvUser, R.id.ivAvatar, 0 };
	private int MAX_MESSAGES = 35;
	private SharedPreferences prefs;
	private View mainView;
	private ListView lv;
	private EditText etMessage;
	private AlertDialog dlgNoInet;
	private Timer t = new Timer("messageloading");
	private TimerTask task = null;
	private Handler updateHandler = new Handler();
	private Runnable updateRunner = new Runnable() {
		public void run() {
			Log.d("UPDATERUNNER", "Notifying dataset changed.");
			messageAdapter.notifyDataSetChanged();
		}
	};
	private Runnable startRunner = new Runnable() {
		public void run() {
			Log.d("RUNNER", "Setting messageAdapter.");
			lv.setAdapter(messageAdapter);
		}
	};
	private Runnable sendmsgRunner = new Runnable() {
		public void run() {
			Log.d("RUNNER", "Sending message.");
			if (hasInternetConnection()) {
				String msg = etMessage.getText().toString();

				if (msg.compareTo(getResources().getString(R.string.enter_msg)) != 0) {
					etMessage.setText("");
					hideVirtualKeyboard();
					msgHelper.sendMessage(msg);
				} else {
					Log.d("DROIDPRAT", "Tried sending default message hint.");
				}
			} else {
				dlgNoInet.show();
			}
		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setupLayout();
	}

	@Override
	public void onResume() {
		super.onResume();
		setupTask();
	}

	@Override
	public void onPause() {
		super.onPause();
		cancelTask();
	}

	/**
	 * 
	 */
	public void setupMessageHandler() {

        // Set up a UBBMessageAdapter by default, could be others in the future.
		msgHelper = new UBBMessageAdapter(this);
	}

	/**
	 * 
	 */
	public String checkLogin() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String username = prefs.getString("prefUsername", "");
		String password = prefs.getString("prefPassword", "");
        String URL = prefs.getString("prefBaseURL", "");

        String userid = "";
		if (username != "" && password != "" && URL != "") {
			try {
				userid = msgHelper.login(username, password);
			} catch (URLException e) {
				Log.d("MESSAGE", "Can't login with no URL configured.");
			}
		} else {
			// Notify that preferences needs to be configured.
			cancelTask();
			showOKAlert(R.string.enter_prefs, R.string.not_logged_in);
		}
		return userid;
	}
	
	public void showOKAlert(int title, int message) {
		AlertDialog alertDialog = newInfoDialog(title, message);
		alertDialog.show();
	}
	
	public void showAboutDialog() {
        // Set up dialog.
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.about);
        dialog.setTitle(R.string.about_title);
        dialog.setCancelable(true);
        
        // Set about text using html.
        String aboutText = getResources().getString(R.string.about_text);
        Spanned textSpan = android.text.Html.fromHtml(aboutText);
        TextView text = (TextView) dialog.findViewById(R.id.tvAboutText);
        text.setText(textSpan);

        // Set up button.
        Button button = (Button) dialog.findViewById(R.id.btnAboutOk);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	dialog.dismiss();
            }
        });
        
        dialog.show();
	}

	/**
	 * 
	 */
	public void setupLayout() {
		// Setup main view
		final LayoutInflater factory = getLayoutInflater();
		mainView = factory.inflate(R.layout.main, null);
		setContentView(mainView);
		
		lv = (ListView) mainView.findViewById(R.id.lvMessages);
		lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		
		// Setup a reference to edittext
		etMessage = (EditText) mainView.findViewById(R.id.etMessage);
		
		// Setup button listeners
		setupListeners();

		dlgNoInet = newInfoDialog(R.string.error_dialog_title, R.string.error_no_inet_conn);
	}

	/**
	 * Hide virtual keyboard
	 */
	public void hideVirtualKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
	}

	/**
	 * 
	 */
	public void setupListeners() {

		// Preference button listeners
		ImageView editPrefs = (ImageView) findViewById(R.id.prefButton);
		editPrefs.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), SetPrefs.class);
				startActivityForResult(myIntent, 0);
			}

		});
		
		// Info/About button
        ImageView infoButton = (ImageView) findViewById(R.id.infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {		
            	showAboutDialog();
            }
        });

	}
	
	public void setupSendMessageListener() {
		// Send message listener on text box
		etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_NULL) {
					updateHandler.post(sendmsgRunner);
					return true;
				}
				return false;
			}
		});
		
	}

	public void setupTask() {
		// Don't do anything if there isn't an internet connection.
		if (!hasInternetConnection()) {
			dlgNoInet.show();
			return;
		}
		
		setupMessageHandler();
		
		// Activate text box if user is logged in.
		String userid = checkLogin();
		Log.d("TIMER", "User logged in: " + userid);
		if (userid != null && userid.length() > 0 && etMessage != null) {
			setupSendMessageListener();
			etMessage.setEnabled(true);
			hideVirtualKeyboard();
		}
		
		// Setup the message cursor object.
		setupMessageCursor();

		// Start message listener if base url is set.
		startMessageListener();
	}

	public void cancelTask() {
		Log.d("TIMER", "Timer cancelled.");
		if (task != null) {
			task.cancel();
		}
		if (etMessage != null) {
			etMessage.setEnabled(false);
		}
	}
	
	public boolean hasInternetConnection() {
		ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		Log.d("DROIDPRAT", "network0: " + conn.getNetworkInfo(0).getState() + " = " + NetworkInfo.State.CONNECTED +
				", network1: " + conn.getNetworkInfo(1).getState() + " = " + NetworkInfo.State.CONNECTING);
		State c0 = conn.getNetworkInfo(0).getState();
		State c1 = conn.getNetworkInfo(1).getState();
		if (c0 == NetworkInfo.State.CONNECTED || c0 == NetworkInfo.State.CONNECTING ||
			c1 == NetworkInfo.State.CONNECTED || c1 == NetworkInfo.State.CONNECTING) {
			return true;
		} else if (conn.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||
				   conn.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
			return false;
		}
		return false;
	}

	/**
	 * 
	 */
	public void startMessageListener() {
		String baseurl = prefs.getString("prefBaseURL", "");
		if (baseurl.length() > 0) {
			Log.d("TIMER", "Timer started - Starting to listen for new messages.");
			task = new TimerTask() {
				public void run() {
					Log.d("TIMER", "Timer set off");
					loadMessages();
				}
			};
			try {
				t.schedule(task, 0, 1000);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	public void setupMessageCursor() {
		if (msgCursor == null) {
		    msgCursor = new MatrixCursor(msgCols);
		    startManagingCursor(msgCursor);

		    messageAdapter = new MessageCursorAdapter(
		                this, R.layout.msg_row, msgCursor, msgCols, to);
			
			updateHandler.post(startRunner);
		}
	}

	/**
	 * Load messages using the configured message handler and add them to the messageList.
	 */
	public void loadMessages() {
		List<Message> messages = null;
		try {
			messages = msgHelper.getMessages();
		} catch (URLException e) {
			Log.d("MESSAGE", "Tried loading messages without a URL configured.");
			cancelTask();
		}
		if (messages == null) {
			return;
		}
		Log.d("MESSAGE", "Adding messages: " + messages.size());
		if (messages.size() > 0) {
			addMessages(messages);
		}
		//pruneMessages(messageList, MAX_MESSAGES);
		updateHandler.post(updateRunner);
	}

	/**
	 * @param messages
	 */
	public void addMessages(List<Message> messages) {
		for (int i = 0; i < messages.size(); i++) {
			Message msg = messages.get(i);
			msgCursor.addRow(new Object[] { msg.getId(), msg.getBody(), msg.getUsername(), msg.getAvatar(), msg.bodycolor });
		}
	}

	/**
	 * Remove the oldest messages if the size is greater than max
	 */
	public void pruneMessages(List<Message> ml, int max) {
		int count = ml.size();
		if (count > max) {
			int remove = count - max;
			Log.d("MESSAGE", "Pruning messages: " + remove + " count: " + count + " max: " + max);
			for (int i = 0; i < remove; i++) {
				ml.remove(0);
			}
		}
	}
}