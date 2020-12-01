package com.group24.wellnessapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    boolean hasLoggedActivity = false;
    private Context context;
    JSONArray jArr = null;

    // AsyncTask for JSON requests
    class logAsyncTask extends AsyncTask<String, Void, Void> {
        // Gets activity data from server
        protected Void doInBackground (String...userID) {
            try  {
                jArr = getActivities(LoginActivity.getUserID());
                // If there are activities today
                if (jArr != null && jArr.length() > 0) {
                    // We have logged activities
                    hasLoggedActivity = true;
                } else {
                    hasLoggedActivity = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute (Void v) {
            // Setting layout based on whether user has logged activity or not
            // Hasn't logged elements
            Button goToAddActivityBtn = findViewById(R.id.goToAddActivityBtn);
            TextView addActivityPromptTextView = findViewById(R.id.addActivityPromptTextView);
            // Has logged elements
            ScrollView loggedActivityScrollView = findViewById(R.id.scrollView);
            LinearLayout loggedActivityLinearLayout = findViewById(R.id.parentLinearLayout);
            Button goToAdviceBtn = findViewById(R.id.goToAdviceBtn);
            Button addActivityFromLogBtn = findViewById(R.id.addActivityFromLogBtn);
            Button goToAnalyticsBtn = findViewById(R.id.goToAnalyticsBtn);
            TextView logTitleTextView = findViewById(R.id.logTitleTextView);
            TextView logDateTitleTextView = findViewById(R.id.logDateTitleTextView);

            if (hasLoggedActivity == false) {
                // Hasn't logged elements
                goToAddActivityBtn.setVisibility(View.VISIBLE);
                addActivityPromptTextView.setVisibility(View.VISIBLE);
                // Has logged elements
                loggedActivityScrollView.setVisibility(View.GONE);
                loggedActivityLinearLayout.setVisibility(View.GONE);
                addActivityFromLogBtn.setVisibility(View.GONE);
                goToAnalyticsBtn.setVisibility(View.GONE);
                logTitleTextView.setVisibility(View.GONE);
                logDateTitleTextView.setVisibility(View.GONE);
                goToAdviceBtn.setVisibility(View.GONE);

                // Go to AddActivity screen
                goToAddActivityBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent startIntent = new Intent(getApplicationContext(), AddActivity.class);
                        startActivity(startIntent);
                    }
                });
            } else {
                // Hasn't logged elements
                goToAddActivityBtn.setVisibility(View.GONE);
                addActivityPromptTextView.setVisibility(View.GONE);
                // Has logged elements
                loggedActivityScrollView.setVisibility(View.VISIBLE);
                loggedActivityLinearLayout.setVisibility(View.VISIBLE);
                addActivityFromLogBtn.setVisibility(View.VISIBLE);
                goToAnalyticsBtn.setVisibility(View.VISIBLE);
                logTitleTextView.setVisibility(View.VISIBLE);
                logDateTitleTextView.setVisibility(View.VISIBLE);
                goToAdviceBtn.setVisibility(View.VISIBLE);

                // Get current date
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                // Set date title
                logDateTitleTextView.setText(dateFormat.format(calendar.getTime()));

                // Add activities to log screen
                for (int i = jArr.length()-1; i >= 0; i--) {
                    // Create new layout for activities and set parameters
                    LinearLayout parentLinearLayout = findViewById(R.id.parentLinearLayout);

                    LinearLayout newLayout = new LinearLayout(context);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.topMargin = 25;
                    newLayout.setLayoutParams(layoutParams);
                    newLayout.setBackgroundResource(R.drawable.customborder);
                    newLayout.setOrientation(LinearLayout.VERTICAL);

                    TextView activityCategoryTextView = new TextView(context);
                    TextView activityLabelTextView = new TextView(context);

                    try {
                        activityCategoryTextView.setText(Html.fromHtml("<u>" + jArr.getJSONObject(i).getString("type").substring(0, 1).toUpperCase() + jArr.getJSONObject(i).getString("type").substring(1) + "<u>"));
                        activityLabelTextView.setText(jArr.getJSONObject(i).getString("label").substring(0, 1).toUpperCase() + jArr.getJSONObject(i).getString("label").substring(1) + " for " + jArr.getJSONObject(i).getString("duration") + " hours");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    newLayout.addView(activityCategoryTextView);
                    newLayout.addView(activityLabelTextView);

                    parentLinearLayout.addView(newLayout);
                }

                // Remove back button from toolbar
                if (getSupportActionBar() != null) {
                    ActionBar actionBar = getSupportActionBar();
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }

                // Go to Advice screen
                Button goToAdvice = findViewById(R.id.goToAdviceBtn);
                goToAdvice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent startIntent = new Intent(getApplicationContext(), AdviceActivity.class);
                        startActivity(startIntent);
                    }
                });

                // Go to AddActivity screen
                Button addActivityFromLog = findViewById(R.id.addActivityFromLogBtn);
                addActivityFromLog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent startIntent = new Intent(getApplicationContext(), AddActivity.class);
                        startActivity(startIntent);
                    }
                });

                // Go to Analytics screen
                Button goToAnalytics = findViewById(R.id.goToAnalyticsBtn);
                goToAnalytics.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent startIntent = new Intent(getApplicationContext(), AnalyticsActivity.class);
                        startActivity(startIntent);
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);

        // Start AsyncTask
        new logAsyncTask().execute(LoginActivity.getUserID());
    }

    // Get activities based on user and current date
    public static JSONArray getActivities(String userID) throws IOException {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(calendar.getTime());

        // Connect to server
        URL urlForGetRequest = new URL("http://3.92.227.189:80/api/getDate/" + userID + "/" + currentDate);
        String readLine = null;
        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");

        // Create JSON object with token for user validation
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("token", LoginActivity.getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Send token for validation
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        os.write(jObject.toString().getBytes());
        os.flush();
        os.close();

        // Get response
        int responseCode = connection.getResponseCode();
        System.out.println("PUT Response Code :  " + responseCode);
        System.out.println("PUT Response Message : " + connection.getResponseMessage());
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Success
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            } in.close();

            JSONArray jArr = null;

            if (response.toString().equals("\"None found\"")) {
                // Error
                return null;
            } else {
                // Success
                try {
                    // Store activities
                    jArr = new JSONArray(response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jArr;
            }
        } else {
            // Error message
            System.out.println("GET ACTIVITIES NOT WORKED");
            return null;
        }
    }
}