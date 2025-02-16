package com.example.theeventhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

public class updateevent extends AppCompatActivity {

    private int userId;
    private int eventID;
    private Uri imageUri;
    private ImageView uploadPictureIcon;
    private EditText eventTitleInput;
    private EditText eventDateInput;
    private EditText eventStartTimeInput;
    private EditText eventEndTimeInput;
    private EditText eventDescriptionInput;
    private Spinner eventTypeSpinner;
    private Spinner eventLocationInput;
    private Button updateEventButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateevent);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);

        uploadPictureIcon = findViewById(R.id.upload_picture_icon);
        eventTitleInput = findViewById(R.id.event_title_input);
        eventDateInput = findViewById(R.id.event_date_input);
        eventStartTimeInput = findViewById(R.id.event_start_time_input);
        eventEndTimeInput = findViewById(R.id.event_end_time_input);
        eventDescriptionInput = findViewById(R.id.event_description_input);
        eventTypeSpinner = findViewById(R.id.event_type_spinner);
        eventLocationInput = findViewById(R.id.event_location_input);
        updateEventButton = findViewById(R.id.update_event_button);
        backButton = findViewById(R.id.backButton);

        // Initialize the spinner with event types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(adapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(updateevent.this, manageevent.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("EventID", eventID);
                startActivity(intent);
                finish();
            }
        });

        eventDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        eventStartTimeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(eventStartTimeInput);
            }
        });

        eventEndTimeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(eventEndTimeInput);
            }
        });

        new FetchEventDetailsTask().execute(eventID);

        updateEventButton.setOnClickListener(v -> new UpdateEventDetailsTask().execute());
        setupSpinners();
    }

    private void setupSpinners() {
        // Event Type Spinner
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.event_types)) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item
                return position != 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                // Set the text color to white
                tv.setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                // Set the disabled item text color to grey and others to white
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(eventTypeAdapter);

        // Event Location Spinner
        ArrayAdapter<String> eventLocationAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.location_array)) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item
                return position != 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                // Set the text color to white
                tv.setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                // Set the disabled item text color to grey and others to white
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        eventLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventLocationInput.setAdapter(eventLocationAdapter);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();

        // Check if eventDateInput already has a date set
        String currentDateText = eventDateInput.getText().toString();
        if (!currentDateText.isEmpty()) {
            String[] parts = currentDateText.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Month is 0-based in Calendar
            int day = Integer.parseInt(parts[2]);

            calendar.set(year, month, day);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        eventDateInput.setText(selectedDate);
                    }
                },
                year, month, day);

        // Set the minimum date to the current date
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());

        datePickerDialog.show();
    }

    private void showTimePickerDialog(final EditText timeInput) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        timeInput.setText(selectedTime);
                    }
                }, hour, minute, true);
        timePickerDialog.show();
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
                eventTitleInput.setText(eventDetails.getEventName());
                eventDateInput.setText(eventDetails.getEventDate());
                eventStartTimeInput.setText(eventDetails.getEventTime());
                eventEndTimeInput.setText(eventDetails.getEventEnd());
                eventDescriptionInput.setText(eventDetails.getEventDescription());

                ArrayAdapter<CharSequence> typeAdapter = (ArrayAdapter<CharSequence>) eventTypeSpinner.getAdapter();
                int typeSpinnerPosition = typeAdapter.getPosition(eventDetails.getEventType());
                eventTypeSpinner.setSelection(typeSpinnerPosition);

                ArrayAdapter<CharSequence> locationAdapter = (ArrayAdapter<CharSequence>) eventLocationInput.getAdapter();
                int locationSpinnerPosition = locationAdapter.getPosition(eventDetails.getEventVenue());
                eventLocationInput.setSelection(locationSpinnerPosition);

                // Decode and display the image
                if (eventDetails.getImage() != null && !eventDetails.getImage().isEmpty()) {
                    byte[] decodedString = Base64.decode(eventDetails.getImage(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    uploadPictureIcon.setImageBitmap(decodedByte);
                } else {
                    uploadPictureIcon.setImageResource(R.drawable.no_photo_available); // Set a default image if no image is available
                }
            }
        }
    }

    private class UpdateEventDetailsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String serverIp = getString(R.string.server_ip);
                String urlString = serverIp + "/theeventhub/update_event.php";
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject postData = new JSONObject();
                postData.put("EventID", eventID);
                postData.put("EventTitle", eventTitleInput.getText().toString());
                postData.put("EventDate", eventDateInput.getText().toString());
                postData.put("EventTime", eventStartTimeInput.getText().toString());
                postData.put("EventEnd", eventEndTimeInput.getText().toString());
                postData.put("EventDesc", eventDescriptionInput.getText().toString());
                postData.put("EventType", eventTypeSpinner.getSelectedItem().toString());
                postData.put("EVenue", eventLocationInput.getSelectedItem().toString());

                // Encode image if a new one is selected
                if (imageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
                        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        postData.put("image", encodedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8")));
                writer.print(postData.toString());
                writer.flush();
                writer.close();

                int responseCode = urlConnection.getResponseCode();
                StringBuilder response = new StringBuilder();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                }

                Log.d("UpdateEvent", "Server response: " + response.toString());
                return response.toString();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.e("UpdateEvent", "Exception: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");
                    if (status.equals("success")) {
                        Toast.makeText(updateevent.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(updateevent.this, manageevent.class);
                        intent.putExtra("EventID", eventID);
                        intent.putExtra("user_id", userId);
                        startActivity(intent);
                        finish(); // Finish the current activity
                    } else {
                        String message = jsonResponse.optString("message", "Failed to update event");
                        Toast.makeText(updateevent.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(updateevent.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(updateevent.this, "Failed to update event", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
