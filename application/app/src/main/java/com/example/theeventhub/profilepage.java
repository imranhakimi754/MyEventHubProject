package com.example.theeventhub;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class profilepage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private int userId;
    private TextView studentid, name, email, faculty, phonenum;
    private Button changePasswordButton;
    private TextView logoutText;
    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_GALLERY_PERMISSION = 100;
    private Uri selectedImageUri;
    private ShapeableImageView profileImageView;
    private FloatingActionButton floatingActionButton;
    private TextView removeImageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        studentid = findViewById(R.id.studentid);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        faculty = findViewById(R.id.faculty);
        phonenum = findViewById(R.id.phonenum);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        profileImageView = findViewById(R.id.profileImageView);
        removeImageText = findViewById(R.id.removeimage);

        userId = getIntent().getIntExtra("user_id", 0);

        getUserDetails(userId);

        logoutText = findViewById(R.id.logout_text);
        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        changePasswordButton = findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changepasswordIntent = new Intent(profilepage.this, changepasswordpage.class);
                changepasswordIntent.putExtra("user_id", userId);
                startActivity(changepasswordIntent);
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        Intent dashboardintent = new Intent(profilepage.this, dashboardpage.class);
                        dashboardintent.putExtra("user_id", userId);
                        startActivity(dashboardintent);
                        return true;
                    case R.id.event:
                        Intent eventintent = new Intent(profilepage.this, Myeventpage.class);
                        eventintent.putExtra("user_id", userId);
                        startActivity(eventintent);
                        return true;
                    case R.id.profile:
                        return true;
                    case R.id.add:
                        Intent addintent = new Intent(profilepage.this, addeventpage.class);
                        addintent.putExtra("user_id", userId);
                        startActivity(addintent);
                        return true;
                    case R.id.myevent:
                        Intent myeventintent = new Intent(profilepage.this, eventpage.class);
                        myeventintent.putExtra("user_id", userId);
                        startActivity(myeventintent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.profile);

        Button editProfileButton = findViewById(R.id.edit_profile_button);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editprofileIntent = new Intent(profilepage.this, editprofilepage.class);
                editprofileIntent.putExtra("user_id", userId);
                startActivity(editprofileIntent);
            }
        });



        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(profilepage.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(profilepage.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
                } else {
                    openGallery();
                }
            }
        });

        loadProfileImageFromServer();

        removeImageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeProfileImage();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri);

                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    byte[] bytes = new byte[inputStream.available()];
                    inputStream.read(bytes);
                    inputStream.close();

                    String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                    new SaveImageTask().execute(encodedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadProfileImageFromServer() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String serverIp = getString(R.string.server_ip);
                    String urlString = serverIp + "/theeventhub/get_image.php?user_id=" + userId;
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
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null && !result.isEmpty()) {
                    try {
                        // Parse JSON response
                        JSONObject jsonObject = new JSONObject(result);
                        boolean success = jsonObject.getBoolean("success");
                        if (success && jsonObject.has("image")) {
                            String base64Image = jsonObject.getString("image");
                            // Decode Base64-encoded string to byte array
                            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                            // Convert byte array to Bitmap
                            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            // Set the Bitmap to the ImageView
                            profileImageView.setImageBitmap(decodedImage);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }



    private class SaveImageTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;
            try {
                String serverIp = getString(R.string.server_ip);
                URL apiUrl = new URL(serverIp + "/theeventhub/upload.php");
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String postData = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(userId), "UTF-8") + "&" +
                        URLEncoder.encode("image_data", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");

                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                writer.write(postData);
                writer.flush();
                writer.close();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    success = jsonResponse.getBoolean("success");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(profilepage.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(profilepage.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void removeProfileImage() {
        new RemoveImageTask().execute();
    }

    private class RemoveImageTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = false;
            try {
                String serverIp = getString(R.string.server_ip);
                URL apiUrl = new URL(serverIp + "/theeventhub/remove_image.php");
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String postData = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(userId), "UTF-8");

                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                writer.write(postData);
                writer.flush();
                writer.close();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    success = true;
                }

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                profileImageView.setImageDrawable(null);
                SharedPreferences sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("profileImage_" + userId);
                editor.apply();
                Toast.makeText(profilepage.this, "Profile image removed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(profilepage.this, "Failed to remove profile image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences("MySession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(profilepage.this, loginpage.class));
        finish();
    }

    private void getUserDetails(final int userId) {
        // Execute AsyncTask to perform network operation
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // Construct the request URL
                    String serverIp = getString(R.string.server_ip);
                    String urlString = serverIp + "/theeventhub/profile.php?user_id=" + userId;

                    // Open connection
                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    // Get the response
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    inputStream.close();

                    // Log the server response
                    Log.d("ProfilePage", "Server Response: " + response.toString());
                    return response.toString();

                } catch (IOException e) {
                    e.printStackTrace();
                    // Return an error message
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                // Log the response for debugging
                Log.d("ProfilePage", "Response: " + result);

                // Handle the response
                if (result != null && !result.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.getBoolean("success")) {
                            String studentidStr = jsonObject.getString("studentid");
                            String userFullname = jsonObject.getString("user_fullname");
                            String emailStr = jsonObject.getString("user_email");
                            String facultyStr = jsonObject.getString("faculty");
                            String phone_num = jsonObject.getString("phone_num");

                            // Display user details
                            studentid.setText(studentidStr);
                            name.setText(userFullname);
                            email.setText(emailStr);
                            faculty.setText(facultyStr);
                            phonenum.setText(phone_num);
                        } else {
                            showToast("User not found");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Show error message
                        showToast("Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    // Show error message
                    showToast("Empty or null response");
                }
            }

        }.execute();
    }




    private void showToast(String message) {
        Toast.makeText(profilepage.this, message, Toast.LENGTH_SHORT).show();
    }
}
