package com.example.theeventhub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

public class successevent extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;

    private ImageView qrCodeImage;
    private Button downloadTicketButton;
    private Button backtohome;
    private int eventID;
    private int userId;
    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successevent);

        qrCodeImage = findViewById(R.id.qr_code_image);
        downloadTicketButton = findViewById(R.id.download_ticket_button);
        backtohome = findViewById(R.id.back_to_home_button);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);

        fetchEventDetails();

        downloadTicketButton.setOnClickListener(v -> {
            if (qrCodeBitmap != null) {
                saveImageToGallery(qrCodeBitmap);
            } else {
                Toast.makeText(successevent.this, "No QR Code to save", Toast.LENGTH_SHORT).show();
            }
        });

        backtohome.setOnClickListener(v -> {
            Intent intent = new Intent(successevent.this, dashboardpage.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();  // Close the current activity to prevent going back to it
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
        String url = serverIp + "/theeventhub/successevent.php";
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

                                if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
                                    byte[] decodedString = Base64.decode(qrCodeBase64, Base64.DEFAULT);
                                    qrCodeBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    qrCodeImage.setImageBitmap(qrCodeBitmap);
                                } else {
                                    qrCodeImage.setImageResource(R.drawable.no_photo_available); // Set a default image if no image is available
                                }
                            } else {
                                Toast.makeText(successevent.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(successevent.this, "JSON Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(successevent.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
