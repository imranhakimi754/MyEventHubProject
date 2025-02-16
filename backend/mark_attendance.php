<?php
include 'db_conn.php';

// Get POST data
$event_id = isset($_POST['eventID']) ? intval($_POST['eventID']) : 0;
$user_id = isset($_POST['userID']) ? intval($_POST['userID']) : 0;

$response = array();

if ($event_id > 0 && $user_id > 0) {
    // Check if the record exists in the attendance table
    $check_stmt = $conn->prepare("SELECT * FROM attendance WHERE EventID = ? AND userID = ?");
    $check_stmt->bind_param("ii", $event_id, $user_id);
    $check_stmt->execute();
    $result = $check_stmt->get_result();

    if ($result->num_rows > 0) {
        // Update the status to 'present' if the record exists
        $update_stmt = $conn->prepare("UPDATE attendance SET status = 'present', timestamp = NOW() WHERE EventID = ? AND userID = ?");
        $update_stmt->bind_param("ii", $event_id, $user_id);

        if ($update_stmt->execute()) {
            $response['status'] = 'success';
            $response['message'] = 'Attendance marked successfully';
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Failed to mark attendance';
        }

        $update_stmt->close();
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Record not found in attendance table';
    }

    $check_stmt->close();
} else {
    $response['status'] = 'error';
    $response['message'] = 'Invalid event ID or user ID';
}

$conn->close();

// Return JSON response
header('Content-Type: application/json');
echo json_encode($response);
?>
