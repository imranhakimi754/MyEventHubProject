package com.example.theeventhub;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class manageevent extends AppCompatActivity {

    private int userId;
    private int eventID;

    private TextView textViewEventTitle;
    private TextView textViewOrganizerName;
    private TextView textViewEventDate;
    private TextView textViewEventTime;
    private TextView textViewTimeEnd;
    private TextView textViewEventDescription;
    private TextView textViewEventType;
    private TextView textViewEventVenue;
    private ImageView eventImage; // Initialize eventImage
    private Button updateButton;
    private Button deleteButton;
    private ImageButton backButton;

    private TextView qrbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manageevent);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);

        textViewEventTitle = findViewById(R.id.CreatedEventTitle);
        textViewOrganizerName = findViewById(R.id.CreatedOrganizerName);
        textViewEventDate = findViewById(R.id.CreatedEventDate);
        textViewEventTime = findViewById(R.id.CreatedEventTime);
        textViewTimeEnd = findViewById(R.id.CreatedTimeEnd);
        textViewEventDescription = findViewById(R.id.CreatedEventDescription);
        textViewEventType = findViewById(R.id.CreatedEventType);
        textViewEventVenue = findViewById(R.id.CreatedEventVenue);
        eventImage = findViewById(R.id.eventImage); // Add this line
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        backButton = findViewById(R.id.backButton);
        qrbutton = findViewById(R.id.qrcode);

        new FetchEventDetailsTask().execute(eventID);

        TextView attendanceListTextView = findViewById(R.id.attendancelist);
        attendanceListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(manageevent.this, listofattendee.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("EventID", eventID);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(manageevent.this, eventpage.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("EventID", eventID);
                startActivity(intent);
                finish();
            }
        });

        qrbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(manageevent.this, Qrcodepage.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("EventID", eventID);
                startActivity(intent);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(manageevent.this, updateevent.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("EventID", eventID);
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to delete this event?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteEventTask().execute(eventID, userId);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class FetchEventDetailsTask extends AsyncTask<Integer, Void, EventDetails> {
        @Override
        protected EventDetails doInBackground(Integer... eventIDs) {
            EventDetails eventDetails = null;
            try {
                String serverIp = getString(R.string.server_ip);
                String urlString = serverIp + "/theeventhub/eventdetail.php?EventID=" + eventIDs[0];
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
                textViewOrganizerName.setText("Organized By: " + eventDetails.getOrganizerName());
                textViewEventDate.setText(eventDetails.getEventDate());
                textViewEventTime.setText(eventDetails.getEventTime());
                textViewTimeEnd.setText("- " + eventDetails.getEventEnd());
                textViewEventDescription.setText("Event Description: " + eventDetails.getEventDescription());
                textViewEventType.setText("Event Type: " + eventDetails.getEventType());
                textViewEventVenue.setText(eventDetails.getEventVenue());

                // Set the event image
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

    private class DeleteEventTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            int eventID = params[0];
            int userId = params[1];
            boolean success = false;

            try {
                String serverIp = getString(R.string.server_ip);
                String urlString = serverIp + "/theeventhub/deleteevent.php?EventID=" + eventID + "&UserID=" + userId;
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.has("success")) {
                        success = true;
                    } else {
                        Log.e("DeleteEventTask", "Error: " + jsonResponse.getString("error"));
                    }
                } else {
                    Log.e("DeleteEventTask", "HTTP error code: " + responseCode);
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.e("DeleteEventTask", "Exception: " + e.getMessage());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(manageevent.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(manageevent.this, eventpage.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(manageevent.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
