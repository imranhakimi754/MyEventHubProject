<?php

include 'db_conn.php';

// Get user ID from GET request
$user_id = isset($_GET["user_id"]) ? intval($_GET["user_id"]) : 0;

if ($user_id <= 0) {
    echo json_encode(array("error" => "Invalid user ID"));
    $conn->close();
    exit();
}

// Fetch events created by the user
$sql = "SELECT e.EventID, e.EventTitle, e.EventDate, e.EventTime, e.EventEnd, e.EventType, e.EVenue, e.image,
        CASE 
            WHEN e.EventDate > CURDATE() OR (e.EventDate = CURDATE() AND e.EventTime > CURTIME()) THEN 'upcoming'
            WHEN e.EventDate = CURDATE() AND CURTIME() BETWEEN e.EventTime AND e.EventEnd THEN 'ongoing'
            ELSE 'past'
        END as eventStatus
        FROM event e
        WHERE e.UserID = ?
        ORDER BY e.EventDate ASC, e.EventTime ASC";

$stmt = $conn->prepare($sql);
$stmt->bind_param('i', $user_id);
$stmt->execute();
$result = $stmt->get_result();

$events = array();
if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $imagePath = $row['image'];
        
        // Check if the image file exists and encode it to base64
        if (file_exists($imagePath)) {
            $row['image'] = base64_encode(file_get_contents($imagePath));
        } else {
            $row['image'] = null; // or some default value
            // Log an error message if the file does not exist
            error_log("Image file not found: " . $imagePath);
        }
        
        $events[] = $row;
    }
}

echo json_encode($events);

$stmt->close();
$conn->close();
?>
