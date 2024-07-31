#!/bin/bash

SECRET=$(grep sharedSecret /etc/averox/avx-apps-akka.conf | sed 's/^.*=//')
URL=$(grep averox.web.serverURL= /etc/averox/avx-web.properties | sed 's/^.*=//')

echo BBB_URL="$URL/averox/api"
echo BBB_SECRET=$SECRET
