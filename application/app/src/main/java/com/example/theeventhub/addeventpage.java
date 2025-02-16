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
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class addeventpage extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private BottomNavigationView bottomNavigationView;
    private int userId;

    private int eventID;

    private ImageView uploadPictureIcon;
    private Uri imageUri;

    private EditText eventTitleInput;
    private EditText eventDateInput;
    private EditText eventStartTimeInput;
    private EditText eventEndTimeInput;
    private EditText eventDescriptionInput;
    private Spinner eventTypeSpinner;
    private Spinner eventLocationInput;
    private Button createEventButton;

    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addeventpage);

        userId = getIntent().getIntExtra("user_id", 0);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        Intent dashboardIntent = new Intent(addeventpage.this, dashboardpage.class);
                        dashboardIntent.putExtra("user_id", userId);
                        startActivity(dashboardIntent);
                        return true;
                    case R.id.event:
                        Intent eventIntent = new Intent(addeventpage.this, Myeventpage.class);
                        eventIntent.putExtra("user_id", userId);
                        startActivity(eventIntent);
                        return true;
                    case R.id.profile:
                        Intent profileIntent = new Intent(addeventpage.this, profilepage.class);
                        profileIntent.putExtra("user_id", userId);
                        startActivity(profileIntent);
                        return true;
                    case R.id.add:
                        // Stay on addeventpage
                        return true;
                    case R.id.myevent:
                        Intent myeventIntent = new Intent(addeventpage.this, eventpage.class);
                        myeventIntent.putExtra("user_id", userId);
                        startActivity(myeventIntent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.add);

        // Initialize views
        uploadPictureIcon = findViewById(R.id.upload_picture_icon);
        eventTitleInput = findViewById(R.id.event_title_input);
        eventDateInput = findViewById(R.id.event_date_input);
        eventStartTimeInput = findViewById(R.id.event_start_time_input);
        eventEndTimeInput = findViewById(R.id.event_end_time_input);
        eventDescriptionInput = findViewById(R.id.event_description_input);
        eventTypeSpinner = findViewById(R.id.event_type_spinner);
        eventLocationInput = findViewById(R.id.event_location_input);
        createEventButton = findViewById(R.id.create_event_button);

        // Set click listeners
        uploadPictureIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestStoragePermission();
            }
        });

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send data to the database
                sendEventDataToDatabase();
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        eventDateInput.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Set the minimum date to today
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

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openImageChooser();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "image/gif"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadPictureIcon.setImageURI(imageUri);

            // Encode image to Base64
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = byteArrayOutputStream.toByteArray();
                encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendEventDataToDatabase() {
        String eventTitle = eventTitleInput.getText().toString().trim();
        String eventDate = eventDateInput.getText().toString().trim();
        String eventStartTime = eventStartTimeInput.getText().toString().trim();
        String eventEndTime = eventEndTimeInput.getText().toString().trim();
        String eventDescription = eventDescriptionInput.getText().toString().trim();
        String eventType = eventTypeSpinner.getSelectedItem().toString();
        String eventLocation = eventLocationInput.getSelectedItem().toString().trim();

        new SendEventDataTask(eventTitle, eventDate, eventStartTime, eventEndTime, eventDescription, eventType, eventLocation, encodedImage).execute();
    }

    private class SendEventDataTask extends AsyncTask<Void, Void, String> {
        private String eventTitle;
        private String eventDate;
        private String eventStartTime;
        private String eventEndTime;
        private String eventDescription;
        private String eventType;
        private String eventLocation;
        private String encodedImage;

        public SendEventDataTask(String eventTitle, String eventDate, String eventStartTime, String eventEndTime,
                                 String eventDescription, String eventType, String eventLocation, String encodedImage) {
            this.eventTitle = eventTitle;
            this.eventDate = eventDate;
            this.eventStartTime = eventStartTime;
            this.eventEndTime = eventEndTime;
            this.eventDescription = eventDescription;
            this.eventType = eventType;
            this.eventLocation = eventLocation;
            this.encodedImage = encodedImage;
        }

        @Override
        protected void onPreExecute() {
            // Disable the button to prevent multiple requests
            createEventButton.setEnabled(false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String serverIp = getString(R.string.server_ip);
            String url = serverIp + "/theeventhub/addevent.php";

            Map<String, String> params = new HashMap<>();
            params.put("userID", String.valueOf(userId));
            params.put("EventTitle", eventTitle);
            params.put("EventTime", eventStartTime);
            params.put("EventEnd", eventEndTime);
            params.put("EventDate", eventDate);
            params.put("EventDesc", eventDescription);
            params.put("EventType", eventType);
            params.put("Evenue", eventLocation);

            if (encodedImage != null) {
                params.put("image", encodedImage);
            }

            String response = null;
            try {
                response = makePostRequest(url, params);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            createEventButton.setEnabled(true); // Re-enable the button

            if (response != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        int eventID = jsonResponse.getInt("EventID");

                        Intent successIntent = new Intent(addeventpage.this, successevent.class);
                        successIntent.putExtra("user_id", userId);
                        successIntent.putExtra("EventID", eventID);
                        startActivity(successIntent);

                        Toast.makeText(addeventpage.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = jsonResponse.getString("message");
                        Toast.makeText(addeventpage.this, "Error creating event: " + message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(addeventpage.this, "JSON Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(addeventpage.this, "Error creating event: No response from server", Toast.LENGTH_SHORT).show();
            }
        }

        private String makePostRequest(String url, Map<String, String> params) throws IOException {
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(java.net.URLEncoder.encode(param.getValue(), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            java.net.URL requestUrl = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) requestUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            InputStream responseStream = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            return responseBuilder.toString();
        }
    }
}
