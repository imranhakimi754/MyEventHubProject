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

// Fetch all events ordered by latest
$events_sql = "SELECT EventID, EventTitle, Org_name, EventTime, EventEnd, EventDate, EventDesc, EventType, EVenue 
               FROM event 
               ORDER BY EventDate DESC, EventTime DESC";
$events_result = $conn->query($events_sql);

$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Events</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 0;
            display: flex;
        }

        .sidebar {
            width: 250px;
            background-color: #333;
            padding-top: 20px;
            color: white;
            position: fixed;
            height: 100%;
        }

        .sidebar h2 {
            margin: 0;
            padding: 20px 0;
            font-size: 24px;
            text-align: center;
        }

        .sidebar a {
            padding: 15px;
            text-decoration: none;
            font-size: 18px;
            color: white;
            display: block;
            cursor: pointer;
        }

        .sidebar a:hover {
            background-color: #575757;
        }

        .sidebar .sub-menu {
            display: none;
            padding-left: 20px;
        }

        .sidebar .sub-menu a {
            font-size: 16px;
        }

        .sidebar a.active + .sub-menu {
            display: block;
        }

        .sidebar .dropdown-icon::after {
            content: '\25BC';
            float: right;
        }

        .sidebar a.active .dropdown-icon::after {
            content: '\25B2';
        }

        .content {
            margin-left: 250px;
            padding: 20px;
        }

        h1 {
            text-align: center;
            color: #333;
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
            background-color: #333;
            color: white;
        }

        .action-button {
            padding: 8px 12px;
            background-color: #007BFF;
            color: white;
            text-decoration: none;
            border-radius: 4px;
        }

        .action-button:hover {
            background-color: #0056b3;
        }
    </style>
    <script>
        function toggleSubMenu(event) {
            event.preventDefault();
            const parent = event.target.closest('a');
            parent.classList.toggle('active');
        }
    </script>
</head>
<body>
    <div class="sidebar">
        <h2>TheEventHub</h2>
        <a href="dashboard.php">Dashboard</a>
        <a href="#" onclick="toggleSubMenu(event)">
            <span class="dropdown-icon">Events</span>
        </a>
        <div class="sub-menu">
            <a href="events.php">All Events</a>
            <a href="upcoming_events.php">Upcoming Events</a>
            <a href="ongoing_events.php">Ongoing Events</a>
            <a href="completed_events.php">Completed Events</a>
        </div>
        
        
    </div>
    <div class="content">
        <h1>All Events</h1>
        <table>
            <tr>
                <th>#</th>
                <th>Title</th>
                <th>Organizer</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Date</th>
                <th>Description</th>
                <th>Type</th>
                <th>Venue</th>
                <th>Action</th>
            </tr>
            <?php
            if ($events_result->num_rows > 0) {
                $counter = 1;
                while($row = $events_result->fetch_assoc()) {
                    echo "<tr>
                            <td>{$counter}</td>
                            <td>{$row['EventTitle']}</td>
                            <td>{$row['Org_name']}</td>
                            <td>{$row['EventTime']}</td>
                            <td>{$row['EventEnd']}</td>
                            <td>{$row['EventDate']}</td>
                            <td>{$row['EventDesc']}</td>
                            <td>{$row['EventType']}</td>
                            <td>{$row['EVenue']}</td>
                            <td><a class='action-button' href='event_details.php?id={$row['EventID']}'>View</a></td>
                          </tr>";
                    $counter++;
                }
            } else {
                echo "<tr><td colspan='10'>No events found</td></tr>";
            }
            ?>
        </table>
    </div>
</body>
</html>
