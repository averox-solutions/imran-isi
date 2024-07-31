#!/bin/bash -ex

TARGET=`basename $(pwd)`


PACKAGE=$(echo $TARGET | cut -d'_' -f1)
VERSION=$(echo $TARGET | cut -d'_' -f2)
DISTRO=$(echo $TARGET | cut -d'_' -f3)

DIRS="/usr/share/avx-libreoffice /usr/share/avx-libreoffice-conversion"
for dir in $DIRS; do
  mkdir -p staging$dir
  DIRECTORIES="$DIRECTORIES --directories $dir"
done

##

if [ $DISTRO != "amzn2" ]; then 
  mkdir -p staging/etc/sudoers.d
  cp assets/zzz-avx-docker-libreoffice  staging/etc/sudoers.d/zzz-avx-docker-libreoffice
fi

cp assets/etherpad-export.sh staging/usr/share/avx-libreoffice-conversion/etherpad-export.sh
cp assets/convert-local.sh  staging/usr/share/avx-libreoffice-conversion/convert-cool.sh
cp assets/convert-local.sh  staging/usr/share/avx-libreoffice-conversion/convert-local.sh
cp assets/convert-remote.sh staging/usr/share/avx-libreoffice-conversion/convert-remote.sh

chmod +x staging/usr/share/avx-libreoffice-conversion/convert-cool.sh
chmod +x staging/usr/share/avx-libreoffice-conversion/convert-local.sh
chmod +x staging/usr/share/avx-libreoffice-conversion/convert-remote.sh
chmod +x staging/usr/share/avx-libreoffice-conversion/etherpad-export.sh


cp -r docker staging/usr/share/avx-libreoffice

##

. ./opts-$DISTRO.sh

fpm -s dir -C ./staging -n $PACKAGE \
    --version $VERSION --epoch $EPOCH \
    --after-install after-install.sh        \
    --before-remove before-remove.sh        \
    --after-remove after-remove.sh          \
    --description "Averox setup for LibreOffice running in docker" \
    $DIRECTORIES \
    $OPTS
