#!/usr/bin/env bash

sudo service avx-apps-akka stop

rm -rf src/main/resources
cp -R src/universal/conf src/main/resources

#Set correct sharedSecret and avxWebAPI
sudo sed -i "s/sharedSecret = \"changeme\"/sharedSecret = \"$(sudo avx-conf --salt | grep Secret: | cut -d ' ' -f 6)\"/g" src/main/resources/application.conf
sudo sed -i "s/avxWebAPI = \"https:\/\/192.168.23.33\/averox\/api\"/avxWebAPI = \"https:\/\/$(hostname -f)\/averox\/api\"/g" src/main/resources/application.conf

#sbt update - Resolves and retrieves external dependencies, more details in https://www.scala-sbt.org/1.x/docs/Command-Line-Reference.html
#sbt ~reStart (instead of run) - run with "triggered restart" mode, more details in #https://github.com/spray/sbt-revolver
exec sbt update ~reStart
