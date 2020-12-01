package com.group24.wellnessapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AdviceActivity extends AppCompatActivity {
    Context context = this;
    JSONArray jArr = null;

    // AsyncTask for JSON requests
    class adviceAsyncTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground (String...userID) {
            // Get data for tips
            try {
                jArr = getTipData(LoginActivity.getUserID());
            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Create and display tips
                    for (int i = 0; i < jArr.length(); i++) {
                        LinearLayout parentLinearLayout = findViewById(R.id.linearLayout);

                        // Create new layout to put tips in
                        LinearLayout newLayout = new LinearLayout(context);
                        // Set parameters for the layout
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.topMargin = 25;
                        layoutParams.leftMargin = 30;
                        layoutParams.rightMargin = 30;
                        newLayout.setLayoutParams(layoutParams);
                        newLayout.setBackgroundResource(R.drawable.customborder);
                        newLayout.setOrientation(LinearLayout.VERTICAL);
                        int newLayoutID = 2;
                        newLayout.setId(newLayoutID);

                        // Add tip
                        TextView adviceTextView = new TextView(context);
                        try {
                            adviceTextView.setText(jArr.get(i).toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adviceTextView.setTextSize(17);

                        TextView titleTextView = new TextView(context);
                        titleTextView.setText("Tip!");
                        titleTextView.setTextSize(19);

                        // Add tips to layout
                        newLayout.addView(titleTextView);
                        newLayout.addView(adviceTextView);

                        parentLinearLayout.addView(newLayout);
                    }
                }
            });

            return null;
        }

        protected void onPostExecute (Void v) {
            // Remove back button from toolbar
            if (getSupportActionBar() != null) {
                ActionBar actionBar = getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice);

        // Start AsyncTask
        new adviceAsyncTask().execute(LoginActivity.getUserID());
    }

    // Getting data for tips based on user
    public static JSONArray getTipData(String userID) throws IOException {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(calendar.getTime());

        // Connect to server based on the user and the date
        URL urlForGetRequest = new URL("http://3.92.227.189:80/api/tips/" + userID + "/" + currentDate);
        String readLine = null;
        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        // Get response
        int responseCode = connection.getResponseCode();
        System.out.println("TIPS Response Code :  " + responseCode);
        System.out.println("TIPS Response Message : " + connection.getResponseMessage());
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Success
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            } in.close();

            // Receive data
            JSONArray jArr = null;
            try {
                jArr = new JSONArray(response.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jArr;
        } else {
            // Error message
            System.out.println("TIPS NOT WORKED");
            return null;
        }
    }
}