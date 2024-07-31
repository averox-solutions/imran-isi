#!/bin/bash -e

BBB_USER=averox

case "$1" in
  configure|upgrade|1|2)
    
  TARGET=/usr/local/averox/core/scripts/podcast.yml

  if [ -f $SERVLET_DIR/WEB-INF/classes/averox.properties ]; then
    HOST=$(cat $SERVLET_DIR/WEB-INF/classes/averox.properties | sed -n '/^averox.web.serverURL/{s/.*\///;p}')
  else
    HOST=$IP
  fi

  if [ -f $TARGET ]; then
    yq e -i ".playback_host = \"$HOST\"" $TARGET
  else
    echo "No: $TARGET"
    exit 1
  fi
    
    mkdir -p /var/averox/published/podcast
    chown -R $BBB_USER:$BBB_USER /var/averox/published/podcast
    chmod -R o+rx /var/averox/published/
    
    mkdir -p /var/log/averox/podcast
    chown -R $BBB_USER:$BBB_USER /var/log/averox/podcast
    
    mkdir -p /var/averox/recording/publish/podcast
    chown -R $BBB_USER:$BBB_USER /var/averox/recording/publish/podcast
    
    if [ -f /var/averox/published/podcast/index.html ]; then
      rm /var/averox/published/podcast/index.html
    fi
    
  ;;
  
  failed-upgrade)
  ;;

  *)
    echo "## postinst called with unknown argument \`$1'" >&2
  ;;
esac

