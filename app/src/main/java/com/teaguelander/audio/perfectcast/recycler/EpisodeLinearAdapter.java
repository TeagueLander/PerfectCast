package com.teaguelander.audio.perfectcast.recycler;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.objects.RowItemClickListener;
import com.teaguelander.audio.perfectcast.services.PicassoService;

import java.util.ArrayList;

import static com.teaguelander.audio.perfectcast.PerfectCastApp.basicDateFormatter;

/**
 * Created by Teague-Win10 on 1/15/2017.
 */

public class EpisodeLinearAdapter extends RecyclerView.Adapter<EpisodeLinearAdapter.ViewHolder> {

	public static final String PODCAST_DETAIL_MODE = "podcast_detail_mode";
	public static final String UP_NEXT_MODE = "up_next_mode";

	private ArrayList<PodcastEpisode> mEpisodes;
	private int mEpisodesCount;
	private RowItemClickListener mItemClickListener;
	private String mMode;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View mView;
		public ImageView mPodcastImage;
		public TextView mTitleTextView;
		public TextView mDurationTextView;
		public TextView mSizeTextView;
		public TextView mPubDateTextView;
		public String mUrl;

		public ViewHolder(View v) {
			super(v);
			mView = v;
			mPodcastImage = (ImageView) v.findViewById(R.id.podcast_image);
			mTitleTextView = (TextView) v.findViewById(R.id.episode_title);
			mDurationTextView = (TextView) v.findViewById(R.id.episode_duration);
			mPubDateTextView = (TextView) v.findViewById(R.id.episode_pubdate);
		}
	}

	public EpisodeLinearAdapter(ArrayList<PodcastEpisode> episodes, String mode, RowItemClickListener itemClickListener) {
		Log.d("edf", "EPISODES LINEARA CREATED");
		mEpisodesCount = episodes.size();
		mEpisodes = episodes;
		mItemClickListener = itemClickListener;
		mMode = mode;
	}


	@Override
	public EpisodeLinearAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.episode_item_row, parent, false);

		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final PodcastEpisode episode = mEpisodes.get(position);

		if (mMode == UP_NEXT_MODE) {
			PicassoService.loadImage(episode.mPodcast.mImageUrl, holder.mPodcastImage);
			holder.mPodcastImage.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mTitleTextView.getLayoutParams();
			params.addRule(RelativeLayout.RIGHT_OF, R.id.podcast_image);
			holder.mTitleTextView.setLayoutParams(params);
		}

		holder.mTitleTextView.setText(episode.mTitle);
		holder.mDurationTextView.setText( episode.mDuration );
		if (episode.mPubDate != null) {
			holder.mPubDateTextView.setText(basicDateFormatter.format(episode.mPubDate));
		}

		holder.mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mItemClickListener.onRowItemClicked(episode);
			}
		});
	}

	@Override
	public int getItemCount() {
		return mEpisodesCount;
	}

}
