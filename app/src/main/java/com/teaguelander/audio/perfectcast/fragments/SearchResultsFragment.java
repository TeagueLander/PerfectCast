package com.teaguelander.audio.perfectcast.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.Response;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.teaguelander.audio.perfectcast.objects.PodcastDetail;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.services.DataService;
import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.objects.ItemClickListener;
import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.recycler.PodcastLinearAdapter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Teague-Win10 on 1/9/2017.
 */

public class SearchResultsFragment extends Fragment implements ItemClickListener {

	private RecyclerView mRecyclerView;
	private LinearLayoutManager mLinearLayoutManager;
	private PodcastLinearAdapter mAdapter;
	private JSONObject mSearchResultsDataset;
	private String mCurrentQuery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCurrentQuery = getArguments().getString("currentQuery");
		sendSearchQuery(mCurrentQuery);
//		mAdapter = new PodcastLinearAdapter(searchResultsDataset);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.view_search_results, parent, false);

		final MainActivity mainActivity = (MainActivity) getActivity();
		final FloatingSearchView searchView = mainActivity.getSearchView();

		searchView.setSearchText(mCurrentQuery);

		mRecyclerView = (RecyclerView) v.findViewById(R.id.search_results_recycler_view);
		mRecyclerView.setHasFixedSize(true);
		mLinearLayoutManager = new LinearLayoutManager(getContext());
		mLinearLayoutManager.setOrientation(LinearLayout.VERTICAL);
		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mRecyclerView.setAdapter(mAdapter);


		//Events

		return v;
	}

	public void sendSearchQuery(String query) {
		//final SearchResultsFragment srf = this;
		DataService.getInstance(getContext()).searchPodcasts(query, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
//				Log.d("srf", "Got response!");
//				Log.d("srf", response);
				setJSONDataset(response);
			}
		});
	}

	private void setJSONDataset(String jsonString) {
		try {
			mSearchResultsDataset = new JSONObject(jsonString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mAdapter = new PodcastLinearAdapter(mSearchResultsDataset, this);
		if (mRecyclerView != null) {
			mRecyclerView.setAdapter(mAdapter);
		}
	}

	@Override
	public void onItemClicked(String feedUrl) {
		Log.d("srf","Podcast Clicked!");
		PodcastDetailFragment podcastDetailFragment = new PodcastDetailFragment();
		Bundle args = new Bundle();
		args.putString("feedUrl", feedUrl);
		podcastDetailFragment.setArguments(args);

		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, podcastDetailFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	@Override
	public void onItemClicked(PodcastEpisode episode) {}

	@Override
	public void onItemClicked(PodcastDetail podcast) {}
}
