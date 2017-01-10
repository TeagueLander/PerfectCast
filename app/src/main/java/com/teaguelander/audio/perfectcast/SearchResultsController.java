package com.teaguelander.audio.perfectcast;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.teaguelander.audio.perfectcast.recycler.PodcastLinearAdapter;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class SearchResultsController {

	Context mContext;
	LinearLayout mView;
	FloatingSearchView mSearchView;
	JSONObject jsonResults;

	public SearchResultsController(Context context, FloatingSearchView searchView, LinearLayout searchresultsView) {
		mContext = context;
		mView = searchresultsView;
		mSearchView = searchView;
	}

	private void buildRecyclerView(RecyclerView recyclerView) {
		Log.e("pla", "GOT TO BUILD RECYCLER");

		recyclerView = new RecyclerView(mContext);
		mView.addView(recyclerView);
		recyclerView.setHasFixedSize(true);
		RecyclerView.Adapter adapter = new PodcastLinearAdapter(jsonResults);
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

	}



	public void sendSearchQuery(String query) {
		DataService.getInstance(mContext).searchPodcasts(query, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("ma", "Got response!");
				Log.d("ma", response);
				setJSONResults(response);
			}
		});
	}

	public void setJSONResults(String json) {
		try {
			jsonResults = new JSONObject(json);
			Log.e("pla", "JSON BUILT");
		}catch  (Exception e){
			Log.e("src", e.toString());
		}
		Log.e("pla", "GOT END OF SET JSON");
		buildRecyclerView((RecyclerView) mView.findViewById(R.id.search_results_recycler_view));
	}


	public void clear() {
		//Remove everything here
	}




	public void hideSearchResultsView() {
		if (mView.getVisibility() == View.VISIBLE) {
			Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_top);
			animation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}
				@Override
				public void onAnimationEnd(Animation animation) {
					mView.setVisibility(View.GONE);
				}
				@Override
				public void onAnimationRepeat(Animation animation) {}
			});
			mView.startAnimation(animation);
			mView.setVisibility(View.GONE);
			mSearchView.setLeftActionMode(FloatingSearchView.LEFT_ACTION_MODE_SHOW_HAMBURGER);
			mSearchView.clearSearchFocus();
			mSearchView.clearQuery();
		}
	}

	public void showSearchResultsView() {
		if (mView.getVisibility() != View.VISIBLE) {
			Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_from_top);
			animation.setFillAfter(false);
			mView.startAnimation(animation);
			mView.setVisibility(View.VISIBLE);
			mSearchView.setLeftActionMode(FloatingSearchView.LEFT_ACTION_MODE_SHOW_HOME);
		}
	}


}
