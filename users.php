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

	function has_team() {
		global $conn;
		if (valid_session()) {
			$id = $_SESSION['id'];
			$sql = "SELECT id from common where id=$id";
			$result = $conn->query($sql);
			if ($result->num_rows > 0) {
				return true;
			} else {
				return false;
			}
		}
	}

    if(isset($_POST['function'])) {
        $function = $_POST['function'];

		if($function === "login") {
			$username = $_POST['username'];
			$password = $_POST['password'];
			$token = $_POST['token'];
            $sql = "SELECT id, isAdmin, password FROM users WHERE username = '$username'";
            $result = $conn->query($sql);
			
			if ($row = mysqli_fetch_assoc($result)) {

				if(password_verify($password, $row['password'])) {
					
					// Se añade el token del dispositivo para ese usuario si no está añadido ya
					$id_user = $row['id'];
					$sql_comprobar_no_esta_token = "SELECT id FROM tokens WHERE id_user = $id_user AND token = '$token'";
					$result_token = $conn->query($sql_comprobar_no_esta_token);

					//existe el token
					if($result_token->num_rows > 0){ 
						// Existe el token
					
					}else{
						$sql_insertar_token = "INSERT INTO tokens (token, id_user) VALUES ('$token', $id_user)";
						$conn->query($sql_insertar_token);
						// El token no existe
					}
					
					$_SESSION['id'] = $id_user;
					if ($row['isAdmin']) {
						echo "Has iniciado como Administrador";
					} else {
						if (has_team()) {
							echo "Has iniciado como usuario normal y tienes equipo";
						} else {
							echo "Has iniciado como usuario normal, pero no tienes equipo";
						}
					}
					http_response_code(200);

                }else {
					echo 'Username or password incorrect';
                    http_response_code(401);
                }

            } else {
				echo 'Username or password incorrect';
                http_response_code(401);
            }
	
        } elseif ($function === "register") {
			$username = $_POST['username'];
			$password = $_POST['password'];
			$secure_password = password_hash($password, PASSWORD_DEFAULT);
			$name = $_POST['name'];
			$city = $_POST['city'];
			$age = $_POST['age'];
            
            // Comprobar que el usuario no existe
            $sql_comprobar = "SELECT id FROM users WHERE username='$username'";
            $res = $conn->query($sql_comprobar);
            if ($row = mysqli_fetch_assoc($res)) {
                // Existe un usuario con el mismo nombre
                echo $row['id'];
                http_response_code(200);
				
            } else {
                $sql = "INSERT INTO users(username, password, name, city, age, isAdmin) VALUES('$username', '$secure_password', '$name', '$city', $age, 0)";
                $conn->query($sql);
				http_response_code(200);
            }
		} 
		elseif ($function === "logout") {
			destroy_session();

        } else {
			http_response_code(500);
		}
	} 
	else {
		http_response_code(500);
    }
?>	