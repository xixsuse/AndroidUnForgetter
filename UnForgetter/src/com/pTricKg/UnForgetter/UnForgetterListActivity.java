package com.pTricKg.UnForgetter;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class UnForgetterListActivity extends ListActivity {
	
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private UnForgetterDbAdapter mDbHelper;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unforgetter_list);
                
        mDbHelper = new UnForgetterDbAdapter(this);
        mDbHelper.open();
        fillData();                                 
        
        registerForContextMenu(getListView());
         
    }
    
    //attempting fix for deprecated Cursor
    private ArrayList<Cursor> managedCursors = new ArrayList<Cursor>();

    @Override
    public void startManagingCursor(Cursor c) {
    	synchronized(managedCursors) {
    		if (!managedCursors.contains(c)) managedCursors.add(c);
    	}
    }
    
    @Override
    public void stopManagingCursor(Cursor c) {
    	synchronized(managedCursors) {
    		managedCursors.remove(c);
    	}
    }

    private void destroyManagedCursors() {
    	synchronized(managedCursors) {
    		for (Cursor c:managedCursors) {
    			c.close();
    		}
    	}
    }
    
  //loads SQL data into ListView
    @SuppressWarnings("deprecation")
	private void fillData() {
        Cursor taskerCursor = mDbHelper.fetchAllReminders();
        startManagingCursor(taskerCursor);
        
        //create array.  returns task title
        String[] from = new String[]{UnForgetterDbAdapter.KEY_TITLE};
        
        // specify which fields of array to return
        int[] to = new int[]{R.id.textRow};
        
        // set cursor to pull form XML layout
        SimpleCursorAdapter reminders = 
        	    new SimpleCursorAdapter(this, R.layout.unforgetter_row, taskerCursor, from, to);
        setListAdapter(reminders);
    }
    
  //creates menu
  	@Override
  	public boolean onCreateOptionsMenu(Menu menu){
  		super.onCreateOptionsMenu(menu);
  		MenuInflater mi = getMenuInflater();
          mi.inflate(R.menu.list_menu, menu);
          return true;
          
  	}
    
  //handles user interaction in menu
  	@Override
  	public boolean onMenuItemSelected(int featureId, MenuItem item) {
  		switch(item.getItemId()) {
  		case R.id.menu_insert:
  			createReminder();
  			return true;
  		
  		case R.id.menu_settings: 
        	Intent i = new Intent(this, UnForgetterPreferences.class); 
        	startActivity(i); 
            return true;
  		}
  		return super.onMenuItemSelected(featureId, item);
  	}
    
  	 //creates ContextMenu
  		@Override
  		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
  		super.onCreateContextMenu(menu, v, menuInfo);
  			MenuInflater mi = getMenuInflater();
  			mi.inflate(R.menu.list_menu_item_longpress, menu);
  			
  			}
  	  	
  		//Start UnForgetterEditActivity.  for creating UnForgetter tasks
  		private void createReminder() {
  			
  			Intent i = new Intent(this, UnForgetterEditActivity.class);
  			startActivityForResult(i, ACTIVITY_CREATE);
  		}
  		
    //set up event-handling
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	//Set up Intent for checking whether to edit present tasks or create new
    	Intent i = new Intent(this, UnForgetterEditActivity.class);
    	i.putExtra(UnForgetterDbAdapter.KEY_ROWID, id); //pull data
        startActivityForResult(i, ACTIVITY_EDIT); 
    	
    }
    
   
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();   //returns edited data from DB
		
		destroyManagedCursors();
	}
	
	//handles user interaction for ContextMenu
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			switch(item.getItemId()) {
			case R.id.menu_delete:
				//delete task
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		        mDbHelper.deleteReminder(info.id);
		        fillData();                                                  //returns deleted data from DB
				return true;
		}
			return super.onContextItemSelected(item);
			
		}
		
		
}