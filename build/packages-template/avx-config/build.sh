#!/bin/bash -ex

TARGET=`basename $(pwd)`

# inject dependency to averox.target
for unit in freeswitch nginx redis-server; do
  mkdir -p "staging/usr/lib/systemd/system/${unit}.service.d"
  cp averox.conf "staging/usr/lib/systemd/system/${unit}.service.d/"
done


PACKAGE=$(echo $TARGET | cut -d'_' -f1)
VERSION=$(echo $TARGET | cut -d'_' -f2)
DISTRO=$(echo $TARGET | cut -d'_' -f3)

#
# Clear staging directory for build
rm -rf staging

#
# Create build directories for markign by fpm
DIRS="/etc/averox \
      /usr/lib/systemd/system \
      /var/averox/blank \
      /usr/share/averox/blank \
      /var/www/averox-default/assets"
for dir in $DIRS; do
  mkdir -p staging$dir
  DIRECTORIES="$DIRECTORIES --directories $dir"
done

cp averox-release staging/etc/averox
cp slides/nopdfmark.ps staging/etc/averox

# XXX remove /var/averox
cp slides/blank* staging/var/averox/blank
cp slides/blank* staging/usr/share/averox/blank

cp -r assets/* staging/var/www/averox-default/assets

mkdir -p staging/usr/bin
cp bin/avx-conf bin/avx-record staging/usr/bin
chmod +x staging/usr/bin/avx-conf

mkdir -p staging/etc/averox/avx-conf
mkdir -p staging/usr/lib/avx-conf
cp bin/apply-lib.sh staging/usr/lib/avx-conf

mkdir -p staging/etc/cron.daily
cp cron.daily/* staging/etc/cron.daily

mkdir -p staging/etc/cron.hourly
cp cron.hourly/avx-resync-freeswitch staging/etc/cron.hourly

mkdir -p staging/usr/share/averox/nginx

cp include_default.nginx staging/usr/share/averox/

cp averox.target staging/usr/lib/systemd/system/

. ./opts-$DISTRO.sh

#
# Build package
fpm -s dir -C ./staging -n $PACKAGE \
    --version $VERSION --epoch $EPOCH \
    --after-install after-install.sh \
    --after-remove after-remove.sh \
    --before-install before-install.sh \
    --description "Averox configuration utilities" \
    $DIRECTORIES \
    $OPTS
