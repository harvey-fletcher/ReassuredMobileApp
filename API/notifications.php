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
		$query = "SELECT * FROM users WHERE email regexp '[[:<:]]" . $_GET['email'] . "[[:>:]]' AND password regexp '[[:<:]]". $_GET['password'] ."[[:>:]]'";
		
		//The notifications part
		if(mysqli_num_rows(mysqli_query($conn, $query)) == 1){
			//User can send notifications
			if(isset($_GET['type'])){
				$FB_SERVER_KEY = 'AIzaSyCUQWJWypXPXxDV2s3wfNPprx5Kiu0uvho';

				if($_GET['type'] == "everyone"){
					if(isset($_GET['notify'])){
						$notify = $_GET['notify'];

						if($notify == 'travel'){
							$query = "SELECT a.application_token AS application_token, u.firstname AS firstname FROM application_tokens a JOIN users u ON a.user_id=u.id";

							$result = mysqli_query($conn, $query);

							while($user = mysqli_fetch_array($result, MYSQLI_ASSOC)){
								$FB_MESSAGE_TO = $user['application_token'];

								$FB_MESSAGE_BODY = $user['firstname'] . ', check your route before you travel. There are reported incidents today.';

								$FB_REQUEST_DATA = '{"to":"'. $FB_MESSAGE_TO  .'","notification":{"body":"'. $FB_MESSAGE_BODY  .'"},"priority":10}';

								$request = shell_exec("
										curl -X POST --Header 'Authorization: key=". $FB_SERVER_KEY  ."' --Header 'Content-Type: application/json' -d '". $FB_REQUEST_DATA  ."' 'http://fcm.googleapis.com/fcm/send'
								");
								
								$data = array(
									'status' => '200',
									'reason' => 'Notification sent.'
								);
							};
						} else if($notify == 'calendar'){
							if(isset($_GET['timeframe'])){
								if($_GET['timeframe'] == 'today'){
									$query = "SELECT a.application_token AS application_token, u.firstname AS firstname FROM application_tokens a JOIN users u ON a.user_id=u.id";

									$result = mysqli_query($conn, $query);

									while($user = mysqli_fetch_array($result, MYSQLI_ASSOC)){
										$FB_MESSAGE_TO = $user['application_token'];

										$FB_MESSAGE_BODY = $user['firstname'] . ', there is an event in the calendar for today.';

										$FB_REQUEST_DATA = '{"to":"'. $FB_MESSAGE_TO  .'","notification":{"body":"'. $FB_MESSAGE_BODY  .'"},"priority":10}';

										$request = shell_exec("
												curl -X POST --Header 'Authorization: key=". $FB_SERVER_KEY  ."' --Header 'Content-Type: application/json' -d '". $FB_REQUEST_DATA  ."' 'http://fcm.googleapis.com/fcm/send'
										");

										$data = array(
												'status' => '200',
												'reason' => 'Notification sent.'
										);
									};
								} else if($_GET['timeframe'] == 'future'){
									$query = "SELECT a.application_token AS application_token, u.firstname AS firstname FROM application_tokens a JOIN users u ON a.user_id=u.id";

										$result = mysqli_query($conn, $query);

									while($user = mysqli_fetch_array($result, MYSQLI_ASSOC)){
											$FB_MESSAGE_TO = $user['application_token'];

											$FB_MESSAGE_BODY = $user['firstname'] . ", there are new events in the calendar.";

											$FB_REQUEST_DATA = '{"to":"'. $FB_MESSAGE_TO  .'","notification":{"body":"'. $FB_MESSAGE_BODY  .'"},"priority":10}';

											$request = shell_exec("
													curl -X POST --Header 'Authorization: key=". $FB_SERVER_KEY  ."' --Header 'Content-Type: application/json' -d '". $FB_REQUEST_DATA  ."' 'http://fcm.googleapis.com/fcm/send'
											");

										$data = array(
											'status' => '200',
											'reason' => 'Notification sent.'
										);
									};
								} else {
									$data = array(
										'status' => '500',
										'reason' => 'You have not specified a VALID calendar timeframe. Timeframes are today and future.'
									);
								}
							} else {
								$data = array(
									'status' => '500',
									'reason' => 'You have not specified a calendar timeframe. Timeframes are today and future.'
								);
							}
						} else if($notify == 'social'){

							$query = "SELECT a.application_token AS application_token, u.firstname AS firstname FROM application_tokens a JOIN users u ON a.user_id=u.id";

							$result = mysqli_query($conn, $query);

							while($user = mysqli_fetch_array($result, MYSQLI_ASSOC)){
								$FB_MESSAGE_TO = $user['application_token'];

								$FB_MESSAGE_BODY = $user['firstname'] . ", there are new MyReassured posts!.";

								$FB_REQUEST_DATA = '{"to":"'. $FB_MESSAGE_TO  .'","notification":{"body":"'. $FB_MESSAGE_BODY  .'"},"priority":10}';

								$request = shell_exec("
									curl -X POST --Header 'Authorization: key=". $FB_SERVER_KEY  ."' --Header 'Content-Type: application/json' -d '". $FB_REQUEST_DATA  ."' 'http://fcm.googleapis.com/fcm/send'
								");

								$data = array(
									'status' => '200',
									'reason' => 'Notification sent.'
								);
							};
						} else if($notify == 'meeting'){
							$query = "SELECT a.application_token AS application_token, u.firstname AS firstname FROM application_tokens a JOIN users u ON a.user_id=u.id";

							$result = mysqli_query($conn, $query);

							while($user = mysqli_fetch_array($result, MYSQLI_ASSOC)){
								$FB_MESSAGE_TO = $user['application_token'];

								$FB_MESSAGE_BODY = $user['firstname'] . ", There is a company wide meeting today";

								$FB_REQUEST_DATA = '{"to":"'. $FB_MESSAGE_TO  .'","notification":{"body":"'. $FB_MESSAGE_BODY  .'"},"priority":10}';

								$request = shell_exec("
										curl -X POST --Header 'Authorization: key=". $FB_SERVER_KEY  ."' --Header 'Content-Type: application/json' -d '". $FB_REQUEST_DATA  ."' 'http://fcm.googleapis.com/fcm/send'
								");

								$data = array(
									'status' => '200',
									'reason' => 'Notification sent.'
								);
						   };
						} else {
							$data = array(
								'status' => '500',
								'reason' => 'That is not a valid notify value. Notification types for everyone are travel, calendar, social or meeting'
							);
						};
					} else {
						$data = array(
							'status' => '500',
							'reason' => 'You have not specified a notify value. Notification types for everyone are travel, calendar, social or meeting'
						);
					}
				} else if($_GET['type'] == 'team'){

					if(isset($_GET['notify'])){
						$notify = $_GET['notify'];

						if(isset($_GET['team_id'])){
							if($notify == 'lockout'){
								if(isset($_GET['user_id'])){
									$query = "SELECT * FROM application_tokens a JOIN users u WHERE u.team_id = " . $_GET['team_id'] ." AND u.id != ". $_GET['user_id'];
									$result = mysqli_query($conn, $query);
									$affected_user = mysqli_fetch_array(mysqli_query($conn, "SELECT * FROM users WHERE id = " . $_GET['user_id']), MYSQLI_ASSOC)['firstname'];
									
									while($user = mysqli_fetch_array($result, MYSQLI_ASSOC)){
										$FB_MESSAGE_TO = $user['application_token'];

										$FB_MESSAGE_BODY = $affected_user . " is locked out please let them in";

										$FB_REQUEST_DATA = '{"to":"'. $FB_MESSAGE_TO  .'","notification":{"body":"'. $FB_MESSAGE_BODY  .'"},"priority":10}';

										$request = shell_exec("
												curl -X POST --Header 'Authorization: key=". $FB_SERVER_KEY  ."' --Header 'Content-Type: application/json' -d '". $FB_REQUEST_DATA  ."' 'http://fcm.googleapis.com/fcm/send'
										");

										$data = array(
											'status' => '200',
											'reason' => 'Notification sent.'
										);
									};
								} else {
									$data = array(
										'status' => '500',
										'reason' => 'You must specify the affected user'
									);
								}
							} else if($notify == 'meeting'){
								$query = "SELECT * FROM application_tokens a JOIN users u ON a.user_id=u.id WHERE u.team_id = " . $_GET['team_id'];
								$result = mysqli_query($conn, $query);
								
								$team_manager = mysqli_fetch_array(mysqli_query($conn, "SELECT * FROM users u JOIN teams t ON t.team_manager=u.id WHERE t.id = " . $_GET['team_id']), MYSQLI_ASSOC)['firstname'];

								while($user = mysqli_fetch_array($result, MYSQLI_ASSOC)){
									$FB_MESSAGE_TO = $user['application_token'];

									$FB_MESSAGE_BODY = $team_manager . " has called a team meeting";

									$FB_REQUEST_DATA = '{"to":"'. $FB_MESSAGE_TO  .'","notification":{"body":"'. $FB_MESSAGE_BODY  .'"},"priority":10}';

									$request = shell_exec("
										curl -X POST --Header 'Authorization: key=". $FB_SERVER_KEY  ."' --Header 'Content-Type: application/json' -d '". $FB_REQUEST_DATA  ."' 'http://fcm.googleapis.com/fcm/send'
									");

									$data = array(
											'status' => '200',
											'reason' => 'Notification sent.'
									);
								};
							} else if($notify == 'late'){
								if(isset($_GET['user_id'])){
									$query = "SELECT * FROM application_tokens a JOIN users u ON a.user_id=u.id WHERE u.team_id = " . $_GET['team_id'] . " AND u.id != " . $_GET['user_id'];
									$result = mysqli_query($conn, $query);
									$affected_user = mysqli_fetch_array(mysqli_query($conn, "SELECT * FROM users WHERE id = " . $_GET['user_id']), MYSQLI_ASSOC)['firstname'];

									while($user = mysqli_fetch_array($result, MYSQLI_ASSOC)){
										$FB_MESSAGE_TO = $user['application_token'];

										$FB_MESSAGE_BODY = $affected_user . " is running late.";

										$FB_REQUEST_DATA = '{"to":"'. $FB_MESSAGE_TO  .'","notification":{"body":"'. $FB_MESSAGE_BODY  .'"},"priority":10}';

										$request = shell_exec("
											curl -X POST --Header 'Authorization: key=". $FB_SERVER_KEY  ."' --Header 'Content-Type: application/json' -d '". $FB_REQUEST_DATA  ."' 'http://fcm.googleapis.com/fcm/send'
										");

										$data = array(
											'status' => '200',
											'reason' => 'Notification sent.'
										);
									};
								} else {
									$data = array(
											'status' => '500',
											'reason' => 'You must specify the affected user'
									);
								}
							} else {
								$data = array(
									'status' => '500',
									'reason' => 'You have not specified a valid team event, events are late, meeting and lockout'
								);
							}
						} else {
							$data = array(
								'status' => '500',
								'reason' => 'You have not specified a team id'
							);
						}
					} else {
						$data = array(
							'status' => '500',
						   'reason' => 'You have not specified a notify value. Notification types for everyone are late, meeting, lockout'
						);
					}
				}

			} else {
				$data = array(
					'status' => '500',
					'reason' => 'You have not specified a notification type. Notification types are everyone, team or single'
				);
			}
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