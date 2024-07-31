#!/bin/bash -e

if [ ! -f /etc/averox/livekit.yaml ]; then
  keys="$(livekit-server generate-keys|awk 'BEGIN { ORS=": "} { print $3 }')"
 (umask 007; cat << EOT
# This file will be merged with /usr/share/livekit-server/livekit.yaml
# on startup. Settings specified here will take  precedence.

logging:
  level: debug
keys:
  $keys
cat << EOT > /etc/averox/livekit.yaml
    chown averox:averox /etc/averox/livekit.yaml
  fi

  if [ ! -f /.dockerenv ]; then
    systemctl enable livekit-server.service
    systemctl daemon-reload
    startService livekit-server.service || echo "livekit-server service could not be registered or started"
  fi
EOT
  )
fi
