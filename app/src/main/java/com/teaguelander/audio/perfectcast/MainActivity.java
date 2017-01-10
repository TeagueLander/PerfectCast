package com.teaguelander.audio.perfectcast;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import com.android.volley.Response;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.teaguelander.audio.perfectcast.DataService;
import com.teaguelander.audio.perfectcast.fragments.FavouritesFragment;
import com.teaguelander.audio.perfectcast.fragments.NowPlayingFragment;
import com.teaguelander.audio.perfectcast.fragments.UpNextFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity { //implements SearchView.OnQueryTextListener, SearchView.OnCloseListener

	//Useful everywhere
	boolean isAudioPlaying = false;
	BroadcastReceiver receiver;
	AppCompatActivity thisActivity = this;

	//Views
	FloatingSearchView searchView;
	LinearLayout searchResultsView;
	SearchResultsController searchResultsViewController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Allow Internet Access
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		//The top bar with search
		searchView = (FloatingSearchView) findViewById(R.id.searchView);
		searchResultsView = (LinearLayout) findViewById(R.id.searchResultsView);
		searchResultsViewController = new SearchResultsController(getBaseContext(), searchView, searchResultsView);
		//The bottom toolbar which has audio controls
		Toolbar controlToolbar = (Toolbar) findViewById(R.id.control_toolbar);
		ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
		//Main Content View (contains everything not in toolbars)
		RelativeLayout mainContentView = (RelativeLayout) findViewById(R.id.activity_main_content);
		//Tabs
		FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
		adapter.addFragment(new FavouritesFragment(), getString(R.string.tab_favourites));
		adapter.addFragment(new NowPlayingFragment(), getString(R.string.tab_now_playing));
		adapter.addFragment(new UpNextFragment(), getString(R.string.tab_up_next));
		//View Pager
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(adapter);
		//Bind tabs to the ViewPager
		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);

		//GetViews
//		LinearLayout searchResultsView = (LinearLayout) findViewById(R.id.searchResultsView);


		//BroadcastReceiver and filter - recieves actions like play and pause from the notification tray
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d("ma", "MainActivity received Intent: " + action);
				ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);

//				Toast.makeText(getApplicationContext(), "Action Received: " + action, Toast.LENGTH_SHORT).show();
				if (action.equals(AudioService.PAUSE_ACTION) || action.equals(AudioService.STOP_ACTION) || action.equals(AudioService.DESTROY_ACTION)) {
					setPlayButton(false);
				}
				else if (action.equals(AudioService.PLAY_ACTION)) {
					setPlayButton(true);
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioService.PLAY_ACTION);
		filter.addAction(AudioService.PAUSE_ACTION);
		filter.addAction(AudioService.STOP_ACTION);
		filter.addAction(AudioService.DESTROY_ACTION);
		registerReceiver(receiver, filter);


		/*EVENT LISTENERS*/
		//Search Submitted
		searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
			@Override
			public void onSuggestionClicked(SearchSuggestion searchSuggestion) {}
			@Override
			public void onSearchAction(String currentQuery) {
				searchResultsViewController.sendSearchQuery(currentQuery);
				searchResultsViewController.showSearchResultsView();
			}
		});
		//Back Arrow
		searchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
			@Override
			public void onHomeClicked() {
				searchResultsViewController.hideSearchResultsView();
			}
		});

//		Button retrieveButton = (Button) findViewById(R.id.retrieveButton);
//		if (retrieveButton != null)
//		retrieveButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//
//				try {
//					(new DataService()).pull(thisActivity);
//				}
//				catch (Exception e){
//					Log.d("ma", e.toString());
//				}
//			}
//		});
	}


	@Override
	public void onBackPressed() {
		if (searchResultsView.getVisibility() == View.VISIBLE) {
			searchResultsViewController.hideSearchResultsView();
			return;
		}
		super.onBackPressed();
	}

	@Override()
	public void onDestroy() {
		//Cleanup goes here
		super.onDestroy();
		stopAudioService();
		unregisterReceiver(receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		return super.onOptionsItemSelected(item);
	}

	public void startAudioService() {
		startService(new Intent(getBaseContext(), AudioService.class));
		setPlayButton(true);
	}
	public void stopAudioService() {
		stopService(new Intent(getBaseContext(), AudioService.class));
		setPlayButton(false);
	}

	//Ideally the button would have a pending intent on it instead.  That way an intent is sent to the service and the service returns an intent which changes the icon
	public void playButtonPressed(View view) {
		if(isAudioPlaying){
//			startService(new Intent(AudioService.PAUSE_ACTION, null, getBaseContext(), AudioService.class));
			sendBroadcast(new Intent(AudioService.PAUSE_ACTION));
			isAudioPlaying = false;
		}else {
			startService(new Intent(AudioService.PLAY_ACTION, null, getBaseContext(), AudioService.class));
			isAudioPlaying = true;
		}
		setPlayButton(isAudioPlaying);
	}

	//Controls the play/pause button
	private void setPlayButton(boolean playing) {
		ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
		if (playing) {
			playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
		}else {
			playPauseButton.setImageResource(android.R.drawable.ic_media_play);
		}
	}

}
