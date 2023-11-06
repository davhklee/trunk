
// Developed by David L (Copyright 2013, 2014)

package com.automechtic.modelvue;

import android.app.*;
import android.os.*;
import android.view.*;
import android.content.*;
import android.widget.*;
import android.view.View.*;
import java.util.*;

public class IndexActivity extends Activity {

	private static final int FILE_CHOOSER = 11;
	
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_index);
		
		ImageView iv = (ImageView) findViewById(R.id.item_top);
        iv.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				doOpen();
			}
		});
        
	}
	
    private void doOpen() {
    	Intent intent = new Intent(this, FileChooser.class);
    	ArrayList<String> ext = new ArrayList<String>();
    	ext.add(".obj");
    	intent.putStringArrayListExtra("ext", ext);
    	startActivityForResult(intent, FILE_CHOOSER);

    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == FILE_CHOOSER) {
    		if (resultCode == RESULT_OK) {
    			String fileSelected = data.getStringExtra("fileselected");
        		
        		if (!fileSelected.equalsIgnoreCase("")) {
        			Toast.makeText(this, fileSelected, Toast.LENGTH_SHORT).show();
        			Intent intent = new Intent(this, MainActivity.class);
        			startActivity(intent);
        		}
        		
    		}
    	}
    		
    }

}
