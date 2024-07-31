#!/bin/bash -e

if [ -f /etc/redhat-release ]; then
  DISTRIB_ID=centos
else
  . /etc/lsb-release    # Get value for DISTRIB_ID
fi


case "$1" in
  configure|upgrade|1|2)
    
    mkdir -p /var/averox/published/screenshare
    chown -R averox:averox /var/averox/published/screenshare
    chmod -R o+rx /var/averox/published/
    
    mkdir -p /var/log/averox/screenshare
    chown -R averox:averox /var/log/averox/screenshare
    
    mkdir -p /var/averox/recording/publish/screenshare
    chown -R averox:averox /var/averox/recording/publish/screenshare
    
    if [ -f /var/averox/published/screenshare/index.html ]; then
      rm /var/averox/published/screenshare/index.html
    fi
 
    if [ ! -f /.dockerenv ]; then
      systemctl restart nginx
    fi
    
  ;;
  
  failed-upgrade)
  ;;

  *)
    echo "## postinst called with unknown argument \`$1'" >&2
  ;;
esac

