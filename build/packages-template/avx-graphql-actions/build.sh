#!/bin/bash -ex

TARGET=`basename $(pwd)`

PACKAGE=$(echo $TARGET | cut -d'_' -f1)
VERSION=$(echo $TARGET | cut -d'_' -f2)
DISTRO=$(echo $TARGET | cut -d'_' -f3)
TAG=$(echo $TARGET | cut -d'_' -f4)

#
# Clean up directories
rm -rf staging

#
# package

mkdir -p staging/usr/local/averox/avx-graphql-actions-temp

find -maxdepth 1 ! -path . ! -name staging $(printf "! -name %s " $(cat .build-files)) -exec cp -r {} staging/usr/local/averox/avx-graphql-actions-temp/ \;

pushd .
cd staging/usr/local/averox/avx-graphql-actions-temp/
npm -v
npm ci --no-progress
npm run build
popd

mkdir -p staging/usr/local/averox/avx-graphql-actions
pushd .
cd staging/usr/local/averox/avx-graphql-actions/
mv ../avx-graphql-actions-temp/dist/* .
mv index.js avx-graphql-actions.js
cp ../avx-graphql-actions-temp/package.json .
cp ../avx-graphql-actions-temp/package-lock.json .
npm ci --no-progress --omit=dev
rm -rf ../avx-graphql-actions-temp/
popd

mkdir -p staging/usr/lib/systemd/system
cp avx-graphql-actions.service staging/usr/lib/systemd/system

echo "List files"
find staging/

##

. ./opts-$DISTRO.sh

#
# Build package
fpm -s dir -C ./staging -n $PACKAGE \
    --version $VERSION --epoch $EPOCH \
    --after-install after-install.sh \
    --before-install before-install.sh \
    --before-remove before-remove.sh \
    --description "Averox GraphQL Actions" \
    $DIRECTORIES \
    $OPTS \
    -d 'nodejs (>= 18)' -d 'nodejs (<< 20)'

