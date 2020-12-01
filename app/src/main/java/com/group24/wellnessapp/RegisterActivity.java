package com.group24.wellnessapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    // AsyncTask for JSON requests
    class registerAsyncTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground (String...userID) {
            final EditText usernameEditText = findViewById(R.id.usernameEditText);
            final EditText passwordEditText = findViewById(R.id.passwordEditText);

            // Log in button to go to LogActivity screen
            Button registerBtn = findViewById(R.id.registerBtn);
            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView registerErrorTextView = findViewById(R.id.registerErrorTextView);
                    try {
                        // If valid username and password
                        if (registerUser(usernameEditText.getText().toString(), passwordEditText.getText().toString()) == true) {
                            // Success
                            // Send user credentials to Login screen
                            registerErrorTextView.setVisibility(View.GONE);
                            Intent startIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            startIntent.putExtra("username", usernameEditText.getText().toString());
                            startIntent.putExtra("password", passwordEditText.getText().toString());
                            startActivity(startIntent);
                        } else {
                            // Error
                            registerErrorTextView.setVisibility(View.VISIBLE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
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
        setContentView(R.layout.activity_register);

        // Start AsyncTask
        new registerAsyncTask().execute("");
    }

    // Register user with inputted username and password
    public static boolean registerUser(String username, String password) throws IOException {
        // Connect to server
        URL obj = new URL("http://3.92.227.189:80/api/users/register");
        HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
        postConnection.setRequestMethod("POST");
        postConnection.setRequestProperty("Content-Type", "application/json");

        // Create JSON object with user credentials
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("username", username);
            jObj.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Send new user credentials
        postConnection.setDoOutput(true);
        OutputStream os = postConnection.getOutputStream();
        os.write(jObj.toString().getBytes());
        os.flush();
        os.close();

        // Get response
        int responseCode = postConnection.getResponseCode();
        System.out.println("POST Response Code :  " + responseCode);
        System.out.println("POST Response Message : " + postConnection.getResponseMessage());
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            // Success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    postConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();

            return true;
        } else {
            // Error message
            System.out.println("REGISTER NOT WORKED");
            return false;
        }
    }
}