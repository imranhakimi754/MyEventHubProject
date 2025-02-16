<?php
include 'db_conn.php';

// Retrieve user ID from request
$user_id = $_GET['user_id'];

// Prepare and execute SQL query to get user details
$sql = "SELECT user_fullname, studentid, profile_image FROM user WHERE userID = $user_id";
$result = $conn->query($sql);

// Initialize response
$response = array('success' => false);

if ($result->num_rows > 0) {
    // User found, return user details as JSON
    $row = $result->fetch_assoc();
    $response['user_fullname'] = $row['user_fullname'];
    $response['studentid'] = $row['studentid'];
    
    // Check if the profile image exists
    $imagePath = $row['profile_image'];
    if ($imagePath && file_exists($imagePath)) {
        // Encode the image data in base64
        $imageData = base64_encode(file_get_contents($imagePath));
        $response['image'] = $imageData;
    }
    $response['success'] = true;
} else {
    // User not found
    $response['message'] = "User not found";
}

// Output the response as JSON
header('Content-Type: application/json');
echo json_encode($response);

// Close database connection
$conn->close();
?>
