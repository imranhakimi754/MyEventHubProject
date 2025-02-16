<?php
session_start();

// Check if admin is logged in
if (!isset($_SESSION['admin'])) {
    header("Location: login.php");
    exit();
}

// Database credentials
$servername = "localhost";
$db_username = "root";
$db_password = "";
$dbname = "theeventhub";

// Create connection
$conn = new mysqli($servername, $db_username, $db_password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get EventID from URL
$event_id = isset($_GET['id']) ? $_GET['id'] : 0;

// Fetch event details
$event_sql = "SELECT * FROM event WHERE EventID = ?";
$stmt = $conn->prepare($event_sql);
$stmt->bind_param("i", $event_id);
$stmt->execute();
$event_result = $stmt->get_result();
$event = $event_result->fetch_assoc();

$stmt->close();
$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Details</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #fff;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            flex-direction: column;
            min-height: 100vh;
            background-color: #F8A8A8;
        }

        .content {
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 1400px;
            margin-top: 30px;
            height: 670px;
            overflow: hidden;
        }

        .event-image-container {
            width: 100%;
            text-align: center;
            position: relative;
            margin-top: 20px;
        }

        .event-image {
            width: 90%;
            height: 300px;
            object-fit: cover;
            display: block;
            margin: auto;
        }

        .event-info {
            padding: 20px;
            width: 90%;
            padding-left: 5%;
            margin: auto;
        }

        .event-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .event-header h1 {
            margin: 0;
            font-size: 24px;
            color: #333;
        }

        .event-details {
            display: flex;
            justify-content: space-between;
            margin-bottom: 20px;
        }

        .event-details p {
            margin: 0;
            font-size: 14px;
            color: #666;
        }

        .event-details .date-time {
            width: 48%;
            border-right: 1px solid;
        }

        .event-details .location {
            width: 48%;
        }

        .event-description {
            margin-top: 20px;
        }

        .button-container {
            display: flex;
            justify-content: center;
            gap: 10px;
            margin-top: 20px;
        }

        .button-container a {
            padding: 10px 20px;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            text-align: center;
        }

        .participants-button {
            background-color: #28a745;
        }

        .participants-button:hover {
            background-color: #218838;
        }

        .feedback-button {
            background-color: #ff9800;
        }

        .feedback-button:hover {
            background-color: #e68900;
        }
    </style>
</head>
<body>
    <div class="content">
        <div class="event-image-container">
            <img src="../<?php echo htmlspecialchars($event['image']); ?>" alt="Event Image" class="event-image">
        </div>
        <div class="event-info">
            <p><strong>Organizer:</strong> <?php echo htmlspecialchars($event['Org_name']); ?></p>
            <div class="event-header">
                <h1><?php echo htmlspecialchars($event['EventTitle']); ?></h1>
            </div>
            <div class="event-details">
                <div class="date-time">
                    <p><strong>Date and time:</strong></p>
                    <p><?php echo htmlspecialchars($event['EventDate']); ?>, <?php echo htmlspecialchars($event['EventTime']); ?> - <?php echo htmlspecialchars($event['EventEnd']); ?></p>
                </div>
                <div class="location">
                    <p><strong>Location:</strong></p>
                    <p><?php echo htmlspecialchars($event['EVenue']); ?></p>
                </div>
            </div>
            <div class="event-description">
                <p><strong>Description:</strong></p>
                <p><?php echo htmlspecialchars($event['EventDesc']); ?></p>
            </div>
            <p><strong>Type:</strong> <?php echo htmlspecialchars($event['EventType']); ?></p>
            <div class="button-container">
                <a class="participants-button" href="participants.php?id=<?php echo htmlspecialchars($event['EventID']); ?>">View Participants</a>
                <a class="feedback-button" href="feedback.php?id=<?php echo htmlspecialchars($event['EventID']); ?>">View Feedback</a>
            </div>
        </div>
    </div>
</body>
</html>
