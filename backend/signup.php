<?php
include 'db_conn.php';

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Create connection
   

    // Check connection


    // Get data from POST request
    $fullName = $_POST["fullName"];
    $studentid = $_POST["studentid"];
    $email = $_POST["email"];
    $faculty = $_POST["faculty"];
    $phone_num = $_POST["phone_num"];
    $password = $_POST["password"];

    // Check if the email belongs to the allowed domains
    $allowedStudentDomain = '@student.uitm.edu.my';
    $allowedStaffDomain = '@uitm.edu.my';
    if (!filter_var($email, FILTER_VALIDATE_EMAIL) || 
        (!preg_match('/' . preg_quote($allowedStudentDomain, '/') . '$/', $email) && 
        !preg_match('/' . preg_quote($allowedStaffDomain, '/') . '$/', $email))) {
        echo "Error: Please use a valid UITM email address.";
    } elseif (strlen($password) < 8) {
        echo "Error: Password must be at least 8 characters long.";
    } else {
        // Check if the email already exists in the database
        $emailCheckSql = "SELECT * FROM user WHERE user_email = '$email'";
        $emailCheckResult = $conn->query($emailCheckSql);

        if ($emailCheckResult->num_rows > 0) {
            // Email already exists, return error message
            echo "Error: Email already exists. Please sign in.";
        } else {
            // Email is valid and not taken, allow registration
            $sql = "INSERT INTO user (studentid, user_fullname, user_email, faculty, phone_num, password) VALUES ('$studentid', '$fullName', '$email', '$faculty', '$phone_num', '$password')";

            if ($conn->query($sql) === TRUE) {
                // Send congratulatory email
                $to = $email;
                $subject = "Registration Successful - TheEventHub";
                $message = "Dear $fullName,\n\nCongratulations! Your registration at TheEventHub application is successful.\n\nBest regards,\nThe Event Hub Team";
                $headers = "From: no-reply@theeventhub.com";

                if (mail($to, $subject, $message, $headers)) {
                    echo "New record created successfully and email sent.";
                } else {
                    echo "New record created successfully but email sending failed.";
                }
            } else {
                echo "Error: " . $sql . "<br>" . $conn->error;
            }
        }
    }

    $conn->close();
} else {
    echo "Error: Invalid request method.";
}
?>
