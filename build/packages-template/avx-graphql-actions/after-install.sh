#!/bin/bash -e

case "$1" in
  configure|upgrade|1|2)

  fc-cache -f
  if [ ! -f /.dockerenv ]; then
    systemctl enable avx-graphql-actions.service
    systemctl daemon-reload
    startService avx-graphql-actions.service || echo "avx-graphql-actions service could not be registered or started"
  fi
  ;;

  abort-upgrade|abort-remove|abort-deconfigure)
  ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac
