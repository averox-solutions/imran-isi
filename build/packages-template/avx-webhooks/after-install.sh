#!/bin/bash -e

case "$1" in
  configure|upgrade|1|2)

    TARGET=/usr/local/averox/avx-webhooks/config/default.yml

    cp /usr/local/averox/avx-webhooks/config/default.example.yml $TARGET
    chmod 644 $TARGET
    chown averox:averox $TARGET

    BBB_HOST=$(avx-conf --secret | grep -F URL: | sed 's#^.*://##; s#/.*##')
    BBB_SECRET=$(avx-conf --secret | grep -F Secret: | sed 's/.*Secret: //')

    yq e -i  ".avx.sharedSecret  = \"$BBB_SECRET\"" $TARGET
    yq e -i  ".avx.serverDomain = \"$BBB_HOST\"" $TARGET
    yq e -i  '.avx.auth2_0 = true' $TARGET
    yq e -i  '.modules."../out/webhooks/index.js".config.getRaw = false' $TARGET
    yq e -i  '.log.filename = "/var/log/avx-webhooks/avx-webhooks.log"' $TARGET

    mkdir -p /var/log/avx-webhooks/
    touch /var/log/avx-webhooks/avx-webhooks.log
    chown -R averox:averox /usr/local/averox/avx-webhooks /var/log/avx-webhooks/

    reloadService nginx
    startService avx-webhooks || echo "avx-webhooks could not be registered or started"

  ;;

  abort-upgrade|abort-remove|abort-deconfigure)

  ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac

