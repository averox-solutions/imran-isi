#!/bin/bash -e

BIGBLUEBUTTON_USER=averox

# set ownership of activity directory
chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/learning-dashboard/
#
# Restart nginx to take advantage of the updates to nginx configuration
#
reloadService nginx

