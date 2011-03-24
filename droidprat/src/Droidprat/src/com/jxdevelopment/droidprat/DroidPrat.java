package com.jxdevelopment.droidprat;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

public class DroidPrat extends ListActivity {
	private UBBMessageAdapter msgHelper;
	private MessageRowAdapter messageAdapter;
	private int MAX_MESSAGES = 35;
	private List<Message> messageList;
	private Timer t = new Timer("messageloading");
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
	    	setListAdapter(messageAdapter);
	    }
    };
    private TimerTask task = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msgHelper = new UBBMessageAdapter(this);
        ListView lv = getListView();
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lv.setStackFromBottom(true);
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