<?php

	//The DB connection
	$conn = mysqli_connect('127.0.0.1','root','*HArGB@@1979','reassured_app');
	
	//The results array
	$data = array();

	//Have user credentials been provided?
	if(isset($_GET['email']) && isset($_GET['password'])){
		$credentials = 1;
	} else {
		$credentials = 0;
	}
	
	//Check if the user exists in the database
	if($credentials == 1){
		$query = "SELECT * FROM users WHERE email regexp '[:<:]" . $_GET['email'] . "[:>:]' AND password regexp '". $_GET['password'] ."'";
		
		//The notifications part
		if(mysqli_num_rows(mysqli_query($query)) == 1){
			//User can send notifications
		} else {
			$data = array(
				'status' => '403',
				'reason' => 'Email address or password incorrect'
			);
		}
	} else {
		$data = array(
			'status' => '403',
			'reason' => 'Both username and password are required.'
		);
	}
	
	echo json_encode($data);

?>