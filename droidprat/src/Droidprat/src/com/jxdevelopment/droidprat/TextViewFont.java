package com.jxdevelopment.droidprat;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class TextViewFont extends TextView {
    Context context;
    String ttfName;

    String TAG = getClass().getName();

    public TextViewFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            Log.i(TAG, attrs.getAttributeName(i));
            this.ttfName = attrs.getAttributeValue(
                "http://schemas.android.com/apk/res/com.jxdevelopment.droidprat", "ttf_name");
            init();
        }

    }

    private void init() {
    	if (fontAvailable(ttfName)) {
        	Log.d("TEXTVIEWFONT", "Selecting font: " + ttfName);
    		Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/" + ttfName);
        	setTypeface(font);
        } else {
        	Log.d("TEXTVIEWFONT", "Selected font not found: " + ttfName);
        }
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);
    }
    
    public Boolean fontAvailable(String fontname) {
    	String[] fonts = getFontNames();
    	for (int i = 0; i < fonts.length; i++) {
    		if (fontname.compareTo(fonts[i]) == 0) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public String[] getFontNames() {
        AssetManager assetManager = this.context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("fonts");
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
    	return files;
    }

}