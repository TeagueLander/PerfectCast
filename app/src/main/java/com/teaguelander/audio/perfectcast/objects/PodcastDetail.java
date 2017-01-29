package com.teaguelander.audio.perfectcast.objects;

import android.util.Log;
import android.util.Xml;

import com.teaguelander.audio.perfectcast.services.DatabaseService;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;

import static com.teaguelander.audio.perfectcast.PerfectCastApp.rssDateFormatter;

/**
 * Created by Teague-Win10 on 1/14/2017.
 */

public class PodcastDetail {

	//Database Columns
	public static final String KEY_ID = "id";
	public static final String KEY_URL = "url";
	public static final String KEY_TITLE = "title";
	public static final String KEY_IMAGE_URL = "imageUrl";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_SUBSCRIBED = "subscribed";
	public static final String KEY_XML = "xml";
	public static final String[] COLUMNS = { KEY_ID, KEY_URL, KEY_TITLE, KEY_IMAGE_URL, KEY_DESCRIPTION, KEY_SUBSCRIBED, KEY_XML };

	//Object fields
	public long mId = -1;
	public String mUrl;
	public String mTitle;
	public String mImageUrl;
	public String mDescription;
	public boolean mSubscribed = false;
	public String mXml;
	public ArrayList<PodcastEpisode> mEpisodes;


	public PodcastDetail(String url, String xml) throws IOException, XmlPullParserException {
		mUrl = url;
		mXml = xml;
		parsePodcastXml(xml);
	}

	public PodcastDetail(String url, String title, String imageUrl, String description, boolean subscribed, String xml) {
		mUrl = url;
		mTitle = title;
		mImageUrl = imageUrl;
		mDescription = description;
		mSubscribed = subscribed;
		mXml = xml;
	}

	public void setPodcastDetails(String title, String imageUrl, String description, ArrayList<PodcastEpisode> episodes) {
		mTitle = title;
		mImageUrl = imageUrl;
		mDescription = description;
		mEpisodes = episodes;
	}

	public void setId(long id) {
		mId = id;
	}

	private void parsePodcastXml(String xml) throws XmlPullParserException, IOException {
		String title = null;
		String imageUrl = null;
		String description = null;
		ArrayList<PodcastEpisode> episodes = new ArrayList<PodcastEpisode>();

		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput( new StringReader(xml) );
		parser.nextTag();

		parser.require(XmlPullParser.START_TAG, null, "rss");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			//Log.d("name of tag", name);
			//Starts by looking for the entry tag
			if (name.equalsIgnoreCase("channel")) { /* Steps into the channel */ }
			else if (name.equalsIgnoreCase("title")) {

				title = readTagText(parser, "title");

			} else if (name.equalsIgnoreCase("image")) {

				imageUrl = readImageTagUrl(parser);

			} else if (name.equalsIgnoreCase("description")) {

				description = readTagText(parser, "description");

			} else if (name.equalsIgnoreCase("item")) {

				episodes.add(readItemTag(parser));

			} else if (name.equalsIgnoreCase("media:thumbnail")) {

				imageUrl = readMediaImageTag(parser);

			} else if (name.equalsIgnoreCase("itunes:image")) {

				imageUrl = readItunesImageTag(parser);

			} else {
				skipTag(parser);
			}
		}
		Log.d("pd", "Title: " + title);
		Log.d("pd", "Description: " + description);
		Log.d("pd", "ImageUrl: " + imageUrl);
//		Log.d("pd", ">>episodes>>");

//		for (PodcastEpisode episode: episodes) {
//			Log.d("pdepisode", "title: " + episode.mTitle);
//			Log.d("pdepisode", "description: " + episode.mDescription);
//			Log.d("pdepisode", "url: " + episode.mUrl);
//			Log.d("pdepisode", "duration: " + episode.mDuration);
//			Log.d("pdepisode", "pubDate: " + episode.mPubDate);
//			Log.d("pd", ">>");
//		}

//		return new PodcastDetail(title, imageUrl, description, episodes);
		setPodcastDetails(title, imageUrl, description, episodes);
	}

	private static String readImageTagUrl(XmlPullParser parser) throws IOException, XmlPullParserException {

		String url = "";
		parser.require(XmlPullParser.START_TAG, null, "image");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equalsIgnoreCase("url")) {
//				Log.d("pd", "Found image URL!");
				url = readTagText(parser, "url");
			} else {
				skipTag(parser);
			}
		}
		parser.require(XmlPullParser.END_TAG, null, "image");
		return url;
	}

	private static String readMediaImageTag(XmlPullParser parser) throws IOException, XmlPullParserException {
		String url = parser.getAttributeValue(null, "url");
		parser.nextTag();
		return url;
	}

	private static String readItunesImageTag(XmlPullParser parser) throws IOException, XmlPullParserException {
		String url = parser.getAttributeValue(null, "href");
		parser.nextTag();
		return url;
	}

	private PodcastEpisode readItemTag(XmlPullParser parser) throws IOException, XmlPullParserException {
		String title = "";
		String description = null;
		String url = "";
		String duration = "";
		Date pubDate = null;
		String bytes = "";

		parser.require(XmlPullParser.START_TAG, null, "item");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equalsIgnoreCase("title")) {
				title = readTagText(parser, "title");
			} else if (name.equalsIgnoreCase("pubDate")) {
				try {
					pubDate = rssDateFormatter.parse(readTagText(parser, "pubDate"));
				}catch (Exception e) { Log.e("pd", "Failed to parse pubDate"); }
			} else if (name.equalsIgnoreCase("enclosure")) {
				//url = parser.getAttributeName(null, "url");
				url = parser.getAttributeValue(null, "url");
				bytes = parser.getAttributeValue(null, "length");
				parser.nextTag();
			} else if (name.equalsIgnoreCase("description")){
				description = readTagText(parser, "description");
			} else if (name.equalsIgnoreCase("itunes:duration")){
				duration = readTagText(parser, "itunes:duration");
			}else {
				skipTag(parser);
			}
		}
		PodcastEpisode episode = new PodcastEpisode(title, description, url, duration, pubDate, bytes, this);
		//TODO check if episode already exists and get its ID
		episode.setIds(-1, mId);
		return episode;
	}

	private static String readTagText(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
		String text = "";
		parser.require(XmlPullParser.START_TAG, null, tag);
			parser.next();
			text = parser.getText();
			if (text == null) {
				int event = parser.nextToken();
				if (event == XmlPullParser.CDSECT) {
					//text = Html.escapeHtml(parser.getText()).toString();
					text = parser.getText();
				}
			}
			parser.nextTag();

		parser.require(XmlPullParser.END_TAG, null, tag);
		return text;
	}

	private static void skipTag(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}

	public boolean getSubscribed() {
		return mSubscribed;
	}

	public void setSubscribed(boolean value) {
		mSubscribed = value;
		if (value == true) {
			addToDatabase();
		} else {
//			remove?
		}


		//TODO remove TrackQueue test
		new TrackQueue();
	}

	public void addToDatabase() {
		DatabaseService ds = DatabaseService.getInstance(null);

		mId = ds.addPodcast(this);
		Log.d("pd", "ID: " + mId);
	}

}
