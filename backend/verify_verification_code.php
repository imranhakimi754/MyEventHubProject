<?php
include 'db_conn.php';

header('Content-Type: application/json');

// Initialize response array
$response = array('success' => false);

// Getting email and verification code from POST request
$email = $_POST['email'] ?? '';
$verificationCode = $_POST['verification_code'] ?? '';

// Validate email and verification code
if (empty($email) || empty($verificationCode)) {
    $response['message'] = "Email and verification code are required";
} else {
    // Check if email and verification code match in database
    $stmt = $conn->prepare("SELECT * FROM user WHERE user_email = ? AND verification_code = ?");
    if ($stmt) {
        $stmt->bind_param("ss", $email, $verificationCode);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows > 0) {
            // Email and verification code match, update verification code to NULL
            $updateStmt = $conn->prepare("UPDATE user SET verification_code = 1 WHERE user_email = ?");
            if ($updateStmt) {
                $updateStmt->bind_param("s", $email);
                if ($updateStmt->execute()) {
                    // Verification successful
                    $response['success'] = true;
                    $response['message'] = "Verification successful";
                } else {
                    // Failed to update verification code
                    $response['message'] = "Failed to update verification code: " . $updateStmt->error;
                }
                $updateStmt->close();
            } else {
                $response['message'] = "Failed to prepare update statement: " . $conn->error;
            }
        } else {
            // Email or verification code does not match
            $response['message'] = "Invalid verification code";
        }
        $stmt->close();
    } else {
        $response['message'] = "Failed to prepare select statement: " . $conn->error;
    }
}

// Return JSON response
echo json_encode($response);

// Close the database connection
$conn->close();
?>
