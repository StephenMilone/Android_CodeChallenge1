package com.milone.codechallenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class ResultsActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// XML Layout Variable Declarations
		setContentView(R.layout.activity_results);
		TextView txtResults = (TextView) findViewById(R.id.txtResults);

		// Get the ArrayList that was passed via MainActivity
		Intent extras = getIntent();
		ArrayList<String> list = extras.getStringArrayListExtra("list");

		// The following block gets the results and sorts them so the most
		// picked name is the first, it does this by a loop in a loop.

		// The inner loop uses Collections to find out how many times each Name
		// is in the list

		// The outer loop is a counter from 10 to 1.

		// If the item the inner loop is checking is in the list the same amount
		// of times
		// as the outer loop counter. Add it to the results display.

		// This is how we get the ones picked the most on top of the list.

		// the pos variable is to display their rank next to their name
		int pos = 1;
		String resultstxt = "";

		for (int x = 10; x > 0; x--) {
			Set<String> unique = new HashSet<String>(list);
			for (String key : unique) {
				int amount = Collections.frequency(list, key);

				if (amount == x) {
					 //amount = 1 check is only for plural check
					if (amount == 1)
						resultstxt = resultstxt + pos
								+ ")  " + key + " - 1pt\n";
					else
						resultstxt = resultstxt +  pos
								+ ")  " + key + " - " + amount + "pts\n";
					pos++;
				}
			}
			
		}
		txtResults.setText(resultstxt);
	}
}