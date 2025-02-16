<?php
include 'db_conn.php';

// Get eventType from the request, if available
$eventType = isset($_GET['eventType']) ? $_GET['eventType'] : 'all';

// Prepare and execute SQL query to get events
if ($eventType === 'all') {
    $sql = "SELECT EventID, EventTitle, EventTime, EventDate, EventType, Evenue, image FROM event";
} else {
    $sql = $conn->prepare("SELECT EventID, EventTitle, EventTime, EventDate, EventType, Evenue, image FROM event WHERE EventType = ?");
    $sql->bind_param("s", $eventType);
}

if ($eventType === 'all') {
    $result = $conn->query($sql);
} else {
    $sql->execute();
    $result = $sql->get_result();
}

// Check if the query was successful
if ($result) {
    if ($result->num_rows > 0) {
        // Array to store events
        $events = array();

        // Loop through each row in the result set
        while ($row = $result->fetch_assoc()) {
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

        // Return events data as JSON
        header('Content-Type: application/json');
        echo json_encode($events);
    } else {
        // No events found
        echo json_encode(array("message" => "No events found"));
    }
} else {
    // Query error
    echo json_encode(array("error" => "Error executing query: " . $conn->error));
}

// Close the connection
$conn->close();
?>
