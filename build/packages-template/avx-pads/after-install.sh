#!/bin/bash -e

case "$1" in
  configure|upgrade|1|2)
    TARGET=/usr/local/averox/avx-pads/config/settings.json
    cp /usr/local/averox/avx-pads/config/settings.json.template $TARGET

    if [ -f /usr/share/etherpad-lite/APIKEY.txt ]; then
      API_KEY=$(cat /usr/share/etherpad-lite/APIKEY.txt)
      sed -i "s/ETHERPAD_API_KEY/\"$API_KEY\"/g" $TARGET

      startService avx-pads || echo "avx-pads could not be registered or started"
    else
      echo "avx-pads missing Etherpad's APIKEY.txt file"
      echo "avx-pads could not be registered or started"
    fi
  ;;

  abort-upgrade|abort-remove|abort-deconfigure)
  ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac
