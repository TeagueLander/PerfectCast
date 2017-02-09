package com.teaguelander.audio.perfectcast.recycler;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.fragments.FavouritesFragment;
import com.teaguelander.audio.perfectcast.objects.ItemClickListener;
import com.teaguelander.audio.perfectcast.objects.PodcastDetail;
import com.teaguelander.audio.perfectcast.services.PicassoService;

import java.util.ArrayList;

/**
 * Created by Teague-Win10 on 2/6/2017.
 */

public class PodcastGridAdapter extends RecyclerView.Adapter<PodcastGridAdapter.ViewHolder> {

	ItemClickListener mItemClickListener;
	ArrayList<PodcastDetail> mPodcasts;
	int mPodcastsLength;
	int mParentWidth;
	int mItemSize; //TODO there should be a max size, we divide parent width to get a good size

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View mView;
		public ImageView mImageView;

		public ViewHolder(View v) {
			super(v);
			mView = v;
			mImageView = (ImageView) v.findViewById(R.id.item_image);
		}
	}

	public PodcastGridAdapter(ArrayList<PodcastDetail> podcasts, ItemClickListener itemClickListener){
		mPodcasts = podcasts;
		mPodcastsLength = podcasts.size();

		mItemClickListener = itemClickListener;
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.podcast_grid_item, parent, false);

		mParentWidth = parent.getWidth();
		mItemSize = mParentWidth / FavouritesFragment.GRID_SQUARES_WIDE;
		GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) v.getLayoutParams();
		params.height = mItemSize;
		params.width = mItemSize;
		v.setLayoutParams(params);

		ViewHolder vh = new ViewHolder(v);

		return vh;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final PodcastDetail podcast = mPodcasts.get(position);

		PicassoService.loadImage(podcast.mImageUrl, holder.mImageView);

		holder.mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mItemClickListener.onItemClicked(podcast.mUrl);
			}
		});
	}

	@Override
	public int getItemCount() {
		return mPodcastsLength;
	}

}
