package com.teaguelander.audio.perfectcast.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.teaguelander.audio.perfectcast.objects.PodcastDetail;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by Teague-Win10 on 1/22/2017.
 */

public class DatabaseService extends SQLiteOpenHelper {

	private static DatabaseService mInstance;
	private static Context mContext;

	private static final int DATABASE_VERSION = 11;
	private static final String DATABASE_NAME = "PerfectCast";
	private static final String TABLE_EPISODES = "episodes";
	private static final String TABLE_PODCASTS = "podcasts";

	public DatabaseService(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	public static synchronized DatabaseService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DatabaseService (context);
		}
		return mInstance;
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
											   PodcastDetail.KEY_SUBSCRIBED + " TEXT," +
											   PodcastDetail.KEY_XML + " TEXT);";
//											   "UNIQUE (" + TextUtils.join(",", PodcastDetail.COLUMNS)  +") );";
		String CREATE_TABLE_EPISODES = "CREATE TABLE " + TABLE_EPISODES + " ( " +
											   PodcastEpisode.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
											   PodcastEpisode.KEY_PODCAST_ID + " INTEGER," +
											   PodcastEpisode.KEY_TITLE + " TEXT," +
											   PodcastEpisode.KEY_DESCRIPTION + " TEXT," +
											   PodcastEpisode.KEY_URL + " TEXT UNIQUE," +
											   "FOREIGN KEY(" + PodcastEpisode.KEY_PODCAST_ID + ") REFERENCES " + TABLE_PODCASTS + "(" + PodcastDetail.KEY_ID + ") )";
//											  "UNIQUE (" + TextUtils.join(",", PodcastEpisode.COLUMNS)  +") );";
		db.execSQL(CREATE_TABLE_EPISODES);
		db.execSQL(CREATE_TABLE_PODCASTS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PODCASTS);
//		db.execSQL("DROP TABLE IF EXISTS " + "up_next");
		this.onCreate(db);
	}

	//ADDS

	//Add Podcast
	public long addPodcast(PodcastDetail podcast) {
		Log.d("dbs", "Adding podcast " + podcast.mTitle);

		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cursor = db.query(TABLE_PODCASTS,
								 new String[] {PodcastDetail.KEY_ID},
								 PodcastDetail.KEY_URL + " = ?",
								 new String[] { podcast.mUrl },
								 null, null, null);
		if (cursor != null) { //Podcast already exists
			Log.d("dbs", "Cursor not null!");
			//TODO We should do an update statement
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				db.close();
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

		id = db.insert(TABLE_PODCASTS, null, values);

		db.close();

		return id;
	}

	//Add episode
	public long addEpisode(PodcastEpisode episode) {
		Log.d("dbs", "Adding episode " + episode.mTitle);

		SQLiteDatabase db = this.getWritableDatabase();

		//Check if episode already exists in database
		Cursor cursor = db.query(TABLE_EPISODES,
								 new String[] { PodcastEpisode.KEY_ID },
								 PodcastEpisode.KEY_URL + " = ?",
								 new String[] { episode.mUrl },
								 null, null, null);
		if (cursor != null) {
			Log.d("dbs", "Episode cursor not null!");
			//TODO Update with latest podcast episode info (I doubt it has changed...)
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				db.close();
				return cursor.getLong(0);
			}
		}

//		We need the Podcast id and to add it if it hasn't been done yet
		long podcastId;
		long episodeId;

		if (episode.mPodcastId == -1) {
			podcastId = addPodcast(episode.mPodcast);
			episode.mPodcastId = podcastId;
			episode.mPodcast.mId = podcastId;
		}

		ContentValues values = new ContentValues();
		values.put(PodcastEpisode.KEY_PODCAST_ID, episode.mPodcastId );
		values.put(PodcastEpisode.KEY_TITLE, episode.mTitle);
		values.put(PodcastEpisode.KEY_DESCRIPTION, episode.mDescription);
		values.put(PodcastEpisode.KEY_URL, episode.mUrl );

		episodeId = db.insert(TABLE_EPISODES, null, values);

		db.close();

		return episodeId;
	}

//GETS

	//Get episode by id
	public PodcastEpisode getEpisodeById(Long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_EPISODES,
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
			PodcastDetail podcast = getPodcast(cursor.getInt(1));

			episode = new PodcastEpisode(cursor.getString(2),
										 cursor.getString(3),
										 cursor.getString(4),
										 null,
										 null,
										 null,
										 podcast);
			episode.setIds(cursor.getLong(0), cursor.getLong(1));

			Log.d("dbs", "Got next episode from database" + episode.toString());
			Log.d("dbs", "Podcast ID1: " + podcast.mId + " Podcast ID2: " + cursor.getLong(1) + " EpID: " + cursor.getLong(0));

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

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_PODCASTS,
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
										Boolean.parseBoolean(cursor.getString(5)),
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

	private String encode(String string) {
		String encodedString = "";
		try {
			encodedString = URLEncoder.encode(string, StorageService.CHARSET);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return encodedString;
	}

	private String decode(String string) {
		String decodedString = "";
		try {
			decodedString = URLDecoder.decode(string, StorageService.CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decodedString;
	}


}
