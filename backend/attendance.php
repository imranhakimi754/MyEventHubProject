<?php
include 'db_conn.php';

$eventID = isset($_GET['eventID']) ? intval($_GET['eventID']) : 0;

$response = array();

if ($eventID > 0) {
    // Fetch event title
    $eventQuery = "SELECT EventTitle FROM event WHERE EventID = ?";
    $stmt = $conn->prepare($eventQuery);
    $stmt->bind_param("i", $eventID);
    $stmt->execute();
    $eventResult = $stmt->get_result();

    if ($eventResult && $eventResult->num_rows > 0) {
        $eventRow = $eventResult->fetch_assoc();
        $response['EventTitle'] = $eventRow['EventTitle'];
    } else {
        $response['EventTitle'] = '';
    }

    $stmt->close();

    // Fetch total students
    $totalStudentsQuery = "SELECT COUNT(*) AS total_students FROM attendance WHERE EventID = ?";
    $stmt = $conn->prepare($totalStudentsQuery);
    $stmt->bind_param("i", $eventID);
    $stmt->execute();
    $totalStudentsResult = $stmt->get_result();

    if ($totalStudentsResult && $totalStudentsResult->num_rows > 0) {
        $totalStudentsRow = $totalStudentsResult->fetch_assoc();
        $response['total_students'] = $totalStudentsRow['total_students'];
    } else {
        $response['total_students'] = 0;
    }

    $stmt->close();

    // Fetch students with attendance status
    $studentsQuery = "SELECT u.userID, u.user_fullname, u.studentid, a.status FROM attendance a JOIN user u ON a.userID = u.userID WHERE a.EventID = ?";
    $stmt = $conn->prepare($studentsQuery);
    $stmt->bind_param("i", $eventID);
    $stmt->execute();
    $studentsResult = $stmt->get_result();

    $students = array();
    if ($studentsResult && $studentsResult->num_rows > 0) {
        while ($row = $studentsResult->fetch_assoc()) {
            $students[] = $row;
        }
    }

    $stmt->close();

    $response['students'] = $students;
} else {
    $response['error'] = 'Invalid event ID';
}

echo json_encode($response);

$conn->close();
?>
