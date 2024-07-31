#!/bin/bash
#
# Install a Big Blue Button testing server on a VM

# if these are running, our apt operations may error out unable to get a lock
sudo systemctl stop unattended-upgrades.service
echo Waiting for apt-daily.service and apt-daily-upgrade.service
sudo systemd-run --property="After=apt-daily.service apt-daily-upgrade.service" --wait /bin/true

sudo apt update
sudo DEBIAN_FRONTEND=noninteractive apt -y upgrade

DOMAIN=$(hostname --domain)
FQDN=$(hostname --fqdn)

EMAIL="root@$FQDN"

# /avx-install.sh (the proper version; either 2.4, 2.5 or 2.6) is created by gns3-avx.py
# INSTALL_OPTIONS and RELEASE get passed in the environment from gns3-avx.py
#
# INSTALL_OPTIONS can include -w (firewall) -a (api demos; deprecated in 2.6) -r (repository)

sudo /avx-install.sh -v $RELEASE -s $FQDN -e $EMAIL $INSTALL_OPTIONS

sudo avx-conf --salt avxci
echo "NODE_EXTRA_CA_CERTS=/usr/local/share/ca-certificates/avx-dev/avx-dev-ca.crt" | sudo tee -a /usr/share/meteor/bundle/avx-html5-with-roles.conf

# avx-conf --salt doesn't set the shared secret on the web demo
if [ -r /var/lib/tomcat9/webapps/demo/avx_api_conf.jsp ]; then
   sudo sed -i '/salt/s/"[^"]*"/"avxci"/'  /var/lib/tomcat9/webapps/demo/avx_api_conf.jsp
fi

# if nginx didn't start because of a hash bucket size issue,
# certbot didn't work properly and we need to re-run the entire install script
if systemctl -q is-failed nginx; then
    sudo sed -i '/server_names_hash_bucket_size/s/^\(\s*\)# /\1/' /etc/nginx/nginx.conf
    sudo /avx-install.sh -v $RELEASE -s $FQDN -e $EMAIL $INSTALL_OPTIONS
fi

# We can't restart if nginx isn't running.  It'll just complain "nginx.service is not active, cannot reload"
# sudo avx-conf --restart
sudo avx-conf --stop
sudo avx-conf --start
