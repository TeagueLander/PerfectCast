package com.teaguelander.audio.perfectcast.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teaguelander.audio.perfectcast.R;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class FavouritesFragment extends Fragment {
	public FavouritesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//		RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.view_recycler, container, false);
//		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
//		return recyclerView;

		View view = inflater.inflate(R.layout.view_favourites, container, false);
		return view;

	}
}
