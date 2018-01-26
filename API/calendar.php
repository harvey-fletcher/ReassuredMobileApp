<?php
        $conn = mysqli_connect('127.0.0.1','root','*HArGB@@1979','reassured_app');

        //The output
        $data = array();

        if(isset($_GET['list'])){
                if(isset($_GET['start']) && isset($_GET['end']) ){
                        $query = "SELECT * FROM company_calendar WHERE event_start BETWEEN '". $_GET['start']  ."' AND '". $_GET['end']  ."'";

                        if($result = mysqli_query($conn, $query)){
                                while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){
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
        } else if($_GET['create']){
                        //Create an event
        } else if($_GET['delete']){
                        //Delete an event
        } else {
                $data = array(
                        0 => 'Error',
                        1 => 'You have not specified an API mode. API modes: list, create, delete'
                );
        }

        echo json_encode($data);
?>
