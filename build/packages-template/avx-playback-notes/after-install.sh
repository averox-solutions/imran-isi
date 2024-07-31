#!/bin/bash -e
BIGBLUEBUTTON_USER=averox

case "$1" in
  configure|upgrade|1|2)

    TARGET=/usr/local/averox/core/scripts/notes.yml

    chmod +r $TARGET
 
    mkdir -p /var/averox/published/notes
    chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/published/notes
    chmod -R o+rx /var/averox/published/
    
    mkdir -p /var/log/averox/notes
    chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/log/averox/notes
    
    mkdir -p /var/averox/recording/publish/notes
    chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/recording/publish/notes
    
    if [ -f /var/averox/published/notes/index.html ]; then
      rm /var/averox/published/notes/index.html
    fi
    
    systemctl reload nginx
  ;;
  
  failed-upgrade)
  ;;

  *)
    echo "## postinst called with unknown argument \`$1'" >&2
  ;;
esac

