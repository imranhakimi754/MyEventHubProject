package com.example.theeventhub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Qrcodepage extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;

    private int userId;
    private int eventID;
    private ImageView qrCodeImage;
    private TextView eventTitleTextView;
    private ImageView backButton;
    private Button shareButton;
    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodepage);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);

        qrCodeImage = findViewById(R.id.qrCodeImage);
        eventTitleTextView = findViewById(R.id.titlevent);
        shareButton = findViewById(R.id.shareButton);
        backButton = findViewById(R.id.backButton);

        fetchEventDetails();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Qrcodepage.this, manageevent.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("EventID", eventID);
                startActivity(intent);
                finish();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qrCodeBitmap != null) {
                    saveImageToGallery(qrCodeBitmap);
                } else {
                    Toast.makeText(Qrcodepage.this, "No QR Code to save", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Check and request permission to write to storage
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }

    private void fetchEventDetails() {
        String serverIp = getString(R.string.server_ip);
        String url = serverIp + "/theeventhub/qrcode.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                JSONObject event = jsonResponse.getJSONObject("event");
                                String qrCodeBase64 = event.getString("qrcode");
                                String eventTitle = event.getString("EventTitle");

                                if (eventTitle != null && !eventTitle.isEmpty()) {
                                    eventTitleTextView.setText(eventTitle); // Set the event title in the TextView
                                } else {
                                    eventTitleTextView.setText("No Title Available");
                                }

                                if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
                                    byte[] decodedString = Base64.decode(qrCodeBase64, Base64.DEFAULT);
                                    qrCodeBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    qrCodeImage.setImageBitmap(qrCodeBitmap);
                                } else {
                                    qrCodeImage.setImageResource(R.drawable.no_photo_available); // Set a default image if no image is available
                                }
                            } else {
                                Toast.makeText(Qrcodepage.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Qrcodepage.this, "JSON Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Qrcodepage.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("EventID", String.valueOf(eventID));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void saveImageToGallery(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "QRCode_" + eventID + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/" + "QRCodeImages");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    Toast.makeText(this, "QR Code saved to gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission to write to storage is required to save images", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
