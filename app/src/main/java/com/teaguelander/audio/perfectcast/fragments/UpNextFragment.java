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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.NetworkImageView;
import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.objects.RowItemClickListener;
import com.teaguelander.audio.perfectcast.recycler.EpisodeLinearAdapter;
import com.teaguelander.audio.perfectcast.services.TrackQueueService;

import java.util.ArrayList;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class UpNextFragment extends Fragment implements RowItemClickListener {

	View mView;
	private RecyclerView mUpNextRecycler;
	private LinearLayoutManager mUpNextLinearLayoutManager;
	private EpisodeLinearAdapter mUpNextLinearAdapter;
	private ArrayList<PodcastEpisode> mEpisodes;
	private MainActivity mMainActivity;
	private TrackQueueService queueService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMainActivity = (MainActivity) getActivity();
		queueService = mMainActivity.getQueueService();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mEpisodes = queueService.getQueueItems();

		mView = inflater.inflate(R.layout.view_up_next, container, false);

		mUpNextRecycler = (RecyclerView) mView.findViewById(R.id.upNextRecycler);
		mUpNextRecycler.setHasFixedSize(true);
		mUpNextLinearLayoutManager = new LinearLayoutManager(getContext());
		mUpNextLinearLayoutManager.setOrientation(LinearLayout.VERTICAL);
		mUpNextRecycler.setLayoutManager(mUpNextLinearLayoutManager);
		mUpNextLinearAdapter = new EpisodeLinearAdapter(mEpisodes, EpisodeLinearAdapter.UP_NEXT_MODE, this);
		mUpNextRecycler.setAdapter(mUpNextLinearAdapter);

		return mView;

	}

	@Override
	public void onRowItemClicked(String feedUrl) {}
	@Override
	public void onRowItemClicked(PodcastEpisode episode) {
		Log.d("pdf", "Podcast Episode Clicked! " + episode.mTitle);
		ImageView image = (ImageView) mView.findViewById(R.id.podcast_detail_image);

		//Log.d("pdf", "Image resource: " + image.getResources());
//		StorageService.getInstance(getContext()).saveImageToStorage(getContext(), episode.mPodcast.mImageUrl);

		EpisodeDetailFragment episodeDetail = new EpisodeDetailFragment();
		episodeDetail.setEpisode(episode);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(R.id.fragment_container, episodeDetail);
		ft.addToBackStack(null);
		ft.commit();

	}
}
