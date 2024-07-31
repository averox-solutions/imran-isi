#!/bin/bash -ex

TARGET=`basename $(pwd)`


PACKAGE=$(echo $TARGET | cut -d'_' -f1)
VERSION=$(echo $TARGET | cut -d'_' -f2)
DISTRO=$(echo $TARGET | cut -d'_' -f3)

#
# Clean up directories
rm -rf staging

#
# Create directory for fpm to process
DIRS="/usr/local/averox/core \
      /etc/logrotate.d \
      /var/averox/recording/status/ended \
      /var/averox/captions/inbox \
      /var/averox/recording/status"
for dir in $DIRS; do
  mkdir -p staging$dir
done

##

mkdir -p staging/var/log/averox
cp -r scripts lib Gemfile Gemfile.lock  staging/usr/local/averox/core

pushd staging/usr/local/averox/core
  bundle config set --local deployment true
  bundle install
  # Remove unneeded files to reduce package size
  bundle clean
  rm -r vendor/bundle/ruby/*/cache
  find vendor/bundle -name '*.o' -delete
  chmod -R a+rX .
  find vendor/bundle/ruby/*/gems/resque-*/lib/resque/server/public -type f -name "*.png" -execdir chmod 0644 {} \;
  find vendor/bundle/ruby/*/gems/resque-*/bin -type f -execdir chmod 0755 {} \;
popd

cp Rakefile  staging/usr/local/averox/core
cp avx-record-core.logrotate staging/etc/logrotate.d

SYSTEMDSYSTEMUNITDIR=$(pkg-config --variable systemdsystemunitdir systemd)
mkdir -p "staging${SYSTEMDSYSTEMUNITDIR}"
cp systemd/* "staging${SYSTEMDSYSTEMUNITDIR}"

if [ -f "staging/usr/local/averox/core/scripts/basic_stats.nginx" ]; then \
  mkdir -p staging/usr/share/averox/nginx; \
  mv staging/usr/local/averox/core/scripts/basic_stats.nginx staging/usr/share/averox/nginx; \
fi

##

. ./opts-$DISTRO.sh

fpm -s dir -C ./staging -n $PACKAGE \
    --version $VERSION --epoch $EPOCH \
    --before-install before-install.sh        \
    --after-install after-install.sh    \
    --before-remove before-remove.sh    \
    --description "Averox record and playback" \
    $DIRECTORIES \
    $OPTS