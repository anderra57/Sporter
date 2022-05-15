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
                    $sql = "SELECT name, description, fecha, city, imageName, latitude, longitude from actividad where active=1";
                    $result = $conn->query($sql);
                    $actividades = array();
                    while ($row = $result->fetch_assoc()) {
                        $actividad = array();
                        $actividad['name'] = $row['name'];
                        $actividad['description'] = $row['description'];
                        $actividad['fecha'] = $row['fecha'];
                        $actividad['city'] = $row['city'];
                        $actividad['imageName'] = $row['imageName'];
                        $actividad['latitude'] = $row['latitude'];
                        $actividad['longitude'] = $row['longitude'];
                        array_push($actividades, $actividad);
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
                    $descripcion = $_POST['description'];
                    $city = $_POST['city'];
                    $fecha = date('Y-m-d');

                    // Comprobar si la actividad ya existe
                    $s = "SELECT id from actividad where name = '$actividad'";
                    $result = $conn->query($s);
                    if ($result->num_rows > 0) {
                        http_response_code(400);
                    } else {
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
                                "function" => "crear",
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
                    }
                
                } else {
                    echo "Access denied";
                    http_response_code(403);
                }

            } elseif ($func === "borrar") {
                if (verify_user() == 1) {
                    // El adminsitrador ha borrado una actividad
                    $actividad = $_POST['actividad'];

                    // Conseguir el identificador de la actividad a borrar
                    $id_act = buscar_id_actividad($actividad);


                    // Borrar el registro de la tabla actividad_admin
                    $sql = "DELETE FROM actividad_admin WHERE id = $id_act";
                    $conn->query($sql);
                    // Borrar el registro de la tabla actividad_grupo
                    $sq = "DELETE FROM actividad_grupo WHERE id = $id_act";
                    $conn->query($sq);
                    // Borrar todos los registros de participaciones que contengan esta actividad
                    $sql2 = "DELETE FROM participaciones WHERE actividad_id = $id_act";
                    $conn->query($sql2);
                    // Borrar la actividad de la tabla actividad
                    $sql3 = "DELETE FROM actividad WHERE id = $id_act";
                    $conn->query($sql3);
                    
                    // Enviar mensaje a todos los participantes del evento
                    // Buscar los tokens de los participantes
                    $tokens = id_participantes();
                    // Preparar el mensaje para ser enviado a los participantes
                    // Preparar el mensaje para enviar
                    $msg = array(
                        'registration_ids' => $tokens,
                        'data' => array(
                            "function" => "borrar",
                            "actividad" => "$actividad"
                        ),
                        'notification' => array(
                            "body" => "La actividad $actividad ha sido borrada",
                            "title" => "Actividad borrada",
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

            } elseif ($func === "update") {
                if (verify_user() == 1) {
                    // Borrar todos los registros de la tabla actividad_admin que ya se hayan celebrado de la base de datos
                    $sql = "DELETE FROM actividad_admin WHERE id = (SELECT id from actividad where fecha <= now())";
                    $conn->query($sql);

                    // Borrar todos los registros de la tabla actividad_grupo que ya se hayan celebrado de la base de datos
                    $sql1 = "DELETE FROM actividad_grupo WHERE id = (SELECT id from actividad where fecha <= now())";
                    $conn->query($sql1);

                    // Borrar todos los registros de la tabla actividad que ya se hayan celebrado de la base de datos
                    $sql2 = "DELETE FROM actividad WHERE fecha <= now()";
                    $conn->query($sql2);
                    
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