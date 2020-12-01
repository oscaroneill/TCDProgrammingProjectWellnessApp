package com.group24.wellnessapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AnalyticsActivity extends AppCompatActivity {
    private Context context = this;
    JSONObject jObj;

    // AsyncTask for JSON requests
    class analyticsAsyncTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground (String...userID) {
            BarChart chart = null;

            // Counts for different user measures
            int prodCount = -1;
            int socialCount = -1;
            int restCount = -1;
            int sleepCount = -1;
            int fitCount = -1;

            BarDataSet set = null;
            ArrayList<BarEntry> entries = null;

            String chartType = "";
            String chartTitle = "";

            // Create each graph
            for (int i = 0; i < 3; i++) {
                // Set different variables based on current graph being displayed
                switch(i) {
                    case 0:
                        chart = (BarChart)findViewById(R.id.barGraphDaily);
                        chartType = "daily";
                        chartTitle = "Your Daily Activities";
                        chart.getAxisLeft().setAxisMaximum(8.0f);
                        break;
                    case 1:
                        chart = (BarChart)findViewById(R.id.barGraphWeekly);
                        chartType = "weekly";
                        chartTitle = "Your Weekly Activities";
                        break;
                    case 2:
                        chart = (BarChart)findViewById(R.id.barGraphMonthly);
                        chartType = "monthly";
                        chartTitle = "Your Monthly Activities";
                        break;
                }

                // Get data for current graph
                try {
                    jObj = getGraphData(LoginActivity.getUserID(), chartType);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Set data for current graph
                try {
                    prodCount = Integer.parseInt(jObj.getString("prodCount"));
                    socialCount = Integer.parseInt(jObj.getString("socialCount"));
                    restCount = Integer.parseInt(jObj.getString("restCount"));
                    sleepCount = Integer.parseInt(jObj.getString("sleepCount"));
                    fitCount = Integer.parseInt(jObj.getString("fitCount"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Add data to graph
                entries = new ArrayList<>();
                entries.add(new BarEntry(0, prodCount));
                entries.add(new BarEntry(1, socialCount));
                entries.add(new BarEntry(2, restCount));
                entries.add(new BarEntry(3, sleepCount));
                entries.add(new BarEntry(4, fitCount));
                set = new BarDataSet(entries, chartTitle);

                // Customise graph
                chart.setDrawBarShadow(false);
                chart.setDrawValueAboveBar(true);
                chart.setPinchZoom(false);
                chart.setDrawGridBackground(false);

                set.setColors(new int[] {R.color.teal1, R.color.teal2, R.color.teal3, R.color.teal4, R.color.teal5}, context);

                BarData data = new BarData(set);
                data.setBarWidth(0.9f);
                chart.setData(data);

                String[] types = new String[]{"Productivity", "Social", "Rest", "Sleep", "Fitness"};
                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new MyXAxisValueFormatter(types));

                xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
                xAxis.setGranularity(1);
                xAxis.setCenterAxisLabels(false);

                YAxis yAxis = chart.getAxisLeft();
                yAxis.setAxisMinimum(0);
                yAxis.setDrawGridLines(false);
                xAxis.setDrawGridLines(false);

                chart.getDescription().setEnabled(false);

                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            }
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
        setContentView(R.layout.activity_analytics);

        // Start AsyncTask
        new analyticsAsyncTask().execute(LoginActivity.getUserID());
    }

    // Format method for x-axis of graphs
    public class MyXAxisValueFormatter implements IAxisValueFormatter {
        private String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mValues[(int)value];
        }
    }

    // Getting data for graphs based on user and type of graph (daily, weekly, monthly)
    public static JSONObject getGraphData(String userID, String type) throws IOException {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(calendar.getTime());

        // Connect to server based of type of graph, the user, and the date
        URL urlForGetRequest = new URL("http://3.92.227.189:80/api/" + type + "Stat/" + userID + "/" + currentDate);
        String readLine = null;
        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");

        // Create object to store user token to validate user
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

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Success
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            } in.close();

            // Receive data
            JSONObject jObj = null;
            try {
                 jObj = new JSONObject(response.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jObj;
        } else {
            // Error message
            System.out.println("GRAPH NOT WORKED");
            return null;
        }
    }
}