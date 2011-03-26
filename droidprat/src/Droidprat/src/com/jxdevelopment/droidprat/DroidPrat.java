package com.jxdevelopment.droidprat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class DroidPrat extends Activity {
	private UBBMessageAdapter msgHelper;
	private MessageRowAdapter messageAdapter;
	private int MAX_MESSAGES = 35;
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
        super.onCreate(savedInstanceState);
        final LayoutInflater factory = getLayoutInflater();
        mainView = factory.inflate(R.layout.main, null);
        setContentView(mainView);
        lv = (ListView) mainView.findViewById(R.id.lvMessages);
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //lv.setStackFromBottom(true);
        
        etMessage = (EditText) mainView.findViewById(R.id.etMessage);
        setupListeners();
        
        //etMessage.setText("");

        msgHelper = new UBBMessageAdapter(this);
        // FIXME: Only login if values are set in config, otherwise disable text box.
        msgHelper.login("anonymous", "secret");
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
		// FIXME: Add button to view and hook up listener to that.
		/*
		etMessage.setOnClickListener(new Button.OnClickListener() {
    	    public void onClick(View v) {
    	    	updateHandler.post(sendmsgRunner);
    	    }
    	});
    	*/
		/*
		// Clear text when text box is clicked.
		etMessage.setOnClickListener(new EditText.OnClickListener() {
    	    public void onClick(View v) {
    	    	String msg = etMessage.getText().toString();
    	    	if (msg.compareTo(R.string.enter_msg == 0) {
    	    		etMessage.setText("");
    	    	}
    	    }
    	});
    	*/
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

    public void setupTask() {
		Log.d("TIMER", "Timer started.");
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
    
    public void cancelTask() {
		Log.d("TIMER", "Timer cancelled.");
    	task.cancel();
    }
    
    /**
     * Load messages using the configured message handler and add them to the messageList.
     */
    public void loadMessages() {
    	List<Message> messages = msgHelper.getMessages();
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