<?php

$username = $_REQUEST["username"];
$password = $_REQUEST["password"];


// Check for error conditions and return back sane messages and HTTP code
// so that the client knows the problem.

if(empty($username) or empty($password)) {
    header("HTTP/1.0 400 Bad Request");
    die("Username or Password cannot be blank.");
}


// Connect with the database.

$db_username = file_get_contents('./database_config.txt', NULL, NULL, 0, 11);
$db_password = file_get_contents('./database_config.txt', NULL, NULL, 0, 15);
$con = mysql_connect("localhost", $db_username, $db_password);

if(!$con) {
    header("HTTP/1.0 500 Internal Server Error");
    die('Could not connect: ' . mysql_error());
}

$db_selected = mysql_select_db("mobile_artisans", $con);

if(!$db_selected) {
    header("HTTP/1.0 500 Internal Server Error");
    die('Can\'t use mobile_artisans: ' . mysql_error());
}

if(!table_exists("user")) {
    header("HTTP/1.0 500 Internal Server Error");
    die('Oops we are not in production currently!');
}


// Authenticate the user with the stored username and password in the database

$sql = "SELECT * from user where Username='$username'";

$result = mysql_query($sql, $con);

if(!$result) {
    header("HTTP/1.0 401 Unauthorized");
    die("The username or password you entered is incorrect.");
}

$actual_password = mysql_result($result, 0, "Password");

if($actual_password != $password) {
    header("HTTP/1.0 401 Unauthorized");
    die("The username or password you entered is incorrect.");
}

mysql_close($con);

print "Success";

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
