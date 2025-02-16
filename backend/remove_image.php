<?php
include 'db_conn.php';

// Get the POST data
$user_id = isset($_POST['user_id']) ? $_POST['user_id'] : '';

if ($user_id) {

    // Prepare and bind
    $sql = "UPDATE user SET profile_image = NULL WHERE userID = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $userId);

    // Execute the query
    if ($stmt->execute()) {
        echo json_encode(array("success" => true, "message" => "Image uploaded successfully."));
    } else {
        echo json_encode(array("success" => false, "message" => "Failed to upload image."));
    }

    // Close statement
    $stmt->close();
} else {
    echo json_encode(array("success" => false, "message" => "Invalid parameters."));
}

// Close connection
$conn->close();
?>
