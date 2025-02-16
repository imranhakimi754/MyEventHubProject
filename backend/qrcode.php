<?php

include 'db_conn.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $eventID = isset($_POST['EventID']) ? intval($_POST['EventID']) : 0;

    if ($eventID === 0) {
        echo json_encode(array("success" => false, "message" => "Invalid EventID."));
        exit;
    }

    $sql = "SELECT EventTitle, qrcode FROM event WHERE EventID = ?";
    $stmt = $conn->prepare($sql);
    if ($stmt === false) {
        die("Error preparing statement: " . $conn->error);
    }
    $stmt->bind_param("i", $eventID);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $imagePath = $row['qrcode'];

        // Encode QR code image
        if (file_exists($imagePath)) {
            $row['qrcode'] = base64_encode(file_get_contents($imagePath));
        } else {
            $row['qrcode'] = null;
            error_log("QR code file not found: " . $imagePath);
        }

        echo json_encode(array("success" => true, "event" => $row));
    } else {
        echo json_encode(array("success" => false, "message" => "Event not found."));
    }

    $stmt->close();
    $conn->close();
} else {
    echo json_encode(array("success" => false, "message" => "Invalid request method."));
}

?>
