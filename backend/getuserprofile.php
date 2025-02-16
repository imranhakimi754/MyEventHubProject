<?php

include 'db_conn.php';

// Retrieve user ID from request
if(isset($_GET['user_id'])){
    $user_id = $_GET['user_id'];



    // Prepare and execute SQL query to get user details
    $sql = "SELECT * FROM user WHERE userID = $user_id";
    $result = $conn->query($sql);

    // Check if the query was successful
    if ($result->num_rows > 0) {
        // Fetch user details
        $row = $result->fetch_assoc();
        $response = array(
            'user_email' => $row['user_email'],
            'phone_num' => $row['phone_num'],
        );

        // Return user details as JSON
        echo json_encode($response);
    } else {
        // No user found with the provided user ID
        echo "User not found";
    }

    // Close database connection
    $conn->close();
} else {
    // Handle case where user_id parameter is not set
    echo "User ID parameter not provided";
}
?>
