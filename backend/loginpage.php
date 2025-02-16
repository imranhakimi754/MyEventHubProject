<?php
include 'db_conn.php';

// Check if the request method is POST
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Check if the required POST parameters exist
    if (isset($_POST['studentid']) && isset($_POST['password'])) {
        // Retrieve the username and password from the POST parameters
        $studentid = $_POST['studentid'];
        $password = $_POST['password'];

        // Validate username and password (e.g., check if they are not empty)
        if (empty($studentid) || empty($password)) {
            echo "Error: student and password cannot be empty";
            exit;
        }

        // Prepare and execute the SQL statement to retrieve user_id based on username and password
        $sql = "SELECT userID FROM user WHERE studentid=? AND password=?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ss", $studentid, $password);
        $stmt->execute();
        $result = $stmt->get_result();

        // Check if a row is returned
        if ($result->num_rows > 0) {
            // Fetch the user data from the result
            $row = $result->fetch_assoc();
            $user_id = $row['userID'];

            // Return success message with user_id
            echo "Success: " . $user_id;
        } else {
            // No matching user found
            echo "Error: Invalid username or password";
        }

        // Close the statement
        $stmt->close();

        // Close the connection
        $conn->close();

    } else {
        // If required POST parameters are missing, return an error message
        echo json_encode(array("error" => "Missing required parameters"));
    }
} else {
    // If the request method is not POST, return an error message
    echo json_encode(array("error" => "Invalid request method"));
}
?>
