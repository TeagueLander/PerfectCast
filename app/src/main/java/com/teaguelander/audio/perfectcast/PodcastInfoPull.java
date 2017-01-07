package com.teaguelander.audio.perfectcast;


import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/** WE CAN PROBABLY DELETE THIS FILE
 * Created by Teague-Win10 on 7/28/2016.
 */
public class PodcastInfoPull {

	public void pull(AppCompatActivity app) {
		TextView titleView = (TextView) app.findViewById(R.id.infoTitle);
		TextView descView = (TextView) app.findViewById(R.id.infoDesc);

		Document doc = null;
		try {
			doc = Jsoup.connect("https://www.spreaker.com/show/1672823/episodes/feed").get();
		} catch (IOException e) {
			e.printStackTrace();
			titleView.setText("Didnt work now!");
		}

		//Parsing here
		Element title = doc.select("title").first();
		Element desc = doc.select("description").first();

		titleView.setText(title.text());
		descView.setText(desc.text());


	}

	/*public PodcastInfoPull() {
		TextView textView = (TextView) findViewById(R.id.testText);
		textView.setText("Working now!");
	}*/

}
