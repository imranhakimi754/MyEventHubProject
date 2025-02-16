<?php
include 'db_conn.php';

// Get the raw POST data
$input = file_get_contents("php://input");
$data = json_decode($input, true);

$eventID = $data['EventID'];
$eventTitle = $data['EventTitle'];
$eventDate = $data['EventDate'];
$eventTime = $data['EventTime'];
$eventEnd = $data['EventEnd'];
$eventDesc = $data['EventDesc'];
$eventType = $data['EventType'];
$eVenue = $data['EVenue'];
$image = isset($data['image']) ? $data['image'] : null;

$response = array();

try {
    if (!empty($image)) {
        $image = base64_decode($image);
        $imageName = uniqid() . '.jpg';
        $imagePath = 'uploads/' . $imageName;
        file_put_contents($imagePath, $image);
        $sql = "UPDATE event 
                SET EventTitle = ?, EventDate = ?, EventTime = ?, EventEnd = ?, EventDesc = ?, EventType = ?, EVenue = ?, image = ?
                WHERE EventID = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ssssssssi", $eventTitle, $eventDate, $eventTime, $eventEnd, $eventDesc, $eventType, $eVenue, $imagePath, $eventID);
    } else {
        $sql = "UPDATE event 
                SET EventTitle = ?, EventDate = ?, EventTime = ?, EventEnd = ?, EventDesc = ?, EventType = ?, EVenue = ?
                WHERE EventID = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("sssssssi", $eventTitle, $eventDate, $eventTime, $eventEnd, $eventDesc, $eventType, $eVenue, $eventID);
    }

    if ($stmt->execute()) {
        $response["status"] = "success";

        // Send the response back to the client before sending emails
        header('Content-Type: application/json');
        echo json_encode($response);

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

        // Send notification email to each registered user
        $subject = "Event Update Notification";
        $message = "Dear participant,\n\nThe organizer has updated the event '$eventTitle'.\n\nEvent Details:\nTitle: $eventTitle\nDate: $eventDate\nTime: $eventTime - $eventEnd\nVenue: $eVenue\nDescription: $eventDesc\n\nBest regards,\nThe EventHub Team";
        $headers = "From: TheEventHub@gmail.com";

        foreach ($emails as $to) {
            mail($to, $subject, $message, $headers);
        }
    } else {
        $response["status"] = "error";
        $response["message"] = $stmt->error;
        header('Content-Type: application/json');
        echo json_encode($response);
    }
} catch (Exception $e) {
    $response["status"] = "error";
    $response["message"] = $e->getMessage();
    header('Content-Type: application/json');
    echo json_encode($response);
}

$stmt->close();
$conn->close();
?>
