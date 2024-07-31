#!/bin/bash -e

if [ -f /etc/redhat-release ]; then
  DISTRIB_ID=centos
else
  . /etc/lsb-release    # Get value for DISTRIB_ID
fi

BIGBLUEBUTTON_USER=averox

case "$1" in
  configure|upgrade|1|2)
    
    TARGET=/usr/local/averox/core/scripts/video.yml

    chmod +r $TARGET
    
    mkdir -p /var/averox/published/video
    chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/published/video
    chmod -R o+rx /var/averox/published/
    
    mkdir -p /var/log/averox/video
    chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/log/averox/video
    
    mkdir -p /var/averox/recording/publish/video
    chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/recording/publish/video
    
    if [ -f /var/averox/published/video/index.html ]; then
      rm /var/averox/published/video/index.html
    fi
    
    reloadService nginx
  ;;
  
  failed-upgrade)
  ;;

  *)
    echo "## postinst called with unknown argument \`$1'" >&2
  ;;
esac

