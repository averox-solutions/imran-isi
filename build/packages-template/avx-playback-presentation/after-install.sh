#!/bin/bash -e

if [ -f /etc/redhat-release ]; then
  DISTRIB_ID=centos
else
  . /etc/lsb-release    # Get value for DISTRIB_ID
fi

BIGBLUEBUTTON_USER=averox

case "$1" in
  configure|upgrade|1|2)
    
    TARGET=/usr/local/averox/core/scripts/presentation.yml

    chmod +r $TARGET
    
    mkdir -p /var/averox/published/presentation
    chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/published/presentation
    chmod -R o+rx /var/averox/published/
    
    mkdir -p /var/log/averox/presentation
    chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/log/averox/presentation
    
    mkdir -p /var/averox/recording/publish/presentation
    chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/recording/publish/presentation
    
    if [ -f /var/averox/published/presentation/index.html ]; then
      rm /var/averox/published/presentation/index.html
    fi
    
  ;;
  
  failed-upgrade)
  ;;

  *)
    echo "## postinst called with unknown argument \`$1'" >&2
  ;;
esac

