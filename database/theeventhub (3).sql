-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 16, 2025 at 09:10 AM
-- Server version: 10.4.27-MariaDB
-- PHP Version: 8.2.0

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `theeventhub`
--

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE `admins` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admins`
--

INSERT INTO `admins` (`id`, `username`, `password`) VALUES
(1, 'admin', 'admin123');

-- --------------------------------------------------------

--
-- Table structure for table `attendance`
--

CREATE TABLE `attendance` (
  `attendanceID` int(11) NOT NULL,
  `EventID` int(11) NOT NULL,
  `userID` int(11) NOT NULL,
  `status` varchar(50) DEFAULT 'absent',
  `timestamp` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `attendance`
--

INSERT INTO `attendance` (`attendanceID`, `EventID`, `userID`, `status`, `timestamp`) VALUES
(6, 24, 6, 'absent', '00:00:00'),
(10, 29, 6, 'absent', '00:00:00'),
(11, 33, 6, 'absent', '00:00:00'),
(18, 31, 6, 'absent', '00:00:00'),
(19, 35, 6, 'absent', '00:00:00'),
(20, 35, 10, 'absent', '00:00:00'),
(21, 35, 11, 'absent', '00:00:00'),
(25, 61, 20, 'absent', '00:00:00'),
(26, 61, 21, 'absent', '00:00:00'),
(30, 64, 24, 'present', '05:37:02'),
(31, 64, 25, 'present', '00:00:00'),
(35, 73, 25, 'present', '00:06:22'),
(36, 83, 32, 'present', '09:22:42'),
(37, 84, 32, 'present', '00:00:00'),
(38, 85, 31, 'absent', '00:00:00'),
(39, 86, 33, 'present', '00:27:35');

-- --------------------------------------------------------

--
-- Table structure for table `booking`
--

CREATE TABLE `booking` (
  `bookingID` int(255) NOT NULL,
  `EventID` int(255) NOT NULL,
  `userID` int(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `booking`
--

INSERT INTO `booking` (`bookingID`, `EventID`, `userID`) VALUES
(12, 24, 6),
(16, 29, 6),
(17, 33, 6),
(24, 31, 6),
(25, 35, 6),
(26, 35, 10),
(27, 35, 11),
(31, 61, 20),
(32, 61, 21),
(36, 64, 24),
(37, 64, 25),
(41, 73, 25),
(42, 83, 32),
(43, 84, 32),
(44, 85, 31),
(45, 86, 33);

-- --------------------------------------------------------

--
-- Table structure for table `event`
--

CREATE TABLE `event` (
  `EventID` int(11) NOT NULL,
  `EventTitle` varchar(255) NOT NULL,
  `Org_name` varchar(255) NOT NULL,
  `EventTime` time NOT NULL,
  `EventEnd` time NOT NULL,
  `EventDate` date NOT NULL,
  `EventDesc` varchar(255) NOT NULL,
  `EventType` varchar(255) NOT NULL,
  `EVenue` varchar(255) NOT NULL,
  `image` varchar(255) NOT NULL,
  `userID` int(11) DEFAULT NULL,
  `qrcode` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `event`
--

INSERT INTO `event` (`EventID`, `EventTitle`, `Org_name`, `EventTime`, `EventEnd`, `EventDate`, `EventDesc`, `EventType`, `EVenue`, `image`, `userID`, `qrcode`) VALUES
(24, 'Majlis Berbuka Puasa', 'MUHAMMAD IMRAN HAKIMI BIN RAZALI', '19:30:00', '23:59:00', '2024-06-23', 'Majlis Berbuka Puasa untuk semua pelajar CS240', 'Social', 'Dewan Pro', 'uploads/66780234230bd.jpg', 2, 'uploads/QRCode_24.png'),
(29, 'Bacaan Yasin', 'MUHAMMAD IMRAN HAKIMI BIN RAZALI', '00:12:00', '23:59:00', '2024-06-27', 'Come join us', 'Others', 'AU300', 'uploads/667aec6a0f9e1.jpg', 2, 'uploads/QRCode_29.png'),
(31, 'Art Class', 'MUHAMMAD IMRAN HAKIMI BIN RAZALI', '07:00:00', '23:59:00', '2024-07-02', 'Come on join us!', 'Social', 'Dewan Pro', 'uploads/668236ba4d169.jpg', 2, 'uploads/QRCode_31.png'),
(33, 'Mobile Legend Tournament', 'MUHAMMAD IMRAN HAKIMI BIN RAZALI', '13:15:00', '23:59:00', '2024-07-01', 'Come on join us', 'Sport', 'Dewan Pro', 'uploads/66823799dd033.jpg', 2, 'uploads/QRCode_33.png'),
(35, 'Mandarin Tutorial', 'MUHAMMAD IMRAN HAKIMI BIN RAZALI', '10:00:00', '23:59:00', '2024-07-02', 'Come on join us', 'Academic', 'AU300', 'uploads/6682387743074.jpg', 2, 'uploads/QRCode_35.png'),
(83, 'Inclusive Class', 'MUHAMMAD IMRAN HAKIMI BIN RAZALI', '08:00:00', '12:00:00', '2024-07-15', 'Join us for the inclusive Classroom Workshop, where educators will learn practical strategies for creating a supportive and inclusive learning environment.', 'Academic', 'AU200', 'uploads/6694297923c81.jpg', 31, 'uploads/QRCode_83.png'),
(84, 'Art Heals', 'MUHAMMAD IMRAN HAKIMI BIN RAZALI', '08:00:00', '12:00:00', '2024-07-01', 'Join us for \"Art Heals\" an event exploring the therapeutic power of art.', 'Social', 'AU300', 'uploads/66942e2ed4efa.jpg', 31, 'uploads/QRCode_84.png'),
(85, 'Mandarin Tutorial', 'MUHAMMAD ASYRAF', '09:00:00', '12:00:00', '2024-07-17', 'Join us', 'Academic', 'Dewan Pro', 'uploads/6694761cec25d.jpg', 33, 'uploads/QRCode_85.png'),
(86, 'ITCOMM NIGHT DINNER', 'MUHAMMAD IMRAN HAKIMI BIN RAZALI', '00:00:00', '23:59:00', '2024-07-30', 'COME ON JOIN US.!', 'Social', 'AU300', '', 34, 'uploads/QRCode_86.png');

-- --------------------------------------------------------

--
-- Table structure for table `feedback`
--

CREATE TABLE `feedback` (
  `feedbackID` int(11) NOT NULL,
  `EventID` int(11) DEFAULT NULL,
  `userID` int(11) DEFAULT NULL,
  `feedback` text DEFAULT NULL,
  `feedbackdate` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `feedback`
--

INSERT INTO `feedback` (`feedbackID`, `EventID`, `userID`, `feedback`, `feedbackdate`) VALUES
(5, 61, 21, 'good event', '2024-07-07'),
(6, 64, 24, 'good', '2024-07-12'),
(7, 64, 25, 'good', '2024-07-12'),
(8, 73, 25, 'good', '2024-07-14'),
(9, 84, 32, 'good', '2024-07-15'),
(10, 86, 33, 'good event', '2024-07-29');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `userID` int(11) NOT NULL,
  `studentid` varchar(100) NOT NULL,
  `user_fullname` varchar(100) NOT NULL,
  `user_email` varchar(100) NOT NULL,
  `faculty` varchar(100) NOT NULL,
  `phone_num` varchar(255) NOT NULL,
  `password` varchar(100) NOT NULL,
  `profile_image` varchar(255) NOT NULL,
  `verification_code` int(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`userID`, `studentid`, `user_fullname`, `user_email`, `faculty`, `phone_num`, `password`, `profile_image`, `verification_code`) VALUES
(32, '2021862594', 'MUHAMMAD FAIZ BIN ZULKIFLI', '2021862594@student.uitm.edu.my', 'College of Computing, Informatics and Mathematics', '0189072066', 'imranhakimi123', '', 0),
(33, '2021843668', 'MUHAMMAD ASYRAF', 'kucingsegar83@gmail.com', 'College of Computing, Informatics and Mathematics', '0189072066', 'imran123', '', 0),
(34, '2021823326', 'MUHAMMAD IMRAN HAKIMI BIN RAZALI', '2021823326@student.uitm.edu.my', 'College of Computing, Informatics and Mathematics', '0189072066', 'imranhakimi', '', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admins`
--
ALTER TABLE `admins`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `attendance`
--
ALTER TABLE `attendance`
  ADD PRIMARY KEY (`attendanceID`),
  ADD KEY `EventID` (`EventID`),
  ADD KEY `userID` (`userID`);

