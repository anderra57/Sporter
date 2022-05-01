<?php

	$DB_SERVER="localhost"; #la direccion del servidor
	$DB_USER="Xahernandez141"; #el usuario para la base de datos
	$DB_PASS="***"; #la clave para este usuario
	$DB_DATABASE="Xahernandez141_prueba"; #la base de datos a la que hay que conectarse
	
    $mysqli = new mysqli($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);
    if ($mysqli->connect_errno) {
        echo "Se ha producido un error al conectarse a MySQL: (" . $mysqli->connect_errno . ") ";
    }

    $mysqli->set_charset("utf8");
	


    if(isset($_POST['function'])) {
        $function = $_POST['function'];
	
		// Mostrar las actividades del grupo (Buscar en participaciones + unir con las tablas teams y actividad)
		if($function === "mostrarActivas") {
			$username = $_POST['username'];
			// Buscar el equipo del usuario

            $sql_id_team = "SELECT id_team FROM common WHERE id = (SELECT id FROM users WHERE username = '$username')";
            $res_id_team = $mysqli->query($sql_id_team);
			
			if($row = mysqli_fetch_assoc($res_id_team)) {
				$id_team = $row['id_team'];
				// Seleccionar las actividades de grupo
				$sql_actividades_grupo = "SELECT actividad_id FROM participaciones WHERE team_id = $id_team";
                $res_actividades_grupo = $mysqli->query($sql_actividades_grupo);
                $actividades_activas_array = [];
                while($row = mysqli_fetch_assoc($res_actividades_grupo)){
                    $id_actividad = $row['actividad_id'];
                    $sql_actividad_activa = "SELECT * FROM actividad WHERE id = $id_actividad AND active = 1";
                    $res_actividad_activa = $mysqli->query($sql_actividad_activa);
                    while($row_actividad_activa = mysqli_fetch_assoc($res_actividad_activa)){
                        $id = $row_actividad_activa['id'];
                        $name = $row_actividad_activa['name'];
                        $description = $row_actividad_activa['description'];
                        $active = $row_actividad_activa['active'];
                        $fecha = $row_actividad_activa['fecha'];
                        $city = $row_actividad_activa['city'];
                        array_push($actividades_activas_array, $id);
                        array_push($actividades_activas_array, $name);
                        array_push($actividades_activas_array, $description);
                        array_push($actividades_activas_array, $active);
                        array_push($actividades_activas_array, $fecha);
                        array_push($actividades_activas_array, $city);
                    }
                }
                echo json_encode($actividades_activas_array);
                http_response_code(200);
				
            } else {
				echo 'El jugador no pertenece a ningún equipo';
                http_response_code(401);
            }
	
        }elseif($function === "mostrarNoInscritos"){
            $username = $_POST['username'];

            $sql_id_team = "SELECT id_team FROM common WHERE id = (SELECT id FROM users WHERE username = '$username')";
            $res_id_team = $mysqli->query($sql_id_team);
			if($row = mysqli_fetch_assoc($res_id_team)) {
				$id_team = $row['id_team'];
            // Seleccionar las actividades de grupo
				$sql_actividades_grupo = "SELECT actividad_id FROM participaciones WHERE team_id = $id_team";
                $res_actividades_grupo = $mysqli->query($sql_actividades_grupo);
                $actividades_id_inscrito_array = [];
                while($row = mysqli_fetch_assoc($res_actividades_grupo)){
                    $id_actividad = $row['actividad_id'];
                    $sql_actividades_grupo_activa = "SELECT id FROM actividad WHERE id = $id_actividad AND active = 1";
                    $res_actividades_grupo_activa = $mysqli->query($sql_actividades_grupo_activa);
                    if($row = mysqli_fetch_assoc($res_actividades_grupo_activa)){
                        array_push($actividades_id_inscrito_array, $row['id']);
                    }
                }
                $sql_todas_actividades_activas = "SELECT id FROM actividad WHERE active = 1";
                $res_todas_actividades_activas = $mysqli->query($sql_todas_actividades_activas);
                $actividades_id_todas_activas = [];
                while($row = mysqli_fetch_assoc($res_todas_actividades_activas)){
                    $id_actividad = $row['id'];
                    array_push($actividades_id_todas_activas, $id_actividad);
                }
                
                $no_inscritas = array_diff($actividades_id_todas_activas, $actividades_id_inscrito_array);
                $array_final_no_inscritas = [];
                foreach($no_inscritas as $id){
                    $sql = "SELECT * FROM actividad WHERE id = $id";
                    $res = $mysqli->query($sql);
                    while($row = mysqli_fetch_assoc($res)){
                        $id = $row['id'];
                        $name = $row['name'];
                        $description = $row['description'];
                        $active = $row['active'];
                        $fecha = $row['fecha'];
                        $city = $row['city'];
                        array_push($array_final_no_inscritas, $id);
                        array_push($array_final_no_inscritas, $name);
                        array_push($array_final_no_inscritas, $description);
                        array_push($array_final_no_inscritas, $active);
                        array_push($array_final_no_inscritas, $fecha);
                        array_push($array_final_no_inscritas, $city);
                    }
                }
                echo json_encode($array_final_no_inscritas);
                http_response_code(200);
            }else{
                echo "mal";
            }
		}
		
		else {
			http_response_code(500);  			
        }

    } 
	
	else {
		http_response_code(500);

    }
?>