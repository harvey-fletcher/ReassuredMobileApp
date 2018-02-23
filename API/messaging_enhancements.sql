/*This script will create a table to store user to user messaging*/
CREATE TABLE user_messages(
	id INT(10) AUTO_INCREMENT,
	from_user_id INT(10) NOT NULL,
	to_user_id INT(10) NOT NULL,
	sent_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	message_body TEXT(9999) NOT NULL,
	PRIMARY KEY(id)
);