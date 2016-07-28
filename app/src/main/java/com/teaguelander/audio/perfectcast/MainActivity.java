package com.teaguelander.audio.perfectcast;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SearchView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
    implements SearchView.OnQueryTextListener, SearchView.OnCloseListener{

    boolean isAudioPlaying = false;
    BroadcastReceiver receiver;

    //Search
    private SearchManager searchManager;
    private android.widget.SearchView searchView;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView myList;
    private ArrayList<ParentRow> parentList = new ArrayList<ParentRow>();
    private ArrayList<ParentRow> showTheseParentList = new ArrayList<ParentRow>();
    private MenuItem searchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //The top bar with search
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //The bottom toolbar which has audio controls
        Toolbar controlToolbar = (Toolbar) findViewById(R.id.control_toolbar);
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);

        //BroadcastReceiver and filter - recieves actions like play and pause from the notification tray
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
                if (action.equals(AudioService.PAUSE_ACTION)) {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
                    isAudioPlaying = false;
                }
                else if (action.equals(AudioService.PLAY_ACTION)) {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
                    isAudioPlaying = true;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioService.PLAY_ACTION);
        filter.addAction(AudioService.PAUSE_ACTION);
        registerReceiver(receiver, filter);

        /*Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAudioService();
            }
        });
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAudioService();
            }
        });*/

        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        parentList = new ArrayList<ParentRow>();
        showTheseParentList = new ArrayList<ParentRow>();

        //App will crash if not called here
        displayList();
        //Expands list of contents
        expandAll();
    }

    @Override()
    public void onDestroy() {
        //Cleanup goes here
        super.onDestroy();
        stopAudioService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.requestFocus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void startAudioService() {
        startService(new Intent(getBaseContext(), AudioService.class));
        isAudioPlaying = true;
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
    }
    public void stopAudioService() {
        stopService(new Intent(getBaseContext(), AudioService.class));
        isAudioPlaying = false;
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    //Ideally the button would have a pending intent on it instead.  That way an intent is sent to the service and the service returns an intent which changes the icon
    public void playButtonPressed(View view) {
        if(isAudioPlaying){
            startService(new Intent(AudioService.PAUSE_ACTION, null, getBaseContext(), AudioService.class));
            ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
            playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
            isAudioPlaying = false;
        }else {
            startService(new Intent(AudioService.PLAY_ACTION, null, getBaseContext(), AudioService.class));
            ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
            playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
            isAudioPlaying = true;
        }
    }

    @Override
    public boolean onClose() {
        listAdapter.filterData("");
        expandAll();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        listAdapter.filterData(query);
        expandAll();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        listAdapter.filterData(newText);
        expandAll();
        return false;
    }

    //Temp function
    private void loadData() {
        ArrayList<ChildRow> childRows = new ArrayList<ChildRow>();
        ParentRow parentRow = null;

        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Lorem ipsum dolor sit amet"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Sit Fido, sit"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Lorem ipsum dolor sit amet"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Sit Fido, sit"));
        parentRow = new ParentRow("First Group", childRows);
        parentList.add(parentRow);

        childRows = new ArrayList<ChildRow>();
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Fido is the name of my dog"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Two plus two is ten"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Fido is the name of my dog"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Two plus two is ten"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Fido is the name of my dog"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Two plus two is ten"));
        parentRow = new ParentRow("Second Group", childRows);
        parentList.add(parentRow);

        childRows = new ArrayList<ChildRow>();
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Fido is the name of my dog"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Two plus two is ten"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Fido is the name of my dog"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Two plus two is ten"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Fido is the name of my dog"));
        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Two plus two is ten"));
        parentRow = new ParentRow("Third Group", childRows);
        parentList.add(parentRow);
    }

    private void expandAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            myList.expandGroup(i);
        }
    }

    private void displayList() {
        loadData();
        myList = (ExpandableListView) findViewById(R.id.expandableListView_search);
        listAdapter = new ExpandableListAdapter(MainActivity.this, parentList);
        myList.setAdapter(listAdapter);
    }

}
