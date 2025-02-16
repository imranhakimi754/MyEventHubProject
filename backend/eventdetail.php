<?php

include 'db_conn.php';

// Check if EventID is set
if (isset($_GET['EventID'])) {
    $eventID = $_GET['EventID'];

    // Query to retrieve event details based on EventID
    $sql = "SELECT * FROM event WHERE EventID = $eventID";

    // Perform the query
    $result = $conn->query($sql);

    if ($result->num_rows > 0) {
        // Fetch the event details
        $row = $result->fetch_assoc();

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

        // Create an array to hold the event details
        $eventDetails = array(
            "EventTitle" => $row["EventTitle"],
            "Org_name" => $row["Org_name"],
            "EventDate" => $row["EventDate"],
            "EventTime" => $row["EventTime"],
            "EventEnd" => $row["EventEnd"],
            "EventDesc" => $row["EventDesc"],
            "EventType" => $row["EventType"],
            "EVenue" => $row["EVenue"],
            "image" => $row["image"]
        );

        // Return the event details as JSON
        echo json_encode($eventDetails);
    } else {
        // Return error message
        echo json_encode(array('error' => 'Event not found'));
    }
} else {
    // Return error message
    echo json_encode(array('error' => 'EventID parameter is missing'));
}

// Close connection
$conn->close();

?>
