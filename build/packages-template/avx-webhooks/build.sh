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
DIRS="/usr/share/averox/nginx /usr/local/averox/avx-webhooks"
for dir in $DIRS; do
  mkdir -p staging$dir
done

##

mkdir -p staging/usr/local/averox/avx-webhooks

find -maxdepth 1 ! -path . ! -name staging ! -name .git $(printf "! -name %s " $(cat .build-files)) -exec cp -r {} staging/usr/local/averox/avx-webhooks/ \;

pushd .
cd staging/usr/local/averox/avx-webhooks/
npm ci --omit=dev
popd

cp webhooks.nginx staging/usr/share/averox/nginx/webhooks.nginx

mkdir -p staging/usr/lib/systemd/system
cp avx-webhooks.service staging/usr/lib/systemd/system

##

. ./opts-$DISTRO.sh

fpm -s dir -C ./staging -n $PACKAGE                 \
    --version $VERSION --epoch $EPOCH \
    --after-install after-install.sh                \
    --before-install before-install.sh              \
    --before-remove before-remove.sh                \
    --description "Averox Webhooks"          \
    $DIRECTORIES                                    \
    $OPTS                                           \
    -d 'nodejs (>= 18)' -d 'nodejs (<< 20)'
