<?php
include 'db_conn.php';

$eventID = isset($_GET['eventID']) ? $_GET['eventID'] : '';

$response = array();

if (!empty($eventID)) {
    // Fetch Event Title
    $eventStmt = $conn->prepare("SELECT EventTitle FROM event WHERE EventID = ?");
    $eventStmt->bind_param("i", $eventID);
    $eventStmt->execute();
    $eventResult = $eventStmt->get_result();
    
    if ($eventResult->num_rows > 0) {
        $eventRow = $eventResult->fetch_assoc();
        $response['EventTitle'] = $eventRow['EventTitle'];
    } else {
        $response['success'] = false;
        $response['message'] = "Invalid Event ID.";
        echo json_encode($response);
        exit();
    }

    // Fetch Attendance Data
    $query = "SELECT a.attendanceID, a.EventID, a.userID, a.status, u.user_fullname, u.studentid 
              FROM attendance a
              JOIN user u ON a.userID = u.userID
              WHERE a.EventID = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $eventID);
    $stmt->execute();
    $result = $stmt->get_result();

    $attendanceList = array();
    while ($row = $result->fetch_assoc()) {
        $attendanceList[] = $row;
    }

    $response['success'] = true;
    $response['attendance'] = $attendanceList;

    $stmt->close();
    $eventStmt->close();
} else {
    $response['success'] = false;
    $response['message'] = "Invalid Event ID.";
}

$conn->close();

header('Content-Type: application/json');
echo json_encode($response);
?>
