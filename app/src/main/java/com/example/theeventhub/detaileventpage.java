package com.example.theeventhub;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class detaileventpage extends AppCompatActivity {

    private TextView textViewEventTitle;
    private TextView textViewOrganizerName;
    private TextView textViewEventDate;
    private TextView textViewEventTime;
    private TextView textViewTimeEnd;
    private TextView textViewEventDescription;
    private TextView textViewEventType;
    private TextView textViewEventVenue;
    private ImageView eventImage;
    private int eventID;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detaileventpage);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);

        Log.d("UserId", "User ID received: " + userId);

        textViewEventTitle = findViewById(R.id.textViewEventTitle);
        textViewOrganizerName = findViewById(R.id.textViewOrganizerName);
        textViewEventDate = findViewById(R.id.textViewEventDate);
        textViewEventTime = findViewById(R.id.textViewEventTime);
        textViewTimeEnd = findViewById(R.id.textViewTimeEnd);
        textViewEventDescription = findViewById(R.id.textViewEventDescription);
        textViewEventType = findViewById(R.id.textViewEventType);
        textViewEventVenue = findViewById(R.id.textViewEventVenue);
        eventImage = findViewById(R.id.eventImage);

        new FetchEventDetailsTask().execute(eventID);

        ImageButton cancelButton = findViewById(R.id.backButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detaileventpage.this, dashboardpage.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to join this event?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new RegisterForEventTask().execute(userId, eventID);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private class RegisterForEventTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int userId = params[0];
            int eventID = params[1];
            try {
                String serverIp = getString(R.string.server_ip);
                String urlString = serverIp + "/theeventhub/register_event.php";
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String data = URLEncoder.encode("userID", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(userId), "UTF-8") +
                        "&" + URLEncoder.encode("eventID", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(eventID), "UTF-8");

                OutputStream os = urlConnection.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

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
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean success = jsonObject.getBoolean("success");
                    String message = jsonObject.optString("message", "");
                    if (success) {
                        Toast.makeText(detaileventpage.this, "Successfully registered for the event!", Toast.LENGTH_SHORT).show();
                        // Start DashboardPage activity with current userId
                        Intent intent = new Intent(detaileventpage.this, ticketpage.class);
                        intent.putExtra("user_id", userId);
                        intent.putExtra("EventID", eventID);
                        startActivity(intent);
                        finish(); // Optional: Close the current activity
                    } else if ("User is already registered for this event.".equals(message)) {
                        Toast.makeText(detaileventpage.this, "You already joined this event", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(detaileventpage.this, "Failed to register for the event.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(detaileventpage.this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(detaileventpage.this, "Error connecting to server.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class FetchEventDetailsTask extends AsyncTask<Integer, Void, EventDetails> {
        @Override
        protected EventDetails doInBackground(Integer... eventID) {
            EventDetails eventDetails = null;
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

                JSONObject jsonObject = new JSONObject(response.toString());
                String eventName = jsonObject.getString("EventTitle");
                String organizerName = jsonObject.getString("Org_name");
                String eventDate = jsonObject.getString("EventDate");
                String eventTime = jsonObject.getString("EventTime");
                String eventEnd = jsonObject.getString("EventEnd");
                String eventDescription = jsonObject.getString("EventDesc");
                String eventType = jsonObject.getString("EventType");
                String eventVenue = jsonObject.getString("EVenue");
                String image = jsonObject.getString("image");

                eventDetails = new EventDetails(eventName, organizerName, eventDate, eventTime, eventEnd, eventDescription, eventType, eventVenue, image);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return eventDetails;
        }

        @Override
        protected void onPostExecute(EventDetails eventDetails) {
            if (eventDetails != null) {
                textViewEventTitle.setText(eventDetails.getEventName());
                textViewOrganizerName.setText("Organizer By : " + eventDetails.getOrganizerName());
                textViewEventDate.setText(eventDetails.getEventDate());
                textViewEventTime.setText(eventDetails.getEventTime());
                textViewTimeEnd.setText("-  " + eventDetails.getEventEnd());
                textViewEventDescription.setText("Event Description : " + eventDetails.getEventDescription());
                textViewEventType.setText("Event Type : " + eventDetails.getEventType());
                textViewEventVenue.setText(eventDetails.getEventVenue());

                // Decode the base64 image and set it to the ImageView
                if (eventDetails.getImage() != null && !eventDetails.getImage().isEmpty()) {
                    byte[] decodedString = Base64.decode(eventDetails.getImage(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    eventImage.setImageBitmap(decodedByte);
                } else {
                    eventImage.setImageResource(R.drawable.no_photo_available); // Set a default image if no image is available
                }
            }
        }
    }
}