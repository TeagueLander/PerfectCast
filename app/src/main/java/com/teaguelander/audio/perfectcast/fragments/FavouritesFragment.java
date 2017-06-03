package com.teaguelander.audio.perfectcast.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.ItemClickListener;
import com.teaguelander.audio.perfectcast.objects.PodcastDetail;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.recycler.EpisodeLinearAdapter;
import com.teaguelander.audio.perfectcast.recycler.PodcastGridAdapter;
import com.teaguelander.audio.perfectcast.services.DatabaseService;

import java.util.ArrayList;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class FavouritesFragment extends Fragment implements ItemClickListener {

	public static final int GRID_SQUARES_WIDE = 4;

	ArrayList<PodcastDetail> mSubbedPodcasts;
	DatabaseService mDatabaseService;
	View mView;
	TextView mNoSubsView;

	RecyclerView subbedPodcastRecycler;
	GridLayoutManager subbedPodcastGridLayoutManager;
	PodcastGridAdapter subbedPodcastGridAdapter;

	public FavouritesFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDatabaseService = DatabaseService.getInstance(null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mSubbedPodcasts = mDatabaseService.getSubscribedPodcasts(); //Synchronous

		mView = inflater.inflate(R.layout.view_favourites, container, false);
		mNoSubsView = (TextView) mView.findViewById(R.id.noSubs);

		subbedPodcastRecycler = (RecyclerView) mView.findViewById(R.id.subbedPodcastRecycler);
		subbedPodcastRecycler.setHasFixedSize(true);
		subbedPodcastGridLayoutManager = new GridLayoutManager(getContext(), GRID_SQUARES_WIDE);
		subbedPodcastGridLayoutManager.setOrientation(GridLayout.VERTICAL);
		subbedPodcastRecycler.setLayoutManager(subbedPodcastGridLayoutManager);
		subbedPodcastGridAdapter = new PodcastGridAdapter(mSubbedPodcasts, this);
		subbedPodcastRecycler.setAdapter(subbedPodcastGridAdapter);
		
//		If there are podcasts, then hide the "No Subs" message
		if (mSubbedPodcasts.size() > 0) {
			mNoSubsView.setVisibility(View.INVISIBLE);
		} else {
			mNoSubsView.setVisibility(View.VISIBLE);
		}

		return mView;

	}

	@Override
	public void onItemClicked(String feedUrl) {
		Log.d("srf", "Podcast Clicked!");
		PodcastDetailFragment podcastDetailFragment = new PodcastDetailFragment();
		Bundle args = new Bundle();
		args.putString("feedUrl", feedUrl);
		podcastDetailFragment.setArguments(args);

		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, podcastDetailFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onItemClicked(PodcastEpisode episode) {}

	@Override
	public void onItemClicked(PodcastDetail podcast) { //TODO maybe remove this? Or improve it

	}
}
