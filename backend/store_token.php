<?php
include 'db_conn.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $userID = isset($data['userID']) ? intval($data['userID']) : 0;
    $fcmToken = isset($data['fcm_token']) ? $data['fcm_token'] : '';

    if ($userID === 0 || empty($fcmToken)) {
        echo "Invalid input.";
        exit;
    }

    // Update the user's FCM token in the database
    $sql = "UPDATE user SET fcm_token = ? WHERE userID = ?";
    $stmt = $conn->prepare($sql);
    if ($stmt === false) {
        die("Error preparing statement: " . $conn->error);
    }
    $stmt->bind_param("si", $fcmToken, $userID);

    if ($stmt->execute()) {
        echo "Token updated successfully.";
    } else {
        echo "Error: " . $stmt->error;
    }

    $stmt->close();
    $conn->close();
} else {
    echo "Invalid request method.";
}
?>
