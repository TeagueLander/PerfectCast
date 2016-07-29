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

    public void pull(View view, AppCompatActivity app) {
        TextView textView = (TextView) view;

        Document doc = null;
        try {
            doc = Jsoup.connect("https://www.spreaker.com/show/1672823/episodes/feed").get();
        } catch (IOException e) {
            e.printStackTrace();
            textView.setText("Didnt work now!");
        }
        String htmlString = doc.toString();
        textView.setText(htmlString);

        Element title = doc.select("title").first();
        textView.setText(title.text());

    }

    /*public PodcastInfoPull() {
        TextView textView = (TextView) findViewById(R.id.testText);
        textView.setText("Working now!");
    }*/

}
