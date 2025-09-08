#!/bin/bash
set -e

# Execute SQL with environment variables
mysql -u root -p$MYSQL_ROOT_PASSWORD <<EOF
CREATE DATABASE IF NOT EXISTS \`vacation_planning-notifications\`;
CREATE DATABASE IF NOT EXISTS \`vacation_planning\`;
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';
GRANT ALL PRIVILEGES ON \`vacation_planning-notifications\`.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON \`vacation_planning\`.* TO '${MYSQL_USER}'@'%';
FLUSH PRIVILEGES;
EOF