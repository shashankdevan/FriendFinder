<?php

$latitude = $_REQUEST["latitude"];
$longitude = $_REQUEST["longitude"];
$time= $_REQUEST["time"];
$id = $_REQUEST["id"];

$db_username = file_get_contents('./database_config.txt', NULL, NULL, 0, 11);
$db_password = file_get_contents('./database_config.txt', NULL, NULL, 12, 15);
$con = mysql_connect("localhost", $db_username, $db_password);

if(!$con) {
    die('Could not connect: ' . mysql_error());
}

$db_selected = mysql_select_db("mobile_artisans", $con);

if(!$db_selected) {
    die('Can\'t use mobile_artisans: ' . mysql_error());
}

if(!table_exists("$id")) {

    $sql = "CREATE TABLE $id (
            Date DATETIME,
            PRIMARY KEY(Date),
            Latitude FLOAT(14,10),
            Longitude FLOAT(14,10)
    )";

    echo $sql;
    $result = mysql_query($sql, $con);
    if(!$result) {
        $msg = 'Invalid query: ' . mysql_error() . "\n";
        $msg .= 'Whole Query: ' . $sql;
    }

}

$sql = "INSERT INTO $id (Date, Latitude, Longitude) VALUES ('$time', '$latitude', '$longitude')";

$result = mysql_query($sql, $con);

if(!$result) {
    echo "Error in inserting elements to the table: $id " . mysql_error() . "\n";
}

$list = mysql_list_tables("mobile_artisans");
$i = 0;
$idarray = array();
$latarray = array();
$longarray = array();
$distarray = array();
$count = 0;

while($i < mysql_num_rows($list)) {
    $tb_names[$i] = mysql_tablename($list, $i);
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
       if($dist < 0.2) {
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

?>
