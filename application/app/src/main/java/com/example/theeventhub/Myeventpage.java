package com.example.theeventhub;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Myeventpage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private int userId;

    private RecyclerView upcomingEventsRecyclerView;
    private RecyclerView ongoingEventsRecyclerView;
    private RecyclerView pastEventsRecyclerView;

    private EventListAdapter upcomingEventsAdapter;
    private EventListAdapter ongoingEventsAdapter;
    private EventListAdapter pastEventsAdapter;
    private EventListAdapter eventListAdapter;
    private int currentEventId;

    private Eventlist currentEvent; // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myeventpage);


        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        userId = getIntent().getIntExtra("user_id", 0);

        // Set up bottom navigation view
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        Intent dashboardIntent = new Intent(Myeventpage.this, dashboardpage.class);
                        dashboardIntent.putExtra("user_id", userId);
                        startActivity(dashboardIntent);
                        return true;
                    case R.id.event:
                        // Stay on Myeventpage
                        return true;
                    case R.id.profile:
                        Intent profileIntent = new Intent(Myeventpage.this, profilepage.class);
                        profileIntent.putExtra("user_id", userId);
                        startActivity(profileIntent);
                        return true;
                    case R.id.add:
                        Intent addintent = new Intent(Myeventpage.this, addeventpage.class);
                        addintent.putExtra("user_id", userId);
                        startActivity(addintent);
                        return true;
                    case R.id.myevent:
                        Intent myeventintent = new Intent(Myeventpage.this, eventpage.class);
                        myeventintent.putExtra("user_id", userId);
                        startActivity(myeventintent);
                        return true;
                    default:
                        return false;
                }
            }
        });



        bottomNavigationView.setSelectedItemId(R.id.event);

        upcomingEventsRecyclerView = findViewById(R.id.recyclerViewUpcoming);
        ongoingEventsRecyclerView = findViewById(R.id.recyclerViewOngoing);
        pastEventsRecyclerView = findViewById(R.id.recyclerViewPast);

        upcomingEventsAdapter = new EventListAdapter(this, new ArrayList<>(), userId, false);
        ongoingEventsAdapter = new EventListAdapter(this, new ArrayList<>(), userId, false);
        pastEventsAdapter = new EventListAdapter(this, new ArrayList<>(), userId, false);

        upcomingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ongoingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pastEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        upcomingEventsRecyclerView.setAdapter(upcomingEventsAdapter);
        ongoingEventsRecyclerView.setAdapter(ongoingEventsAdapter);
        pastEventsRecyclerView.setAdapter(pastEventsAdapter);

        // Add onItemClickListener to set the currentEvent
        upcomingEventsAdapter.setOnItemClickListener(event -> currentEvent = event);
        ongoingEventsAdapter.setOnItemClickListener(event -> currentEvent = event);
        pastEventsAdapter.setOnItemClickListener(event -> currentEvent = event);

        new FetchRegisteredEventsTask(this, userId).execute();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // QR code scanned successfully
                try {
                    int scannedEventId = Integer.parseInt(result.getContents());
                    // Pass the current userId and scannedEventId to the registerattendance activity
                    Intent intent = new Intent(this, registerattendancepage.class);
                    intent.putExtra("EventID", scannedEventId);
                    intent.putExtra("user_id", userId);
                    startActivity(intent);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid QR Code format", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "QR Code scan cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void refreshEventList(int userId) {
        new FetchRegisteredEventsTask(this, userId).execute();
    }

    private static class FetchRegisteredEventsTask extends AsyncTask<Void, Void, JSONArray> {
        private WeakReference<Myeventpage> activityReference;
        private int userId;

        FetchRegisteredEventsTask(Myeventpage context, int userId) {
            activityReference = new WeakReference<>(context);
            this.userId = userId;
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            try {
                Myeventpage activity = activityReference.get();
                if (activity == null || activity.isFinishing()) return null;

                String serverIp = activity.getString(R.string.server_ip);
                URL url = new URL(serverIp + "/theeventhub/myevent.php?user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return new JSONArray(response.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray eventsArray) {
            Myeventpage activity = activityReference.get();
            if (activity == null || activity.isFinishing() || eventsArray == null) return;

            try {
                ArrayList<Eventlist> upcomingEvents = new ArrayList<>();
                ArrayList<Eventlist> ongoingEvents = new ArrayList<>();
                ArrayList<Eventlist> pastEvents = new ArrayList<>();

                for (int i = 0; i < eventsArray.length(); i++) {
                    JSONObject jsonObject = eventsArray.getJSONObject(i);
                    int eventID = jsonObject.getInt("EventID");
                    String eventName = jsonObject.getString("EventTitle");
                    String eventDate = jsonObject.getString("EventDate");
                    String eventTime = jsonObject.getString("EventTime");
                    String eventType = jsonObject.getString("EventType");
                    String eventLocation = jsonObject.getString("EVenue");
                    String image = jsonObject.getString("image");
                    String eventStatus = jsonObject.getString("eventStatus");

                    Eventlist event = new Eventlist(eventID, eventName, eventDate, eventTime, eventType, eventLocation, image, eventStatus);

                    if (eventStatus.equals("upcoming")) {
                        upcomingEvents.add(event);
                    } else if (eventStatus.equals("ongoing")) {
                        ongoingEvents.add(event);
                    } else if (eventStatus.equals("past")) {
                        pastEvents.add(event);
                    }
                }


                activity.runOnUiThread(() -> {
                    activity.upcomingEventsAdapter.updateEvents(upcomingEvents);
                    activity.ongoingEventsAdapter.updateEvents(ongoingEvents);
                    activity.pastEventsAdapter.updateEvents(pastEvents);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
