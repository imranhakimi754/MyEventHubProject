<?php
include 'db_conn.php';

$eventID = $_GET['eventID'];

$response = array();

// Fetch event title
$eventQuery = "SELECT EventTitle FROM event WHERE EventID = $eventID";
$eventResult = mysqli_query($conn, $eventQuery);

if ($eventResult && mysqli_num_rows($eventResult) > 0) {
    $eventRow = mysqli_fetch_assoc($eventResult);
    $response['EventTitle'] = $eventRow['EventTitle'];
} else {
    $response['EventTitle'] = '';
}

// Fetch total students
$totalStudentsQuery = "SELECT COUNT(*) AS total_students FROM booking WHERE EventID = $eventID";
$totalStudentsResult = mysqli_query($conn, $totalStudentsQuery);

if ($totalStudentsResult && mysqli_num_rows($totalStudentsResult) > 0) {
    $totalStudentsRow = mysqli_fetch_assoc($totalStudentsResult);
    $response['total_students'] = $totalStudentsRow['total_students'];
} else {
    $response['total_students'] = 0;
}

// Fetch students
$studentsQuery = "SELECT u.userID, u.user_fullname, u.studentid FROM booking b JOIN user u ON b.userID = u.userID WHERE b.EventID = $eventID";
$studentsResult = mysqli_query($conn, $studentsQuery);

$students = array();
if ($studentsResult && mysqli_num_rows($studentsResult) > 0) {
    while ($row = mysqli_fetch_assoc($studentsResult)) {
        $students[] = $row;
    }
}

$response['students'] = $students;

echo json_encode($response);

mysqli_close($conn);
?>
