package com.teaguelander.audio.perfectcast.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.teaguelander.audio.perfectcast.objects.PodcastDetail;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

//import static com.teaguelander.audio.perfectcast.PerfectCastApp.basicDateFormatter;
import static com.teaguelander.audio.perfectcast.PerfectCastApp.rssDateFormatter;


/**
 * Created by Teague-Win10 on 1/22/2017.
 */

public class DatabaseService extends SQLiteOpenHelper {

	private static DatabaseService mInstance;
	private static Context mContext;
	private static SQLiteDatabase mDatabase;

	private static final int DATABASE_VERSION = 30;
	private static final String DATABASE_NAME = "PerfectCast";
	private static final String TABLE_EPISODES = "episodes";
	private static final String TABLE_PODCASTS = "podcasts";
	private static final String TABLE_TRACK_QUEUE = "track_queue";
	private static final String VIEW_TRACK_QUEUE = "view_track_queue";

	//TODO implement a LRU cache for podcast maybe?
	public DatabaseService(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
		mDatabase = this.getWritableDatabase();
	}

	public static synchronized DatabaseService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DatabaseService (context);
		}
		return mInstance;
	}

	public void closeDatabase() {
		mDatabase.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//SQL statements to create tables
		String CREATE_TABLE_PODCASTS = "CREATE TABLE " + TABLE_PODCASTS + " ( " +
											   PodcastDetail.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
											   PodcastDetail.KEY_URL + " TEXT UNIQUE, " +
											   PodcastDetail.KEY_TITLE + " TEXT," +
											   PodcastDetail.KEY_IMAGE_URL + " TEXT," +
											   PodcastDetail.KEY_DESCRIPTION + " TEXT," +
											   PodcastDetail.KEY_SUBSCRIBED + " INTEGER," +
											   PodcastDetail.KEY_XML + " TEXT);";
		String CREATE_TABLE_EPISODES = "CREATE TABLE " + TABLE_EPISODES + " ( " +
											   PodcastEpisode.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
											   PodcastEpisode.KEY_PODCAST_ID + " INTEGER," +
											   PodcastEpisode.KEY_TITLE + " TEXT," +
											   PodcastEpisode.KEY_DESCRIPTION + " TEXT," +
											   PodcastEpisode.KEY_URL + " TEXT UNIQUE," +
											   PodcastEpisode.KEY_DURATION + " TEXT ," +
											   PodcastEpisode.KEY_PUB_DATE + " TEXT," + //TODO set this to DATE
											   PodcastEpisode.KEY_BYTES + " INTEGER," +
											   PodcastEpisode.KEY_PROGRESS + " INTEGER," +
											   "FOREIGN KEY(" + PodcastEpisode.KEY_PODCAST_ID + ") REFERENCES " + TABLE_PODCASTS + "(" + PodcastDetail.KEY_ID + ") )";
		String CREATE_TABLE_TRACK_QUEUE = "CREATE TABLE " + TABLE_TRACK_QUEUE + " ( " +
												  TrackQueueService.KEY_EPISODE_ID + " INTEGER UNIQUE, " +
												  TrackQueueService.KEY_ORDER_NUMBER + " INTEGER, " +
												  "FOREIGN KEY(" + TrackQueueService.KEY_EPISODE_ID + ") REFERENCES " + TABLE_EPISODES + "(" + PodcastEpisode.KEY_ID + ") )";
		String CREATE_VIEW_TRACK_QUEUE = "CREATE VIEW " + VIEW_TRACK_QUEUE + " AS " +
												 "SELECT " + TrackQueueService.KEY_ORDER_NUMBER + ", " + TextUtils.join(",", PodcastEpisode.COLUMNS) +
												 " FROM " + TABLE_EPISODES + " ep, " + TABLE_TRACK_QUEUE + " q " +
												 "WHERE ep." + PodcastEpisode.KEY_ID + " = q." + TrackQueueService.KEY_EPISODE_ID;
		db.execSQL(CREATE_TABLE_EPISODES);
		db.execSQL(CREATE_TABLE_PODCASTS);
		db.execSQL(CREATE_TABLE_TRACK_QUEUE);
		db.execSQL(CREATE_VIEW_TRACK_QUEUE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PODCASTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK_QUEUE);
		db.execSQL("DROP VIEW IF EXISTS " + VIEW_TRACK_QUEUE);
		this.onCreate(db);
	}

	//ADDS

	//Add Podcast
	public long addPodcast(PodcastDetail podcast) {
		Log.d("dbs", "Adding podcast " + podcast.mTitle + " Subscribed: " + podcast.mSubscribed);

		//TODO made this a get by url function
		Cursor cursor = mDatabase.query(TABLE_PODCASTS,
								 new String[] {PodcastDetail.KEY_ID},
								 PodcastDetail.KEY_URL + " = ?",
								 new String[] { podcast.mUrl },
								 null, null, null);
		if (cursor != null) { //Podcast already exists
			Log.d("dbs", "Cursor not null!");
			//TODO We should do an update statement
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				return cursor.getLong(0);
			}
		}

		//Podcast doesnt exist in db, insert statement
		long id = -1;
		ContentValues values = new ContentValues();
		values.put(PodcastDetail.KEY_URL, podcast.mUrl);
		values.put(PodcastDetail.KEY_TITLE, podcast.mTitle);
		values.put(PodcastDetail.KEY_IMAGE_URL, podcast.mImageUrl);
		values.put(PodcastDetail.KEY_DESCRIPTION, podcast.mDescription);
		values.put(PodcastDetail.KEY_SUBSCRIBED, podcast.mSubscribed);
		values.put(PodcastDetail.KEY_XML, podcast.mXml );

		id = mDatabase.insert(TABLE_PODCASTS, null, values);

		return id;
	}

	//Adds episode if it doesnt exists and returns its ID
	public long addEpisode(PodcastEpisode episode) {
		Log.d("dbs", "Adding episode " + episode.mTitle);

		//Check if episode already exists in database
		//TODO made this a get by url function
		Cursor cursor = mDatabase.query(TABLE_EPISODES,
								 new String[] { PodcastEpisode.KEY_ID },
								 PodcastEpisode.KEY_URL + " = ?",
								 new String[] { episode.mUrl },
								 null, null, null);
		if (cursor != null) {
			Log.d("dbs", "Episode cursor not null!");
			//TODO Update with latest podcast episode info (I doubt it has changed...)
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				return cursor.getLong(0);
			}
		}

