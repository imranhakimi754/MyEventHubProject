<?php
header('Content-Type: application/json');

include 'db_conn.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $eventID = isset($_GET['EventID']) ? intval($_GET['EventID']) : 0;

    if ($eventID > 0) {
        // Fetch event title
        $eventStmt = $conn->prepare("SELECT EventTitle FROM event WHERE EventID = ?");
        $eventStmt->bind_param("i", $eventID);
        $eventStmt->execute();
        $eventResult = $eventStmt->get_result();

        if ($eventResult->num_rows > 0) {
            $eventRow = $eventResult->fetch_assoc();
            $eventTitle = $eventRow['EventTitle'];
        } else {
            echo json_encode(array("status" => "error", "message" => "Event not found"));
            exit();
        }

        $eventStmt->close();

        // Fetch feedbacks and count total feedback
        $feedbackStmt = $conn->prepare("SELECT f.feedback, f.feedbackdate, u.user_fullname FROM feedback f JOIN user u ON f.userID = u.userID WHERE f.EventID = ?");
        $feedbackStmt->bind_param("i", $eventID);
        $feedbackStmt->execute();
        $result = $feedbackStmt->get_result();

        $feedbacks = array();
        while ($row = $result->fetch_assoc()) {
            $feedbacks[] = $row;
        }

        $totalFeedback = count($feedbacks);

        echo json_encode(array("EventTitle" => $eventTitle, "TotalFeedback" => $totalFeedback, "Feedbacks" => $feedbacks));

        $feedbackStmt->close();
    } else {
        echo json_encode(array("status" => "error", "message" => "Invalid input"));
    }
} else {
    echo json_encode(array("status" => "error", "message" => "Invalid request method"));
}

$conn->close();
?>
