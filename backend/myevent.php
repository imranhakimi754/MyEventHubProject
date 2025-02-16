<?php
include 'db_conn.php';

// Get user ID from GET request
$user_id = $_GET["user_id"];

// Fetch registered events for the user
$sql = "SELECT e.EventID, e.EventTitle, e.EventDate, e.EventTime, e.EventEnd, e.EventType, e.EVenue, e.image,
        CASE 
            WHEN e.EventDate > CURDATE() OR (e.EventDate = CURDATE() AND e.EventTime > CURTIME()) THEN 'upcoming'
            WHEN e.EventDate = CURDATE() AND CURTIME() BETWEEN e.EventTime AND e.EventEnd THEN 'ongoing'
            ELSE 'past'
        END as eventStatus
        FROM booking b
        JOIN event e ON b.EventID = e.EventID
        WHERE b.userID = '$user_id'
        ORDER BY e.EventDate ASC, e.EventTime ASC";

$result = $conn->query($sql);

$events = array();
if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        // Construct the full path to the image file
        $imagePath = $row['image'];
        
        // Check if the image file exists and encode it to base64
        if (file_exists($imagePath)) {
            $row['image'] = base64_encode(file_get_contents($imagePath));
        } else {
            $row['image'] = null; // or some default value
            // Log an error message if the file does not exist
            error_log("Image file not found: " . $imagePath);
        }
        
        // Add event data to the events array
        $events[] = $row;
    }
}

echo json_encode($events);

$conn->close();
?>
