<?php
include 'db_conn.php';

// Check if the required parameters are set
if(isset($_POST['user_id']) && isset($_POST['current_password']) && isset($_POST['new_password'])) {
    
    // Sanitize user inputs to prevent SQL injection
    $user_id = mysqli_real_escape_string($con, $_POST['user_id']);
    $current_password = mysqli_real_escape_string($con, $_POST['current_password']);
    $new_password = mysqli_real_escape_string($con, $_POST['new_password']);

    // Check if the current password is correct
    $check_query = "SELECT * FROM user WHERE userID = $user_id AND password = '$current_password'";
    $check_result = mysqli_query($con, $check_query);

    if(mysqli_num_rows($check_result) > 0) {
        // Current password is correct, update the password
        $update_query = "UPDATE user SET password = '$new_password' WHERE userID = $user_id";
        $update_result = mysqli_query($con, $update_query);

        if($update_result) {
            // Password updated successfully
            echo json_encode(array("Password updated successfully"));
        } else {
            // Failed to update password
            echo json_encode(array("Failed to update password"));
        }
    } else {
        // Current password is incorrect
        echo json_encode(array("Current password is incorrect"));
    }

} else {
    // Required parameters missing
    echo json_encode(array("error" => "Missing required parameters"));
}

// Close database connection
mysqli_close($con);
?>
