<?php

include 'db_conn.php';

header('Content-Type: application/json');

// Initialize response array
$response = array('success' => false);

// Retrieve user ID from request
if (isset($_GET['user_id'])) {
    $user_id = $_GET['user_id'];


    // Prepare and execute SQL query to get user details
    $sql = "SELECT * FROM user WHERE userID = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    // Check if the query was successful
    if ($result->num_rows > 0) {
        // Fetch user details
        $row = $result->fetch_assoc();
        $response = array(
            'success' => true,
            'studentid' => $row['studentid'],
            'user_fullname' => $row['user_fullname'],
            'user_email' => $row['user_email'],
            'faculty' => $row['faculty'],
            'phone_num' => $row['phone_num']
        );
    } else {
        // No user found with the provided user ID
        $response['message'] = "User not found";
    }

    // Close database connection
    $stmt->close();
    $conn->close();
} else {
    // Handle case where user_id parameter is not set
    $response['message'] = "User ID parameter not provided";
}

// Log the final response for debugging
error_log(json_encode($response));

echo json_encode($response);
?>
