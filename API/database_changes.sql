CREATE TABLE users(
	id int(10) AUTO_INCREMENT,
	email VARCHAR(75) NOT NULL,
	password VARCHAR(300) NOT NULL,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	firstname VARCHAR(75) NOT NULL,
	lastname VARCHAR(75) NOT NULL,
	team_id int(3) NOT NULL,
	location_id int(3) NOT NULL,
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

/*This is a table that contains scheduled meetings*/
CREATE TABLE scheduled_meetings(
	id int(10) AUTO_INCREMENT,
	organizer_id int(10) NOT NULL,
	title varchar(50) NOT NULL,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	start_time TIMESTAMP,
	duration INT(4),
	invited VARCHAR(2000) DEFAUlT "[]",
	attending VARCHAR(2000) DEFAULT "[]",
	declined VARCHAR(2000) DEFAULT "[]",
	PRIMARY KEY(id)
);

/*Insert a test meeting*/
insert into scheduled_meetings (`organizer_id`,`title`,`start_time`,`duration`,`invited`,`attending`,`declined`) VALUES ('1','Test Meeting','2018-02-12 17:00:00','30','[1]','[]','[]');