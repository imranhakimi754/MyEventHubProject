<?php
include 'db_conn.php';

// Get data from POST request
$userID = isset($_POST['userID']) ? $_POST['userID'] : '';
$eventID = isset($_POST['eventID']) ? $_POST['eventID'] : '';

$response = array();

if (!empty($userID) && !empty($eventID)) {
    // Check if the user is already registered for the event
    $checkStmt = $conn->prepare("SELECT * FROM booking WHERE userID = ? AND eventID = ?");
    $checkStmt->bind_param("ii", $userID, $eventID);
    $checkStmt->execute();
    $checkStmt->store_result();

    if ($checkStmt->num_rows > 0) {
        // User is already registered for the event
        $response['success'] = false;
        $response['message'] = "User is already registered for this event.";
    } else {
        // Start transaction
        $conn->begin_transaction();

        try {
            // Register the user for the event
            $stmt = $conn->prepare("INSERT INTO booking (userID, eventID) VALUES (?, ?)");
            $stmt->bind_param("ii", $userID, $eventID);
            $stmt->execute();

            // Insert into attendance table
            $attendanceStmt = $conn->prepare("INSERT INTO attendance (userID, eventID, status) VALUES (?, ?, 'absent')");
            $attendanceStmt->bind_param("ii", $userID, $eventID);
            $attendanceStmt->execute();

            // Fetch user details
            $userStmt = $conn->prepare("SELECT user_fullname, studentid FROM user WHERE userID = ?");
            $userStmt->bind_param("i", $userID);
            $userStmt->execute();
            $userResult = $userStmt->get_result();
            $userData = $userResult->fetch_assoc();
            $userName = $userData['user_fullname'];
            $studentID = $userData['studentid'];

            // Fetch event and organizer details
            $eventStmt = $conn->prepare("SELECT event.EventTitle, user.user_email FROM event JOIN user ON event.userID = user.userID WHERE event.eventID = ?");
            $eventStmt->bind_param("i", $eventID);
            $eventStmt->execute();
            $eventResult = $eventStmt->get_result();
            $eventData = $eventResult->fetch_assoc();
            $eventTitle = $eventData['EventTitle'];
            $organizerEmail = $eventData['user_email'];

            // Commit transaction
            $conn->commit();

            // Send notification email to the organizer
            $subject = "New Event Registration";
            $message = "A new user has registered for your event.\n\nFull Name: $userName\nStudent ID: $studentID\nEvent Title: $eventTitle";
            $headers = "From: TheEventHub@gmail.com";

            if (mail($organizerEmail, $subject, $message, $headers)) {
                $response['success'] = true;
                $response['message'] = "Registration successful and notification sent.";
            } else {
                $response['success'] = true;
                $response['message'] = "Registration successful but failed to send notification.";
            }

            $userStmt->close();
            $eventStmt->close();
        } catch (Exception $e) {
            // Rollback transaction
            $conn->rollback();

            $response['success'] = false;
            $response['message'] = "Failed to register for the event.";
        }

        $stmt->close();
        $attendanceStmt->close();
    }
    $checkStmt->close();
} else {
    $response['success'] = false;
    $response['message'] = "Invalid input.";
}

$conn->close();

// Return JSON response
header('Content-Type: application/json');
echo json_encode($response);
?>
