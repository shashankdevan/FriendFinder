<?php

$database = "mobile_artisans";

$latitude = $_REQUEST["latitude"];
$longitude = $_REQUEST["longitude"];
$time= $_REQUEST["time"];
$id = $_REQUEST["id"];

if(empty($latitude) or empty($longitude)) {
    header("HTTP/1.0 400 Bad Request");
    die("Location attributes cannot be blank.");
}

if(empty($time)) {
    header("HTTP/1.0 400 Bad Request");
    die("Time cannot be blank.");
}

if(empty($id)) {
    header("HTTP/1.0 400 Bad Request");
    die("Username cannot be blank.");
}

$db_username = file_get_contents('./database_config.txt', NULL, NULL, 0, 11);
$db_password = file_get_contents('./database_config.txt', NULL, NULL, 0, 15);
$con = mysql_connect("localhost", $db_username, $db_password);

if(!$con) {
    header("HTTP/1.0 500 Internal Server Error");
    die('Could not connect: ' . mysql_error());
}

$db_selected = mysql_select_db($database, $con);

if(!$db_selected) {
    header("HTTP/1.0 500 Internal Server Error");
    die("Can\'t use '$database': " . mysql_error());
}

if(!table_exists("$id")) {
    header("HTTP/1.0 400 Bad Request");
    die("User does not exist.");
}

$sql = "INSERT INTO $id (Date, Latitude, Longitude) VALUES ('$time', '$latitude', '$longitude')";

$result = mysql_query($sql, $con);

if(!$result) {
    header("HTTP/1.0 400 Bad Request");
    die("Only one location update is allowed at a given time.");
}

$list = mysql_list_tables($database);
$i = 0;
$idarray = array();
$latarray = array();
$longarray = array();
$distarray = array();
$count = 0;

while($i < mysql_num_rows($list)) {
    $tb_names[$i] = mysql_tablename($list, $i);
    if($tb_names[$i] == "user" or $tb_names[$i] == $id) {
        $i++;
        continue;
    }
    $sql = "SELECT * FROM $tb_names[$i] order by Date desc limit 1";
    $result = mysql_query($sql, $con);
    $num = mysql_numrows($result);
    $j = 0;

    while($j < $num) {
        $fielddate = mysql_result($result, $j, "Date");
        $fieldlatitude = mysql_result($result, $j, "Latitude");
        $fieldlongitude = mysql_result($result, $j, "Longitude");
        $phpdate = strtotime($fielddate);
        $dist = distance($fieldlatitude, $fieldlongitude, $latitude, $longitude);
        if($dist < 2) {
            $idarray[] = $tb_names[$i];
            $latarray[] = $fieldlatitude;
            $longarray[] = $fieldlongitude;
            $distarray[] = $dist;
            $count++;
        }
        $j++;
    }
    $i++;
}

for ($i = 0; $i < $count; $i++) {
    print "$idarray[$i],";
    print "$latarray[$i],";
    print "$longarray[$i],";
    print "$distarray[$i]";
    print "\n";
}

mysql_close($con);

function distance($lat1, $long1, $lat2, $long2) {
    $theta = $lon1 - $lon2;
    $dist = sin(deg2rad($lat1)) * sin(deg2rad($lat2)) +  cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * cos(deg2rad($theta));
    $dist = acos($dist);
    return ($dist * 6371);
}

function table_exists($tablename, $database = false) {
    if(!$database) {
        $res = mysql_query("SELECT DATABASE()");
        $database = mysql_result($res, 0);
    }

    $res = mysql_query("
        SELECT COUNT(*) AS count
        FROM information_schema.tables
        WHERE table_schema = '$database'
        AND table_name = '$tablename'
    ");

    return mysql_result($res, 0) == 1;
}

function send_notification($reg_ids, $message) {
    $url = 'https://android.googleapis.com/gcm/send';

    $fields = array(
        'registration_ids' => $reg_ids,
        'data' => $message,
    );

    $headers = array(
        'Authorization: key=AIzaSyAJFQyyTTShpeQ4lroqa-Ur27QBcJ3UtDg',
        'Content-Type: application/json'
    );

    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, $url);

    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

    $result = curl_exec($ch);
    if ($result === FALSE) {
        die('Curl failed: ' . curl_error($ch));
    }

    curl_close($ch);
}

?>
