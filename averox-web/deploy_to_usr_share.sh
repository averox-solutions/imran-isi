#!/usr/bin/env bash
cd "$(dirname "$0")"
sudo service avx-web stop
./build.sh

grails assemble
mkdir -p exploded && cd exploded
jar -xvf ../build/libs/averox-0.10.0.war

if [ ! -d /usr/share/avx-web-old ] ; then
	sudo cp -R /usr/share/avx-web /usr/share/avx-web-old
	echo "A backup was saved in /usr/share/avx-web-old"
else
	echo "A backup in /usr/share/avx-web-old already exists. Skipping.."
fi
sudo rm -rf /usr/share/avx-web/assets/ /usr/share/avx-web/META-INF/ /usr/share/avx-web/org/ /usr/share/avx-web/WEB-INF/
sudo cp -R . /usr/share/avx-web/
sudo chown averox:averox /usr/share/avx-web
sudo chown -R averox:averox /usr/share/avx-web/assets/ /usr/share/avx-web/META-INF/ /usr/share/avx-web/org/ /usr/share/avx-web/WEB-INF/
echo ''
echo ''
echo '----------------'
echo 'avx-web updated'

cd ..
sudo rm -r exploded
sudo service avx-web start

echo 'starting service avx-web'

