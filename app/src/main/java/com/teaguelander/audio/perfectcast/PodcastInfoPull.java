package com.teaguelander.audio.perfectcast;


import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

/** WE CAN PROBABLY DELETE THIS FILE
 * Created by Teague-Win10 on 7/28/2016.
 */
public class PodcastInfoPull {

	public void pull(AppCompatActivity app) throws IOException, JSONException {
		TextView titleView = (TextView) app.findViewById(R.id.infoTitle);
		TextView descView = (TextView) app.findViewById(R.id.infoDesc);

		BufferedReader reader = null;
		JSONObject json = readJsonFromUrl("https://itunes.apple.com/search?term=collider&country=CA&media=podcast&entity=podcast");

		//Parsing here
		String title = "Title"; //doc.select("title").first();
		String desc = json.toString(); //doc.select("description").first();

		titleView.setText(title);
		descView.setText(desc);


	}

	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
			String jsonText = sb.toString();
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	/*public PodcastInfoPull() {
		TextView textView = (TextView) findViewById(R.id.testText);
		textView.setText("Working now!");
	}*/

}
