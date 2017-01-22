package com.teaguelander.audio.perfectcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.teaguelander.audio.perfectcast.fragments.MainFragment;
import com.teaguelander.audio.perfectcast.fragments.SearchResultsFragment;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.services.AudioService;

public class MainActivity extends AppCompatActivity { //implements SearchView.OnQueryTextListener, SearchView.OnCloseListener

	//Useful everywhere
	boolean isAudioPlaying = false;
	BroadcastReceiver receiver;
	AppCompatActivity thisActivity = this;

	FloatingSearchView searchView;

	//Control Toolbar
	ImageButton mPlayPauseButton;
	TextView mAudioServiceStatusTextView;
	ProgressBar mProgressCircle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Allow Internet Access
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		//FrameLayout (For Fragments)
		FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_container);
		MainFragment mainFragment = new MainFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mainFragment).commit();
		//The top bar with search
		searchView = (FloatingSearchView) findViewById(R.id.searchView);

		//Control toolbar
		mPlayPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
		mProgressCircle = (ProgressBar) findViewById(R.id.progressCircle);
		mAudioServiceStatusTextView = (TextView) findViewById(R.id.audioServiceStatus);

		//TODO remove
		searchView.setSearchText("zelda");

		/*EVENT LISTENERS*/
		//Search Submitted
		searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
			@Override
			public void onSuggestionClicked(SearchSuggestion searchSuggestion) {}
			@Override
			public void onSearchAction(String currentQuery) {
				SearchResultsFragment searchFragment = new SearchResultsFragment();
				Bundle args = new Bundle();
				args.putString("currentQuery", currentQuery);
				searchFragment.setArguments(args);

				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				//Replace whatever is in the fragment view with this fragment and add the transaction to the back stack
				transaction.replace(R.id.fragment_container, searchFragment);
				transaction.addToBackStack(null);

				transaction.commit();
			}
		});


		//BroadcastReceiver and filter - recieves actions like play and pause from the notification tray
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d("ma", "MainActivity received Intent: " + action);

//				Toast.makeText(getApplicationContext(), "Action Received: " + action, Toast.LENGTH_SHORT).show();
//				if (action.equals(AudioService.PAUSE_ACTION) || action.equals(AudioService.STOP_ACTION) || action.equals(AudioService.DESTROY_ACTION)) {
//					setAudioIsPlaying(false);
//				}
//				else if (action.equals(AudioService.PLAYING_STATUS)) {
//					setAudioIsPlaying(true);
//					isAudioPlaying = true;
//				}
				if (action.equals(AudioService.DESTROYED_STATUS)) {
					setAudioIsPlaying(false);
					setAudioServiceStatusText(getResources().getString(R.string.destroyed_status));
				} else if (action.equals(AudioService.ERROR_STATUS)) {
					setAudioIsPlaying(false);
					setAudioServiceStatusText(getResources().getString(R.string.error_status));
				} else if (action.equals(AudioService.PAUSED_STATUS)) {
					setAudioIsPlaying(false);
					setAudioServiceStatusText(getResources().getString(R.string.paused_status));
				} else if (action.equals(AudioService.STOPPED_STATUS)) {
					setAudioIsPlaying(false);
					setAudioServiceStatusText(getResources().getString(R.string.stopped_status));
				} else if (action.equals(AudioService.PREPARING_STATUS)) {
					//setAudioIsPlaying(false);
					setLoadingVisible();
					setAudioServiceStatusText(getResources().getString(R.string.preparing_status));
				} else if (action.equals(AudioService.PLAYING_STATUS)) {
					setAudioIsPlaying(true);
					setAudioServiceStatusText(getResources().getString(R.string.playing_status));
				}
							//|| action.equals(AudioService.ERROR_STATUS) || action.equals(AudioService.PAUSED_STATUS) || )
			}
		};

		IntentFilter filter = new IntentFilter();
		//filter.addAction(AudioService.PLAY_ACTION);
		filter.addAction(AudioService.PLAYING_STATUS);
		filter.addAction(AudioService.PREPARING_STATUS);
		filter.addAction(AudioService.STOPPED_STATUS);
		filter.addAction(AudioService.PAUSED_STATUS);
		filter.addAction(AudioService.ERROR_STATUS);
		filter.addAction(AudioService.DESTROYED_STATUS);


		filter.addAction(AudioService.PAUSE_ACTION);
		filter.addAction(AudioService.STOP_ACTION);
		filter.addAction(AudioService.DESTROY_ACTION);
		registerReceiver(receiver, filter);



	}

	public FloatingSearchView getSearchView() {
		return searchView;
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
		setAudioIsPlaying(true);
	}
	public void stopAudioService() {
		stopService(new Intent(getBaseContext(), AudioService.class));
		setAudioIsPlaying(false);
	}

	//Ideally the button would have a pending intent on it instead.  That way an intent is sent to the service and the service returns an intent which changes the icon
	public void playButtonPressed(View view) {
		if(isAudioPlaying){
//			startService(new Intent(AudioService.PAUSE_ACTION, null, getBaseContext(), AudioService.class));
			sendBroadcast(new Intent(AudioService.PAUSE_ACTION));
		}else {
			startService(new Intent(AudioService.PLAY_ACTION, null, getBaseContext(), AudioService.class));
		}
	}

	//Controls the play/pause button
	private void setAudioIsPlaying(boolean playing) {
		isAudioPlaying = playing;
//		ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
		mPlayPauseButton.setVisibility(View.VISIBLE);
		mProgressCircle.setVisibility(View.INVISIBLE);
		if (playing) {
			mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
		}else {
			mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
		}
	}

	private void setLoadingVisible() {
		isAudioPlaying = false;
		mProgressCircle.setVisibility(View.VISIBLE);
		mPlayPauseButton.setVisibility(View.INVISIBLE);
	}

	public void playEpisode(PodcastEpisode episode) {
		Intent intent = new Intent(AudioService.PLAY_ACTION, null, getBaseContext(), AudioService.class);
		intent.putExtra("url", episode.mUrl);
		Log.d("ma", "Playing: " + episode.mUrl);
		startService(intent);
	}

	private void setAudioServiceStatusText(String status) {

		mAudioServiceStatusTextView.setText(status);
	}

}
