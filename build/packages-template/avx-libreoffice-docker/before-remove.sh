#!/bin/bash -ex

#NETWORK_CHECK=`docker network inspect avx-libreoffice &> /dev/null && echo 1 || echo 0`
if docker network inspect avx-libreoffice &> /dev/null; then
	echo "removing avx-libreoffice docker network"
	docker network remove avx-libreoffice
else
	echo "not removing avx-libreoffice docker network"
fi
