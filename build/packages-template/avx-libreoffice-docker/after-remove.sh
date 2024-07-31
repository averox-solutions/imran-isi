#!/bin/bash -e


if [ $1 == 0 ]; then
  rm -rf /etc/sudoers.d/zzz-avx-docker-libreoffice
fi
