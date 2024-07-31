#!/usr/bin/env bash

sbt clean stage
sudo service avx-apps-akka stop
cd target/universal/stage
exec ./bin/avx-apps-akka
