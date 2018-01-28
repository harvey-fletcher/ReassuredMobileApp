<?php
	$conn = mysqli_connect('127.0.0.1','root','*HArGB@@1979','reassured_app');

	if(isset($_GET['email']) && isset($_GET['password']) && isset($_GET['token'])){
		$data = array();
		$data = mysqli_fetch_array(mysqli_query($conn, "SELECT * FROM users WHERE email regexp '[[:<:]]". $_GET['email'] ."[[:>:]]' AND password regexp '[[:<:]]" . $_GET['password'] . "[[:>:]]'"), MYSQLI_ASSOC);
		
		if(sizeof(array_chunk($data, 14)) != 1){
			$data = array("status"=>"403", "code"=>"2", "reason"=>"Username or password incorrect");
		} else {
			$data["status"] = "200";

			//Insert a new token or refresh the existing one
			$query = "SELECT * FROM application_tokens WHERE user_id='". $data['id']  ."'";

			if(mysqli_num_rows(mysqli_query($conn, $query)) == 1){
				$query = "UPDATE application_tokens SET application_token = '" . $_GET['token'] . "' WHERE user_id = '" . $data['id']  ."'";
			} else {
				$query = "INSERT INTO application_tokens (`user_id`,`application_token`) VALUES ('". $data['id']  ."','". $_GET['token']  ."')";
			}

			$result = mysqli_query($conn, $query);
		}
	} else {
		$data = array("status"=>"403", "code"=>"1", "reason"=>"Please provide a username and password");
	}

	echo json_encode($data);
?>