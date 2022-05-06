<?php

    require_once('authentication.php');

    $servername = "localhost";
    $username = "uname";
    $password = "pass";
    $dbname = "uname_dasapp";

    // Create connection
    $conn = new mysqli($servername, $username, $password, $dbname);
    // Check connection
    if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
    }

    function buscar_id_grupo($id_user) {
        global $conn;
        $sql = "SELECT id_team from common where id = $id_user";
        $result = $conn->query($sql);
        $id = 0;
        if ($result->num_rows > 0) {
            if ($row = $result->fetch_assoc()) {
                $id = $row['id_team'];
            }
        }
        return $id;
    }

    function buscar_id_actividad($actividad) {
        global $conn;
        $sql = "SELECT id from actividad where name = '$actividad'";
        $result = $conn->query($sql);
        $id = 0;
        if ($result->num_rows > 0) {
            if ($row = $result->fetch_assoc()) {
                $id = $row['id'];
            }
        }
        return $id;
    }

    if(isset($_POST['function'])) {
        if (valid_session()) {
            $func = $_POST['function'];
            if($func === "inscribirse") {
                $actividad = $_POST['actividad'];
                // Primeramente buscamos el identificador del grupo al que pertenece este usuario
                $id_team = buscar_id_grupo($_SESSION['id']);
                // Ahora buscamos el identificador de esta actividad del
                $id_act = buscar_id_actividad($actividad);
                // Insertamos una nueva fila en la base de datos en
                $sql = "INSERT INTO participaciones VALUES('$id_act','$id_team')";
                $result = $conn->query($sql);
                http_response_code(200);
            }
        } else {
            echo "Invalid session";
            http_response_code(401);
        }
    }

    $conn->close();
?>