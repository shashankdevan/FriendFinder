<?php

$username = $_REQUEST["username"];
$password = $_REQUEST["password"];

$db_username = file_get_contents('./database_config.txt', NULL, NULL, 0, 11);
$db_password = file_get_contents('./database_config.txt', NULL, NULL, 0, 15);
$con = mysql_connect("localhost", $db_username, $db_password);

if(!$con) {
    die('Could not connect: ' . mysql_error());
}

$db_selected = mysql_select_db("mobile_artisans", $con);

if(!$db_selected) {
    die('Can\'t use mobile_artisans: ' . mysql_error());
}

if(!table_exists("user")) {

    $sql = "CREATE TABLE user (
            Username VARCHAR(32),
            PRIMARY KEY(Username),
            Password VARCHAR(32)
    )";

    echo $sql;
    $result = mysql_query($sql, $con);
    if(!$result) {
        $msg = 'Invalid query: ' . mysql_error() . "\n";
        $msg .= 'Whole Query: ' . $sql;
    }

}

$sql = "INSERT INTO user (Username, Password) VALUES ('$username', '$password')";

$result = mysql_query($sql, $con);

if(!$result) {
    echo "Error in inserting elements to the table: user " . mysql_error() . "\n";
}

mysql_close($con);

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
