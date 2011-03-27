package com.jxdevelopment.droidprat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DroidPrat extends Activity {
	private UBBMessageAdapter msgHelper;
	private MessageRowAdapter messageAdapter;
	private int MAX_MESSAGES = 35;
	private SharedPreferences prefs;
	private View mainView;
	private ListView lv;
	private EditText etMessage;
	private List<Message> messageList;
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
			String msg = etMessage.getText().toString();
			etMessage.setText(R.string.enter_msg);
			hideVirtualKeyboard();
			msgHelper.sendMessage(msg);
		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setupLayout();

		setupMessageHandler();
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
		// FIXME: Only login if values are set in config, otherwise disable text box.
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
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getString(title));
		alertDialog.setMessage(getString(message));
		alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
			   dialog.cancel();
		   }
		});
		alertDialog.setIcon(R.drawable.info);
		alertDialog.show();
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
		//lv.setStackFromBottom(true);
		
		// Setup a reference to edittext
		etMessage = (EditText) mainView.findViewById(R.id.etMessage);
		
/*		Typeface typeFace = Typeface.createFromAsset(assetmanager,"fontfile.ttf");
		TextView view = (TextView) findById(R.id.mytext);
		view.setTypeFace(typeFace);
*/
		// Setup button listeners
		setupListeners();


	}

	/**
	 * Hide virtual keyboard
	 */
	public void hideVirtualKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
	}

	/**
	 * 
	 */
	public void setupListeners() {
		// Send message listener on text box
		etMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
		etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					updateHandler.post(sendmsgRunner);
					return true;
				}
				return false;
			}
		});

		// Preference button listeners
		ImageView editPrefs = (ImageView) findViewById(R.id.prefButton);
		editPrefs.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), SetPrefs.class);
				startActivityForResult(myIntent, 0);
			}

		});

	}

	public void setupTask() {
		//msgHelper.readPrefs();
		setupMessageHandler();
		String userid = checkLogin();
		
		// Activate text box if user is logged in.
		Log.d("TIMER", "User logged in: " + userid);
		if (userid.length() > 0) {
			etMessage.setEnabled(true);
		}
		
		// Start message listener if base url is set.
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

	public void cancelTask() {
		Log.d("TIMER", "Timer cancelled.");
		if (task != null) {
			task.cancel();
		}
		etMessage.setEnabled(false);
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
		if (messageList == null) {
			messageList = messages;
			messageAdapter = new MessageRowAdapter(this, messageList);
			updateHandler.post(startRunner);
		} else if (messages.size() > 0) {
			messageList.addAll(messages);
		}
		pruneMessages(messageList, MAX_MESSAGES);
		updateHandler.post(updateRunner);
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