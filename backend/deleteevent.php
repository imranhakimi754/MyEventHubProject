<?php
include 'db_conn.php';

header('Content-Type: application/json');

if (isset($_GET['EventID']) && isset($_GET['UserID'])) {
    $eventID = $_GET['EventID'];
    $userID = $_GET['UserID'];

    // Start transaction
    $conn->begin_transaction();

    try {
        // Fetch emails of registered students
        $emails = [];
        $stmt = $conn->prepare("SELECT u.user_email FROM booking b JOIN user u ON b.userID = u.userID WHERE b.EventID = ?");
        $stmt->bind_param("i", $eventID);
        $stmt->execute();
        $result = $stmt->get_result();
        while ($row = $result->fetch_assoc()) {
            $emails[] = $row['user_email'];
        }
        $stmt->close();

        // Delete from attendance table
        $stmt = $conn->prepare("DELETE FROM attendance WHERE EventID = ?");
        $stmt->bind_param("i", $eventID);
        $stmt->execute();
        $stmt->close();

        // Delete from booking table
        $stmt = $conn->prepare("DELETE FROM booking WHERE EventID = ?");
        $stmt->bind_param("i", $eventID);
        $stmt->execute();
        $stmt->close();

        // Delete from feedback table
        $stmt = $conn->prepare("DELETE FROM feedback WHERE EventID = ?");
        $stmt->bind_param("i", $eventID);
        $stmt->execute();
        $stmt->close();

        // Delete from event table
        $stmt = $conn->prepare("DELETE FROM event WHERE EventID = ? AND userID = ?");
        $stmt->bind_param("ii", $eventID, $userID);
        $stmt->execute();

        if ($stmt->affected_rows > 0) {
            $conn->commit();
            echo json_encode(["success" => "Event deleted successfully"]);

            // Send email notifications
            $subject = "Event Cancellation Notification";
            $message = "Dear student,\n\nWe regret to inform you that the event you registered has been canceled. We apologize for any inconvenience caused.\n\nBest regards,\nThe Event Hub Team";
            $headers = "From: TheEventHub@gmail.com";

            foreach ($emails as $email) {
                if (!mail($email, $subject, $message, $headers)) {
                    error_log("Failed to send email to: " . $email);
                }
            }
        } else {
            $conn->rollback();
            echo json_encode(["error" => "Event not found or you do not have permission to delete this event"]);
        }
        $stmt->close();
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(["error" => "Failed to delete event: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["error" => "Invalid EventID or UserID"]);
}

$conn->close();
?>
