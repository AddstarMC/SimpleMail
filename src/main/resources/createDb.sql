CREATE SCHEMA SimpleMail;
USE SimpleMail;
CREATE TABLE IF NOT EXISTS SM_Mail
(
    id         INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
    sender_id  char(36),
    sender     varchar(16),
    target_id  char(36),
    target     varchar(16),
    date       datetime,
    message    text,
    isread     tinyint(1),
    expiration datetime
);
ALTER TABLE SM_Mail
    ADD COLUMN (sender_id CHAR(36), target_id CHAR(36));
ALTER TABLE SM_Mail
    ADD INDEX idx_sender (sender_id);
ALTER TABLE SM_Mail
    ADD INDEX idx_target (target_id);