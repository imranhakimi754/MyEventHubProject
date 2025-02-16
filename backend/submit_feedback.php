<?php
header('Content-Type: application/json');

include 'db_conn.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $eventID = isset($_POST['event_id']) ? intval($_POST['event_id']) : 0;
    $userID = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
    $feedback = isset($_POST['feedback']) ? trim($_POST['feedback']) : '';
    $dateSubmitted = isset($_POST['date']) ? trim($_POST['date']) : date('Y-m-d'); // Default to current date if not provided

    if ($eventID > 0 && $userID > 0 && !empty($feedback)) {
        // Check if feedback already exists for this event and user
        $check_stmt = $conn->prepare("SELECT * FROM feedback WHERE EventID = ? AND userID = ?");
        $check_stmt->bind_param("ii", $eventID, $userID);
        $check_stmt->execute();
        $check_result = $check_stmt->get_result();

        if ($check_result->num_rows > 0) {
            echo json_encode(array("status" => "error", "message" => "You already submitted feedback for this event"));
        } else {
            // Insert new feedback
            $stmt = $conn->prepare("INSERT INTO feedback (EventID, userID, feedback, feedbackdate) VALUES (?, ?, ?, ?)");
            $stmt->bind_param("iiss", $eventID, $userID, $feedback, $dateSubmitted);

            if ($stmt->execute()) {
                echo json_encode(array("status" => "success", "message" => "Feedback submitted successfully"));
            } else {
                echo json_encode(array("status" => "error", "message" => "Failed to submit feedback"));
            }

            $stmt->close();
        }

        $check_stmt->close();
    } else {
        echo json_encode(array("status" => "error", "message" => "Invalid input"));
    }
} else {
    echo json_encode(array("status" => "error", "message" => "Invalid request method"));
}

$conn->close();
?>
