#!/bin/bash -ex

source /etc/lsb-release

case "$1" in
  configure|upgrade|1|2)
    TARGET=/usr/local/averox/avx-webrtc-sfu/config/default.yml

    cp /usr/local/averox/avx-webrtc-sfu/config/default.example.yml $TARGET
    chown averox:averox $TARGET

    # Set mediasoup IPs
    yq e -i ".mediasoup.webrtc.listenIps[0].announcedIp = \"$IP\"" $TARGET
    yq e -i ".mediasoup.plainRtp.listenIp.announcedIp = \"$IP\"" $TARGET

    FREESWITCH_IP=$(xmlstarlet sel -t -v '//X-PRE-PROCESS[@cmd="set" and starts-with(@data, "local_ip_v4=")]/@data' /opt/freeswitch/conf/vars.xml | sed 's/local_ip_v4=//g')
    if [ "$FREESWITCH_IP" != "" ]; then
      yq e -i ".freeswitch.ip = \"$FREESWITCH_IP\"" $TARGET
      yq e -i ".freeswitch.sip_ip = \"$IP\"" $TARGET
    else
      # Looks like the FreeSWITCH package is being installed, let's fall back to the default value
      yq e -i ".freeswitch.ip = \"$IP\"" $TARGET
      if [ "$DISTRIB_CODENAME" == "focal" ]; then
        yq e -i ".freeswitch.sip_ip = \"$IP\"" $TARGET
      fi
    fi

    cd /usr/local/averox/avx-webrtc-sfu
    mkdir -p node_modules

    mkdir -p /var/log/avx-webrtc-sfu/
    touch /var/log/avx-webrtc-sfu/avx-webrtc-sfu.log

    yq e -i '.recordWebcams = true' $TARGET
    # Set avx-webrtc-recorder as the default recordingAdapter
    yq e -i '.recordingAdapter = "avx-webrtc-recorder"' $TARGET
    # Do not configure any Kurento instances - BBB >= 2.8 doesn't provide Kurento by default
    yq e -i '.kurento = []' $TARGET

    echo "Resetting mcs-address from localhost to 127.0.0.1"
    yq e -i '.mcs-address = "127.0.0.1"' $TARGET

    if id averox > /dev/null 2>&1; then
      chown -R averox:averox /usr/local/averox/avx-webrtc-sfu /var/log/avx-webrtc-sfu/
    else
      echo "#"
      echo "# Warning: Unable to assign ownership of averox to sfu files"
      echo "#"
    fi

    # Creates the mediasoup raw media file dir if needed
    if [ ! -d /var/mediasoup ]; then
      mkdir -p /var/mediasoup
    fi

    chmod 644 $TARGET
    chown averox:averox $TARGET

    reloadService nginx
    startService avx-webrtc-sfu || echo "avx-webrtc-sfu could not be registered or started"
    ;;

  abort-upgrade|abort-remove|abort-deconfigure)
    ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
    ;;
esac
