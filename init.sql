-- Create both databases
CREATE DATABASE IF NOT EXISTS `vacation_planning-notifications`;
CREATE DATABASE IF NOT EXISTS `vacation_planning`;

-- Create user first, then grant privileges
CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON `vacation_planning-notifications`.* TO '${DB_USERNAME}'@'%';
GRANT ALL PRIVILEGES ON `vacation_planning`.* TO '${DB_USERNAME}'@'%';
FLUSH PRIVILEGES;
