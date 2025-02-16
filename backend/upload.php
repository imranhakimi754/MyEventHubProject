<?php
include 'db_conn.php';

// Get the POST data
$user_id = isset($_POST['user_id']) ? $_POST['user_id'] : '';
$image_data = isset($_POST['image_data']) ? $_POST['image_data'] : '';

if ($user_id && $image_data) {
    // Decode the image data
    $decoded_image_data = base64_decode($image_data);

    // Generate a unique file name
    $imageName = uniqid() . '.jpg';
    $imagePath = 'uploads/' . $imageName;

    // Save the image to the file system
    if (file_put_contents($imagePath, $decoded_image_data)) {
        // Prepare the SQL statement to update the image path in the database
        $stmt = $conn->prepare("UPDATE user SET profile_image = ? WHERE userID = ?");
        $stmt->bind_param("si", $imagePath, $user_id);

        // Execute the query
        if ($stmt->execute()) {
            echo json_encode(array("success" => true, "message" => "Image uploaded successfully."));
        } else {
            echo json_encode(array("success" => false, "message" => "Failed to upload image to database."));
        }

        // Close statement
        $stmt->close();
    } else {
        echo json_encode(array("success" => false, "message" => "Failed to save image to file system."));
    }
} else {
    echo json_encode(array("success" => false, "message" => "Invalid parameters."));
}

// Close connection
$conn->close();
?>
