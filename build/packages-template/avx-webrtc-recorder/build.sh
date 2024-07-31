#!/bin/bash -ex

TARGET=`basename $(pwd)`


PACKAGE=$(echo $TARGET | cut -d'_' -f1)
VERSION=$(echo $TARGET | cut -d'_' -f2)
DISTRO=$(echo $TARGET | cut -d'_' -f3)
BUILD=$1

# Clean up directories
rm -rf staging
rm -rf ./build

# Create directories for fpm to process
DIRS="/usr/lib/systemd/system /usr/bin /etc/default/ /etc/avx-webrtc-recorder /var/lib/avx-webrtc-recorder"
for dir in $DIRS; do
  mkdir -p staging$dir
done

mkdir -p ./build
cp ./packaging/env ./build/env
git config --global --add safe.directory ${PWD}
APP_VERSION=$(cat ./VERSION)
GOMOD=$(go list -m)
APP_REV=$(git rev-parse --short HEAD)
echo $GOMOD ${APP_VERSION[@]} $COMMIT
go mod tidy
go build -o ./build/avx-webrtc-recorder \
  -ldflags="-X '$GOMOD/internal.AppVersion=v${APP_VERSION[0]}-${APP_VERSION[1]} (${APP_REV})'" \
  ./cmd/avx-webrtc-recorder

cp ./build/avx-webrtc-recorder staging/usr/bin
cp ./build/env staging/etc/default/avx-webrtc-recorder
cp ./config/avx-webrtc-recorder.yml staging/etc/avx-webrtc-recorder/avx-webrtc-recorder.yml
cp avx-webrtc-recorder.service staging/usr/lib/systemd/system

. ./opts-$DISTRO.sh

fpm -s dir -C ./staging -n $PACKAGE                 \
    --version $VERSION --epoch $EPOCH               \
    --after-install after-install.sh                \
    --before-remove before-remove.sh                \
    --description "Averox WebRTC Recorder"   \
    $DIRECTORIES                                    \
    $OPTS
