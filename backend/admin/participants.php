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

// Get EventID and status from URL
$event_id = isset($_GET['id']) ? $_GET['id'] : 0;
$status = isset($_GET['status']) ? $_GET['status'] : '';
$sort = isset($_GET['sort']) ? $_GET['sort'] : '';
$filter_status = isset($_GET['filter_status']) ? $_GET['filter_status'] : '';

// Fetch event title
$event_title_sql = "SELECT EventTitle FROM event WHERE EventID = ?";
$title_stmt = $conn->prepare($event_title_sql);
$title_stmt->bind_param("i", $event_id);
$title_stmt->execute();
$title_result = $title_stmt->get_result();
$event_title_row = $title_result->fetch_assoc();
$event_title = $event_title_row['EventTitle'];
$title_stmt->close();

// Determine the table to fetch data from based on event status
if ($status === 'upcoming') {
    $sql = "SELECT u.user_fullname, u.user_email, u.faculty, u.phone_num
            FROM booking b
            JOIN user u ON b.userId = u.userId
            WHERE b.eventID = ?";
} else { // ongoing and completed
    $sql = "SELECT u.user_fullname, u.user_email, u.faculty, u.phone_num, a.status
            FROM attendance a
            JOIN user u ON a.userId = u.userId
            WHERE a.eventID = ?";
    // Add filtering by status
    if ($filter_status && $filter_status !== 'all') {
        $sql .= " AND a.status = ?";
    }
    // Add sorting by status
    if ($sort === 'status') {
        $sql .= " ORDER BY a.status";
    }
}

$stmt = $conn->prepare($sql);

if ($filter_status && $filter_status !== 'all') {
    $stmt->bind_param("is", $event_id, $filter_status);
} else {
    $stmt->bind_param("i", $event_id);
}

$stmt->execute();
$result = $stmt->get_result();

$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Participants of <?php echo htmlspecialchars($event_title); ?></title>
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

        .icon {
            display: inline-block;
            width: 20px;
            height: 20px;
            border-radius: 50%;
            text-align: center;
            color: white;
            line-height: 20px;
        }

        .icon.present {
            background-color: green;
        }

        .icon.absent {
            background-color: red;
        }

        td.status {
            text-align: center;
        }

        .sort-dropdown {
            margin-bottom: 20px;
        }

        select {
            padding: 10px;
            font-size: 16px;
            border-radius: 4px;
            border: 1px solid #ddd;
        }

        .no-participants {
            text-align: center;
            margin-top: 20px;
            font-size: 18px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Participants of <?php echo htmlspecialchars($event_title); ?></h1>
        <div class="sort-dropdown">
            <label for="sort">Sort by status: </label>
            <select id="sort" onchange="updateURL()">
                <option value="all" <?php echo $filter_status === 'all' ? 'selected' : ''; ?>>All</option>
                <option value="present" <?php echo $filter_status === 'present' ? 'selected' : ''; ?>>Present</option>
                <option value="absent" <?php echo $filter_status === 'absent' ? 'selected' : ''; ?>>Absent</option>
            </select>
        </div>
        <?php if ($result->num_rows > 0) { ?>
        <table>
            <tr>
                <th>Full Name</th>
                <th>Email</th>
                <th>Faculty</th>
                <th>Phone Number</th>
                <?php if ($status !== 'upcoming') echo '<th>Status</th>'; ?>
            </tr>
            <?php while ($row = $result->fetch_assoc()) { ?>
            <tr>
                <td><?php echo htmlspecialchars($row['user_fullname']); ?></td>
                <td><?php echo htmlspecialchars($row['user_email']); ?></td>
                <td><?php echo htmlspecialchars($row['faculty']); ?></td>
                <td><?php echo htmlspecialchars($row['phone_num']); ?></td>
                <?php if ($status !== 'upcoming') { ?>
                <td class="status">
                    <?php if ($row['status'] === 'present') { ?>
                        <span class="icon present">&#10004;</span>
                    <?php } else { ?>
                        <span class="icon absent">&#10008;</span>
                    <?php } ?>
                </td>
                <?php } ?>
            </tr>
            <?php } ?>
        </table>
        <?php } else { ?>
        <p class="no-participants">No participants found for this event.</p>
        <?php } ?>
    </div>

    <script>
        function updateURL() {
            const sort = document.getElementById('sort').value;
            const urlParams = new URLSearchParams(window.location.search);
            urlParams.set('filter_status', sort);
            window.location.search = urlParams.toString();
        }
    </script>
</body>
</html>
