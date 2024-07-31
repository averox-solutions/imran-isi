---
id: guide
slug: /development/guide
title: Development Guide
sidebar_position: 2
description: Averox Development Guide
keywords:
- development
- developer
---

Welcome to the Averox Developer's Guide for Averox 3.0.

This document gives you an overview of how to set up a development environment for Averox 3.0.

## Before you begin

You first need to set up a Averox 3.0 server. See the instructions at [Install Averox 3.0](/administration/install).

## Overview

A Averox server is built from a number of components that correspond to Ubuntu packages. Some of these components are

- avx-web -- Implements the Averox API and conversion of documents for presentation
- avx-akka-apps -- Server side application that handles the state of meetings on the server
- avx-akka-fsesl -- server component handling the communication with FreeSWITCH
- avx-html5 -- HTML5 client that loads in the browser
- avx-learning-dashboard -- a live dashboard available to moderators, which displays user activity information that can be useful for instructors
- avx-fsesl-akka -- Component to send commands to FreeSWITCH
- avx-playback-presentation -- Record and playback script to create presentation layout
- avx-playback-video -- Record and playback script to create video layout
- avx-export-annotations -- Handles capture of breakout content and annotated presentation download
- avx-webrtc-sfu -- Server that bridges incoming requests from client to Kurento
- mediasoup -- WebRTC media server for sending/receiving/recording video (webcam and screenshare)
- avx-freeswitch-core -- WebRTC media server for sending/receiving/recording audio
- avx-graphql-server -- Handles GraphQL queries and subscriptions from clients, checks user permissions
- avx-graphql-middleware -- forwards messages between the clients and avx-graphql-server, json-patch minimizes the info needed to be sent
- avx-graphql-actions -- handles requests from the clients on their way to the core
- avx-webrtc-sfu -- manages media connections
- avx-webrtc-recorder -- handles recording of the media
- avx-pads -- manages the control to Etherpad
- avx-transcription-controller -- an optional component managing captions for third party services like VOSK or Gladia
- avx-etherpad -- used for shared notes and captions, live edit of text by multiple parties
- avx-webhooks -- an optional componen, listens for all events on Averox and sends POST requests with details about these events to hooks registered via an API

This document describes how to set up a development environment using an existing Averox 3.0 server. Once the environment is set up, you will be able to make custom changes to Averox source code, compile it, and replace the corresponding components on the server (such as updating the Averox client).

The instructions in this guide are step-by-step so you can understand each step needed to modify a component. If you encounter problems or errors at any section, don't ignore the errors. Stop and double-check that you have done the step correctly. If you are unable to determine the cause of the error, do the following

