<?php

    session_start();

    $servername = "localhost";
    $username = "uname";
    $password = "passwd";
	$dbname = "uname_dasapp";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	// Check connection
	if ($conn->connect_error) {
	    die("Connection failed: " . $conn->connect_error);
	}

    function verify_user() {
        global $conn;
        if (isset($_SESSION['id']) && $_SESSION['id'] != '') {
            $id_user = $_SESSION['id'];
            // Comprobar si es un usuario normal o un administrador
            $sql = "SELECT isAdmin from users where id = $id_user";
            $result = $conn->query($sql);
            if ($result->num_rows > 0) {
                if ($row = $result->fetch_assoc()) {
                    return $row['isAdmin'];
                } else {
                    die("This session is invalid");
                }
            } else {
                die("This session is invalid");
            }
        } else {
            die("This session is invalid");
        }
    }

    function valid_session() {
        if (isset($_SESSION['id']) && $_SESSION['id'] != '') {
            return true;
        } else {
            return false;
        }
    }

?>