//		We need the Podcast id and to add it if it hasn't been done yet
		long podcastId;
		long episodeId;

		if (episode.mPodcastId == -1) {
			podcastId = addPodcast(episode.mPodcast);
			episode.mPodcastId = podcastId; //Maybe do this outside of here?
			episode.mPodcast.mId = podcastId; //Maybe do this outside of here?
		}

		ContentValues values = new ContentValues();
		values.put(PodcastEpisode.KEY_PODCAST_ID, episode.mPodcastId );
		values.put(PodcastEpisode.KEY_TITLE, episode.mTitle);
		values.put(PodcastEpisode.KEY_DESCRIPTION, episode.mDescription);
		values.put(PodcastEpisode.KEY_URL, episode.mUrl );
		values.put(PodcastEpisode.KEY_DURATION, episode.mDuration);
		values.put(PodcastEpisode.KEY_PUB_DATE, rssDateFormatter.format(episode.mPubDate) );
		values.put(PodcastEpisode.KEY_BYTES, episode.mBytes);
		values.put(PodcastEpisode.KEY_PROGRESS, episode.mProgress);

		episodeId = mDatabase.insert(TABLE_EPISODES, null, values);
//		episode.mId = episodeId; //Maybe do this outside of here? DOESNT WORK

		Log.d("dbs", "Adding or returning episode id " + episodeId);
		return episodeId;
	}

	public void updateTrackQueue(ArrayList<PodcastEpisode> queue) {
		mDatabase.beginTransaction();

		mDatabase.delete(TABLE_TRACK_QUEUE, null, null);
//		mDatabase.execSQL("DELETE FROM " + TABLE_TRACK_QUEUE);

		int currentItemNumber = 0;
		for (PodcastEpisode item : queue) {
			ContentValues values = new ContentValues();
			values.put(TrackQueueService.KEY_EPISODE_ID, item.mId);
			values.put(TrackQueueService.KEY_ORDER_NUMBER, currentItemNumber);

			mDatabase.insert(TABLE_TRACK_QUEUE, null, values);

			currentItemNumber++;
		}

		mDatabase.setTransactionSuccessful();
		mDatabase.endTransaction();
		Log.d("dbs", "Updated Track Queue in Database");
	}

//UPDATES

	//Update Progress
	public void updateEpisodeProgress(PodcastEpisode episode) {
		ContentValues values = new ContentValues();
		values.put(PodcastEpisode.KEY_PROGRESS, episode.mProgress);

		mDatabase.update(TABLE_EPISODES, values, PodcastEpisode.KEY_ID + "= ?", new String[]{ Long.toString(episode.mId) });
	}


