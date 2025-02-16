package com.example.theeventhub;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class dashboardpage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private ArrayList<Eventlist> eventArrayList;
    private EventListAdapter eventListAdapter;
    private TextView textView3, textView8;
    private int userId;
    private Button btnAll, btnEducation, btnSports, btnSocial, btnAdditional;
    private Button selectedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboardpage);

        userId = getIntent().getIntExtra("user_id", 0);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        textView3 = findViewById(R.id.textView3);
        textView8 = findViewById(R.id.textView8);
        recyclerView = findViewById(R.id.recyclerViewEvents);
        btnAll = findViewById(R.id.btn_all);
        btnEducation = findViewById(R.id.btn_education);
        btnSports = findViewById(R.id.btn_sports);
        btnSocial = findViewById(R.id.btn_social);
        btnAdditional = findViewById(R.id.btn_additional);

        eventArrayList = new ArrayList<>();
        eventListAdapter = new EventListAdapter(this, eventArrayList, userId, true);
        recyclerView.setAdapter(eventListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Execute AsyncTask to fetch all events initially
        fetchEvents("all");

        // Fetch user's full name and position
        getUserDetails(userId);

        // Set up bottom navigation view
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        // Stay on DashboardPage
                        return true;
                    case R.id.event:
                        // Go to My Event page
                        Intent eventIntent = new Intent(dashboardpage.this, Myeventpage.class);
                        eventIntent.putExtra("user_id", userId);
                        startActivity(eventIntent);
                        return true;
                    case R.id.profile:
                        // Go to Profile page
                        Intent profileIntent = new Intent(dashboardpage.this, profilepage.class);
                        profileIntent.putExtra("user_id", userId);
                        startActivity(profileIntent);
                        return true;
                    case R.id.add:
                        Intent addintent = new Intent(dashboardpage.this, addeventpage.class);
                        addintent.putExtra("user_id", userId);
                        startActivity(addintent);
                        return true;
                    case R.id.myevent:
                        Intent myeventintent = new Intent(dashboardpage.this, eventpage.class);
                        myeventintent.putExtra("user_id", userId);
                        startActivity(myeventintent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Set initial selected item
        bottomNavigationView.setSelectedItemId(R.id.home);

        // Set button click listeners
        setButtonListeners();
    }

    private void setButtonListeners() {
        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEvents("all");
                setButtonColor(btnAll);
            }
        });

        btnEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEvents("Academic");
                setButtonColor(btnEducation);
            }
        });

        btnSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEvents("Sport");
                setButtonColor(btnSports);
            }
        });

        btnSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEvents("Social");
                setButtonColor(btnSocial);
            }
        });

        btnAdditional.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEvents("Others");
                setButtonColor(btnAdditional);
            }
        });
    }

    private void fetchEvents(String eventType) {
        FetchEventsTask fetchEventsTask = new FetchEventsTask();
        fetchEventsTask.execute(eventType);
    }

    private void setButtonColor(Button selectedButton) {
        // If there's a previously selected button, set it back to its original color
        if (this.selectedButton != null) {
            this.selectedButton.setBackgroundColor(Color.parseColor("#3b505e")); // Original color
        }

        // Set the newly selected button to yellow
        selectedButton.setBackgroundColor(Color.parseColor("#FFAE42")); // Yellow color

        // Update the reference to the currently selected button
        this.selectedButton = selectedButton;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events onResume
        fetchEvents("all"); // Fetch all events
    }

    private class FetchEventsTask extends AsyncTask<String, Void, ArrayList<Eventlist>> {

        @Override
        protected ArrayList<Eventlist> doInBackground(String... params) {
            ArrayList<Eventlist> eventList = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            try {
                String serverIp = getString(R.string.server_ip);
                String eventType = params[0]; // Get eventType parameter
                String urlString = serverIp + "/theeventhub/event.php";
                if (!eventType.equals("all")) {
                    urlString += "?eventType=" + eventType;
                }
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

                JSONArray jsonArray = new JSONArray(response.toString());
                Date currentDate = new Date();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int eventID = jsonObject.getInt("EventID");
                    String eventName = jsonObject.getString("EventTitle");
                    String eventDate = jsonObject.getString("EventDate");
                    String eventTime = jsonObject.getString("EventTime");
                    String eventLocation = jsonObject.getString("Evenue");
                    String image = jsonObject.getString("image");

                    Date eventDateTime = dateFormat.parse(eventDate + " " + eventTime);
                    if (eventDateTime != null && eventDateTime.after(currentDate)) {
                        Eventlist event = new Eventlist(eventID, eventName, eventDate, eventTime, eventType, eventLocation, image);
                        eventList.add(event);
                    }
                }

            } catch (IOException | JSONException | ParseException e) {
                e.printStackTrace();
            }

            // Sort events by nearest date and time
            Collections.sort(eventList, new Comparator<Eventlist>() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                @Override
                public int compare(Eventlist event1, Eventlist event2) {
                    try {
                        Date date1 = dateFormat.parse(event1.getEventDate() + " " + event1.getEventTime());
                        Date date2 = dateFormat.parse(event2.getEventDate() + " " + event2.getEventTime());
                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            });

            return eventList;
        }

        @Override
        protected void onPostExecute(ArrayList<Eventlist> eventList) {
            eventArrayList.clear();
            eventArrayList.addAll(eventList);
            eventListAdapter.notifyDataSetChanged();
        }
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
                            textView3.setText(userFullName);
                        }
                        if (jsonObject.has("studentid")) {
                            String position = jsonObject.getString("studentid");
                            textView8.setText(position);
                        }
                        if (jsonObject.has("image")) {
                            String base64Image = jsonObject.getString("image");
                            // Decode Base64-encoded string to byte array
                            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                            // Convert byte array to Bitmap
                            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            // Set the Bitmap to the ImageView
                            ImageView profileImageView = findViewById(R.id.profile_picture);
                            profileImageView.setImageBitmap(decodedImage);
                        }
                    } else {
                        Toast.makeText(dashboardpage.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(dashboardpage.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

}
