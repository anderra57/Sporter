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

	function has_team($username) {
		global $conn;
		$sql = "SELECT id from common where id=(select id from users where username = '$username')";
		$result = $conn->query($sql);
		if ($result->num_rows > 0) {
			return true;
		} else {
			return false;
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
						echo "administrator";
					} else {
						if (has_team($username)) {
							echo "team";
						} else {
							echo "nogroup";
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
			$name = "";
			$city = "";
			$age = 0;
            // Comprobar que el usuario no existe
            $sql_comprobar = "SELECT id FROM users WHERE username='$username'";
            $res = $conn->query($sql_comprobar);
            if ($row = mysqli_fetch_assoc($res)) {
                // Existe un usuario con el mismo nombre
                echo $row['id'];
                http_response_code(500);
				
            } else {
                $sql = "INSERT INTO users(username, password, name, city, age, isAdmin) VALUES('$username', '$secure_password', '$name', '$city', $age, 0)";
                $conn->query($sql);
				http_response_code(200);
            }
		} 
		elseif ($function === "updatepass") {
			$username = $_POST['username'];
			$password = $_POST['password'];
			$secure_password = password_hash($password, PASSWORD_DEFAULT);
       
			$sql = "UPDATE users SET password='$secure_password' WHERE username='$username'";
			$conn->query($sql);
			http_response_code(200);

		} 
		elseif ($function === "logout") {
			if (isset($_SESSION['id'])) {
				$_SESSION['id'] = '';
				session_destroy();
			}
		} elseif ($function === "verifysession") {
			if (valid_session()) {
				$id_user = $_SESSION['id'];
				$sql = "SELECT id, isAdmin from users where id = $id_user";
				$result = $conn->query($sql);
				if ($result->num_rows > 0) {
					$isAdmin = 0;
					if($row = mysqli_fetch_assoc($result)) {
						$isAdmin = $row['isAdmin'];
					}
					// Esto hay que refractorizarlo
					if($isAdmin == 0) {
						$sql_hasgroup = "SELECT id from common where id = $id_user";
						$res = $conn->query($sql_hasgroup);
						if ($res->num_rows > 0) {
							echo $isAdmin;
							http_response_code(200);
						} else {
							http_response_code(401);
						}
					} else {
						echo $isAdmin;
						http_response_code(200);
					}
				} else {
					http_response_code(401);
				}
			} else {
				http_response_code(401);
			}
		} else {
			http_response_code(500);
		}
	} 
	else {
		http_response_code(500);
    }
?>	