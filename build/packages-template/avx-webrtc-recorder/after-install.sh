#!/bin/bash -e

BIGBLUEBUTTON_USER=averox

case "$1" in
  configure|upgrade|1|2)

    if id $BIGBLUEBUTTON_USER > /dev/null 2>&1 ; then
      chown $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/lib/avx-webrtc-recorder
      chmod 0700 /var/lib/avx-webrtc-recorder
    fi

    systemctl enable avx-webrtc-recorder
  ;;

  *)
    echo "## postinst called with unknown argument \`$1'" >&2
  ;;
esac

systemctl daemon-reload