- First, use Google to search for the error. There is a wealth of information in [averox-dev](https://groups.google.com/forum/?fromgroups=#!forum/averox-dev) that has been indexed by Google.
- Try doing the same steps on a different Averox server.
- Post a question to averox-dev with a description of the problem and the steps to reproduce. Post logs and error messages to [Pastebin](https://pastebin.com) link them in your post.

#### You Have a Working Averox Server

Before you can start developing on Averox, you must install Averox (see [installation steps](/administration/install)) and ensure it's working correctly. Make sure there were no errors during the installation and that you can join a session successfully.

We emphasize that your Averox server must be working **before** you start setting up the development environment. Be sure that you can log in, start sessions, join the audio bridge, share your webcam, and record and play back sessions -- you can verify this if you install [Greenlight](/averoxserver/v3/install) or navigate to [API MATE](https://mconf.github.io/api-mate/) using your server's secret and url (just run the command `avx-conf --salt` to obtain a link for API MATE).

By starting with a working Averox server, you have the ability to switch back-and-forth between the default-packaged components and any modifications you make.

For example, suppose you modify the Averox client and something isn't working (such as the client is not fully loading), you can easily switch back to the default-packaged client and check that it's working correctly (thus ruling out any environment issues that may also be preventing your modified client from loading).

**Another Note:** These instructions assume you have Greenlight installed so you can create and join meetings to test your setup. Alternatively you can use API MATE (just run the command `avx-conf --salt` to obtain a link for API MATE).

#### Developing on Windows

To develop Averox from within Windows, you have two options:

- use Windows Subsystem for Linux
- use VMWare Player or VirtualBox to create a virtual machine (VM).

Choose the OS to be Ubuntu 22.04 64-bit. The associated documentation for VMWare Player and VirtualBox or WSL will guide you on setting up a new 22.04 64-bit VM.

**Note:** When setting up the VM, it does not matter to Averox if you set up Ubuntu 22.04 server or desktop. If you install desktop, you'll have the option of using a graphical interface to edit files. When running the VM, you will need a host operating system capable of running a [64-bit virtual machine](https://stackoverflow.com/questions/56124/can-i-run-a-64-bit-vmware-image-on-a-32-bit-machine).

#### Developing on Linux host via container

Consider using a Docker setup for a development environment - [https://github.com/averox/docker-dev](https://github.com/averox/docker-dev). This is the way most of the core developers follow.

#### Root Privileges

**Important:** The setup is **much** easier if you develop as user 'averox' and just add a home directory via

```
usermod -a -G sudo averox
mkhomedir_helper averox # to add homedir to existing user
chown -R averox:averox /home/averox/
sudo su - averox
# if you cannot switch to user averox, you may need to switch /bin/false to /bin/bash for user averox in /etc/passwd
# and then retry "sudo su - averox"
# Note that you may want to disable terminal sessions for user averox if you will later use the server in production
```

```bash
sudo ls
```

#### wget

You'll need to download some files throughout these instructions using wget. If it's not installed on your server, you can install the package using the following command

```bash
sudo apt-get install wget
```

#### Have a GitHub Account

The Averox [source is hosted on GitHub](https://github.com/averox/averox). You need a GitHub account. In addition, you need to be very familiar with how git works. Specifically, you need to know how to

- clone a repository
- create a branch
- push changes back to a repository

If you have not used git before, or if the terms **_clone_**, **_branch_**, and **_commit_** are unfamiliar to you, stop now. These are fundamental concepts to git that you need to become competent with before trying to develop on Averox. To become competent, a good place to start is this [free book](https://git-scm.com/book) and [GitHub Help pages](https://help.github.com/).

Using GitHub makes it easy for you to work on your own copy of the Averox source, store your updates to the source to your GitHub account, and make it easy for you to [contribute to Averox](/support/faq#contributing-to-averox).

#### Subscribe to averox-dev

We recommend you subscribe to the [averox-dev](https://groups.google.com/group/averox-dev/topics?gvc=2) mailing list to follow updates to the development of Averox and to collaborate with other developers. Make sure to include a brief note when you request access so we can distinguish you from spam bots.

## Set up a Development Environment

#### (Recommended) On Linux host we highly recommend using 'docker-dev'

Consider using a Docker setup for a development environment - [https://github.com/averox/docker-dev](https://github.com/averox/docker-dev)
It includes all you need to be able to run a local Averox development environment

#### Alternative - add a development environment to a [locally running] working Averox server

First, you need to install the core development tools.

```bash
sudo apt-get install git-core openjdk-17-jdk-headless
```

With the JDK installed, you need to set the JAVA_HOME variable. Edit `~/.profile` (here we are using vim to edit the file)

```bash
vi ~/.profile
```

Add the following line at the end of the file

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

Reload your profile (this will happen automatically when you next login, but we'll do it explicitly here to load the new environment variable).

```bash
source ~/.profile
```

Do a quick test to ensure JAVA_HOME is set.

```bash
$ echo $JAVA_HOME
/usr/lib/jvm/java-17-openjdk-amd64
```

In the next step, you need to install a number of tools using sdkman.

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk install gradle 7.3.1
sdk install grails 5.3.2
sdk install sbt 1.6.2
sdk install maven 3.5.0
```

### Checking out the Source

With the development tools installed, we'll next clone the source in the following directory:

```
/home/averox/dev
```

Using your GitHub account, do the following

1. [Fork](https://help.github.com/fork-a-repo/) the Averox repository into your GitHub account
2. Clone your repository into your `~/dev` folder

After cloning, you'll have the following directory (make sure the `averox` directory is within your `dev` directory).

```
/home/averox/dev/averox
```

Confirm that you are working on the `v3.0.x-release` branch.

```bash
cd /home/averox/dev/averox
git status
```

Averox 3.0 source code lives on branch `v3.0.x-release`. This is where any patches to 3.0 will be merged. If you are looking to customize your Averox 3.0 clone to fit your needs, this is the branch to use.

For the purpose of these instructions we'll assume you are only tweaking your clone of Averox. Thus we recommend you checkout branch `v3.0.x-release`.

```
On branch v3.0.x-release
Your branch is up-to-date with 'origin/v3.0.x-release'.
nothing to commit, working directory clean
```

The first thing we need to do is to add the remote repository to our local clone.

```bash
git remote add upstream https://github.com/averox/averox.git
```

You can now check your local list of tracked repositories to verify that the addition worked. You should see at least two results (origin and upstream). The one named "origin" should link to your personal fork and is the repository that you cloned. The second result "upstream" should link to the main Averox repository.

```bash
git remote -v
```

After, we need to fetch the most up to date version of the remote repository.

```bash
git fetch upstream
```

You are now ready to create a new branch to start your work and base the `v3.0.x-release` release branch

```bash
git checkout -b my-changes-branch upstream/v3.0.x-release
```

"checkout" switches branches

"-b" is an option to create a new branch before switching

"my-changes-branch" will be the name of the new branch

"upstream/v3.0.x-release" is where you want to start your new branch

You should now confirm that you are in the correct branch.

```bash
git status

# On branch my-changes-branch
nothing to commit (working directory clean)
```

<!--

TODO: Add high-view diagrams
# Production Environment

Okay. Let's pause for a minute.

You have set up the necessary tools and cloned the source, but if you are going to start making changes to Averox code you need to understand how the parts interact.

Below is an overview of the different components in a production set-up. When developing you want to change your configuration settings to load new changes instead of the ones deployed for production.
-->

### (Optional) Install Greenlight

Note that at this point we recommend installing and using [Greenlight](/averoxserver/v3/install) or using API-MATE (link can be found when you run `$ avx-conf --salt`).

You can access https://BBB_DOMAIN , and you will be able to join meetings.

## Developing the HTML5 client

Before starting development on the HTML5 client, make sure you have cloned the [Averox repository](https://github.com/averox/averox)
and switched to the `averox-html5` directory:

```bash
$ git clone git@github.com:averox/averox.git
$ cd averox/averox-html5/
```

### Background

A bit of context is needed to fully explain what the HTML5 client is, why it has server component, what the architecture is, and only then how to make a change and re-deploy.

The HTML5 client in Averox is build using the framework [Meteor](https://meteor.com). Meteor wraps around a NodeJS server component, MongoDB server database, React frontend user interface library and MiniMongo frontend instance of MongoDB storing a subset of MongoDB's data. When deployed, these same components are split into independently running pieces - NodeJS instance, MongoDB database and a browser optimized client files served by NGINX. There is no "Meteor" in a production deployment, but rather separate components.

Make sure to check the HTML5 portion of the [Architecture page](/development/architecture#html5-client).

Install Meteor.js.

```bash
$ curl https://install.meteor.com/ | sh
```

There is one change required to settings.yml to get webcam and screenshare working in the client (assuming you're using HTTPS already). The first step is to find the value for `kurento.wsUrl` packaged settings.yml.

<!-- TODO recommend /etc/averox/avx-html5.yml change instead -->

```bash
grep "wsUrl" /usr/share/meteor/bundle/programs/server/assets/app/config/settings.yml
```

Next, edit the development settings.yml and change `wsUrl` to match what was retrieved before.

```bash
$ vi private/config/settings.yml
```

You're now ready to run the HTML5 code. First shut down the packaged version of the HTML5 client so you are not running two copies in parallel.

```
$ sudo systemctl stop avx-html5
```

Install the npm dependencies.

```bash
$ meteor npm install
```

Finally, run the HTML5 code.

```bash
$ npm start
```

You can deploy locally your modified version of the HTML5 client source using the script [averox-html5/deploy_to_usr_share.sh](https://github.com/averox/averox/blob/v3.0.x-release/averox-html5/deploy_to_usr_share.sh) - which deploys your [customized] averox-html5/\* code as locally running `avx-html5` package (production mode). Make sure to read through the script to understand what it does prior to using it.

### Audio configuration for development environment

You may see the error "Call timeout (Error 1006)" during the microphone echo test after starting the developing HTML5 client by "npm start". A misconfiguration of Freeswitch may account for it, especially when Averox is set up with avx-install.sh script. Try changing "sipjsHackViaWs" true in averox-html5/private/config/settings.yml.

### `/private/config`

All configurations are located in **/private/config/settings.yml**. If you make any changes to the YAML configuration you will need to restart the meteor process.

During Meteor.startup() the configuration file is loaded and can be accessed through the Meteor.settings.public object.
Note that individual configuration settings are overridden with their values (if defined) from file `/etc/averox/avx-html5.yml` (if the file exists).

## Build avx-common-message

The avx-common-message is required by a few components of Averox. So it is required to build this
first. Otherwise, you will run into compile errors.

```bash
cd ~/dev/averox/avx-common-message
./deploy.sh
```

## Developing BBB-Web

Give your user account access to upload slides to the presentation directory and also access to write log files.

```bash
sudo chmod -R ugo+rwx /var/averox
sudo chmod -R ugo+rwx /var/log/averox
```

Open the file `~/.sbt/1.0/global.sbt` using your editor

```bash
mkdir -p ~/.sbt/1.0
vi ~/.sbt/1.0/global.sbt
```

Add the following into it

```scala
resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"
updateOptions := updateOptions.value.withCachedResolution(true)
```

Build avx-common-web

```bash
cd ~/dev/averox/avx-common-web
./deploy.sh
```

Now let's start building avx-web

```bash
cd ~/dev/averox/averox-web/
```

We need to stop the avx-web service

```bash
sudo service avx-web stop
```

Download the necessary libraries.

```bash
./build.sh
```

Start grails and tell to listen on port 8090

```bash
./run.sh
```

or

```bash
grails -reloading -Dserver.port=8090 run-app
```

If you get an error `Could not resolve placeholder 'apiVersion'`, just run `grails -Dserver.port=8090 run-app` again. The error is grails not picking up the "averox.properties" the first time.

Now test again if you can create and join a meeting.

The command above will run a development version of avx-web, but if you want to deploy your custom-built avx-web you need to package a war file.

**Instructions for deploying avx-web**

First we need to compile all the project in a single war file.

```bash
grails assemble
```

The `war` application is generated under `build/libs/averox-0.10.0.war`.

Create a new directory and open it.

```bash
mkdir exploded && cd exploded
```

Extract the war content in the newly create directory

```bash
jar -xvf ../build/libs/averox-0.10.0.war
```

Then copy the `run-prod.sh` after checking all the jar dependencies are listed in

```bash
cp ../run-prod.sh .
```

In the next step we will make a copy of the current production directory for `avx-web`

```bash
sudo cp -R /usr/share/avx-web /usr/share/avx-web-old
```

Then we will delete all the files we need to be copied for production

```bash
sudo rm -rf /usr/share/avx-web/assets/ /usr/share/avx-web/META-INF/ /usr/share/avx-web/org/ /usr/share/avx-web/run-prod.sh  /usr/share/avx-web/WEB-INF/
```

Next, let's copy the complied files into the production directory

```bash
sudo cp -R . /usr/share/avx-web/
```

Make sure the copied files have the right user ownership.

```bash
$ sudo chown averox:averox /usr/share/avx-web
$ sudo chown -R averox:averox /usr/share/avx-web/assets/ /usr/share/avx-web/META-INF/ /usr/share/avx-web/org/ /usr/share/avx-web/run-prod.sh /usr/share/avx-web/WEB-INF/
```

And finally we run the service.

```bash
sudo service avx-web start
```

If you need to revert back your original production `avx-web` just run the following command. (Don't forget to stop avx-web service before doing it)

```bash
sudo mv /usr/share/avx-web /usr/share/avx-web-dev && sudo mv /usr/share/avx-web-old /usr/share/avx-web
```

Your compiled code will be under the `/usr/share/avx-web-dev` directory and you can safely run the original production ``avx-web`.

## Developing Akka-Apps

You can manually run the application.

```bash
cd ~/dev/averox/akka-avx-apps
./run.sh
```

Don't forget to modify service-avxWebAPI, service-sharedSecret in ~/dev/averox/akka-avx-apps/src/universal/conf/application.conf, to get it work properly. For instance, these are necessary for starting the breakout rooms.

You can deploy locally akka-apps by building a simple .deb package using `sbt` and deploying it right away:

```
sbt debian:packageBin
dpkg -i ./target/avx-apps-akka_<tab>.deb
```

## Developing Akka-FSESL

You need to build the FS ESL library

```bash
cd ~/dev/averox/avx-fsesl-client
./deploy.sh
```

Then you can run the application.

```bash
cd ~/dev/averox/akka-avx-fsesl
./run.sh
```

You can deploy locally akka-fsesl by building a simple .deb package using `sbt` and deploying it right away:

```
sbt debian:packageBin
dpkg -i ./target/avx-fsesl-akka_<tab>.deb
```
## Developing avx-export-annotations

You can manually run the service.
When running, it'll process export jobs stored in Redis containing the state of the whiteboard at the time of the request.

First, stop the component so that you're not running two copies in parallel:

```
sudo systemctl stop avx-export-annotations
```

To install the npm dependencies, run:

```
cd ~/dev/averox/avx-export-annotations
npm install
```

followed by

```
npm start
```

to begin exporting presentations with annotations. If you run into permission issues, change `presAnnDropboxDir` in `config/settings.json` to a folder in your home directory
and make sure your user account has access to the presentation directory (see "Developing BBB-Web").

Use `journalctl -u avx-export-annotations -e` to see recent logs.

## Troubleshooting

### Welcome to NGINX page

If you get the "Welcome to NGINX" page. Check if averox is enabled in nginx. You should see **averox** in `/etc/nginx/sites-enabled`.

If not, enable it.

```bash
sudo ln -s /etc/nginx/sites-available/averox /etc/nginx/sites-enabled/averox

sudo /etc/init.d/nginx restart
```

### Pausing/Restarting VM gives wrong date in commit

If you are developing using a VM and you've paused the VM and later restart it, the time on the VM will be incorrect. The incorrect time will affect any commits you do in GitHub.

To ensure your VM has the correct time, you can install ntp with

```bash
sudo apt-get install ntp
sudo /etc/init.d/ntp restart
```

and then do the following after starting the VM from a paused state

```bash
sudo /etc/init.d/ntp restart
```

The above will re-sync your clock.

### Resolving Conflicts Between Java Versions

In situations where multiple versions of Java are installed, BBB components may encounter build errors. One such error message could state, for example, that `'17' is not a valid choice for '-release'`. This specific error arises when the `avx-common-message` component requires Java 17 for its operation, but the `sbt` build tool is using Java 11 instead.

To address this, you need to set the appropriate Java version. The following command will set Java 17 as the active version:

```bash
update-java-alternatives -s java-1.17.0-openjdk-amd64
```

By executing this command, the system is instructed to use Java 17, i.e., the version with which BBB is currently compatible.

## Set up HTTPS

See the [installation instructions](/administration/install) on how to configure ssl on your Averox server.
