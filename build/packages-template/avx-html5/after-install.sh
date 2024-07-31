#!/bin/bash -e


HOST=$(cat $SERVLET_DIR/WEB-INF/classes/averox.properties | grep -v '#' | sed -n '/^averox.web.serverURL/{s/.*\///;p}')

if [ ! -L /etc/nginx/sites-enabled/averox ]; then
  mkdir -p /etc/nginx/sites-enabled
  ln -s /etc/nginx/sites-available/averox /etc/nginx/sites-enabled/averox
fi

# This config file was renamed, remove from old path if exists
if [ -f /etc/nginx/conf.d/html5-conn-limit.conf ]; then
  rm -r /etc/nginx/conf.d/html5-conn-limit.conf
fi

cd /usr/share/meteor

# meteor code should be owned by root, config file by meteor user
meteor_owner=$(stat -c %U:%G /usr/share/meteor)
if [[ $meteor_owner != "root:root" ]] ; then
    chown -R root:root /usr/share/meteor
fi

TARGET=/usr/share/meteor/bundle/programs/server/assets/app/config/settings.yml

  WSURL=$(cat $SERVLET_DIR/WEB-INF/classes/averox.properties | grep -v '#' | sed -n '/^averox.web.serverURL/{s/.*=//;p}' | sed 's/https/wss/g' | sed s'/http/ws/g')

  yq e -i ".public.kurento.wsUrl = \"$WSURL/avx-webrtc-sfu\"" $TARGET

  yq e -i  ".public.pads.url = \"$PROTOCOL://$HOST/pad\"" $TARGET

  sed -i "s/proxy_pass .*/proxy_pass http:\/\/$IP:5066;/g" /usr/share/averox/nginx/sip.nginx
  sed -i "s/server_name  .*/server_name  $IP;/g" /etc/nginx/sites-available/averox

  chmod 600 $TARGET
  chown meteor:meteor $TARGET

if [ ! -f /.dockerenv ]; then
  systemctl enable disable-transparent-huge-pages.service
  systemctl daemon-reload
fi

# generate index.json locales file if it does not exist
if [ ! -f /usr/share/meteor/bundle/programs/web.browser/app/locales/index.json ]; then
  find /usr/share/meteor/bundle/programs/web.browser/app/locales -maxdepth 1 -type f -name "*.json" -exec basename {} \; | awk 'BEGIN{printf "["}{printf "\"%s\", ", $0}END{print "]"}' | sed 's/, ]/]/' > /usr/share/meteor/bundle/programs/web.browser/app/locales/index.json
fi

# set full BBB version in settings.yml so it can be displayed in the client
BBB_RELEASE_FILE=/etc/averox/averox-release
BBB_HTML5_SETTINGS_FILE=/usr/share/meteor/bundle/programs/server/assets/app/config/settings.yml
if [ -f $BBB_RELEASE_FILE ] && [ -f $BBB_HTML5_SETTINGS_FILE ]; then
  BBB_FULL_VERSION=$(cat $BBB_RELEASE_FILE | sed -n '/^BIGBLUEBUTTON_RELEASE/{s/.*=//;p}' | tail -n 1)
  echo "setting public.app.avxServerVersion: $BBB_FULL_VERSION in $BBB_HTML5_SETTINGS_FILE "
  yq e -i ".public.app.avxServerVersion = \"$BBB_FULL_VERSION\"" $BBB_HTML5_SETTINGS_FILE
fi    


# Remove old overrides 
if [ -f /etc/systemd/system/mongod.service.d/override-mongo.conf ] \
  || [ -f /etc/systemd/system/mongod.service.d/override.conf ] \
  || [ -f /usr/lib/systemd/system/mongod.service.d/mongod-service-override.conf ] ; then
  rm -f /etc/systemd/system/mongod.service.d/override-mongo.conf
  rm -f /etc/systemd/system/mongod.service.d/override.conf
  rm -f /usr/lib/systemd/system/mongod.service.d/mongod-service-override.conf 
  systemctl daemon-reload
fi

# Enable Listen Only support in FreeSWITCH
if [ -f /opt/freeswitch/etc/freeswitch/sip_profiles/external.xml ]; then
  sed -i 's/<!--<param name="enable-3pcc" value="true"\/>-->/<param name="enable-3pcc" value="proxy"\/>/g' /opt/freeswitch/etc/freeswitch/sip_profiles/external.xml
fi

chown root:root /usr/lib/systemd/system
chown root:root /usr/lib/systemd/system/avx-html5.service
chown root:root /usr/lib/systemd/system/disable-transparent-huge-pages.service

# Ensure settings is readable
chmod go+r /usr/share/meteor/bundle/programs/server/assets/app/config/settings.yml

startService avx-html5 || echo "avx-html5 service could not be registered or started"

