<?php

include 'db_conn.php';
include 'phpqrcode/qrlib.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Start a transaction
    $conn->begin_transaction();

    // Retrieve and sanitize input data
    $userID = isset($_POST['userID']) ? intval($_POST['userID']) : 0;
    $eventTitle = isset($_POST['EventTitle']) ? $_POST['EventTitle'] : '';
    $eventTime = isset($_POST['EventTime']) ? $_POST['EventTime'] : '';
    $eventEnd = isset($_POST['EventEnd']) ? $_POST['EventEnd'] : '';
    $eventDate = isset($_POST['EventDate']) ? $_POST['EventDate'] : '';
    $eventDesc = isset($_POST['EventDesc']) ? $_POST['EventDesc'] : '';
    $eventType = isset($_POST['EventType']) ? $_POST['EventType'] : '';
    $eventVenue = isset($_POST['Evenue']) ? $_POST['Evenue'] : '';
    $image = isset($_POST['image']) ? $_POST['image'] : '';

    // Validate input data
    if ($userID === 0 || empty($eventTitle) || empty($eventTime) || empty($eventEnd) || empty($eventDate) || empty($eventDesc) || empty($eventType) || empty($eventVenue)) {
        echo json_encode(array("success" => false, "message" => "All fields are required."));
        exit;
    }

    // Fetch user_fullname from user table using userID
    $sql = "SELECT user_fullname FROM user WHERE userID = ?";
    $stmt = $conn->prepare($sql);
    if ($stmt === false) {
        die("Error preparing statement: " . $conn->error);
    }
    $stmt->bind_param("i", $userID);
    $stmt->execute();
    $result = $stmt->get_result();
    $userFullName = '';

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $userFullName = $row['user_fullname'];
    } else {
        echo json_encode(array("success" => false, "message" => "User not found."));
        exit;
    }
    $stmt->close();

    // Process the image if available
    $imagePath = '';
    if (!empty($image)) {
        $image = base64_decode($image);

        // Detect image MIME type and set appropriate file extension
        $finfo = new finfo(FILEINFO_MIME_TYPE);
        $mimeType = $finfo->buffer($image);
        $extension = '';

        switch ($mimeType) {
            case 'image/jpeg':
                $extension = '.jpg';
                break;
            case 'image/png':
                $extension = '.png';
                break;
            case 'image/gif':
                $extension = '.gif';
                break;
            default:
                echo json_encode(array("success" => false, "message" => "Unsupported image type."));
                exit;
        }

        $imageName = uniqid() . $extension;
        $imagePath = 'uploads/' . $imageName;
        if (file_put_contents($imagePath, $image) === false) {
            echo json_encode(array("success" => false, "message" => "Failed to save image."));
            exit;
        }
    }

    // Insert event data into the event table
    $sql = "INSERT INTO event (EventTitle, Org_name, EventTime, EventEnd, EventDate, EventDesc, EventType, EVenue, image, userID) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    if ($stmt === false) {
        die("Error preparing statement: " . $conn->error);
    }
    $stmt->bind_param("sssssssssi", $eventTitle, $userFullName, $eventTime, $eventEnd, $eventDate, $eventDesc, $eventType, $eventVenue, $imagePath, $userID);

    if ($stmt->execute()) {
        // Get the ID of the last inserted event
        $eventId = $stmt->insert_id;

        // Generate the QR code
        $qrData = $eventId;
        $qrFileName = 'uploads/QRCode_' . $eventId . '.png';

        // Generate the QR code and save as a PNG image
        QRcode::png($qrData, $qrFileName, QR_ECLEVEL_L, 10);

        // Update the event with the QR code path
        $updateSql = "UPDATE event SET qrcode = ? WHERE EventID = ?";
        $updateStmt = $conn->prepare($updateSql);
        if ($updateStmt === false) {
            $conn->rollback();
            die("Error preparing update statement: " . $conn->error);
        }
        $updateStmt->bind_param("si", $qrFileName, $eventId);
        $updateStmt->execute();
        $updateStmt->close();

        // Fetch all users' email addresses
        $userEmails = [];
        $sql = "SELECT user_email FROM user";
        $result = $conn->query($sql);
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                $userEmails[] = $row['user_email'];
            }
        }

        // Send email notification to all users
        $subject = 'New Event Created: ' . $eventTitle;
        $message = "
            <html>
            <head>
            <title>New Event Created</title>
            </head>
            <body>
            <p>Hello,</p>
            <p>A new event titled \"<strong>$eventTitle</strong>\" has been created by $userFullName.</p>
            <p><strong>Event Details:</strong></p>
            <ul>
                <li><strong>Title:</strong> $eventTitle</li>
                <li><strong>Time:</strong> $eventTime - $eventEnd</li>
                <li><strong>Date:</strong> $eventDate</li>
                <li><strong>Description:</strong> $eventDesc</li>
                <li><strong>Category:</strong> $eventType</li>
                <li><strong>Venue:</strong> $eventVenue</li>
            </ul>
            <p>You can view the event details and register using the QR code attached.</p>
            <p>Thank you,</p>
            <p><strong>The Event Hub</strong></p>
            </body>
            </html>
        ";

        $headers = "MIME-Version: 1.0" . "\r\n";
        $headers .= "Content-type:text/html;charset=UTF-8" . "\r\n";
        $headers .= "From: TheEventHub@gmail.com";

        foreach ($userEmails as $email) {
            mail($email, $subject, $message, $headers);
        }

        // Commit the transaction
        $conn->commit();

        echo json_encode(array("success" => true, "EventID" => $eventId));
    } else {
        $conn->rollback();
        echo json_encode(array("success" => false, "message" => "Error: " . $stmt->error));
    }

    $stmt->close();
    $conn->close();
} else {
    echo json_encode(array("success" => false, "message" => "Invalid request method."));
}
?>
