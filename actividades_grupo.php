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



    if(isset($_POST['function'])) {
        if (valid_session()) {
            $function = $_POST['function'];
        
            // Mostrar las actividades del grupo (Buscar en participaciones + unir con las tablas teams y actividad)
            if($function === "mostrarActivas") {
                $id_user = $_SESSION['id'];
                // Buscar el equipo del usuario

                $sql_id_team = "SELECT id_team FROM common WHERE id = $id_user";
                $res_id_team = $conn->query($sql_id_team);
                
                if($row = mysqli_fetch_assoc($res_id_team)) {
                    $id_team = $row['id_team'];
                    // Seleccionar las actividades de grupo
                    $sql_actividades_grupo = "SELECT name, description, fecha, city from actividad join participaciones on actividad_id = id and team_id = $id_team and active = 1";
                    $res_actividades_grupo = $conn->query($sql_actividades_grupo);
                    $actividades_activas_array = [];
                    while($row = mysqli_fetch_assoc($res_actividades_grupo)){
                        $name = $row_actividad_activa['name'];
                        $description = $row_actividad_activa['description'];
                        $fecha = $row_actividad_activa['fecha'];
                        $city = $row_actividad_activa['city'];
                        array_push($actividades_activas_array, $name);
                        array_push($actividades_activas_array, $description);
                        array_push($actividades_activas_array, $fecha);
                        array_push($actividades_activas_array, $city);
                    }
                    echo json_encode($actividades_activas_array);
                    http_response_code(200);
                    
                } else {
                    echo 'El jugador no pertenece a ningún equipo';
                    http_response_code(401);
                }
        
            }elseif($function === "mostrarNoInscritos"){
                $id_user = $_SESSION['id'];
                $sql_id_team = "SELECT id_team FROM common WHERE id = $id_user";
                $res_id_team = $conn->query($sql_id_team);
                if($row = mysqli_fetch_assoc($res_id_team)) {
                    $id_team = $row['id_team'];
                // Seleccionar las actividades de grupo
                    $sql_actividades_grupo = "SELECT name, description, fecha, city from actividad as a where active = 1 except select name, description, fecha, city from actividad join participaciones as p on actividad_id = id and team_id = $id_team and active = 1";
                    $res_actividades_grupo = $conn->query($sql_actividades_grupo);
                    $array_final_no_inscritas = [];
                    while($row = mysqli_fetch_assoc($res_actividades_grupo)){
                        $name = $row_actividad_activa['name'];
                        $description = $row_actividad_activa['description'];
                        $fecha = $row_actividad_activa['fecha'];
                        $city = $row_actividad_activa['city'];
                        array_push($array_final_no_inscritas, $name);
                        array_push($array_final_no_inscritas, $description);
                        array_push($array_final_no_inscritas, $fecha);
                        array_push($array_final_no_inscritas, $city);
                    }
                    echo json_encode($array_final_no_inscritas);
                    http_response_code(200);
                }else{
                    echo "Ha ocurrdo un error";
                    http_response_code(500);
                }
            }
            else {
                http_response_code(500);  			
            }
        } else {
            echo "Invalid session";
            http_response_code(401);
        }

    } 
	else {
		http_response_code(500);

    }
?>