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

// Fetch all users
$faculty_filter = isset($_GET['faculty']) ? $_GET['faculty'] : '';
if ($faculty_filter) {
    if ($faculty_filter === 'all') {
        $users_sql = "SELECT studentid, user_fullname, user_email, faculty, phone_num, profile_image FROM user";
    } else {
        $users_sql = "SELECT studentid, user_fullname, user_email, faculty, phone_num, profile_image FROM user WHERE faculty = '$faculty_filter'";
    }
} else {
    $users_sql = "SELECT studentid, user_fullname, user_email, faculty, phone_num, profile_image FROM user";
}
$users_result = $conn->query($users_sql);

$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Users</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
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
            width: 100%;
        }

        h1 {
            text-align: center;
            color: #333;
        }

        .faculty-dropdown {
            float: right;
            margin-bottom: 20px;
        }

        .faculty-dropdown select {
            padding: 10px;
            font-size: 16px;
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
            vertical-align: middle;
        }

        th {
            background-color: #333;
            color: white;
        }

        td.center {
            text-align: center;
            vertical-align: middle;
        }

        .profile-image {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            object-fit: cover;
        }

        .default-profile-icon {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background-color: #ddd;
            display: flex;
            justify-content: center;
            align-items: center;
            margin: 0 auto;
        }

        .action-button {
            padding: 8px 12px;
            background-color: #007BFF;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            cursor: pointer;
        }

        .action-button:hover {
            background-color: #0056b3;
        }

        .modal {
        display: none;
        position: fixed;
        z-index: 1;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        overflow: auto;
        background-color: rgba(0, 0, 0, 0.4);
        padding-top: 60px;
    }

    .modal-content {
        background-color: #fefefe;
        margin: 0 auto; /* Center horizontally */
        padding: 20px;
        border: 1px solid #888;
        width: 80%;
        max-width: 600px; /* Example: restrict maximum width for better presentation */
        position: relative;
        top: 50%;
        transform: translateY(-50%); /* Center vertically */
    }

    .close {
        color: #aaa;
        float: right;
        font-size: 28px;
        font-weight: bold;
    }

    .close:hover,
    .close:focus {
        color: black;
        text-decoration: none;
        cursor: pointer;
    }

    .form-group {
        margin-bottom: 15px;
    }

    .form-group label {
        display: block;
        margin-bottom: 5px;
    }

    .form-group input,
    .form-group select {
        width: calc(100% - 16px); /* Adjust width to match other inputs */
        padding: 8px;
        box-sizing: border-box;
    }

    .form-group input[type="file"] {
        padding: 3px;
    }
    </style>
    <script>
        function toggleSubMenu(event) {
            event.preventDefault();
            const parent = event.target.closest('a');
            parent.classList.toggle('active');
        }

        function filterByFaculty() {
            const faculty = document.getElementById('faculty').value;
            window.location.href = `?faculty=${faculty}`;
        }

        document.addEventListener('DOMContentLoaded', function() {
            var modal = document.getElementById("addStaffModal");
            var btn = document.getElementById("addStaffBtn");
            var span = document.getElementsByClassName("close")[0];

            btn.onclick = function() {
                modal.style.display = "block";
            }

            span.onclick = function() {
                modal.style.display = "none";
            }

            window.onclick = function(event) {
                if (event.target == modal) {
                    modal.style.display = "none";
                }
            }
        });
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
        <a href="users.php">Manage User</a>
        <a href="#">Statistics</a>
    </div>
    <div class="content">
        <h1>Registered Users</h1>
        <button id="addStaffBtn" class="action-button">Add Staff</button>
        <div class="faculty-dropdown">
            <label for="faculty">Filter by Faculty: </label>
            <select id="faculty" name="faculty" onchange="filterByFaculty()">
                <option value="all">All Students</option>
                <option value="Faculty of Accountancy" <?php if($faculty_filter == "Faculty of Accountancy") echo "selected"; ?>>Faculty of Accountancy</option>
                <option value="Faculty of Administrative Science and Policy Studies" <?php if($faculty_filter == "Faculty of Administrative Science and Policy Studies") echo "selected"; ?>>Faculty of Administrative Science and Policy Studies</option>
                <option value="College of Creative Arts" <?php if($faculty_filter == "College of Creative Arts") echo "selected"; ?>>College of Creative Arts</option>
                <option value="Faculty of Business And Management" <?php if($faculty_filter == "Faculty of Business And Management") echo "selected"; ?>>Faculty of Business And Management</option>
                <option value="Faculty of Computer and Mathematical Sciences" <?php if($faculty_filter == "Faculty of Computer and Mathematical Sciences") echo "selected"; ?>>Faculty of Computer and Mathematical Sciences</option>
                <option value="Faculty of Information Management" <?php if($faculty_filter == "Faculty of Information Management") echo "selected"; ?>>Faculty of Information Management</option>
            </select>
        </div>
        <table>
            <tr>
                <th>#</th>
                <th>Profile Image</th>
                <th>Student ID</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Faculty</th>
                <th>Phone Number</th>
            </tr>
            <?php
            if ($users_result->num_rows > 0) {
                $counter = 1;
                while($row = $users_result->fetch_assoc()) {
                    echo "<tr>
                            <td>{$counter}</td>
                            <td class='center'>";
                    if ($row['profile_image']) {
                        echo "<img src='../{$row['profile_image']}' alt='Profile Image' class='profile-image'>";
                    } else {
                        echo "<div class='default-profile-icon'><i class='fas fa-user'></i></div>";
                    }
                    echo "</td>
                            <td>{$row['studentid']}</td>
                            <td>{$row['user_fullname']}</td>
                            <td>{$row['user_email']}</td>
                            <td>{$row['faculty']}</td>
                            <td>{$row['phone_num']}</td>
                          </tr>";
                    $counter++;
                }
            } else {
                echo "<tr><td colspan='7'>No users found</td></tr>";
            }
            ?>
        </table>
    </div>

    <!-- Modal for adding staff -->
    <div id="addStaffModal" class="modal">
    <div class="modal-content">
        <span class="close">&times;</span>
        <h2>Add Staff</h2>
        <form action="add_user.php" method="POST" enctype="multipart/form-data">
            <div class="form-group">
                <label for="studentid">Student ID:</label>
                <input type="text" id="studentid" name="studentid" required>
            </div>
            <div class="form-group">
                <label for="user_fullname">Full Name:</label>
                <input type="text" id="user_fullname" name="user_fullname" required>
            </div>
            <div class="form-group">
                <label for="user_email">Email:</label>
                <input type="email" id="user_email" name="user_email" required>
            </div>
            <div class="form-group">
                <label for="faculty">Faculty:</label>
                <select id="faculty" name="faculty" required>
                    <option value="" disabled selected>Select Faculty</option>
                    <option value="Faculty of Accountancy">Faculty of Accountancy</option>
                    <option value="Faculty of Administrative Science and Policy Studies">Faculty of Administrative Science and Policy Studies</option>
                    <option value="College of Creative Arts">College of Creative Arts</option>
                    <option value="Faculty of Business And Management">Faculty of Business And Management</option>
                    <option value="Faculty of Computer and Mathematical Sciences">Faculty of Computer and Mathematical Sciences</option>
                    <option value="Faculty of Information Management">Faculty of Information Management</option>
                </select>
            </div>
            <div class="form-group">
                <label for="phone_num">Phone Number:</label>
                <input type="text" id="phone_num" name="phone_num" required>
            </div>
            <div class="form-group">
                <label for="profile_image">Profile Image:</label>
                <input type="file" id="profile_image" name="profile_image">
            </div>
            <button type="submit" class="action-button">Add Staff</button>
        </form>
    </div>
</div>
</body>
</html>