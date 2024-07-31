#!/usr/bin/env bash

echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo "  **** This is for development only *****"
echo " "
echo " Make sure you change permissions to /var/averox/"
echo " to allow avx-web to write to the directory. "
echo " "
echo " chmod -R 777 /var/averox/"
echo " "
echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"

sudo service avx-web stop

exec grails prod run-app --port 8090 -reloading
