#!/bin/bash -e

if ! which docker > /dev/null; then
	echo "#"
	echo "# Unable to install avx-libreoffice-docker -- no docker available"
	echo "#"
	exit 0
fi

#if ! docker image inspect avx-soffice > /dev/null 2>&1; then
	cd /usr/share/avx-libreoffice
	echo "#"
	echo "# Building avx-libreoffice docker image"
	echo "#"
	docker build -t avx-soffice docker/
#fi


chmod +x /usr/share/avx-libreoffice-conversion/convert-cool.sh
chmod +x /usr/share/avx-libreoffice-conversion/convert-local.sh
chmod +x /usr/share/avx-libreoffice-conversion/convert-remote.sh
chmod +x /usr/share/avx-libreoffice-conversion/etherpad-export.sh


if [ ! -L /usr/share/avx-libreoffice-conversion/convert.sh ]; then
	ln -s /usr/share/avx-libreoffice-conversion/convert-local.sh /usr/share/avx-libreoffice-conversion/convert.sh
fi

cat > /etc/sudoers.d/zzz-avx-docker-libreoffice <<HERE
averox ALL=(ALL) NOPASSWD: /usr/bin/docker run --rm --memory=1g --memory-swap=1g --network none --env=HOME=/tmp/ -w /tmp/ --user=[0-9][0-9][0-9][0-9][0-9] -v /tmp/avx-soffice-averox/tmp.[0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z]/\:/data/ -v /usr/share/fonts/\:/usr/share/fonts/\:ro --rm avx-soffice sh -c timeout [0-9][0-9][0-9]s /usr/bin/soffice -env\:UserInstallation=file\:///tmp/ --convert-to pdf --outdir /data /data/file
etherpad ALL=(ALL) NOPASSWD: /usr/bin/docker run --rm --memory=1g --memory-swap=1g --network none --env=HOME=/tmp/ -w /tmp/ --user=[0-9][0-9][0-9][0-9][0-9] -v /tmp/avx-soffice-etherpad/tmp.[0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z]/\:/data/ -v /usr/share/fonts/\:/usr/share/fonts/\:ro --rm avx-soffice sh -c timeout [0-9][0-9][0-9]s /usr/bin/soffice -env\:UserInstallation=file\:///tmp/ --convert-to pdf --writer --outdir /data /data/file
etherpad ALL=(ALL) NOPASSWD: /usr/bin/docker run --rm --memory=1g --memory-swap=1g --network none --env=HOME=/tmp/ -w /tmp/ --user=[0-9][0-9][0-9][0-9][0-9] -v /tmp/avx-soffice-etherpad/tmp.[0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z]/\:/data/ -v /usr/share/fonts/\:/usr/share/fonts/\:ro --rm avx-soffice sh -c timeout [0-9][0-9][0-9]s /usr/bin/soffice -env\:UserInstallation=file\:///tmp/ --convert-to odt --writer --outdir /data /data/file
etherpad ALL=(ALL) NOPASSWD: /usr/bin/docker run --rm --memory=1g --memory-swap=1g --network none --env=HOME=/tmp/ -w /tmp/ --user=[0-9][0-9][0-9][0-9][0-9] -v /tmp/avx-soffice-etherpad/tmp.[0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z]/\:/data/ -v /usr/share/fonts/\:/usr/share/fonts/\:ro --rm avx-soffice sh -c timeout [0-9][0-9][0-9]s /usr/bin/soffice -env\:UserInstallation=file\:///tmp/ --convert-to doc --outdir /data /data/file
HERE

#for i in `seq 1 4` ; do
#
#	SOFFICE_WORK_DIR="/var/tmp/soffice_"`printf "%02d\n" ${i}`
#	mkdir -p $SOFFICE_WORK_DIR
#	chown averox:averox $SOFFICE_WORK_DIR
#
#        systemctl enable avx-libreoffice@${i}
#        systemctl start avx-libreoffice@${i}
#done

exit 0
