<?php
include 'db_conn.php';

$eventID = intval($_GET['EventID']);
$userId = intval($_GET['userId']);

// Fetch event details
$eventSql = "SELECT EventTitle, EventDate, EventTime, EVenue FROM event WHERE EventID = $eventID";
$eventResult = $conn->query($eventSql);

$eventDetails = [];
if ($eventResult->num_rows > 0) {
    $eventDetails = $eventResult->fetch_assoc();
} else {
    $eventDetails = ["error" => "No event found"];
}

// Fetch user details
$userSql = "SELECT  studentid, user_fullname FROM user WHERE userID = $userId";
$userResult = $conn->query($userSql);

$userDetails = [];
if ($userResult->num_rows > 0) {
    $userDetails = $userResult->fetch_assoc();
} else {
    $userDetails = ["error" => "No user found"];
}

// Combine results
$response = [
    "eventDetails" => $eventDetails,
    "userDetails" => $userDetails
];

echo json_encode($response);

$conn->close();
?>
