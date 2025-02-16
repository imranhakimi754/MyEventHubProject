<?php
include 'db_conn.php';

header('Content-Type: application/json');

if (isset($_GET['user_id'])) {
    $userId = intval($_GET['user_id']);

    $sql = "SELECT user_fullname FROM user WHERE userID = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        echo json_encode(['user_fullname' => $row['user_fullname']]);
    } else {
        echo json_encode(['error' => 'User not found']);
    }

    $stmt->close();
} else {
    echo json_encode(['error' => 'Invalid request']);
}

$conn->close();
?>
