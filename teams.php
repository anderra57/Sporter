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
	
		if($function === "join") {
			
			$teamname = $_POST['teamname'];
			$teampass = $_POST['teampass'];
			$username = $_POST['username'];
            $sql = "SELECT id, password FROM teams WHERE name = '$teamname'";
            $result = $mysqli->query($sql);
			
			if ($row = mysqli_fetch_assoc($result)) {
			$team_password = $row;
				if(password_verify($teampass, $team_password['password'])) {
					// Existe el equipo
                    echo $team_password['id'];
					http_response_code(200);
					
					// Añadir a COMMON el identificador del usuario y del grupo
					$id_team = $team_password['id'];

					// Conseguir el identificador del usuario
					$sql_id_usuario = "SELECT id FROM users where username = '$username'";
					$res_id_usuario = $mysqli->query($sql_id_usuario);
					if($row_id_user = mysqli_fetch_assoc($res_id_usuario)){
						$id_user = $row_id_user['id'];
						http_response_code(200);
					}
					$sql_insertar = "INSERT INTO common (id, id_team) VALUES ($id_user, $id_team)";
					$res_insert = $mysqli->query($sql_insertar);
					http_response_code(200);

                }else {
					echo 'Contraseña incorrecta';
                    http_response_code(401);
                }

            } else {
				echo 'No existe nigún equipo con ese nombre';
                http_response_code(401);
            }
	
        }elseif($function === "create"){
			$teamname = $_POST['teamname'];
			$teampass = $_POST['teampass'];
			$username = $_POST['username'];

			// Comprobar que no hay equipos con el mismo nombre
			$sql_comprobar_nombre_equipo = "SELECT id FROM teams WHERE name = '$teamname'";
			$res_comprobar_nombre_equipo = $mysqli->query($sql_comprobar_nombre_equipo);
			// Ya hay equipo con ese nombre
			if($res_comprobar_nombre_equipo->num_rows > 0){
				echo "Ya existe un equipo con ese nombre";
				http_response_code(401);
			}else{
				$secure_password = password_hash($teampass, PASSWORD_DEFAULT);
				$sql_create_team = "INSERT INTO teams (name, password) VALUES ('$teamname', '$secure_password')";
				$res_create_team = $mysqli->query($sql_create_team);
				if($res_create_team){
					echo "Equipo creado correctamente";
					http_response_code(200);
					// Insertar a la tabla common el id del usuario y del equipo recien creado
					$sql_insertar_common = "INSERT INTO common (id, id_team) VALUES ((SELECT id FROM users WHERE username = '$username'), (SELECT id FROM teams WHERE name = '$teamname'))";
					$res_insertar_common = $mysqli->query($sql_insertar_common);
					if($res_insertar_common){
						echo "Id de usuario y equipo añadidos correctamente a COMMON";
						http_response_code(200);
					}else{
						echo "Ha ocurido un error al añadir id usuario y equipo a COMMON";
						http_response_code(401);
					}
				}else{
					echo "Algo ha fallado al crear el equipo";
				}
				
			}
		}elseif($function === 'estaEn'){
			$username = $_POST['username'];
			// Comprobar si el usuario está en common
			$sql_esta_en_common = "SELECT id FROM common WHERE id = (SELECT id FROM users WHERE username = '$username')";
			$res_esta_en_common = $mysqli->query($sql_esta_en_common);
			if($row = mysqli_fetch_assoc($res_esta_en_common)){
				$id_common = $row['id'];
				echo $id_common;
				http_response_code(200);
			}else{
				echo "El usuario no está en common";
				http_response_code(401);
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