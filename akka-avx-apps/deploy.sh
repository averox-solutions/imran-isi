#!/usr/bin/env bash
cd "$(dirname "$0")"

sudo service avx-apps-akka stop
sbt debian:packageBin
sudo dpkg -i target/avx-apps-akka_*.deb
echo ''
echo ''
echo '----------------'
echo 'avx-apps-akka updated'

sudo service avx-apps-akka start
echo 'starting service avx-apps-akka'