//GETS

	//Get episode by id
	public PodcastEpisode getEpisodeById(Long id) {

		Cursor cursor = mDatabase.query(TABLE_EPISODES,
								 PodcastEpisode.COLUMNS,
								 PodcastEpisode.KEY_ID + " = ?",
								 new String[] { Long.toString(id) },
								 null, null, null, null
		);

		if (cursor != null) {
			cursor.moveToFirst();
		}

		PodcastEpisode episode = null;
		try {
			episode = getPodcastFromCursor(cursor);

		} catch (CursorIndexOutOfBoundsException e) {
			Log.d("dbs", "No next episode to find");
//			e.printStackTrace();
			Log.e("dbs", "Skipping stack trace");
		}

		return episode;
	}

	//GET PODCAST BY ID
	public PodcastDetail getPodcast(long id) { //TODO add a parameter to get XML or not
		Log.d("dbs", "Requesting podcast by id: " + id);

		Cursor cursor = mDatabase.query(TABLE_PODCASTS,
								 PodcastDetail.COLUMNS,
								 PodcastDetail.KEY_ID + " = ?",
								 new String[] { Long.toString(id) },
								 null, null, null);

		if (cursor != null) {
			cursor.moveToFirst();
		}

		PodcastDetail podcast = null;
		try {
			podcast = new PodcastDetail(cursor.getString(1),
										cursor.getString(2),
										cursor.getString(3),
										cursor.getString(4),
										cursor.getInt(5) == 1,
										cursor.getString(6));
			podcast.setId(cursor.getLong(0));
			Log.d("dbs", "Got podcast from database");
		} catch (CursorIndexOutOfBoundsException e) {
			Log.d("dbs", "No podcast or error parsing");
//			e.printStackTrace();
			Log.e("dbs", "Skipping podcast error stack trace");
		}

		return podcast;
	}

	public ArrayList<PodcastEpisode> getTrackQueue() {
//		ArrayList<PodcastEpisode> list = new ArrayList<PodcastEpisode>();
		Log.d("dbs", "GETTING TRACK QUEUE");

		Cursor cursor = mDatabase.query(VIEW_TRACK_QUEUE,
										PodcastEpisode.COLUMNS,
										null, null, null, null,
										TrackQueueService.KEY_ORDER_NUMBER + " asc");
		if (cursor == null) {
			return null;
		}

		PodcastEpisode[] trackQueue = new PodcastEpisode[cursor.getCount()];

		while (cursor.moveToNext()) {

			PodcastEpisode episode = getPodcastFromCursor(cursor);
			trackQueue[cursor.getPosition()] = episode;
			Log.d("dbs", "Queue item " + cursor.getPosition() + ". Episode: " + episode.toString());
		}

		return new ArrayList<PodcastEpisode>(Arrays.asList(trackQueue));
	}

	//Cursor Iterating
	private PodcastEpisode getPodcastFromCursor(Cursor cursor) {

		PodcastDetail podcast = getPodcast(cursor.getInt(1)); //TODO implement a podcast cache so we dont lookup and create the same podcast a bunch of times!

		Date pubDate = null;
		String date = cursor.getString(6);
		if (date != null) {
			try { pubDate = rssDateFormatter.parse(date); } catch (ParseException e) { e.printStackTrace(); }
		}

		PodcastEpisode episode = new PodcastEpisode(cursor.getString(2),
									 cursor.getString(3),
									 cursor.getString(4),
									 cursor.getString(5),
									 pubDate,
									 cursor.getLong(7),
									 podcast);
		episode.setIds(cursor.getLong(0), cursor.getLong(1));
		episode.setProgress(cursor.getLong(8));

		return episode;
	}


	//Podcast Gets//

	public void checkPodcastParameters(PodcastDetail podcast) {
		Cursor cursor = mDatabase.query(TABLE_PODCASTS,
										new String[] { PodcastDetail.KEY_ID, PodcastDetail.KEY_SUBSCRIBED },
										PodcastDetail.KEY_URL + " = ?",
										new String[] { podcast.mUrl },
										null, null, null);

		if (cursor.moveToFirst()) {
			Log.d("dbs", "Getting subscribed " + Boolean.parseBoolean(cursor.getString(1)));
			long id = cursor.getLong(0);
			boolean subscribed = cursor.getInt(1) == 1;

			podcast.setId(id);
			podcast.setSubscribed(subscribed);
		}
	}

	//Podcast Updates//

	public void updatePodcastSubscribed(PodcastDetail podcast) {

		Log.d("dbs", "Updating " + podcast.mTitle + " subscribed " + podcast.mSubscribed);

		ContentValues values = new ContentValues();
		values.put(PodcastDetail.KEY_SUBSCRIBED, podcast.mSubscribed);

		mDatabase.update(TABLE_PODCASTS,
						 values,
						 PodcastDetail.KEY_SUBSCRIBED + " = ?",
						 new String[] { Long.toString(podcast.mId) }
		);

	}

}
