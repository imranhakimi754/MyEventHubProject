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

// Fetch total events
$event_sql = "SELECT COUNT(*) as total_events FROM event";
$event_result = $conn->query($event_sql);
$event_row = $event_result->fetch_assoc();
$total_events = $event_row['total_events'];

// Fetch event statuses
$event_status_sql = "
    SELECT 
        CASE 
            WHEN e.EventDate > CURDATE() OR (e.EventDate = CURDATE() AND e.EventTime > CURTIME()) THEN 'upcoming'
            WHEN e.EventDate = CURDATE() AND CURTIME() BETWEEN e.EventTime AND e.EventEnd THEN 'ongoing'
            ELSE 'past'
        END as eventStatus,
        COUNT(*) as eventCount
    FROM event e
    GROUP BY eventStatus";

$event_status_result = $conn->query($event_status_sql);
$upcoming_events = $ongoing_events = $completed_events = 0;

while ($row = $event_status_result->fetch_assoc()) {
    if ($row['eventStatus'] == 'upcoming') {
        $upcoming_events = $row['eventCount'];
    } elseif ($row['eventStatus'] == 'ongoing') {
        $ongoing_events = $row['eventCount'];
    } elseif ($row['eventStatus'] == 'past') {
        $completed_events = $row['eventCount'];
    }
}

// Fetch event types for pie chart
$event_type_sql = "SELECT EventType, COUNT(*) as eventCount FROM event GROUP BY EventType";
$event_type_result = $conn->query($event_type_sql);
$event_types = [];
$event_type_counts = [];

while ($row = $event_type_result->fetch_assoc()) {
    $event_types[] = $row['EventType'];
    $event_type_counts[] = $row['eventCount'];
}

// Fetch events by month for bar graph
$event_month_sql = "SELECT MONTHNAME(EventDate) as month, COUNT(*) as eventCount FROM event GROUP BY MONTH(EventDate)";
$event_month_result = $conn->query($event_month_sql);
$event_months = [];
$event_month_counts = [];

while ($row = $event_month_result->fetch_assoc()) {
    $event_months[] = $row['month'];
    $event_month_counts[] = $row['eventCount'];
}

$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
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
            width: calc(100% - 250px);
            position: relative;
        }

        h1 {
            text-align: left;
            color: #333;
        }

        .stats {
            display: flex;
            justify-content: space-around;
            margin-top: 20px;
        }

        .stats a {
            text-decoration: none;
            color: inherit;
        }

        .stats div {
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            text-align: center;
            margin: 10px;
            flex: 1;
            min-width: 200px;
            transition: transform 0.2s;
        }

        .stats div:hover {
            transform: scale(1.05);
        }

        .stats .total-events {
            background-color: #ffcccc;
        }

        .stats .upcoming-events {
            background-color: #ccffcc;
        }

        .stats .ongoing-events {
            background-color: #ccccff;
        }

        .stats .completed-events {
            background-color: #ffffcc;
        }

        .logout-form {
            position: absolute;
            top: 20px;
            right: 20px;
        }

        .logout-form button {
            padding: 10px 20px;
            background-color: #d9534f;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }

        .logout-form button:hover {
            background-color: #c9302c;
        }

        .search-bar {
            text-align: center;
            margin: 20px 0;
        }

        .search-bar input[type="text"] {
            width: 50%;
            padding: 10px;
            font-size: 16px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }

        .search-bar button {
            padding: 10px 20px;
            font-size: 16px;
            background-color: #5bc0de;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }

        .search-bar button:hover {
            background-color: #31b0d5;
        }

        .charts-container {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            margin-top: 20px;
        }

        .chart {
            flex: 1;
            min-width: calc(50% - 75px);
            max-width: calc(50% - 75px);
            background: white;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            padding: 20px;
        }
    </style>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chartjs-plugin-datalabels"></script>
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
    <div class="logout-form">
        <form method="POST" action="logout.php">
            <button type="submit">Logout</button>
        </form>
    </div>
    <div class="search-bar">
        <input type="text" placeholder="Search events...">
        <button>Search</button>
    </div>
    <h1>Dashboard</h1>
    <div class="stats">
        <a href="events.php">
            <div class="total-events">
                <h3>Total Events</h3>
                <p><?php echo $total_events; ?></p>
            </div>
        </a>
        <a href="upcoming_events.php">
            <div class="upcoming-events">
                <h3>Upcoming Events</h3>
                <p><?php echo $upcoming_events; ?></p>
            </div>
        </a>
        <a href="ongoing_events.php">
            <div class="ongoing-events">
                <h3>Ongoing Events</h3>
                <p><?php echo $ongoing_events; ?></p>
            </div>
        </a>
        <a href="completed_events.php">
            <div class="completed-events">
                <h3>Completed Events</h3>
                <p><?php echo $completed_events; ?></p>
            </div>
        </a>
    </div>
    <div class="charts-container">
        <div class="chart">
            <canvas id="eventTypeChart"></canvas>
        </div>
        <div class="chart">
            <canvas id="eventMonthChart"></canvas>
        </div>
    </div>
</div>

<script>
    // Pie chart data and config
    const eventTypeCtx = document.getElementById('eventTypeChart').getContext('2d');
    const eventTypeChart = new Chart(eventTypeCtx, {
        type: 'pie',
        data: {
            labels: <?php echo json_encode($event_types); ?>,
            datasets: [{
                label: 'Event Types',
                data: <?php echo json_encode($event_type_counts); ?>,
                backgroundColor: [
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(54, 162, 235, 0.2)',
                    'rgba(255, 206, 86, 0.2)',
                    'rgba(75, 192, 192, 0.2)',
                    'rgba(153, 102, 255, 0.2)',
                    'rgba(255, 159, 64, 0.2)'
                ],
                borderColor: [
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 159, 64, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                },
                title: {
                    display: true,
                    text: 'Event Categories'
                },
                datalabels: {
                    formatter: (value, ctx) => {
                        let sum = ctx.chart.data.datasets[0].data.reduce((a, b) => a + b, 0);
                        let percentage = (value * 100 / sum).toFixed(2) + "%";
                        return percentage;
                    },
                    color: '#fff',
                    display: 'auto',
                    font: {
                        weight: 'bold'
                    }
                }
            }
        }
    });

    // Bar chart data and config
    const eventMonthCtx = document.getElementById('eventMonthChart').getContext('2d');
    const eventMonthChart = new Chart(eventMonthCtx, {
        type: 'bar',
        data: {
            labels: <?php echo json_encode($event_months); ?>,
            datasets: [{
                label: 'Events per Month',
                data: <?php echo json_encode($event_month_counts); ?>,
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                },
                title: {
                    display: true,
                    text: 'Events per Month'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    function toggleSubMenu(event) {
        event.preventDefault();
        const subMenu = event.target.nextElementSibling;
        if (subMenu.style.display === "block") {
            subMenu.style.display = "none";
        } else {
            subMenu.style.display = "block";
        }
    }
</script>
</body>
</html>
