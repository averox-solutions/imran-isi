#!/usr/bin/env bash
IS_BBB_WEB_RUNNING=`ss -an | grep LISTEN | grep 8090 > /dev/null && echo 1 || echo 0`

if [ "$IS_BBB_WEB_RUNNING" = "1" ]; then
	echo "avx-web is running, exiting"
	exit 1
fi

if [ "`whoami`" != "averox" ]; then
	echo "ERROR:  avx-web must run as averox user ( because of the uploaded files permissions )"
	exit 1
fi

exec grails prod run-app --port 8090
