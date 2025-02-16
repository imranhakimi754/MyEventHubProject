package com.example.theeventhub;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {

    private Context mContext;
    private List<Eventlist> mEventList;
    private int userId;
    private boolean isFromDashboard;
    private OnItemClickListener onItemClickListener;
    private int currentEventId;
    private AlertDialog currentAlertDialog;

    public EventListAdapter(Context context, ArrayList<Eventlist> eventList, int userId, boolean isFromDashboard) {
        this.mContext = context;
        this.mEventList = eventList;
        this.userId = userId;
        this.isFromDashboard = isFromDashboard;
    }

    public interface OnItemClickListener {
        void onItemClick(Eventlist event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updateEvents(List<Eventlist> newEventList) {
        mEventList.clear();
        mEventList.addAll(newEventList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.eventlist, parent, false);
        return new ViewHolder(view);
    }

    public static byte[] decodeBase64(String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Eventlist currentEvent = mEventList.get(position);

        holder.eventNameTextView.setText(currentEvent.getEventName());
        holder.eventDateTextView.setText(currentEvent.getEventDate());
        holder.eventTimeTextView.setText(currentEvent.getEventTime());
        holder.eventLocationTextView.setText(currentEvent.getEventLocation());

        byte[] imageBytes = decodeBase64(currentEvent.getImage());

        // Load image using Glide
        Glide.with(mContext)
                .asBitmap()
                .load(imageBytes)
                .into(holder.imageEvent);

        holder.cardView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(currentEvent);
            }

            if (isFromDashboard) {
                Intent intent = new Intent(mContext, detaileventpage.class);
                intent.putExtra("EventID", currentEvent.getEventID());
                intent.putExtra("user_id", userId);
                mContext.startActivity(intent);
            } else if (mContext instanceof eventpage) {
                handleEventPageClick(currentEvent);
            } else {
                handleDefaultClick(currentEvent);
            }
        });
    }


    private void handleEventPageClick(Eventlist currentEvent) {
        Intent intent;
        switch (currentEvent.getEventStatus()) {
            case "upcoming":
                intent = new Intent(mContext, manageevent.class);
                break;
            case "ongoing":
                intent = new Intent(mContext, viewattendance.class);
                break;
            case "past":
                intent = new Intent(mContext, viewfeedback.class);
                break;
            default:
                return;
        }
        intent.putExtra("EventID", currentEvent.getEventID());
        intent.putExtra("user_id", userId);
        mContext.startActivity(intent);
    }

    private void handleDefaultClick(Eventlist currentEvent) {
        switch (currentEvent.getEventStatus()) {
            case "ongoing":
                showOngoingEventDetailsDialog(currentEvent);
                break;
            case "past":
                showPastEventDetailsDialog(currentEvent);
                break;
            default:
                showEventDetailsDialog(currentEvent);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mEventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView, eventDateTextView, eventTimeTextView, eventLocationTextView;
        CardView cardView;
        ImageView imageEvent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.text_event_name);
            eventDateTextView = itemView.findViewById(R.id.text_event_date);
            eventTimeTextView = itemView.findViewById(R.id.text_event_time);
            eventLocationTextView = itemView.findViewById(R.id.text_event_location);
            cardView = itemView.findViewById(R.id.card_view);
            imageEvent = itemView.findViewById(R.id.image_event);
        }
    }


    private void showEventDetailsDialog(Eventlist event) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogView = inflater.inflate(R.layout.dialog_event_details, null);
        dialogBuilder.setView(dialogView);

        TextView eventTitle = dialogView.findViewById(R.id.eventTitle);
        TextView eventDate = dialogView.findViewById(R.id.eventDate);
        TextView eventTime = dialogView.findViewById(R.id.eventTime);
        TextView eventLocation = dialogView.findViewById(R.id.eventLocation);
        Button btnCancelEvent = dialogView.findViewById(R.id.btnCancelEvent);

        eventTitle.setText(event.getEventName());
        eventDate.setText(event.getEventDate());
        eventTime.setText(event.getEventTime());
        eventLocation.setText(event.getEventLocation());

        AlertDialog alertDialog = dialogBuilder.create();

        dialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        btnCancelEvent.setOnClickListener(v -> {
            showCancelConfirmationDialog(event, alertDialog);
        });

        alertDialog.show();
    }

    private void showCancelConfirmationDialog(Eventlist event, AlertDialog parentDialog) {
        AlertDialog.Builder confirmationDialogBuilder = new AlertDialog.Builder(mContext);
        confirmationDialogBuilder.setMessage("Do you really want to cancel the event?");
        confirmationDialogBuilder.setPositiveButton("Yes", (dialog, which) -> {
            Log.d("CancelEvent", "Event ID: " + event.getEventID() + ", User ID: " + userId);
            cancelEvent(event.getEventID(), userId);
            parentDialog.dismiss();
        });
        confirmationDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog confirmationDialog = confirmationDialogBuilder.create();
        confirmationDialog.show();
    }

    private void cancelEvent(int eventID, int userId) {
        new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... params) {
                int eventID = params[0];
                int userId = params[1];
                try {
                    String serverIp = mContext.getString(R.string.server_ip);
                    URL url = new URL(serverIp + "/theeventhub/cancel_booking.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String postParams = "event_id=" + eventID + "&user_id=" + userId;
                    conn.getOutputStream().write(postParams.getBytes());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return response.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(result);
                        String status = jsonResponse.getString("status");
                        if ("success".equals(status)) {
                            Toast.makeText(mContext, "Event cancelled successfully", Toast.LENGTH_SHORT).show();
                            if (mContext instanceof Myeventpage) {
                                ((Myeventpage) mContext).refreshEventList(userId);
                            }
                        } else {
                            Toast.makeText(mContext, "Failed to cancel event", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(mContext, "Error connecting to the server", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(eventID, userId);
    }


    private void showOngoingEventDetailsDialog(Eventlist event) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogView = inflater.inflate(R.layout.dialog_event_details_ongoing, null);
        dialogBuilder.setView(dialogView);

        TextView eventTitle = dialogView.findViewById(R.id.eventTitle);
        TextView eventDate = dialogView.findViewById(R.id.eventDate);
        TextView eventTime = dialogView.findViewById(R.id.eventTime);
        TextView eventLocation = dialogView.findViewById(R.id.eventLocation);
        Button btnTickAttendance = dialogView.findViewById(R.id.btnTickAttendance);

        eventTitle.setText(event.getEventName());
        eventDate.setText(event.getEventDate());
        eventTime.setText(event.getEventTime());
        eventLocation.setText(event.getEventLocation());

        currentEventId = event.getEventID();  // Store the current event ID
        currentAlertDialog = dialogBuilder.create();  // Store the current AlertDialog

        dialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        btnTickAttendance.setOnClickListener(v -> {
            startQRCodeScanner();
        });

        currentAlertDialog.show();
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator((Activity) mContext);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt(""); // Remove the default prompt
        integrator.setOrientationLocked(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setBeepEnabled(true);
        integrator.setTimeout(10000); // Optional: Set timeout in milliseconds for the scanner

        integrator.setCaptureActivity(CustomScanner.class);
        integrator.initiateScan();
    }




    private void showPastEventDetailsDialog(Eventlist event) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogView = inflater.inflate(R.layout.dialog_event_details_past, null);
        dialogBuilder.setView(dialogView);

        TextView eventTitle = dialogView.findViewById(R.id.eventTitle);
        TextView eventDate = dialogView.findViewById(R.id.eventDate);
        TextView eventTime = dialogView.findViewById(R.id.eventTime);
        TextView eventLocation = dialogView.findViewById(R.id.eventLocation);
        EditText feedbackEditText = dialogView.findViewById(R.id.editTextFeedback);
        Button btnSubmitFeedback = dialogView.findViewById(R.id.buttonSubmitFeedback);

        eventTitle.setText(event.getEventName());
        eventDate.setText(event.getEventDate());
        eventTime.setText(event.getEventTime());
        eventLocation.setText(event.getEventLocation());

        AlertDialog alertDialog = dialogBuilder.create();

        dialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        btnSubmitFeedback.setOnClickListener(v -> {
            String feedback = feedbackEditText.getText().toString().trim();
            if (!feedback.isEmpty()) {
                submitFeedback(event.getEventID(), userId, feedback);
                alertDialog.dismiss();
            } else {
                Toast.makeText(mContext, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

    private void submitFeedback(int eventID, int userId, String feedback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String serverIp = mContext.getString(R.string.server_ip);
                    URL url = new URL(serverIp + "/theeventhub/submit_feedback.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String postParams = "event_id=" + eventID + "&user_id=" + userId + "&feedback=" + feedback;
                    conn.getOutputStream().write(postParams.getBytes());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return response.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(result);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");
                        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, "Error connecting to the server", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

}


