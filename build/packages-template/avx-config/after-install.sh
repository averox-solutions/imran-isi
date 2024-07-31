#!/bin/bash -e

set +x

removeOldOverride() {
    service_name=$1
    # check if override file has been modified. If not it can be safely removed
    if [ -f "/etc/systemd/system/${service_name}.service.d/override.conf" ] ; then
        if echo "d32a00b9a2669b3fe757b8de3470e358  /etc/systemd/system/${service_name}.service.d/override.conf" | md5sum -c --quiet 2>/dev/null >/dev/null ; then
            rm -f "/etc/systemd/system/${service_name}.service.d/override.conf"
        fi
    fi
    if [ -d "/etc/systemd/system/${service_name}.service.d" ]; then
        if [ $(ls "/etc/systemd/system/${service_name}.service.d" |wc -l) = 0 ]; then
            rmdir "/etc/systemd/system/${service_name}.service.d"
        fi
    fi
}

BIGBLUEBUTTON_USER=averox

if ! id freeswitch >/dev/null 2>&1; then
  echo "Error: FreeSWITCH not installed"
  exit 1
fi

if lsb_release -d | grep -q CentOS; then
  DISTRO=centos
  FREESWITCH=freeswitch
  FREESWITCH_GROUP=daemon
else
  DISTRO=ubuntu
  FREESWITCH=freeswitch
  FREESWITCH_GROUP=freeswitch
fi

#
# Set the permissions to /var/averox so services can write
#
if [ -d /var/averox ]; then
  echo -n "."
  chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox
  echo -n "."
  
  chmod o+rx /var/averox
 
  #
  # Setup for recordings XXX
  #
  mkdir -p /var/averox/recording
  mkdir -p /var/averox/recording/raw
  mkdir -p /var/averox/recording/process
  mkdir -p /var/averox/recording/publish
  mkdir -p /var/averox/recording/status
  mkdir -p /var/averox/recording/status/recorded
  mkdir -p /var/averox/recording/status/archived
  mkdir -p /var/averox/recording/status/processed
  mkdir -p /var/averox/recording/status/sanity
  echo -n "."
  chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/recording
  
  mkdir -p /var/averox/published
  echo -n "."
  chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/published
  
  mkdir -p /var/averox/deleted
  echo -n "."
  chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/deleted
  
  mkdir -p /var/averox/unpublished
  echo -n "."
  chown -R $BIGBLUEBUTTON_USER:$BIGBLUEBUTTON_USER /var/averox/unpublished
  echo
else
  echo "Warning: Averox not installed"
fi

if [ -f /usr/share/avx-apps-akka/conf/application.conf ]; then
  if [ "$(cat /usr/share/avx-apps-akka/conf/application.conf | sed -n '/sharedSecret.*/{s/[^"]*"//;s/".*//;p}')" == "changeme" ]; then
    SECRET=$(cat $SERVLET_DIR/WEB-INF/classes/averox.properties | grep -v '#' | tr -d '\r' | sed -n '/securitySalt/{s/.*=//;p}')
    sed -i "s/sharedSecret[ ]*=[ ]*\"[^\"]*\"/sharedSecret=\"$SECRET\"/g" \
       /usr/share/avx-apps-akka/conf/application.conf

    HOST=$(cat $SERVLET_DIR/WEB-INF/classes/averox.properties | grep -v '#' | sed -n '/^averox.web.serverURL/{s/.*\///;p}')
    sed -i  "s/avxWebAPI[ ]*=[ ]*\"[^\"]*\"/avxWebAPI=\"http:\/\/$HOST\/averox\/api\"/g" \
       /usr/share/avx-apps-akka/conf/application.conf
    sed -i "s/avxWebHost[ ]*=[ ]*\"[^\"]*\"/avxWebHost=\"$HOST\"/g" \
       /usr/share/avx-apps-akka/conf/application.conf
    sed -i "s/deskshareip[ ]*=[ ]*\"[^\"]*\"/deskshareip=\"$HOST\"/g" \
       /usr/share/avx-apps-akka/conf/application.conf
    sed -i "s/defaultPresentationURL[ ]*=[ ]*\"[^\"]*\"/defaultPresentationURL=\"http:\/\/$HOST\/default.pdf\"/g" \
       /usr/share/avx-apps-akka/conf/application.conf

  fi
fi

#
# Added to enable avx-record-core to move files #8901
#
usermod averox -a -G freeswitch
chmod 0775 /var/freeswitch/meetings

# Verify mediasoup raw media directories ownership and perms
if [ -d /var/mediasoup ]; then
  chown averox:averox /var/mediasoup
  chmod 0700 /var/mediasoup
fi

if [ -d /var/mediasoup/recordings ]; then
  chmod 0700 /var/mediasoup/recordings
fi

if [ -d /var/mediasoup/screenshare ]; then
  chmod 0700 /var/mediasoup/screenshare
fi

sed -i 's/worker_connections 768/worker_connections 10000/g' /etc/nginx/nginx.conf

if grep -q "worker_rlimit_nofile" /etc/nginx/nginx.conf; then
  num=$(grep worker_rlimit_nofile /etc/nginx/nginx.conf | grep -o '[0-9]*')
  if [[ "$num" -lt 10000 ]]; then
    sed -i 's/worker_rlimit_nofile [0-9 ]*;/worker_rlimit_nofile 10000;/g' /etc/nginx/nginx.conf
  fi
else
  sed -i 's/events {/worker_rlimit_nofile 10000;\n\nevents {/g' /etc/nginx/nginx.conf
fi

mkdir -p /etc/averox/nginx

# symlink default avx nginx config from package if it does not exist
if [ ! -e /etc/averox/nginx/include_default.nginx ] ; then
  ln -s /usr/share/averox/include_default.nginx /etc/averox/nginx/include_default.nginx
fi

# set full BBB version in settings.yml so it can be displayed in the client
BBB_RELEASE_FILE=/etc/averox/averox-release
BBB_HTML5_SETTINGS_FILE=/usr/share/meteor/bundle/programs/server/assets/app/config/settings.yml
if [ -f $BBB_RELEASE_FILE ] && [ -f $BBB_HTML5_SETTINGS_FILE ]; then
  BBB_FULL_VERSION=$(cat $BBB_RELEASE_FILE | sed -n '/^BIGBLUEBUTTON_RELEASE/{s/.*=//;p}' | tail -n 1)
  echo "setting public.app.avxServerVersion: $BBB_FULL_VERSION in $BBB_HTML5_SETTINGS_FILE "
  yq e -i ".public.app.avxServerVersion = \"$BBB_FULL_VERSION\"" $BBB_HTML5_SETTINGS_FILE
fi

# Fix permissions for logging
chown averox:averox /var/log/avx-fsesl-akka

# cleanup old overrides

removeOldOverride avx-apps-akka
removeOldOverride avx-fsesl-akka
removeOldOverride avx-transcode-akka


# re-create the symlink for apply-lib.sh to ensure the latest version is present
if [ -f /etc/averox/avx-conf/apply-lib.sh ]; then
  rm /etc/averox/avx-conf/apply-lib.sh
fi
if [ -f /usr/lib/avx-conf/apply-lib.sh ]; then
  ln -s /usr/lib/avx-conf/apply-lib.sh /etc/averox/avx-conf/apply-lib.sh
fi

# Load the overrides
systemctl daemon-reload
