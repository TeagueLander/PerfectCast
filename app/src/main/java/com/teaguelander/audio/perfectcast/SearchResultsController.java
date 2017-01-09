package com.teaguelander.audio.perfectcast;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;

import org.json.JSONObject;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class SearchResultsController {

	Context mContext;
	LinearLayout mView;
	JSONObject jsonResults;

	public SearchResultsController(Context context, LinearLayout view) {
		mContext = context;
		mView = view;
	}

	public void sendSearchQuery(String query) {
		DataService.getInstance(mContext).searchPodcasts(query, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				setJSONResults(response);
				Log.d("ma", "Got response!");
				Log.d("ma", response);
			}
		});
	}

	public void setJSONResults(String json) {
		try {
			jsonResults = new JSONObject(json);
		}catch  (Exception e){
			Log.e("src", e.toString());
		}

		TextView textView = (TextView) mView.findViewById(R.id.searchQuery);
		textView.setText(json.toString());
	}

	public void clear() {
		//Remove everything here
	}



}
