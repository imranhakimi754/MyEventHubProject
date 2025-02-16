<?php
include 'db_conn.php';

// Getting email from POST request
$email = $_POST['email'];

// Check if email exists in database
$stmt = $conn->prepare("SELECT * FROM user WHERE user_email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    // Email exists, generate verification code
    $verificationCode = rand(100000, 999999); // Generate 6-digit code

    // Update verification code in the database
    $updateStmt = $conn->prepare("UPDATE user SET verification_code = ? WHERE user_email = ?");
    $updateStmt->bind_param("ss", $verificationCode, $email);
    $updateStmt->execute();

    if ($updateStmt->affected_rows > 0) {
        // Verification code updated successfully
        $response['success'] = true;
        $response['message'] = "Verification code generated successfully";

        // Send email with verification code
        $to = $email;
        $subject = "Password Reset Verification Code";
        $message = "Your verification code for password reset is: " . $verificationCode;
        $headers = "From: TheEventHub@gmail.com"; // Replace with your email address

        if (mail($to, $subject, $message, $headers)) {
            // Email sent successfully
            $response['email_sent'] = true;
        } else {
            // Email sending failed
            $response['email_sent'] = false;
            $response['message'] = "Failed to send verification code email";
        }
    } else {
        $response['success'] = false;
        $response['message'] = "Failed to generate verification code";
    }
} else {
    // Email does not exist in the database
    $response['success'] = false;
    $response['message'] = "Email not found";
}

// Return JSON response
header('Content-Type: application/json');
echo json_encode($response);

// Close the database connection
$conn->close();
?>
