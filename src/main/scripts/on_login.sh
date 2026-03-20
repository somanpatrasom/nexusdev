#!/bin/bash

# NexusDev Login Hook
# Runs every time a user logs in via SSH
# Logs the connection to PostgreSQL

# Get connection details from environment
LOGIN_USER=$(whoami)
LOGIN_IP=$(echo $SSH_CLIENT | awk '{print $1}')
LOGIN_TIME=$(date '+%Y-%m-%d %H:%M:%S')
HOSTNAME=$(hostname)

#If not an SSH login, exit silently
if [ -z "$SSH_CLIENT" ]; then
    return 0
fi

# Log to PostgreSQL
PGPASSWORD="soman503" psql \
    -h localhost \
    -U nexus_admin \
    -d nexusdev \
    -c "INSERT INTO sessions (topic, duration_minutes, notes)
        VALUES (
            'SSH Login | $LOGIN_USER@$HOSTNAME',
            0,
            'Automatic login event from IP: $LOGIN_IP at $LOGIN_TIME'
        );" > /dev/null 2>&1
