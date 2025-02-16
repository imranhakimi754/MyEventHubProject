package com.example.theeventhub;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ticketpage extends AppCompatActivity {

    private int eventID;
    private int userId;
    private TextView fullname;
    private TextView id;
    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticketpage);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);

        fullname = findViewById(R.id.full_name);
        id = findViewById(R.id.student_id);
        eventTitle = findViewById(R.id.event_title);
        eventDate = findViewById(R.id.event_date);
        eventTime = findViewById(R.id.event_time);
        eventLocation = findViewById(R.id.event_location);

        new FetchTicketDetailsTask().execute(eventID,userId);
        getUserDetails(userId);

        Button backToHomeButton = findViewById(R.id.back_to_home_button);
        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ticketpage.this, dashboardpage.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();  // Close the current activity to prevent going back to it
            }
        });
    }
    private void getUserDetails(final int userId) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String serverIp = getString(R.string.server_ip);
                    String urlString = serverIp + "/theeventhub/user_data.php?user_id=" + userId;
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

                    return response.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        if (jsonObject.has("user_fullname")) {
                            String userFullName = jsonObject.getString("user_fullname");
                            fullname.setText(userFullName);
                        }
                        if (jsonObject.has("studentid")) {
                            String position = jsonObject.getString("studentid");
                            id.setText(position);
                        }

                    } else {
                        Toast.makeText(ticketpage.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ticketpage.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
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
                    Toast.makeText(ticketpage.this, "Error parsing event details", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ticketpage.this, "Error fetching event details", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
