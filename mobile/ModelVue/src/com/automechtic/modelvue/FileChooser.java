
package com.automechtic.modelvue;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.automechtic.modelvue.R;

public class FileChooser extends ListActivity {
	private File currentDir;
	private FileArrayAdapter adapter;
	private FileFilter fileFilter;
	private File fileSelected;
	private ArrayList<String> ext;
	
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			if (extras.getStringArrayList("ext") != null) {
				ext = extras.getStringArrayList("ext");
				fileFilter = new FileFilter() {
					public boolean accept(File pathname) {
						return (pathname.getName().contains(".") ? ext.contains(pathname.getName().substring(pathname.getName().lastIndexOf("."))) : true);
					}
				};
			}
		}

		currentDir = new File("/");
		fill(currentDir);

	}

	private void fill(File f) {
		File[] dirs = null;
		if (fileFilter != null)
			dirs = f.listFiles(fileFilter);
		else 
			dirs = f.listFiles();
			
		this.setTitle(getString(R.string.currentDir) + ": " + f.getName());
		List<Option> dir = new ArrayList<Option>();
		List<Option> fls = new ArrayList<Option>();
		try {
			for (File ff : dirs) {
				if (ff.isDirectory() && !ff.isHidden())
					dir.add(new Option(ff.getName(), getString(R.string.folder), ff
							.getAbsolutePath(), true, false));
				else {
					if (!ff.isHidden())
						fls.add(new Option(ff.getName(), getString(R.string.fileSize) + ": "
								+ ff.length(), ff.getAbsolutePath(), false, false));
				}
			}
		} catch (Exception e) {

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (!f.getName().equalsIgnoreCase("sdcard")) {
			if (f.getParentFile() != null) dir.add(0, new Option("..", getString(R.string.parentDirectory), f.getParent(), false, true));
		}
		adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view,
				dir);
		this.setListAdapter(adapter);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if ((!currentDir.getName().equals("sdcard")) && (currentDir.getParentFile() != null)) {
	        	currentDir = currentDir.getParentFile();
	        	fill(currentDir);
        	} else {
        		finish();
        	}
            return false;
        }
        return super.onKeyDown(keyCode, event);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if (o.isFolder() || o.isParent()) {			
			currentDir = new File(o.getPath());
			fill(currentDir);
		} else {
			fileSelected = new File(o.getPath());
			Intent intent = new Intent();
			intent.putExtra("fileselected", fileSelected.getAbsolutePath());
			setResult(Activity.RESULT_OK, intent);
			finish();
		}		
	}

	private void onFileClick(Option o) {
		Toast.makeText(this, "File Clicked: " + o.getName(), Toast.LENGTH_SHORT)
				.show();
	}

}
