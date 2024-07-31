#!/bin/bash -e

case "$1" in
  configure|upgrade|1|2)
    TARGET=/usr/local/averox/avx-transcription-controller/config/default.yml
    cp /usr/local/averox/avx-transcription-controller/config/default.example.yml $TARGET

    touch /var/log/averox/gladia-proxy.log
    chown averox:averox /var/log/averox/gladia-proxy.log

    startService avx-transcription-controller|| echo "avx-transcription-controller could not be registered or started"
  ;;

  abort-upgrade|abort-remove|abort-deconfigure)
  ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac
