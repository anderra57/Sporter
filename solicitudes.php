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

    function actividad_id($actividad) {
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

    function tokens_grupo($actividad) {
        global $conn;
        $sql = "SELECT token from tokens as t join common as c join teams as g join actividad_grupo as a join actividad as act on t.id_user = c.id and c.id_team = g.id and a.team_id = g.id and a.id = act.id where act.name = '$actividad'";
        echo $sql;
        $result = $conn->query($sql);
        $tokens_array = [];
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                array_push($tokens_array, $row['token']);
            }
        } else {
            echo "no existeeeeeee";
        }
        return $tokens_array;
    }

    function tokens_participantes() {
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
            $actividad = $_POST['actividad'];
            if($func === "aceptar") {
                if (verify_user() == 1) { // Verificar que es un administrador
                    // El administrador ha aceptado la solicitudud del grupo
                    
                    // Conseguir identificador de la actividad
                    $id_act = actividad_id($actividad);
                    // Cambiar valor de la columna 'activo' a 1 en actividad
                    $sql = "UPDATE actividad set active = 1 where id = $id_act";
                    $conn->query($sql);
                    // Cambiar valor de la columna 'aceptada' a 1 en actividad_grupo
                    $sql = "UPDATE actividad_grupo set aceptada = 1 where id = $id_act";
                    $conn->query($sql);
                    // Enviar mensaje a todos los participantes del grupo que han sugerido la actividad
                    $tok_team = tokens_grupo($actividad);
                    // Preparar el mensaje para enviar
                    // Preparar el mensaje para enviar
                    echo $tok_team;
                    $msg = array(
                        'registration_ids' => $tok_team,
                        'data' => array(
                            "function" => "aceptar_grupo",
                            "actividad" => "$actividad"
                        ),
                        'notification' => array(
                            "body" => "La actividad $actividad ha sido aceptada",
                            "title" => "Actividad aceptada",
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
                    // Enviar mensaje a todos los participantes informando de que hay una nueva actividad disponible
                    $tok_participantes = tokens_participantes();
                    // Preparar el mensaje para enviar
                    // Preparar el mensaje para enviar
                    $msg = array(
                        'registration_ids' => $tok_participantes,
                        'data' => array(
                            "function" => "aceptar_general",
                            "actividad" => "$actividad"
                        ),
                        'notification' => array(
                            "body" => "Se ha creado nueva actividad",
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
                
                } else {
                    echo "Access denied";
                    http_response_code(403);
                }

            } elseif ($func === "rechazar") {
                if (verify_user() == 1) {
                    // El administrado ha rechazado la sugerencia

                    // Conseguir identificador de la actividad
                    $id_act = actividad_id($actividad);

                    // Conseguir los tokens de los participantes
                    $tok_team = tokens_grupo($actividad);

                    // Borrar registro de la tabla actividad_grupo
                    $sql = "DELETE from actividad_grupo where id = $id_act";
                    $conn->query($sql);
                    // Borrar el registro de la actividad
                    $sql = "DELETE from actividad where id = $id_act";
                    $conn->query($sql);
                    // Enviar mensaje a todos los participantes del grupo que han sugerido la actividad
                    
                    // Preparar el mensaje para enviar
                    // Preparar el mensaje para enviar
                    $msg = array(
                        'registration_ids' => $tok_team,
                        'data' => array(
                            "function" => "rechazar",
                            "actividad" => "$actividad"
                        ),
                        'notification' => array(
                            "body" => "La actividad $actividad ha sido rechazada",
                            "title" => "Actividad rechaza",
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