#!/usr/bin/env bash

sbt clean stage
sudo service avx-fsesl-akka stop
cd target/universal/stage
exec ./bin/avx-fsesl-akka
