package com.teaguelander.audio.perfectcast.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.ItemDragAndDropHelperCallback;
import com.teaguelander.audio.perfectcast.objects.PodcastDetail;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.objects.ItemClickListener;
import com.teaguelander.audio.perfectcast.recycler.EpisodeLinearAdapter;
import com.teaguelander.audio.perfectcast.services.AudioService;
import com.teaguelander.audio.perfectcast.services.TrackQueueService;

import java.util.ArrayList;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class UpNextFragment extends Fragment implements ItemClickListener {

	View mView;
	private RecyclerView mUpNextRecycler;
	private LinearLayoutManager mUpNextLinearLayoutManager;
	private EpisodeLinearAdapter mUpNextLinearAdapter;
	private ItemTouchHelper mItemTouchHelper;
	private TextView mEmptyQueueTextView;

	private ArrayList<PodcastEpisode> mEpisodes;
	private MainActivity mMainActivity;
	private TrackQueueService queueService;

	private BroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMainActivity = (MainActivity) getActivity();
		queueService = mMainActivity.getQueueService();

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d("ma", "UpNext received Intent: " + action);


				//TODO update %complete and stuff like that
				if (action.equals(AudioService.COMPLETED_STATUS)) {
					//Remove episode from recycler
					mUpNextLinearAdapter.removeEpisodeAt(0); //TODO do we still this still? It was copied to ELA
				}
				if (action.equals(EpisodeDetailFragment.QUEUE_REMOVE_ITEM)) {
					int position = intent.getIntExtra(EpisodeDetailFragment.ITEM_POSITION, -1);
					mUpNextLinearAdapter.removeEpisodeAt(position);
				}
				if (action.equals(EpisodeDetailFragment.QUEUE_ADD_ITEM)) {
					int position = intent.getIntExtra(EpisodeDetailFragment.ITEM_POSITION, -1);
					int oldPosition = intent.getIntExtra(EpisodeDetailFragment.OLD_POSITION, -1);
					if (oldPosition == -1) {
						mUpNextLinearAdapter.addEpisodeAt(position);
					} else {
						mUpNextLinearAdapter.moveEpisodeTo(position, oldPosition);
					}
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioService.PLAYING_STATUS);
		filter.addAction(AudioService.PREPARING_STATUS);
		filter.addAction(AudioService.STOPPED_STATUS);
		filter.addAction(AudioService.PAUSED_STATUS);
		filter.addAction(AudioService.ERROR_STATUS);
		filter.addAction(AudioService.DESTROYED_STATUS);
		filter.addAction(AudioService.NEW_TRACK_STATUS);
		filter.addAction(AudioService.COMPLETED_STATUS);
		filter.addAction(EpisodeDetailFragment.QUEUE_REMOVE_ITEM);
		filter.addAction(EpisodeDetailFragment.QUEUE_ADD_ITEM);
		getActivity().registerReceiver(receiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("UNF", "Up next created");
		mEpisodes = queueService.getQueueItems();

		mView = inflater.inflate(R.layout.view_up_next, container, false);
		mEmptyQueueTextView = (TextView) mView.findViewById(R.id.emptyQueue);

		mUpNextRecycler = (RecyclerView) mView.findViewById(R.id.upNextRecycler);
		mUpNextRecycler.setHasFixedSize(true);
		mUpNextLinearLayoutManager = new LinearLayoutManager(getContext());
		mUpNextLinearLayoutManager.setOrientation(LinearLayout.VERTICAL);
		mUpNextRecycler.setLayoutManager(mUpNextLinearLayoutManager);
		mUpNextLinearAdapter = new EpisodeLinearAdapter(mEpisodes, EpisodeLinearAdapter.UP_NEXT_MODE, this);
		mUpNextRecycler.setAdapter(mUpNextLinearAdapter);

		ItemTouchHelper.Callback callback = new ItemDragAndDropHelperCallback(mUpNextLinearAdapter, onItemMoveEventFinishedRunnable);
		mItemTouchHelper = new ItemTouchHelper(callback);
		mItemTouchHelper.attachToRecyclerView(mUpNextRecycler);
		
		if (mEpisodes.size() > 0) {
			mEmptyQueueTextView.setVisibility(View.INVISIBLE);
		} else {
			mEmptyQueueTextView.setVisibility(View.VISIBLE);
		}

		return mView;

	}

	@Override
	public void onItemClicked(String feedUrl) {}
	@Override
	public void onItemClicked(PodcastEpisode episode) {
		Log.d("pdf", "Podcast Episode Clicked! " + episode.mTitle);

		//Podcast Detail Fragment
		PodcastDetailFragment podcastDetailFragment = new PodcastDetailFragment();
		Bundle args = new Bundle();
		args.putBoolean("podcastSet", true);
		args.putBoolean("noEpisodes", true);
		podcastDetailFragment.setArguments(args);
		podcastDetailFragment.setPodcast(episode.mPodcast);

		//Episode Detail Fragment
		EpisodeDetailFragment episodeDetail = new EpisodeDetailFragment();
		episodeDetail.setEpisode(episode);

		Log.d("pdf", "Podcast Episode Clicked! " + episode.mTitle);
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fragment_container, podcastDetailFragment);
		ft.add(R.id.fragment_container, episodeDetail);
		ft.addToBackStack(null);
		ft.commit();
		Log.d("pdf", "Podcast Episode Clicked! " + episode.mTitle);

	}

	@Override
	public void onItemClicked(PodcastDetail podcast) {}

	Runnable onItemMoveEventFinishedRunnable = new Runnable() {
		@Override
		public void run() {
			Log.d("unf", "Update database");
			queueService.updateDatabase();
		}
	};
}
