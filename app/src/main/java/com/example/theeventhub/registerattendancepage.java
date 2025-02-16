package com.example.theeventhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class registerattendancepage extends AppCompatActivity {

    private int userId;
    private int eventID;

    private TextView time;
    private TextView date;
    private TextView name;
    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventLocation;
    private Button submitbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerattendancepage);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);

        time = findViewById(R.id.time);
        date = findViewById(R.id.date);
        name = findViewById(R.id.name);
        eventTitle = findViewById(R.id.event_title);
        eventDate = findViewById(R.id.event_date);
        eventTime = findViewById(R.id.event_time);
        eventLocation = findViewById(R.id.event_location);
        submitbutton = findViewById(R.id.submitbutton);

        setCurrentTimeAndDate();
        new FetchUserNameTask().execute(userId);
        new FetchTicketDetailsTask().execute(eventID);

        submitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateAttendanceTask().execute(eventID, userId);
            }
        });
    }

    private class UpdateAttendanceTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            String response = null;
            try {
                int eventId = params[0];
                int userId = params[1];
                String serverIp = getString(R.string.server_ip);
                String urlString = serverIp + "/theeventhub/mark_attendance.php";
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("eventID", eventId);
                postDataParams.put("userID", userId);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));
                writer.flush();
                writer.close();
                os.close();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                inputStream.close();

                response = sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                String status = jsonResponse.getString("status");
                String message = jsonResponse.getString("message");

                Toast.makeText(registerattendancepage.this, message, Toast.LENGTH_SHORT).show();

                if (status.equals("success")) {
                    Intent intent = new Intent(registerattendancepage.this, Myeventpage.class);
                    intent.putExtra("user_id", userId);
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(registerattendancepage.this, "Error updating attendance", Toast.LENGTH_SHORT).show();
            }
        }

        private String getPostDataString(JSONObject params) throws Exception {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Iterator<String> itr = params.keys(); itr.hasNext(); ) {
                String key = itr.next();
                Object value = params.get(key);
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));
            }
            return result.toString();
        }
    }


    private void setCurrentTimeAndDate() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());
        time.setText(currentTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        date.setText(currentDate);
    }

    private class FetchUserNameTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... userIds) {
            String userName = null;
            try {
                String serverIp = getString(R.string.server_ip);
                String urlString = serverIp + "/theeventhub/fetchusername.php?user_id=" + userIds[0];
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                inputStream.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                userName = jsonResponse.getString("user_fullname");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return userName;
        }

        @Override
        protected void onPostExecute(String userName) {
            if (userName != null) {
                name.setText("Hi " + userName);
            } else {
                Toast.makeText(registerattendancepage.this, "Error fetching user name", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchTicketDetailsTask extends AsyncTask<Integer, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Integer... eventID) {
            JSONObject eventDetails = null;
            try {
                String serverIp = getString(R.string.server_ip);
                String urlString = serverIp + "/theeventhub/eventdetail.php?EventID=" + eventID[0];
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                inputStream.close();

                eventDetails = new JSONObject(response.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return eventDetails;
        }

        @Override
        protected void onPostExecute(JSONObject eventDetails) {
            if (eventDetails != null) {
                try {
                    eventTitle.setText(eventDetails.getString("EventTitle"));
                    eventDate.setText(eventDetails.getString("EventDate"));
                    eventTime.setText(eventDetails.getString("EventTime"));
                    eventLocation.setText(eventDetails.getString("EVenue"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(registerattendancepage.this, "Error parsing event details", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(registerattendancepage.this, "Error fetching event details", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
