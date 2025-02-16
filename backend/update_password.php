<?php
include 'db_conn.php';

// Getting POST data from Android app
$email = $_POST['email'] ?? '';
$newPassword = $_POST['new_password'] ?? '';

// Validate email and new password
if (empty($email) || empty($newPassword)) {
    $response['success'] = false;
    $response['message'] = "Email and new password are required";
} elseif (strlen($newPassword) < 8) {
    $response['success'] = false;
    $response['message'] = "Password must be at least 8 characters long";
} else {
    // Update password in the database
    $updateStmt = $conn->prepare("UPDATE user SET password = ? WHERE user_email = ?");

    $updateStmt->bind_param("ss", $newPassword, $email);

    if ($updateStmt->execute()) {
        // Password updated successfully
        $response['success'] = true;
        $response['message'] = "Password updated successfully";
    } else {
        // Failed to update password
        $response['success'] = false;
        $response['message'] = "Failed to update password";
    }
}

// Return JSON response
header('Content-Type: application/json');
echo json_encode($response);

// Close the database connection
$conn->close();
?>
