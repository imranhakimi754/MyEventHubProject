<?php
include 'db_conn.php';

// Change to POST method
$user_id = $_POST['user_id'];
$event_id = $_POST['event_id'];

// Delete from booking table
$sql_booking = "DELETE FROM booking WHERE EventID = ? AND userID = ?";
$stmt_booking = $conn->prepare($sql_booking);
$stmt_booking->bind_param("ii", $event_id, $user_id);

if ($stmt_booking->execute()) {
    if ($stmt_booking->affected_rows > 0) {
        // Delete from attendance table
        $sql_attendance = "DELETE FROM attendance WHERE EventID = ? AND userID = ?";
        $stmt_attendance = $conn->prepare($sql_attendance);
        $stmt_attendance->bind_param("ii", $event_id, $user_id);

        if ($stmt_attendance->execute()) {
            if ($stmt_attendance->affected_rows > 0) {
                echo json_encode(array("status" => "success"));
            } else {
                echo json_encode(array("status" => "error", "message" => "No rows affected in attendance table"));
            }
        } else {
            echo json_encode(array("status" => "error", "message" => $stmt_attendance->error));
        }

        $stmt_attendance->close();
    } else {
        echo json_encode(array("status" => "error", "message" => "No rows affected in booking table"));
    }
} else {
    echo json_encode(array("status" => "error", "message" => $stmt_booking->error));
}

$stmt_booking->close();
$conn->close();
?>
