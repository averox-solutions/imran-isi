#!/bin/bash -ex

TARGET=`basename $(pwd)`


PACKAGE=$(echo $TARGET | cut -d'_' -f1)
VERSION=$(echo $TARGET | cut -d'_' -f2)
DISTRO=$(echo $TARGET | cut -d'_' -f3)
TAG=$(echo $TARGET | cut -d'_' -f4)
BUILD=$1

# Clean up directories
rm -rf staging
rm -rf ./build

# Create directories for fpm to process
DIRS="/usr/local/bin /lib/systemd/system /etc/default /usr/share/averox/nginx"
for dir in $DIRS; do
  mkdir -p staging$dir
done

# go mod tidy
go version


CGO_ENABLED=0 go build -o avx-graphql-middleware cmd/avx-graphql-middleware/main.go
echo "Build of avx-graphql-middleware finished"

cp avx-graphql-middleware staging/usr/local/bin/avx-graphql-middleware

mkdir -p staging/etc/nginx/conf.d
cp avx-graphql-client-settings-cache.conf staging/etc/nginx/conf.d

# Create service avx-graphql-middleware
cp avx-graphql-middleware-config.env staging/etc/default/avx-graphql-middleware
cp avx-graphql-middleware.service staging/lib/systemd/system/avx-graphql-middleware.service

# Set nginx location
cp graphql.nginx staging/usr/share/averox/nginx

. ./opts-$DISTRO.sh

#
# Build package
fpm -s dir -C ./staging -n $PACKAGE \
    --version $VERSION --epoch $EPOCH \
    --after-install after-install.sh \
    --after-remove after-remove.sh \
    --before-remove before-remove.sh \
    --description "GraphQL middleware component for Averox" \
    $DIRECTORIES \
    $OPTS
