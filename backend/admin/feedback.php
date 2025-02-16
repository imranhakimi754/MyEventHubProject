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

// Fetch event title
$event_title_sql = "SELECT EventTitle FROM event WHERE EventID = ?";
$title_stmt = $conn->prepare($event_title_sql);
$title_stmt->bind_param("i", $event_id);
$title_stmt->execute();
$title_result = $title_stmt->get_result();
$event_title_row = $title_result->fetch_assoc();
$event_title = $event_title_row['EventTitle'];
$title_stmt->close();

// Fetch feedback for the event, including user names
$feedback_sql = "SELECT feedback.feedback, feedback.feedbackdate, user.user_fullname 
                 FROM feedback 
                 JOIN user ON feedback.userID = user.userID 
                 WHERE feedback.eventID = ?";
$feedback_stmt = $conn->prepare($feedback_sql);
$feedback_stmt->bind_param("i", $event_id);
$feedback_stmt->execute();
$feedback_result = $feedback_stmt->get_result();

$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Feedback for <?php echo htmlspecialchars($event_title); ?></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }

        .container {
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            margin: 20px auto;
            padding: 20px;
            max-width: 1200px;
        }

        h1 {
            margin-top: 0;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        table, th, td {
            border: 1px solid #ddd;
        }

        th, td {
            padding: 15px;
            text-align: left;
        }

        th {
            background-color: #4CAF50; /* Green background color for header */
            color: white; /* White text color for header */
        }

        .no-feedback {
            text-align: center;
            margin-top: 20px;
            font-size: 18px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Feedback for <?php echo htmlspecialchars($event_title); ?></h1>
        <?php if ($feedback_result->num_rows > 0) { ?>
        <table>
            <tr>
                <th>User</th>
                <th>Feedback</th>
                <th>Date</th>
            </tr>
            <?php while ($row = $feedback_result->fetch_assoc()) { ?>
            <tr>
                <td><?php echo htmlspecialchars($row['user_fullname']); ?></td>
                <td><?php echo htmlspecialchars($row['feedback']); ?></td>
                <td><?php echo htmlspecialchars($row['feedbackdate']); ?></td>
            </tr>
            <?php } ?>
        </table>
        <?php } else { ?>
        <p class="no-feedback">No feedback found for this event.</p>
        <?php } ?>
    </div>
</body>
</html>
