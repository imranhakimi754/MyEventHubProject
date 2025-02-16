package com.example.theeventhub;

public class Attendance {
    private int attendanceID;
    private int eventID;
    private int userID;
    private String status;
    private String userFullName;
    private String studentId;

    public Attendance(int attendanceID, int eventID, int userID, String status, String userFullName, String studentId) {
        this.attendanceID = attendanceID;
        this.eventID = eventID;
        this.userID = userID;
        this.status = status;
        this.userFullName = userFullName;
        this.studentId = studentId;
    }

    public int getAttendanceID() {
        return attendanceID;
    }

    public int getEventID() {
        return eventID;
    }

    public int getUserID() {
        return userID;
    }

    public String getStatus() {
        return status;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public String getStudentId() {
        return studentId;
    }
}
