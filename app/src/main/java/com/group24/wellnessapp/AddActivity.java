package com.group24.wellnessapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddActivity extends AppCompatActivity {
    private Context context;

    // AsyncTask for JSON requests
    class addAsyncTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground (final String...userID) {
            try  {
                // Activity type drop down menu
                Spinner categoryDropDown = findViewById(R.id.categorySelectionSpinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                        R.array.activityCategoryOptions, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categoryDropDown.setAdapter(adapter);

                // Log new activity
                Button addActivityBtn = findViewById(R.id.addActivityBtn);
                addActivityBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Grab different activity data entries
                        EditText activityLabelText = findViewById(R.id.activityLabelText);
                        EditText activityTimeText = findViewById(R.id.activityTimeText);
                        Spinner activityCategorySpinner = findViewById(R.id.categorySelectionSpinner);
                        TextView durationErrorTextView = findViewById(R.id.durationErrorTextView);
                        Intent sendActivityData = new Intent(getApplicationContext(), MainActivity.class);

                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);

                        // Send data to server and go to MainActivity if input valid
                        if (Integer.parseInt(activityTimeText.getText().toString()) > 24 || Integer.parseInt(activityTimeText.getText().toString()) < 1) {
                            // Display error message for invalid duration
                            durationErrorTextView.setVisibility(View.VISIBLE);
                        } else {
                            // Hide error message for invalid duration
                            durationErrorTextView.setVisibility(View.GONE);
                            try {
                                // Send data
                                sendActivity(LoginActivity.getUserID(), activityCategorySpinner.getSelectedItem().toString().toLowerCase(), activityLabelText.getText().toString().toLowerCase(), Integer.parseInt(activityTimeText.getText().toString()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Clear activity stack for this activity
                            sendActivityData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            // Go to Main Activity
                            startActivity(sendActivityData);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        context = this;

        // Remove back button from toolbar
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        // Start AsyncTask
        new addAsyncTask().execute(LoginActivity.getUserID());
    }

    // Add activity to database based on user and activity parameters
    public static void sendActivity(String userID, String type, String label, int duration) throws IOException {
        // Connect to server
        URL obj = new URL("http://3.92.227.189:80/api/users/" + userID);
        HttpURLConnection putConnection = (HttpURLConnection) obj.openConnection();
        putConnection.setRequestMethod("PUT");
        putConnection.setRequestProperty("Content-Type", "application/json");

        // Get current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(calendar.getTime());

        // Create activity JSON object
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("date", currentDate);
            jObj.put("type", type);
            jObj.put("label", label);
            jObj.put("duration", duration);
            jObj.put("token", LoginActivity.getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send activity
        putConnection.setDoOutput(true);
        OutputStream os = putConnection.getOutputStream();
        os.write(jObj.toString().getBytes());
        os.flush();
        os.close();

        // Get and print response
        int responseCode = putConnection.getResponseCode();
        System.out.println("ADD Response Code :  " + responseCode);
        System.out.println("ADD Response Message : " + putConnection.getResponseMessage());
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            // Success
            BufferedReader in = new BufferedReader(new InputStreamReader(putConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();
        } else {
            // Error message
            System.out.println("ADD ACTIVITY NOT WORKED");
        }
    }
}