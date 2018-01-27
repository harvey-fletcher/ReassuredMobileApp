<?php
    $conn = mysqli_connect('127.0.0.1','root','*HArGB@@1979','reassured_app');

	//The output
	$data = array();

	if(isset($_GET['from_result'])){
		$onwards_of = $_GET['from_result'];
	} else {
		$onwards_of = 0;
	};

        if(isset($_GET['list'])){
		if(isset($_GET['start']) && isset($_GET['end']) ){
			$query = "SELECT c.id, u.firstname, u.lastname, c.event_start, c.event_location, c.event_name, c.event_information FROM company_calendar c JOIN users u ON c.event_organiser=u.id WHERE c.event_start BETWEEN '". $_GET['start']  ."' AND '". $_GET['end']  ."' ORDER BY event_start ASC LIMIT ". $onwards_of .", 6";

			if($result = mysqli_query($conn, $query)){
				while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){
					$row['event_organiser'] = $row['firstname'] . " " . $row['lastname'];
					unset($row['firstname']);
					unset($row['lastname']);
					array_push($data, json_encode($row, JSON_FORCE_OBJECT));
				}
			} else {
				$data = array(
					0 => 'Error',
					1 => 'The server encountered an error whilst querying the database.'
				);
			}
		} else {
			$data = array(
				0 => 'Error',
				1 => 'You have not specified a start and end date for the date range.'
			);
		}
		//List all events between the date range
	} else if($_GET['add']){
		if(isset($_GET['email']) && isset($_GET['password'])){
			$query = "SELECT team_id FROM users WHERE email regexp '[[:<:]]". $_GET['email']  ."[[:>:]]' AND password regexp '[[:<:]]". $_GET['password']  ."[[:>:]]'" ;
			$isUser = mysqli_num_rows(mysqli_query($conn, $query));
			$team_id = mysqli_fetch_array(mysqli_query($conn, $query))['team_id'];

			if($isUser == 1){
				if(($team_id == 1) || ($team_id == 2) || ($team_id == 3)){
					if( isset($_GET['event_name']) && isset($_GET['event_organiser']) && isset($_GET['event_start']) && isset($_GET['']) && isset($_GET['event_end']) && isset($_GET['event_information']) ){
						`event_name`,`event_location`,`event_organiser`,`event_start`,`event_end`,`event_information`
					} else {
						$data = array(
							'status' => '500',
							'reason' => 'You have not specified enough fields. Required fields are: event_name, event_location, event_organiser, event_start, event_end, event_information'
						)
					}
				}
			}
		}
	} else if($_GET['delete']){
		if(isset($_GET['email']) && isset($_GET['password'])){
			$query = "SELECT team_id FROM users WHERE email regexp '[[:<:]]". $_GET['email']  ."[[:>:]]' AND password regexp '[[:<:]]". $_GET['password']  ."[[:>:]]'" ;
			$isUser = mysqli_num_rows(mysqli_query($conn, $query));
			$team_id = mysqli_fetch_array(mysqli_query($conn, $query))['team_id'];

			if($isUser == 1){
				if(($team_id == 1) || ($team_id == 2) || ($team_id == 3)){
					if(isset($_GET['id'])){
					
						$query = "DELETE FROM company_calendar WHERE id=" . $_GET['id'];
						$execute = mysqli_query($conn, $query);
						$result = mysqli_affected_rows($conn);
					
						if($result != 0){
							$data = array(
								'status' => '200',
								'reason' => 'Deleted ' . $result . ' event from the company calendar'
							);
						} else {
							$data = array(
								'status' => '200',
								'reason' => 'Done, there were no rows to delete'
							);
						};
					} else {
						$data = array(
							'status' => '500',
							'reason' => 'Please provide an event ID'
						);
					}
				} else {
					$data = array(
						'status' => '403',
						'reason' => 'Your user group cannot perform this action'
					);
				}
			} else {
				$data = array(
					'status' => '403',
					'reason' => 'Username or password incorrect.'
				);
			}
		} else {
			$data = array(
                                0 => 'Error',
   				1 => 'You have not given username and password'
			);
		}
	} else {
		$data = array(
			0 => 'Error',
			1 => 'You have not specified an API mode. API modes: list, create, delete'
		);
	}
		
	echo json_encode($data);
?>