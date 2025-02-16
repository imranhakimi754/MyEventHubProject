package com.example.theeventhub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class editprofilepage extends AppCompatActivity {

    private EditText  editTextEmail, editTextPhoneNumber;
    private Button buttonSave, buttonCancel;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofilepage);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);

        userId = getIntent().getIntExtra("user_id", 0);

        // Retrieve user's profile information and populate the EditText fields
        getUserProfile(userId);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String phone_num = editTextPhoneNumber.getText().toString().trim();

                if ( email.isEmpty() || phone_num.isEmpty()) {
                    Toast.makeText(editprofilepage.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateProfile(userId, email, phone_num);
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the profile page without updating
                Intent intent = new Intent(editprofilepage.this, profilepage.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }

    private void updateProfile(int userId, String email, String phone_num) {
        String serverIp = getString(R.string.server_ip);
        String url = serverIp + "/theeventhub/update_profile.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            Toast.makeText(editprofilepage.this, message, Toast.LENGTH_SHORT).show();

                            if (status.equals("success")) {
                                // Update successful, navigate to the profile page
                                Intent intent = new Intent(editprofilepage.this, profilepage.class);
                                intent.putExtra("user_id", userId);
                                startActivity(intent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(editprofilepage.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(editprofilepage.this, "Error occurred", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", String.valueOf(userId));
                params.put("user_email", email);
                params.put("phone_num", phone_num);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void getUserProfile(final int userId) {
        String serverIp = getString(R.string.server_ip);
        String url = serverIp + "/theeventhub/getuserprofile.php?user_id=" + userId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String emailStr = jsonObject.getString("user_email");
                            String phone_numStr = jsonObject.getString("phone_num");

                            // Display user details
                            editTextEmail.setText(emailStr);
                            editTextPhoneNumber.setText(phone_numStr);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            showToast("Error parsing JSON");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToast("Error fetching user profile");
                    }
                });

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void showToast(String message) {
        Toast.makeText(editprofilepage.this, message, Toast.LENGTH_SHORT).show();
    }

}
