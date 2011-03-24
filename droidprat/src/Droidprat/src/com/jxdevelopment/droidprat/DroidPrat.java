package com.jxdevelopment.droidprat;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DroidPrat extends ListActivity {
	private UBBMessageAdapter msgHelper;
	private ArrayAdapter<String> messageAdapter;
	private int MAX_MESSAGES = 200;
	private List<String> messageList;
	private Timer t = new Timer("messageloading");
    private Handler updateHandler = new Handler();
    private Runnable updateRunner = new Runnable() {
	    public void run() {
	    	Log.d("UPDATERUNNER", "Notifying dataset changed.");
	    	messageAdapter.notifyDataSetChanged();
	    }
    };
    private TimerTask task = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        msgHelper = new UBBMessageAdapter(this);
        messageList = new ArrayList<String>();
    	messageAdapter = new ArrayAdapter<String>(this, R.layout.msg_row, messageList);
        setListAdapter(messageAdapter);
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
    	//ArrayList<String> messages = msgHelper.getAllMessages();
    	String[] messages = msgHelper.getMessages();
    	if (messages.length > 0) {
	    	for (int i = 0; i < messages.length; i++) {
	        	Log.d("MESSAGE", "Added message: " + messages[i]);
	        	messageList.add(messages[i]);
	    	}
	    	pruneMessages(messageList, MAX_MESSAGES);
    		updateHandler.post(updateRunner);
    	}
    }

	/**
	 * Remove the oldest messages if the size is greater than max
	 */
	public void pruneMessages(List<String> ml, int max) {
		int count = ml.size();
		if (count > max) {
			int remove = count - max;
			Log.d("MESSAGE", "Pruning messages: " + remove);
			for (int i = 0; i < remove; i++) {
				ml.remove(0);
			}
		}
	}
}