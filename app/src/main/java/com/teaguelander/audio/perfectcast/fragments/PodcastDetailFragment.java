package com.teaguelander.audio.perfectcast.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.PodcastDetail;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.objects.RowItemClickListener;
import com.teaguelander.audio.perfectcast.recycler.EpisodeLinearAdapter;
import com.teaguelander.audio.perfectcast.services.DataService;
import com.teaguelander.audio.perfectcast.services.PicassoService;
import com.teaguelander.audio.perfectcast.services.StorageService;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Teague-Win10 on 1/11/2017.
 */

public class PodcastDetailFragment extends Fragment implements RowItemClickListener {

	String mFeedUrl;
	String mFeedXmlDataset;
	View mView;
	PodcastDetail mPodcastDetail;

	private RecyclerView mEpisodesRecycler;
	private LinearLayoutManager mEpisodesLinearLayoutManager;
	private EpisodeLinearAdapter mEpisodeLinearAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//IF NO feedUrl or no feedUrl response then just use XML
		Log.d("pdf", getArguments().getString("feedUrl"));
		mFeedUrl = getArguments().getString("feedUrl");

		if (mFeedUrl != null) {
			Log.d("pdf", "Requesting Xml");
			sendXmlQuery(mFeedUrl);
		} else {
			Log.d("pdf", "Didnt request Xml");
		}
		/*
		mCurrentQuery = getArguments().getString("currentQuery");
		sendSearchQuery(mCurrentQuery);
//		mAdapter = new PodcastLinearAdapter(searchResultsDataset);*/
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.view_podcast_detail, container, false);

		mView = v;
		return mView;
	}

	public void sendXmlQuery(final String feedUrl) {
		//final PodcastDetailFragment pdf = this;
		DataService.getInstance(getContext()).getPodcastFeed(feedUrl, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					//System.out.println("xml " + response);
					mFeedXmlDataset = response;
					mPodcastDetail = new PodcastDetail(feedUrl, response);
					if (mView != null) {
						setupView();
					}
				}catch (IOException e) {
					Log.e("pdf parse podcast error", e.toString());
				}catch (XmlPullParserException e) {
					Log.e("pdf parse podcast error", e.toString());
				}
			}
		});
	}

	private void setupView() {
		// Podcast Title
		TextView titleView = (TextView) mView.findViewById(R.id.podcast_detail_title);
		titleView.setText(mPodcastDetail.mTitle);
		titleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
		titleView.setSingleLine(true);
		titleView.setMarqueeRepeatLimit(5);
		titleView.setSelected(true);
		//Podcast Image
		ImageView imageView = (ImageView) mView.findViewById(R.id.podcast_detail_image);
		if (mPodcastDetail.mImageUrl != null) {
//			DataService.getInstance(getContext()).loadImageIntoView(mPodcastDetail.mImageUrl, imageView);
			PicassoService.loadImage(mPodcastDetail.mImageUrl, imageView);
		}
		//Subscribe Button
		Button subButton = (Button) mView.findViewById(R.id.button_subscriber);
		refreshSubStatus();
		subButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPodcastDetail.setSubscribed(!mPodcastDetail.getSubscribed());
				refreshSubStatus();
			}
		});
		//Description Area
		TextView descriptionView = (TextView) mView.findViewById(R.id.description);
		if (mPodcastDetail.mDescription != null) { descriptionView.setText(mPodcastDetail.mDescription); }
		//Episodes
		mEpisodesRecycler = (RecyclerView) mView.findViewById(R.id.episodesRecycler);
		mEpisodesRecycler.setHasFixedSize(true);
//		mEpisodesRecycler.setNestedScrollingEnabled(false);
		mEpisodesLinearLayoutManager= new LinearLayoutManager(getContext());
		mEpisodesLinearLayoutManager.setOrientation(LinearLayout.VERTICAL);
		mEpisodesRecycler.setLayoutManager(mEpisodesLinearLayoutManager);
		mEpisodeLinearAdapter = new EpisodeLinearAdapter(mPodcastDetail.mEpisodes, EpisodeLinearAdapter.PODCAST_DETAIL_MODE, this);
		mEpisodesRecycler.setAdapter(mEpisodeLinearAdapter);

	}

	private void refreshSubStatus() {
		Button subButton = (Button) mView.findViewById(R.id.button_subscriber);
		if (mPodcastDetail.getSubscribed() == true) {
			subButton.setBackgroundTintList(  ColorStateList.valueOf( getResources().getColor(R.color.buttonSelected, null) )  );
			subButton.setTextColor(getResources().getColor(R.color.textColorPrimary, null));
			subButton.setText(R.string.button_subscribed);
		} else {
			subButton.setBackgroundTintList(  ColorStateList.valueOf( getResources().getColor(R.color.buttonGrey, null) )  );
			subButton.setTextColor(getResources().getColor(R.color.black, null));
			subButton.setText(R.string.button_subscribe);
		}
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
