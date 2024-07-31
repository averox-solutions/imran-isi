#!/bin/bash -e

BBB_USER=averox

case "$1" in
  configure|upgrade|1|2)
    
    TARGET=/usr/local/averox/core/scripts/averox.yml

    if [ -f /usr/local/averox/core/lib/recordandplayback.rb ]; then
      sed -i "s/require 'recordandplayback\/webrtc_deskshare_archiver/#require 'recordandplayback\/webrtc_deskshare_archiver/g" /usr/local/averox/core/lib/recordandplayback.rb
    fi

  if [ -f /etc/ImageMagick-6/policy.xml ]; then
    sed -i 's/<policy domain="coder" rights="none" pattern="PDF" \/>/<policy domain="coder" rights="write" pattern="PDF" \/>/g' /etc/ImageMagick-6/policy.xml
  fi

    if [ -f $SERVLET_DIR/WEB-INF/classes/averox.properties ]; then
      HOST=$(cat $SERVLET_DIR/WEB-INF/classes/averox.properties | sed -n '/^averox.web.serverURL/{s/.*\///;p}')
    else
      HOST=$IP
    fi

    yq e -i ".playback_host = \"$HOST\"" $TARGET

    chmod +r $TARGET

    # Run recording link fixup/upgrade script
    # Don't abort on failure; users can manually run it later, too
    if id $BBB_USER > /dev/null 2>&1 ; then
      mkdir -p /var/averox/recording/status
      chown $BBB_USER:$BBB_USER /var/averox/recording/status

      mkdir -p /var/averox/events
      chown $BBB_USER:$BBB_USER /var/averox/events

      mkdir -p /var/averox/recording
      mkdir -p /var/averox/recording/raw
      mkdir -p /var/averox/recording/process
      mkdir -p /var/averox/recording/publish
      mkdir -p /var/averox/recording/status/recorded
      mkdir -p /var/averox/recording/status/archived
      mkdir -p /var/averox/recording/status/processed
      mkdir -p /var/averox/recording/status/sanity
      mkdir -p /var/averox/recording/status/published
      chown -R $BBB_USER:$BBB_USER /var/averox/recording

      mkdir -p /var/averox/captions
      chown -R $BBB_USER:$BBB_USER /var/averox/captions

      mkdir -p /var/averox/published
      chown $BBB_USER:$BBB_USER /var/averox/published

      mkdir -p /var/averox/deleted
      chown $BBB_USER:$BBB_USER /var/averox/deleted

      mkdir -p /var/averox/unpublished
      chown $BBB_USER:$BBB_USER /var/averox/unpublished

      mkdir -p /var/averox/basic_stats
      chown $BBB_USER:$BBB_USER /var/averox/basic_stats

      chown -R $BBB_USER:$BBB_USER /var/log/averox
      chmod 755 /var/log/averox

      if [ -f /var/log/averox/avx-rap-worker.log ]; then
        chown $BBB_USER:$BBB_USER /var/log/averox/avx-rap-worker.log
      fi

      if [ -f /var/log/averox/sanity.log ]; then
        chown $BBB_USER:$BBB_USER /var/log/averox/sanity.log
      fi
      if [ -f /var/log/averox/post_process.log ]; then
        chown $BBB_USER:$BBB_USER /var/log/averox/post_process.log
      fi
      if [ -f /var/log/averox/avx-recording-cleanup.log ]; then
        chown $BBB_USER:$BBB_USER /var/log/averox/avx-recording-cleanup.log
      fi
    fi

    if id freeswitch >/dev/null 2>&1; then
      chown -R freeswitch:freeswitch /var/freeswitch/meetings
    else
      echo "Error: FreeSWITCH not installed"
    fi

    systemctl enable avx-rap-resque-worker.service
    systemctl enable avx-rap-starter.service
    systemctl enable avx-rap-caption-inbox.service
  ;;
  
  *)
    echo "## postinst called with unknown argument \`$1'" >&2
  ;;
esac

if dpkg -l | grep -q nginx; then
  reloadService nginx
fi

systemctl daemon-reload
