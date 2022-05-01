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
	
		if($function === "login") {
			
			$username = $_POST['username'];
			$password = $_POST['password'];
			$token = $_POST['token'];
            $sql = "SELECT id, password FROM users WHERE username = '$username'";
            $result = $mysqli->query($sql);
			
			if ($row = mysqli_fetch_assoc($result)) {
			$users_password = $row;
				if(password_verify($_POST['password'], $users_password['password'])) {
                    echo $users_password['id'];
					http_response_code(200);
					
					// Se añade el token del dispositivo para ese usuario si no está añadido ya
					$id_user = $users_password['id'];
					$sql_comprobar_no_esta_token = "SELECT id FROM tokens WHERE id_user = $id_user AND token = '$token'";
					$result_token = $mysqli->query($sql_comprobar_no_esta_token);

					//existe el token
					if($result_token->num_rows > 0){ 
						echo "Ya existe el token";
						//http_response_code(200);
					
					}else{
						$sql_insertar_token = "INSERT INTO tokens (token, id_user) VALUES ('$token', $id_user)";
						$mysqli->query($sql_insertar_token);
						echo "Token añadido correctamente";
						//http_response_code(200);
					}
					
					http_response_code(200);
                }else {
					echo 'La contraseña no es correcta';
                    http_response_code(401);
                }

            } else {
				echo 'No existe nigún usuario con ese nombre';
                http_response_code(401);
            }
	
        } elseif ($function === "register") {
			$username = $_POST['username'];
			$secure_password = password_hash($_POST['password'], PASSWORD_DEFAULT);
			$name = $_POST['name'];
			$city = $_POST['city'];
			$age = $_POST['age'];
			$token = $_POST['token'];
                       
            // Comprobar que el usuario no existe
            $sql_comprobar = "SELECT id FROM users WHERE username='$username'";
            $res = $mysqli->query($sql_comprobar);
            if ($row = mysqli_fetch_assoc($res)) {
                // Existe un usuario con el mismo nombre
                echo $row['id'];
                http_response_code(200);
				
            } else {
                $sql = "INSERT INTO users(username, password, name, city, age) VALUES('$username', '$secure_password', '$name', '$city', $age)";
                $mysqli->query($sql);
				http_response_code(200);
				
				// Esto esta mal, no se añade el token en el registro ->
				
				/*// Añadimos a la tabla de tokens el token + id del usuario
				// Primero logramos el id del usuario que acabamos de añadir
				$sql_id_user = "SELECT id FROM users WHERE username = '$username'";
				$res2 = $mysqli->query($sql_id_user);
				if($row2 = mysqli_fetch_assoc($res2)) {
					$id_user = $row2['id'];
				}
				$sql_token = "INSERT INTO tokens (token, id_user) VALUES ('$token', $id_user)";
				$mysqli->query($sql_token);
                http_response_code(200);*/
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