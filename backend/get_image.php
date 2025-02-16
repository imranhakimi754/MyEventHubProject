<?php
include 'db_conn.php';

// Get the user ID from the request
$user_id = isset($_GET['user_id']) ? $_GET['user_id'] : '';

$response = array('success' => false); // Initialize response

if ($user_id) {
    // Prepare the SQL statement to retrieve the image path
    $stmt = $conn->prepare("SELECT profile_image FROM user WHERE userID = ?");
    $stmt->bind_param("i", $user_id);

    // Execute the query
    $stmt->execute();
    $stmt->bind_result($imagePath);
    $stmt->fetch();
    $stmt->close();

    if ($imagePath) {
        // Check if the file exists
        if (file_exists($imagePath)) {
            // Encode the image data in base64
            $imageData = base64_encode(file_get_contents($imagePath));
            $response['success'] = true;
            $response['image'] = $imageData;
        }
    }
}

// Output the response as JSON
header('Content-Type: application/json');
echo json_encode($response);

// Close connection
$conn->close();
?>
