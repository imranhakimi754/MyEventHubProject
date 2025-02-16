package com.example.theeventhub;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class eventpage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private int userId;

    private RecyclerView upcomingEventsRecyclerView;
    private RecyclerView ongoingEventsRecyclerView;
    private RecyclerView pastEventsRecyclerView;

    private EventListAdapter upcomingEventsAdapter;
    private EventListAdapter ongoingEventsAdapter;
    private EventListAdapter pastEventsAdapter;

    private Eventlist currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventpage);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        userId = getIntent().getIntExtra("user_id", 0);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    navigateToActivity(dashboardpage.class);
                    return true;
                case R.id.event:
                    navigateToActivity(Myeventpage.class);
                    return true;
                case R.id.profile:
                    navigateToActivity(profilepage.class);
                    return true;
                case R.id.add:
                    navigateToActivity(addeventpage.class);
                    return true;
                case R.id.myevent:
                    return true;
                default:
                    return false;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.myevent);

        upcomingEventsRecyclerView = findViewById(R.id.recyclerViewUpcomingMyevent);
        ongoingEventsRecyclerView = findViewById(R.id.recyclerViewOngoingMyevent);
        pastEventsRecyclerView = findViewById(R.id.recyclerViewPastMyevent);

        upcomingEventsAdapter = new EventListAdapter(this, new ArrayList<>(), userId, false);
        ongoingEventsAdapter = new EventListAdapter(this, new ArrayList<>(), userId, false);
        pastEventsAdapter = new EventListAdapter(this, new ArrayList<>(), userId, false);

        upcomingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ongoingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pastEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        upcomingEventsRecyclerView.setAdapter(upcomingEventsAdapter);
        ongoingEventsRecyclerView.setAdapter(ongoingEventsAdapter);
        pastEventsRecyclerView.setAdapter(pastEventsAdapter);

        new FetchCreatedEventsTask(this, userId).execute();
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(eventpage.this, targetActivity);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }


    public void refreshEventList(int userId) {
        new FetchCreatedEventsTask(this, userId).execute();
    }

    private static class FetchCreatedEventsTask extends AsyncTask<Void, Void, JSONArray> {
        private WeakReference<eventpage> activityReference;
        private int userId;

        FetchCreatedEventsTask(eventpage context, int userId) {
            activityReference = new WeakReference<>(context);
            this.userId = userId;
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            try {
                eventpage activity = activityReference.get();
                if (activity == null || activity.isFinishing()) return null;

                String serverIp = activity.getString(R.string.server_ip);
                URL url = new URL(serverIp + "/theeventhub/createdevent.php?user_id=" + userId);
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
            eventpage activity = activityReference.get();
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

                    switch (eventStatus) {
                        case "upcoming":
                            upcomingEvents.add(event);
                            break;
                        case "ongoing":
                            ongoingEvents.add(event);
                            break;
                        case "past":
                            pastEvents.add(event);
                            break;
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
