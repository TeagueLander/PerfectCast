package com.teaguelander.audio.perfectcast.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.teaguelander.audio.perfectcast.FragmentAdapter;
import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.R;

/**
 * Created by Teague-Win10 on 1/10/2017.
 */

public class MainFragment extends Fragment{

	//Views

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.content_main, parent, false);

		final MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSearchView().clearQuery(); //Maybe set it what is was when this fragment was left?
		//The bottom toolbar which has audio controls
		Toolbar controlToolbar = (Toolbar) v.findViewById(R.id.control_toolbar);
		ImageButton playPauseButton = (ImageButton) v.findViewById(R.id.playPauseButton);
		//Main Content View (contains everything not in toolbars)
		RelativeLayout mainContentView = (RelativeLayout) v.findViewById(R.id.activity_main_content);
		//Tabs
		FragmentAdapter adapter = new FragmentAdapter(getChildFragmentManager());
		adapter.addFragment(new FavouritesFragment(), getString(R.string.tab_favourites));
		adapter.addFragment(new NowPlayingFragment(), getString(R.string.tab_now_playing));
		adapter.addFragment(new UpNextFragment(), getString(R.string.tab_up_next));
		//View Pager
		ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);
		viewPager.setOffscreenPageLimit(2);
		viewPager.setAdapter(adapter);
		//Bind tabs to the ViewPager
		TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);

		return v;
	}

}
