#!/bin/bash -ex

TARGET=`basename $(pwd)`


PACKAGE=$(echo $TARGET | cut -d'_' -f1)
DISTRO=$(echo $TARGET | cut -d'_' -f3)
set -e -x
echo "START BUILDING in $PWD"
#
# Clean up directories
STAGING="$PWD/staging"
rm -rf $STAGING

. ./opts-$DISTRO.sh

#
# Create directory for fpm to process
DIRS="/var/averox/configs /var/log/averox /var/log/averox/html5"
for dir in $DIRS; do
  mkdir -p "${STAGING}/${dir}"
done

mkdir -p ~/.sbt/1.0
echo 'resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"' > ~/.sbt/1.0/global.sbt

##

EPHEMERAL_VERSION=0.0.$(date +%s)-SNAPSHOT
sed -i "s|\(version := \)\".*|\1\"$EPHEMERAL_VERSION\"|g" avx-common-message/build.sbt
find -name build.gradle -exec sed -i "s|\(.*org.averox.*avx-common-message[^:]*\):.*|\1:$EPHEMERAL_VERSION\"|g" {} \;
find -name build.sbt -exec sed -i "s|\(.*org.averox.*avx-common-message[^\"]*\"[ ]*%[ ]*\)\"[^\"]*\"\(.*\)|\1\"$EPHEMERAL_VERSION\"\2|g" {} \;

sed -i "s|\(version := \)\".*|\1\"$EPHEMERAL_VERSION\"|g" avx-common-web/build.sbt
find -name build.gradle -exec sed -i "s|\(.*org.averox.*avx-common-web[^:]*\):.*|\1:$EPHEMERAL_VERSION\"|g" {} \;
find -name build.sbt -exec sed -i "s|\(.*org.averox.*avx-common-web[^\"]*\"[ ]*%[ ]*\)\"[^\"]*\"\(.*\)|\1\"$EPHEMERAL_VERSION\"\2|g" {} \;

sed -i 's/\r$//' avx-common-web/project/Dependencies.scala
sed -i 's|\(val avxCommons = \)"[^"]*"$|\1"EPHEMERAL_VERSION"|g' avx-common-web/project/Dependencies.scala
sed -i "s/EPHEMERAL_VERSION/$EPHEMERAL_VERSION/g" avx-common-web/project/Dependencies.scala

echo start building avx-common-message
cd avx-common-message
sbt publish
sbt publishLocal
cd ..
echo end building avx-common-message

# New project directory containing parts of avx-web
cd avx-common-web
sbt update
sbt publish
sbt publishLocal
cd ..

cd averox-web
# Build new version of avx-web
gradle clean
gradle resolveDeps
grails assemble

# Build presentation checker
if [ -d pres-checker ]; then
  cd pres-checker
    gradle clean
    gradle resolveDeps
    gradle jar
    mkdir -p "$STAGING"/usr/share/prescheck/lib
    cp lib/* "$STAGING"/usr/share/prescheck/lib
    cp build/libs/avx-pres-check-0.0.1.jar "$STAGING"/usr/share/prescheck/lib
    cp run.sh "$STAGING"/usr/share/prescheck/prescheck.sh
    chmod +x "$STAGING"/usr/share/prescheck/prescheck.sh
  cd ..
fi

echo $PWD

mkdir -p "$STAGING"/usr/share/avx-web
mv build/libs/averox-0.10.0.war "$STAGING"/usr/share/avx-web

mkdir -p "$STAGING"/etc/default
cp ../avx-web.env "$STAGING"/etc/default/avx-web

mkdir -p "$STAGING"/lib/systemd/system
cp ../avx-web.service "$STAGING"/lib/systemd/system

pushd "$STAGING"/usr/share/avx-web
jar -xvf averox-0.10.0.war
rm averox-0.10.0.war
popd
pwd

# Copy this as simply 'web' and we'll make a symbolic link later in the .postinst script
mkdir -p "$STAGING"/usr/share/averox/nginx
cp avx-web.nginx "$STAGING"/usr/share/averox/nginx/web
cp loadbalancer.nginx "$STAGING"/usr/share/averox/nginx/loadbalancer.nginx

mkdir -p "$STAGING"/var/log/averox
# Copy directive for serving SVG files (HTML5) from nginx
if [ -f nginx-confs/presentation-slides.nginx ]; then
  cp nginx-confs/presentation-slides.nginx "$STAGING"/usr/share/averox/nginx
fi

mkdir -p "$STAGING"/var/averox/diagnostics

##
cd ..

fpm -s dir -C "$STAGING" -n $PACKAGE \
    --version $VERSION --epoch $EPOCH \
    --before-install before-install.sh      \
    --after-install after-install.sh        \
    --description "Averox API" \
    $DIRECTORIES \
    $OPTS