--
-- Indexes for table `booking`
--
ALTER TABLE `booking`
  ADD PRIMARY KEY (`bookingID`),
  ADD KEY `EventID` (`EventID`),
  ADD KEY `userID` (`userID`);

--
-- Indexes for table `event`
--
ALTER TABLE `event`
  ADD PRIMARY KEY (`EventID`),
  ADD KEY `userID` (`userID`);

--
-- Indexes for table `feedback`
--
ALTER TABLE `feedback`
  ADD PRIMARY KEY (`feedbackID`),
  ADD KEY `EventID` (`EventID`),
  ADD KEY `userID` (`userID`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`userID`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admins`
--
ALTER TABLE `admins`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `attendance`
--
ALTER TABLE `attendance`
  MODIFY `attendanceID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- AUTO_INCREMENT for table `booking`
--
ALTER TABLE `booking`
  MODIFY `bookingID` int(255) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- AUTO_INCREMENT for table `event`
--
ALTER TABLE `event`
  MODIFY `EventID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=87;

--
-- AUTO_INCREMENT for table `feedback`
--
ALTER TABLE `feedback`
  MODIFY `feedbackID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `userID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `attendance`
--
ALTER TABLE `attendance`
  ADD CONSTRAINT `attendance_ibfk_1` FOREIGN KEY (`EventID`) REFERENCES `event` (`EventID`),
  ADD CONSTRAINT `attendance_ibfk_2` FOREIGN KEY (`userID`) REFERENCES `user` (`userID`);

--
-- Constraints for table `booking`
--
ALTER TABLE `booking`
  ADD CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`EventID`) REFERENCES `event` (`EventID`),
  ADD CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`userID`) REFERENCES `user` (`userID`);

--
-- Constraints for table `event`
--
ALTER TABLE `event`
  ADD CONSTRAINT `event_ibfk_1` FOREIGN KEY (`userID`) REFERENCES `user` (`userID`);

--
-- Constraints for table `feedback`
--
ALTER TABLE `feedback`
  ADD CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`EventID`) REFERENCES `event` (`EventID`),
  ADD CONSTRAINT `feedback_ibfk_2` FOREIGN KEY (`userID`) REFERENCES `user` (`userID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
