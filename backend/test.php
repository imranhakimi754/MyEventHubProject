<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Generate QR Code</title>
</head>
<body>
    <form method="post">
        <button type="submit" name="generate_qr">Generate QR Code</button>
    </form>

    <?php
    if (isset($_POST['generate_qr'])) {
        include('phpqrcode/qrlib.php');

        // Data to encode
        $data = 'Your data here';

        // Filename to save the generated QR code image
        $filename = 'qrcode.png';

        // Generate QR code and save as a PNG image
        QRcode::png($data, $filename, QR_ECLEVEL_L, 10);

        // Display the generated QR code image
        echo '<img src="'.$filename.'" />';
    }
    ?>
</body>
</html>
