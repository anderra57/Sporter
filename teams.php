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


    if(isset($_POST['function'])) {
		if (valid_session()) {
			$function = $_POST['function'];
			$id_user = $_SESSION['id'];

			if($function === "join") {
				$teamname = $_POST['teamname'];
				$teampass = $_POST['teampass'];
				$sql = "SELECT id, password FROM teams WHERE name = '$teamname'";
				$result = $conn->query($sql);
				
				if ($row = mysqli_fetch_assoc($result)) {
					$team_password = $row;
					if(password_verify($teampass, $team_password['password'])) {						
						// Añadir a COMMON el identificador del usuario y del grupo
						$id_team = $team_password['id'];
						
						// Insertar en la tabla common el usuario
						$sql_insertar = "INSERT INTO common (id, id_team) VALUES ($id_user, $id_team)";
						$res_insert = $conn->query($sql_insertar);
						http_response_code(200);

					}else {
						echo 'Team name or password incorrect';
						http_response_code(401);
					}

				} else {
					echo 'Team name or password incorrect';
					http_response_code(401);
				}
		
			}elseif($function === "create"){
				$teamname = $_POST['teamname'];
				$teampass = $_POST['teampass'];

				// Comprobar que no hay equipos con el mismo nombre
				$sql_comprobar_nombre_equipo = "SELECT id FROM teams WHERE name = '$teamname'";
				$res_comprobar_nombre_equipo = $conn->query($sql_comprobar_nombre_equipo);
				// Ya hay equipo con ese nombre
				if($res_comprobar_nombre_equipo->num_rows > 0){
					if ($row = mysqli_fetch_assoc($res_comprobar_nombre_equipo)) {
						// Existe un usuario con el mismo nombre
						echo $row['id'];
						http_response_code(200);
					}
				}else{
					$secure_password = password_hash($teampass, PASSWORD_DEFAULT);
					$sql_create_team = "INSERT INTO teams (name, password) VALUES ('$teamname', '$secure_password')";
					$res_create_team = $conn->query($sql_create_team);
					if($res_create_team){
						// Insertar a la tabla common el id del usuario y del equipo recien creado
						$sql_insertar_common = "INSERT INTO common (id, id_team) VALUES ($id_user, (SELECT id FROM teams WHERE name = '$teamname'))";
						$res_insertar_common = $conn->query($sql_insertar_common);
						if($res_insertar_common){
							http_response_code(200);
						}else{
							http_response_code(401);
						}
					}else{
						echo "Algo ha fallado al crear el equipo";
						http_response_code(500);
					}
					
				}
			} else {
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