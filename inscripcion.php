<?php
    $servername = "localhost";
    $username = "user";
    $password = "pass";
    $dbname = "das_app";

    // Create connection
    $conn = new mysqli($servername, $username, $password, $dbname);
    // Check connection
    if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
    }

    function buscar_id_grupo($usuario) {
        global $conn;
        $sql = "SELECT id_team from common as c join users as u on c.id = u.id where u.username = '$usuario'";
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
        $func = $_POST['function'];
        if($func === "inscribirse") {
            $usuario = $_POST['usuario'];
            $actividad = $_POST['actividad'];
            // Primeramente buscamos el identificador del grupo al que pertenece este usuario
            $id_team = buscar_id_grupo($usuario);
            // Ahora buscamos el identificador de esta actividad del
            $id_act = buscar_id_actividad($actividad);
            // Insertamos una nueva fila en la base de datos en
            $sql = "INSERT INTO participaciones VALUES('$id_act','$id_team')";
            $result = $conn->query($sql);
            http_response_code(200);
        }
    }

    $conn->close();
?>