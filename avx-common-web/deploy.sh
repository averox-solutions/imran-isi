#!/bin/bash
set -e

#Publish new avx-common-web .jar
sbt clean 
# rm -r target/ project/target/ project/project/ lib_managed/
sbt update publish publishLocal
