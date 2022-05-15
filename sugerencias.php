<?php

    require_once('authentication.php');

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

    $cabecera = array(
        'Authorization: key=AAAADAAVG90:APA91bE5JnH2WxZyFDXq9l5mKNz5uSE6jsJ2IBprxcKRsXo1Zkn8gSyPVFNHYGXWpZsLb08MUBQji-AKg80XB2FB3QT3TLxQDp6U347gMwj0zSoCq2-6HYC6THU3wP2pMRySor1THeGW',
        'Content-Type: application/json'             
    );

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

    function get_teamname($id_team) {
        global $conn;
        $sql = "SELECT name from teams where id = $id_team";
        $result = $conn->query($sql);
        $name = "";
        if ($result->num_rows > 0) {
            if ($row = $result->fetch_assoc()) {
                $name = $row['name'];
            }
        }
        return $name;
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

    function buscar_tokens_admins() {
        global $conn;
        $sql = "SELECT id from admins";
        $result = $conn->query($sql);
        $tokens_array = [];
        while ($row = mysqli_fetch_assoc($result)) {
            $id = $row['id'];
            // Conseguir el token de este id
            $sql_token = "SELECT token from tokens as t join admins as a on t.id_user = a.id where a.id = $id";
            $res_token = $conn->query($sql_token);
            if ($row_token = $res_token->fetch_assoc()) {
                $token = $row_token['token'];
                array_push($tokens_array, $token);
            }
        }
        return $tokens_array;
    }

    if(isset($_POST['function'])) {
        if (valid_session()) {
            $func = $_POST['function'];
            if($func === "sugerir") {
                $actividad = $_POST['actividad'];
                $descripcion = $_POST['description'];
                $city = $_POST['city'];
                $fecha = $_POST['fecha'];

                // Comprobar si la actividad ya existe
                $s = "SELECT id from actividad where name = '$actividad'";
                $result = $conn->query($s);
                if ($result->num_rows > 0) {
                    http_response_code(400);
                } else {
                    // Primeramente añadimos una nueva actividad que no estará activa
                    $sql = "INSERT into actividad(name, description, active, fecha, city) values('$actividad', '$descripcion', 0, '$fecha', '$city')";
                    $result = $conn->query($sql);
                    // Ahora añadimos una nueva fila a la tabla actividad_grupo (necesitamos saber el identificador de la actividad y del grupo al que pertenece el usuario)
                    
                    // Conseguir el identificador del grupo
                    $id_team = buscar_id_grupo($_SESSION['id']);
                    // Conseguir el identificador de la actividad
                    $id_act = buscar_id_actividad($actividad);
                    
                    // Añadir nueva fila a la tabla actividad_grupo
                    $sql = "INSERT INTO actividad_grupo VALUES('$id_act',0,'$id_team')";
                    $result = $conn->query($sql);

                    // Por último, habrá que enviar un mensaje FCM a todos los administradores
                    // Primero buscar los tokens de los administradores
                    $tokens = buscar_tokens_admins();
                    $teamname = get_teamname($id_team);
                    
                    // Preparar el mensaje para enviar
                    $msg = array(
                        'registration_ids' => $tokens,
                        'data' => array(
                            "function" => "sugerir",
                            "actividad" => "$actividad",
                            "receptor" => "$teamname",
                        ),
                        'notification' => array(
                            "body" => "El equipo $teamname ha sugeridad una actividad",
                            "title" => "Actividad sugerida",
                            "icon" => "ic_stat_ic_notification"
                        )
                    );

                    $msgJSON = json_encode($msg);
                    echo $msgJSON;

                    // Enviar el mensaje al receptor
                    $ch = curl_init(); #inicializar el handler de curl
                    #indicar el destino de la petición, el servicio FCM de google
                    curl_setopt( $ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
                    #indicar que la conexión es de tipo POST
                    curl_setopt( $ch, CURLOPT_POST, true );
                    #agregar las cabeceras
                    curl_setopt( $ch, CURLOPT_HTTPHEADER, $cabecera);
                    #Indicar que se desea recibir la respuesta a la conexión en forma de string
                    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
                    #agregar los datos de la petición en formato JSON
                    curl_setopt( $ch, CURLOPT_POSTFIELDS, $msgJSON );
                    #ejecutar la llamada
                    $resultado= curl_exec( $ch );
                    #cerrar el handler de curl
                    curl_close( $ch );

                    if(curl_errno($ch)) {
                        print curl_error($ch);
                    }
                    echo $resultado;
                    http_response_code(200);
                }
            
            } elseif ($func == "mostrarSolicitudes") {
                if (verify_user() == 1) {
                    $sql = "SELECT a.name as actividad, a.description, a.fecha, a.city, a.imageName, a.latitude, a.longitude, t.name as grupo from actividad as a join actividad_grupo as g join teams as t on a.id = g.id and t.id = g.team_id where g.aceptada = 0";
                    $result = $conn->query($sql);
                    $actividades = array();
                    while ($row = mysqli_fetch_assoc($result)) {
                        $element = array();
                        $element['actividad'] = $row['actividad'];
                        $element['description'] = $row['description'];
                        $element['fecha'] = $row['fecha'];
                        $element['city'] = $row['city'];
                        $element['imageName'] = $row['imageName'];
                        $element['latitude'] = $row['latitude'];
                        $element['longitude'] = $row['longitude'];
                        $element['grupo'] = $row['grupo'];
                        array_push($actividades, $element);
                    }

                    echo json_encode($actividades);
                
                } else {
                    echo "Access denied";
                    http_response_code(403);
                }
            }
        } else {
            echo "Invalid session";
            http_response_code(401);
        }
    }

    $conn->close();
?>
