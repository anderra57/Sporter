<?php

    require_once('authentication.php');

    $servername = "localhost";
    $username = "uname";
    $password = "passwd";
    $dbname = "das_app";

    // Create connection
    $conn = new mysqli($servername, $username, $password, $dbname);
    // Check connection
    if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
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

    function id_participantes() {
        global $conn;
        $sql = "SELECT id from common";
        $result = $conn->query($sql);
        $tokens_array = [];
        while ($row = mysqli_fetch_assoc($result)) {
            $id = $row['id'];
            // Conseguir el token de este id
            $sql_token = "SELECT token from tokens as t join common as c on t.id_user = c.id where c.id = $id";
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
            if($func === "listar") {
                if (verify_user() == 1) {
                    // Listar todas las activiadades disponibles
                    $sql = "SELECT name, description, fecha, city from actividad where active=1";
                    $result = $conn->query($sql);
                    $actividades = array();
                    while ($row = $result->fetch_assoc()) {
                        $name = $row['name'];
                        $description = $row['description'];
                        $fecha = $row['fecha'];
                        $city = $row['city'];
                        array_push($actividades,$name);
                        array_push($actividades,$description);
                        array_push($actividades,$fecha);
                        array_push($actividades,$city);
                    }
                    echo json_encode($actividades);
                
                } else {
                    echo "Access denied";
                    http_response_code(403);
                }
            
            } elseif ($func === "crear") {
                if (verify_user() == 1) {
                    // El adminsitrador ha creado una nueva actividad
                    $actividad = $_POST['actividad'];
                    $descripcion = $_POST['descripcion'];
                    $city = $_POST['city'];
                    $fecha = date('Y-m-d');

                    // Conseguir el identificador del administrador
                    $admin_id = $_SESSION['id'];
                    // Añadir a actividad nuevo registro
                    $sql = "INSERT into actividad(name, description, active, fecha, city) values('$actividad', '$descripcion', 1, '$fecha', '$city')";
                    $result = $conn->query($sql);
                    // Conseguir el identificador de la actividad creada
                    $id_act = buscar_id_actividad($actividad);
                    // Añadir registro a la tabla actividad_admin
                    $sql = "INSERT INTO actividad_admin VALUES('$id_act','$admin_id')";
                    $result = $conn->query($sql);
                    // Enviar mensaje a todos los participantes del evento
                    // Buscar los tokens de los participantes
                    $tokens = id_participantes();
                    // Preparar el mensaje para ser enviado a los participantes
                    // Preparar el mensaje para enviar
                    $msg = array(
                        'registration_ids' => $tokens,
                        'data' => array(
                            "actividad" => "$actividad"
                        ),
                        'notification' => array(
                            "body" => "Se ha creado una nueva actividad",
                            "title" => "Actividad creada",
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
?>