package com.milone.codechallenge;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.milone.codechallenge.ImageDownloader;
import com.milone.codechallenge.ImageDownloader.Mode;

public class MainActivity extends Activity implements OnClickListener {

	// TODO Change the ID to your Facebook App ID
	Facebook facebook = new Facebook("your id here");

	// Variables
	private SharedPreferences mPrefs;
	JSONObject jObject;
	JSONArray jArray;
	ArrayList<String> list = null;

	// XML Objects
	ImageButton picture1;
	ImageButton picture2;
	TextView name1;
	TextView name2;
	ProgressBar pbar;

	// variables needed for Basic Gesture Detection
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// XML Layout Variable Declarations
		setContentView(R.layout.activity_main);
		LinearLayout llayout = (LinearLayout) findViewById(R.id.llayout);
		picture1 = (ImageButton) findViewById(R.id.imgProfile1);
		picture2 = (ImageButton) findViewById(R.id.imgProfile2);

		name1 = (TextView) findViewById(R.id.txtProfile1);
		name2 = (TextView) findViewById(R.id.txtProfile2);
		pbar = (ProgressBar) findViewById(R.id.progressBar);

		// ArrayList that stores all the names picked
		list = new ArrayList<String>();

		// Facebook Authentication Code, see SDK for further explanation
		mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}
		if (!facebook.isSessionValid()) {
			facebook.authorize(this, new String[] {}, new DialogListener() {
				@Override
				public void onComplete(Bundle values) {
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("access_token", facebook.getAccessToken());
					editor.putLong("access_expires",
							facebook.getAccessExpires());
					editor.commit();
				}

				@Override
				public void onFacebookError(FacebookError error) {
				}

				@Override
				public void onError(DialogError e) {
				}

				@Override
				public void onCancel() {
				}
			});
		}
		// End Facebook Auth Code

		// Now try/catch to use Facebook Graph API to load a list of users
		// friends into an array (id and name are gathered)
		try {
			// JSON object to called Facebook Graph API
			// JSON array to store the result
			// call function to load 2 friends
			jObject = Util.parseJson(facebook.request("me/friends"));
			jArray = jObject.getJSONArray("data");
			DisplayTwoFriends();
		} catch (FacebookError e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// This block implements a screen swipe to get 2 new friends loaded.
		gestureDetector = new GestureDetector(this, new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};
		llayout.setOnClickListener(MainActivity.this);
		llayout.setOnTouchListener(gestureListener);

		// if picture 1 is 'clicked'
		picture1.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				// Advance the progress bar 1 more spot
				int current = pbar.getProgress() + 1;
				pbar.setProgress(current);

				// Add the name to the ArrayList
				list.add(name1.getText() + "");

				// If this is our 10th pick, send the ArrayList and load
				// ResultsActivity
				if (current >= 10) {
					Intent resultsIntent = new Intent(MainActivity.this,
							ResultsActivity.class);
					resultsIntent.putExtra("list", list);
					startActivity(resultsIntent);
					finish();
				} else
					// If not our 10th pick, show 2 more friends
					DisplayTwoFriends();
			}
		});

		// if picture2 is 'clicked', same exact as picture1 except adding name2
		// to list.
		picture2.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				int current = pbar.getProgress() + 1;
				pbar.setProgress(current);
				list.add(name2.getText() + "");
				if (current >= 10) {
					Intent resultsIntent = new Intent(MainActivity.this,
							ResultsActivity.class);
					resultsIntent.putExtra("list", list);
					startActivity(resultsIntent);
					finish();
				} else
					DisplayTwoFriends();
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		facebook.extendAccessTokenIfNeeded(this, null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	// The class for the screen swipe.
	// it only looks for left -> right swipes, others are not checked
	// http://stackoverflow.com/questions/937313/android-basic-gesture-detection
	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// left to right swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					DisplayTwoFriends();
				}
			} catch (Exception e) {
			}
			return false;
		}

	}

	public void DisplayTwoFriends() {
		try {
			// Get 2 random numbers depending on friend list length
			Random myRandom = new Random();
			int Friend1 = myRandom.nextInt(jArray.length() - 1);
			int Friend2 = myRandom.nextInt(jArray.length() - 1);
			// To prevent the same person being profile 1 and 2
			while (Friend1 == Friend2)
				Friend2 = myRandom.nextInt(jArray.length() - 1);

			// Load friend1's name/photo url
			JSONObject jFriend1;
			jFriend1 = jArray.getJSONObject(Friend1);
			name1.setText(jFriend1.getString("name"));
			String url1 = "http://graph.facebook.com/"
					+ jFriend1.getString("id") + "/picture?type=square";

			// Load friend2's name/photo url
			JSONObject jFriend2 = jArray.getJSONObject(Friend2);
			name2.setText(jFriend2.getString("name"));
			String url2 = "http://graph.facebook.com/"
					+ jFriend2.getString("id") + "/picture?type=square";

			// Download 2 profile pictures
			ImageDownloader imageDownloader = new ImageDownloader();
			imageDownloader.setMode(Mode.CORRECT);
			imageDownloader.download(url1, (ImageButton) picture1);
			imageDownloader.download(url2, (ImageButton) picture2);

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View v) {
	}

}