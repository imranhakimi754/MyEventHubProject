<?php

include 'db_conn.php';

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Retrieve POST parameters
    $userId = $_POST['userID'];
    $email = $_POST['user_email'];
    $phoneNum = $_POST['phone_num'];

    // Update user profile in the database
    $sql = "UPDATE user SET user_email='$email', phone_num='$phoneNum' WHERE userID='$userId'";
    if (mysqli_query($conn, $sql)) {
        $response["status"] = "success";
        $response["message"] = "Profile updated successfully";
    } else {
        $response["status"] = "error";
        $response["message"] = "Error updating profile: " . mysqli_error($conn);
    }

    echo json_encode($response);
} else {
    $response["status"] = "error";
    $response["message"] = "Invalid request method";
    echo json_encode($response);
}
?>
