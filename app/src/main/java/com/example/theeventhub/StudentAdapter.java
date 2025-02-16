package com.example.theeventhub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList;
    private List<Student> filteredList;
    private boolean showAttendance;
    private int eventID;
    private Context context;

    public StudentAdapter(Context context, List<Student> studentList, boolean showAttendance, int eventID) {
        this.context = context;
        this.studentList = studentList;
        this.filteredList = new ArrayList<>(studentList); // Initialize with all students
        this.showAttendance = showAttendance;
        this.eventID = eventID;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_userlist, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = filteredList.get(position);
        holder.textViewName.setText(student.getUserFullname());
        holder.textViewStudentId.setText(student.getStudentId());

        if (showAttendance) {
            holder.imageViewIndicator.setVisibility(View.VISIBLE);
            // Set the status indicator color
            if ("absent".equals(student.getStatus())) {
                holder.imageViewIndicator.setImageResource(R.drawable.red_circle); // assuming you have a red circle drawable
            } else if ("present".equals(student.getStatus())) {
                holder.imageViewIndicator.setImageResource(R.drawable.green_circle); // assuming you have a green circle drawable
            }
        } else {
            holder.imageViewIndicator.setVisibility(View.GONE);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showAttendance) {
                    showAttendanceDialog(student, holder);
                }
            }
        });
    }

    private void showAttendanceDialog(final Student student, final StudentViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Mark Attendance");

        View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.mark_attendance, null);
        TextView dialogTextViewName = dialogView.findViewById(R.id.dialogTextViewName);
        TextView dialogTextViewStudentId = dialogView.findViewById(R.id.dialogTextViewStudentId);
        final CheckBox checkBoxPresent = dialogView.findViewById(R.id.checkBoxPresent);

        dialogTextViewName.setText(student.getUserFullname());
        dialogTextViewStudentId.setText(student.getStudentId());

        if ("present".equals(student.getStatus())) {
            checkBoxPresent.setChecked(true);
        } else {
            checkBoxPresent.setChecked(false);
        }

        builder.setView(dialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkBoxPresent.isChecked()) {
                    markStudentPresent(student, holder);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void markStudentPresent(final Student student, final StudentViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    String serverIp = context.getString(R.string.server_ip); // Get the server IP from resources
                    URL url = new URL(serverIp + "/theeventhub/update_attendance.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    // Prepare data
                    String data = "userID=" + student.getUserID() + "&eventID=" + eventID;

                    // Send data
                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes());
                    os.flush();
                    os.close();

                    // Get response
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        in.close();

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        return jsonResponse.getString("status").equals("success");
                    } else {
                        return false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    student.setStatus("present");
                    holder.imageViewIndicator.setImageResource(R.drawable.green_circle);
                    Toast.makeText(holder.itemView.getContext(), "Marked present for " + student.getUserFullname(), Toast.LENGTH_SHORT).show();
                } else {
                    holder.imageViewIndicator.setImageResource(R.drawable.red_circle);
                    Toast.makeText(holder.itemView.getContext(), "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public void filterList(String status) {
        filteredList.clear();
        if (status.equals("all")) {
            filteredList.addAll(studentList);
        } else {
            for (Student student : studentList) {
                if (student.getStatus().equals(status)) {
                    filteredList.add(student);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
        this.filteredList = new ArrayList<>(studentList); // Reset filtered list to match all students
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewStudentId;
        ImageView imageViewIndicator;
        CardView cardView;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewStudentId = itemView.findViewById(R.id.textViewStudentId);
            imageViewIndicator = itemView.findViewById(R.id.imageViewIndicator);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }
}
