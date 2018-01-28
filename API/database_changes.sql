CREATE TABLE users(
	id int(10) AUTO_INCREMENT,
	email VARCHAR(75) NOT NULL,
	password VARCHAR(300) NOT NULL,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	firstname VARCHAR(75) NOT NULL,
	lastname VARCHAR(75) NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE company_calendar(
	id int(10) AUTO_INCREMENT,
	event_name VARCHAR(75) NOT NULL,
	event_location VARCHAR(75) NOT NULL,
	event_organiser VARCHAR(75) NOT NULL,
	event_start TIMESTAMP DEFAULT NULL,
	event_information TEXT(9999) NOT NULL,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
	primary key(id)
);

CREATE TABLE teams(
	id int(3) AUTO_INCREMENT,
	team_name VARCHAR(40) NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE locations(
	id int(3) AUTO_INCREMENT,
	location_name VARCHAR(30) NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE application_tokens(
	id int(10) AUTO_INCREMENT,
	user_id int(10) NOT NULL,
	application_token varchar(200) NOT NULL,
	PRIMARY KEY(id)
);

/*Insert a test dataset to the calendar*/
INSERT INTO company_calendar (`event_name`,`event_location`,`event_organiser`,`event_start`,`event_end`,`event_information`) VALUES ('Test Event','Basingstoke Office','Harvey Fletcher','2018-01-26 09:00:00','2018-01-26 17:30:00','This is for test purposes');
INSERT INTO company_calendar (`event_name`,`event_location`,`event_organiser`,`event_start`,`event_end`,`event_information`) VALUES ('Test Event 2','Portsmouth Office','Amanda Miranda Panda','2018-01-26 16:00:00','2018-01-26 17:30:00','This is for test purposes');