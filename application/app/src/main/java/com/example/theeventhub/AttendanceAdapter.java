package com.example.theeventhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<Attendance> attendanceList;

    public AttendanceAdapter(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendancelist, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);
        holder.textViewName.setText(attendance.getUserFullName());
        holder.textViewStudentId.setText(attendance.getStudentId());

        // Set the indicator based on the attendance status
        if (attendance.getStatus().equalsIgnoreCase("present")) {
            holder.imageViewIndicator.setImageResource(R.drawable.ic_present); // Replace with your actual drawable for present
        } else {
            holder.imageViewIndicator.setImageResource(R.drawable.ic_absent); // Replace with your actual drawable for absent
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewStudentId;
        ImageView imageViewIndicator;

        CardView cardView;


        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewStudentId = itemView.findViewById(R.id.textViewStudentId);
            imageViewIndicator = itemView.findViewById(R.id.imageViewIndicator);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }
}
