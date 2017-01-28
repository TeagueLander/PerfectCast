package com.teaguelander.audio.perfectcast.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.teaguelander.audio.perfectcast.services.DataService;
import com.teaguelander.audio.perfectcast.objects.RowItemClickListener;
import com.teaguelander.audio.perfectcast.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Teague-Win10 on 1/9/2017.
 */

public class PodcastLinearAdapter extends RecyclerView.Adapter<PodcastLinearAdapter.ViewHolder> {
	private JSONArray mDataset;
	private int mDatasetLength;
	private RowItemClickListener mItemClickListener;

	public static class ViewHolder extends RecyclerView.ViewHolder{

		public View mView;
		public NetworkImageView mImageView;
		public TextView mTitleTextView;
		public String mFeedUrl;

		public ViewHolder(View v) {
			super(v);
			mView = v;
			mImageView = (NetworkImageView) v.findViewById(R.id.item_image);
			mTitleTextView = (TextView) v.findViewById(R.id.item_title);
		}
	}

	public PodcastLinearAdapter(JSONObject dataset, RowItemClickListener itemClickListener) {
		mDatasetLength = 0;
		mItemClickListener = itemClickListener;
		try {
			mDatasetLength = dataset.getInt("resultCount");
			mDataset = dataset.getJSONArray("results");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Create new views (invoked by the layout manager)
	@Override
	public PodcastLinearAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.podcast_item_row, parent, false);
		//Set view's size margins everything here

		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final PodcastLinearAdapter.ViewHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element
		try {
			JSONObject dataItem = mDataset.getJSONObject(position);

			String imgUrl = dataItem.getString("artworkUrl600");
			DataService.getInstance(null).loadImageIntoView(imgUrl, holder.mImageView);

			String title = dataItem.getString("collectionName");
			holder.mTitleTextView.setText(title);

			holder.mFeedUrl = dataItem.getString("feedUrl");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		holder.mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mItemClickListener.onRowItemClicked(holder.mFeedUrl);
			}
		});
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mDatasetLength;
	}

}
