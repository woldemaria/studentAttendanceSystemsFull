#!/bin/bash

# Add Notification Script
# Usage: ./add-notification.sh <username> <title> <message> <type>
# Types: ATTENDANCE_WARNING, SYSTEM_NOTIFICATION, COURSE_UPDATE, REMINDER

if [ $# -lt 4 ]; then
    echo "Usage: ./add-notification.sh <username> <title> <message> <type>"
    echo ""
    echo "Types:"
    echo "  ATTENDANCE_WARNING"
    echo "  SYSTEM_NOTIFICATION"
    echo "  COURSE_UPDATE"
    echo "  REMINDER"
    echo ""
    echo "Example:"
    echo "  ./add-notification.sh student1 'Test' 'This is a test notification' SYSTEM_NOTIFICATION"
    exit 1
fi

USERNAME=$1
TITLE=$2
MESSAGE=$3
TYPE=$4

# Validate type
if [[ ! "$TYPE" =~ ^(ATTENDANCE_WARNING|SYSTEM_NOTIFICATION|COURSE_UPDATE|REMINDER)$ ]]; then
    echo "Error: Invalid type. Must be one of:"
    echo "  ATTENDANCE_WARNING, SYSTEM_NOTIFICATION, COURSE_UPDATE, REMINDER"
    exit 1
fi

# Add notification
/opt/lampp/bin/mysql -u root -e "
USE Wolde;
SET @user_id = (SELECT user_id FROM USERS WHERE username = '$USERNAME' LIMIT 1);
INSERT INTO NOTIFICATIONS (user_id, title, message, type, is_read, created_at)
VALUES (@user_id, '$TITLE', '$MESSAGE', '$TYPE', 0, NOW());
SELECT 'Notification added successfully!' AS Status;
SELECT * FROM NOTIFICATIONS WHERE notification_id = LAST_INSERT_ID();
"

echo ""
echo "✓ Notification added for user: $USERNAME"
